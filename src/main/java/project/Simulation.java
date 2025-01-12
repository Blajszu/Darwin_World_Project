package project;

import project.model.Vector2d;
import project.model.maps.*;
import project.model.worldElements.*;

import java.util.*;


public class Simulation implements Runnable {

    private final WorldMap worldMap;
    private final int energyFromGrass;
    private final int energyNeedToReproduce;
    private final int numberOfGrassGrowingEveryDay;
    private final boolean collectStatistics;

    private final Random rand = new Random();

    public Simulation(
            int mapHeight,
            int mapWith,
            GrowthGrassVariant growthVariant,
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
                mapHeight <= 0 ||
                mapWith <= 0 ||
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

        worldMap = switch (growthVariant) {
            case EQUATOR_MAP -> new EquatorMap(mapHeight, mapWith);
            case MOVING_JUNGLE_MAP -> new MovingJungleMap(mapHeight, mapWith);
        };

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

        numberOfGrassToSpawnOnNotPreferredPositions += numberOfGrassToSpawnOnPreferredPositions + 1;

        while(numberOfGrassToSpawnOnNotPreferredPositions-- > 0) {
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

    @Override
    public void run() {

        try {
            while (true) {
                removeDeadAnimals();
                moveAnimals();
                consumePlantsAndReproduce();
                spawnGrass(numberOfGrassGrowingEveryDay);
                System.out.println(worldMap);
                Thread.sleep(500);
            }
        } catch (IncorrectPositionException e) {
            System.err.printf("Error while running Simulation: %s%n", e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}