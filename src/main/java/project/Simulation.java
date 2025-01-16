package project;

import project.listener.SimulationChangeListener;
import project.listener.SimulationEventType;
import project.model.Vector2d;
import project.model.maps.*;
import project.model.worldElements.*;
import project.statistics.SimulationStatistics;
import project.statistics.StatisticsRecord;

import java.util.*;

public class Simulation implements Runnable {

    private final ArrayList<SimulationChangeListener> listeners = new ArrayList<>();

    private final WorldMap worldMap;
    private final int energyFromGrass;
    private final int energyNeedToReproduce;
    private final int numberOfGrassGrowingEveryDay;
    private final boolean collectStatistics;

    private int currentDay = 0;

    private final Random rand = new Random();

    private final SimulationStatistics statistics = new SimulationStatistics();

    public Simulation(
            WorldMap worldMap,
            int startNumberOfGrass,
            int energyFromGrass,
            int numberOfGrassGrowingEveryDay,
            int startNumberOfAnimals,
            int initialAnimalsEnergy,
            int energyNeedToReproduce,
            int energyUsedToReproduce,
            int minimalNumberOfMutation,
            int maximumNumberOfMutation,
            MutationVariant mutationVariant,
            int numberOfGenes,
            boolean collectStatistics) {

        if(
                startNumberOfGrass < 0 ||
                energyFromGrass <= 0 ||
                numberOfGrassGrowingEveryDay <= 0 ||
                startNumberOfAnimals < 0 ||
                initialAnimalsEnergy <= 0 ||
                energyNeedToReproduce <= 0 ||
                energyUsedToReproduce <= 0 ||
                minimalNumberOfMutation < 0 ||
                maximumNumberOfMutation < 0 ||
                numberOfGenes <= 0) {
            throw new IllegalArgumentException("Invalid Simulation parameters");
        }

        this.worldMap = worldMap;
        this.energyFromGrass = energyFromGrass;
        this.energyNeedToReproduce = energyNeedToReproduce;
        this.numberOfGrassGrowingEveryDay = numberOfGrassGrowingEveryDay;
        this.collectStatistics = collectStatistics;

        MutationStrategy mutationStrategy = switch (mutationVariant) {
            case RANDOM -> new RandomMutationStrategyVariant(minimalNumberOfMutation, maximumNumberOfMutation);
            case INCREMENT_DECREMENT -> new IncrementDecrementMutationStrategyVariant(minimalNumberOfMutation, maximumNumberOfMutation);
        };

        try {
            spawnFirstAnimals(startNumberOfAnimals, initialAnimalsEnergy, energyNeedToReproduce, energyUsedToReproduce, mutationStrategy, numberOfGenes);
            spawnGrass(startNumberOfGrass);
        } catch (IncorrectPositionException e) {
            System.err.printf("Error while creating Simulation: %s%n", e.getMessage());
        }

        statistics.updateStatistics(worldMap, currentDay);
    }

    private void spawnFirstAnimals(
            int numberOfAnimalsToSpawn,
            int initialAnimalsEnergy,
            int energyNeedToReproduce,
            int energyUsedToReproduce,
            MutationStrategy mutationStrategy,
            int numberOfGenes) throws IncorrectPositionException {

        Boundary mapBounds = worldMap.getMapBounds();
        int mapWith = mapBounds.upperRight().getY() + 1;
        int mapHeight = mapBounds.upperRight().getX() + 1;

        for(int i = 0; i < numberOfAnimalsToSpawn; i++) {
            Vector2d positionToSpawnAnimal = new Vector2d(rand.nextInt(mapWith), rand.nextInt(mapHeight));
            Animal animal = new Animal(positionToSpawnAnimal, numberOfGenes, initialAnimalsEnergy, energyNeedToReproduce, energyUsedToReproduce, mutationStrategy);

            worldMap.place(animal);
        }
    }

