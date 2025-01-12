package project;

import project.model.Vector2d;
import project.model.maps.EquatorMap;
import project.model.maps.IncorrectPositionException;
import project.model.maps.MovingJungleMap;
import project.model.maps.WorldMap;
import project.model.worldElements.*;

import java.util.List;
import java.util.Collection;

import java.util.Random;


public class Simulation implements Runnable {

    private final WorldMap worldMap;
    private final int energyFromGrass;
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
        this.numberOfGrassGrowingEveryDay = numberOfGrassGrowingEveryDay;
        this.collectStatistics = collectStatistics;

        MutationStrategy mutationStrategy = switch (mutationVariant) {
            case RANDOM -> new RandomMutationStrategyVariant(minimalNumberOfMutation, maximumNumberOfMutation);
            case INCREMENT_DECREMENT -> new IncrementDecrementMutationStrategyVariant(minimalNumberOfMutation, maximumNumberOfMutation);
        };

        spawnFirstAnimals(startNumberOfAnimals, initialAnimalsEnergy, energyNeedToReproduce, energyUsedToReproduce, mutationStrategy, numberOfGenes);
        spawnGrass(startNumberOfGrass);
    }

    private void spawnFirstAnimals(
            int numberOfAnimalsToSpawn,
            int initialAnimalsEnergy,
            int energyNeedToReproduce,
            int energyUsedToReproduce,
            MutationStrategy mutationStrategy,
            int numberOfGenes) {
    }

    private void spawnGrass(int numberOfGrassToSpawn) {
        int numberOfGrassToSpawnOnPreferredPositions = (int) Math.round(numberOfGrassToSpawn * 0.8);
        int numberOfGrassToSpawnOnNotPreferredPositions = numberOfGrassToSpawn - numberOfGrassToSpawnOnPreferredPositions;

        while(numberOfGrassToSpawnOnPreferredPositions-- > 0) {
            List<Vector2d> preferredPositions = worldMap.getFreeGrassPreferredPositions();

            if(preferredPositions.isEmpty()) {
                break;
            }

            Vector2d positionToSpawnGrass = preferredPositions.get(rand.nextInt(preferredPositions.size()));

            try {
                worldMap.place(new Grass(positionToSpawnGrass));
            } catch (IncorrectPositionException e) {
                System.err.println(e.getMessage());
            }
        }

        numberOfGrassToSpawnOnNotPreferredPositions += numberOfGrassToSpawnOnPreferredPositions + 1;

        while(numberOfGrassToSpawnOnNotPreferredPositions-- > 0) {
            List<Vector2d> notPreferredPositions = worldMap.getFreeGrassNotPreferredPositions();

            if(notPreferredPositions.isEmpty()) {
                break;
            }

            Vector2d positionToSpawnGrass = notPreferredPositions.get(rand.nextInt(notPreferredPositions.size()));

            try {
                worldMap.place(new Grass(positionToSpawnGrass));
            } catch (IncorrectPositionException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void moveAnimals() {
        Collection<Animal> animals = worldMap.getOrderedAnimals();

        for(Animal animal : animals) {
            worldMap.move(animal);
        }
    }

    @Override
    public void run() {

    }

    private void removeDeadAnimals() {
        Collection<Animal> allAnimalsOnMap = worldMap.getOrderedAnimals();
        for (Animal animal : allAnimalsOnMap) {
            if(!animal.isAnimalAlive()){
                worldMap.removeAnimal(animal);
            }
        }
    }
}