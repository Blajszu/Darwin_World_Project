package project;

import project.listener.SimulationChangeListener;
import project.listener.SimulationEventType;
import project.model.Vector2d;
import project.model.maps.*;
import project.model.worldElements.*;
import project.presenter.SimulationParameters;
import project.statistics.SimulationStatistics;
import project.statistics.StatisticsRecord;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Simulation implements Runnable {

    private final ArrayList<SimulationChangeListener> listeners = new ArrayList<>();

    private final WorldMap worldMap;
    SimulationParameters simulationParameters;

    private int currentDay = 0;

    private int coolDown = 200;

    private final Random rand = new Random();

    private final SimulationStatistics statistics = new SimulationStatistics();

    private CountDownLatch countDownLatch;

    public Simulation(SimulationParameters simulationParameters) {
        this.simulationParameters = simulationParameters;

        this.worldMap = switch(simulationParameters.growthGrassVariant()) {
            case EQUATOR_MAP -> new EquatorMap(simulationParameters.mapHeight(), simulationParameters.mapWidth());
            case MOVING_JUNGLE_MAP -> new MovingJungleMap(simulationParameters.mapHeight(), simulationParameters.mapWidth());
        };

        MutationStrategy mutationStrategy = switch (simulationParameters.mutationVariant()) {
            case RANDOM -> new RandomMutationStrategyVariant(simulationParameters.minimalNumberOfMutation(), simulationParameters.maximumNumberOfMutation());
            case INCREMENT_DECREMENT -> new IncrementDecrementMutationStrategyVariant(simulationParameters.minimalNumberOfMutation(), simulationParameters.maximumNumberOfMutation());
        };

        try {
            spawnFirstAnimals(simulationParameters.startNumberOfAnimals(), simulationParameters.initialAnimalsEnergy(), simulationParameters.energyNeedToReproduce(), simulationParameters.energyUsedToReproduce(), mutationStrategy, simulationParameters.numberOfGenes());
            spawnGrass(simulationParameters.numberOfGrassOnMap());
        } catch (IncorrectPositionException e) {
            System.err.printf("Error while creating Simulation: %s%n", e.getMessage());
        }

        statistics.updateStatistics(worldMap, currentDay);
    }

    public WorldMap getWorldMap() {
        return worldMap;
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
                resolvedConflictsAnimals.getFirst().eat(simulationParameters.energyFromGrass());
            }

            if(resolvedConflictsAnimals.size() < 2) {
                continue;
            }

            Animal parent1 = resolvedConflictsAnimals.get(0);
            Animal parent2 = resolvedConflictsAnimals.get(1);

            if(parent1.getCurrentEnergy() >= simulationParameters.energyNeedToReproduce() && parent2.getCurrentEnergy() >= simulationParameters.energyNeedToReproduce()) {
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
        countDownLatch = new CountDownLatch(1);
        StatisticsRecord statisticsRecord = statistics.getStatisticsRecord();

        for(SimulationChangeListener observer : listeners) {
            observer.handleChangeEvent(worldMap, eventType, statisticsRecord);
        }
    }

    public void countDown() {
        countDownLatch.countDown();
    }

    public void setCoolDown(int coolDown) {
        this.coolDown = coolDown;
    }

    @Override
    public void run() {

        try {
            while (true) {
                removeDeadAnimals();
                SimulationChangeEvent(SimulationEventType.ANIMALS_REMOVED);
                Thread.sleep(coolDown);
                countDownLatch.await();
                moveAnimals();
                SimulationChangeEvent(SimulationEventType.ANIMALS_MOVED);
                Thread.sleep(coolDown);
                countDownLatch.await();
                consumePlantsAndReproduce();
                SimulationChangeEvent(SimulationEventType.FOOD_CONSUMED);
                Thread.sleep(coolDown);
                countDownLatch.await();
                spawnGrass(simulationParameters.numberOfGrassGrowingEveryDay());
                SimulationChangeEvent(SimulationEventType.GRASS_SPAWNED);
                Thread.sleep(coolDown);
                countDownLatch.await();

                statistics.updateStatistics(worldMap, currentDay);
                SimulationChangeEvent(SimulationEventType.DAY_ENDED);
                currentDay++;
                countDownLatch.await();
            }
        } catch (IncorrectPositionException e) {
            System.err.printf("Error while running Simulation: %s%n", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}