    private void spawnGrass(int numberOfGrassToSpawn) throws IncorrectPositionException {
        int grassLeft = numberOfGrassToSpawn;
        int numberOfGrassToSpawnOnPreferredPositions = (int) Math.round(numberOfGrassToSpawn * 0.8);
        int numberOfGrassToSpawnOnNotPreferredPositions = numberOfGrassToSpawn - numberOfGrassToSpawnOnPreferredPositions;

        while(numberOfGrassToSpawnOnPreferredPositions > 0) {
            List<Vector2d> preferredPositions = worldMap.getFreeGrassPreferredPositions();

            if(preferredPositions.isEmpty()) {
                break;
            }

            Vector2d positionToSpawnGrass = preferredPositions.get(rand.nextInt(preferredPositions.size()));

            worldMap.place(new Grass(positionToSpawnGrass));
            numberOfGrassToSpawnOnPreferredPositions--;
            grassLeft--;
        }

        while(numberOfGrassToSpawnOnNotPreferredPositions > 0) {
            List<Vector2d> notPreferredPositions = worldMap.getFreeGrassNotPreferredPositions();

            if(notPreferredPositions.isEmpty()) {
                break;
            }

            Vector2d positionToSpawnGrass = notPreferredPositions.get(rand.nextInt(notPreferredPositions.size()));

            worldMap.place(new Grass(positionToSpawnGrass));
            numberOfGrassToSpawnOnNotPreferredPositions--;
            grassLeft--;
        }

        while(grassLeft > 0) {
            List<Vector2d> preferredPositions = worldMap.getFreeGrassPreferredPositions();
            List<Vector2d> notPreferredPositions = worldMap.getFreeGrassNotPreferredPositions();

            if(preferredPositions.isEmpty() && notPreferredPositions.isEmpty()) {
                break;
            }

            List<Vector2d> availablePositions = new ArrayList<>();
            availablePositions.addAll(preferredPositions);
            availablePositions.addAll(notPreferredPositions);

            Vector2d positionToSpawnGrass = availablePositions.get(rand.nextInt(availablePositions.size()));

            worldMap.place(new Grass(positionToSpawnGrass));
            grassLeft--;
        }
    }

    private void moveAnimals() {
        Collection<Animal> animals = worldMap.getOrderedAnimals();

        for(Animal animal : animals) {
            worldMap.move(animal);
        }
    }

    private void removeDeadAnimals() {
        Collection<Animal> allAnimalsOnMap = worldMap.getOrderedAnimals();
        for (Animal animal : allAnimalsOnMap) {
            if(!animal.isAnimalAlive()){
                statistics.registerDeadAnimal(animal);
                worldMap.removeAnimal(animal);
            }
        }
    }

    private void consumePlantsAndReproduce() throws IncorrectPositionException {
        for(Vector2d position : worldMap.getAllAnimalsPositions()) {

            Optional<List<Animal>> animalsAtPosition = worldMap.animalsAt(position);
            List<Animal> resolvedConflictsAnimals = resolveConflicts(animalsAtPosition.get());

            if (worldMap.isGrassAt(position)) {
                worldMap.removeGrass(position);
                resolvedConflictsAnimals.getFirst().eat(energyFromGrass);
            }

            if(resolvedConflictsAnimals.size() < 2) {
                continue;
            }

            Animal parent1 = resolvedConflictsAnimals.get(0);
            Animal parent2 = resolvedConflictsAnimals.get(1);

            if(parent1.getCurrentEnergy() >= energyNeedToReproduce && parent2.getCurrentEnergy() >= energyNeedToReproduce) {
                worldMap.place(parent1.reproduce(parent2));
            }
        }
    }

    private List<Animal> resolveConflicts(List<Animal> animals) {
        return animals.stream()
            .sorted(Comparator
                    .comparingInt(Animal::getCurrentEnergy).reversed()
                    .thenComparingInt(Animal::getLengthOfLife).reversed()
                    .thenComparingInt(animal -> animal.getAnimalsKids().size()).reversed()
            )
            .toList();
    }

    public void addObserver(SimulationChangeListener observer) {
        listeners.add(observer);
    }

    public void removeObserver(SimulationChangeListener observer) {
        listeners.remove(observer);
    }

    private void SimulationChangeEvent(SimulationEventType eventType) {
        StatisticsRecord statisticsRecord = statistics.getStatisticsRecord();

        for(SimulationChangeListener observer : listeners) {
            observer.handleChangeEvent(worldMap, eventType, statisticsRecord);
        }
    }

    @Override
    public void run() {

        try {
            while (true) {
                removeDeadAnimals();
                SimulationChangeEvent(SimulationEventType.ANIMALS_REMOVED);
                Thread.sleep(200);
                moveAnimals();
                SimulationChangeEvent(SimulationEventType.ANIMALS_MOVED);
                Thread.sleep(200);
                consumePlantsAndReproduce();
                SimulationChangeEvent(SimulationEventType.FOOD_CONSUMED);
                Thread.sleep(200);
                spawnGrass(numberOfGrassGrowingEveryDay);
                SimulationChangeEvent(SimulationEventType.GRASS_SPAWNED);
                Thread.sleep(200);

                statistics.updateStatistics(worldMap, currentDay);
                SimulationChangeEvent(SimulationEventType.DAY_ENDED);
                currentDay++;
            }
        } catch (IncorrectPositionException e) {
            System.err.printf("Error while running Simulation: %s%n", e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}