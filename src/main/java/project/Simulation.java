package project;

import project.listener.SimulationChangeListener;
import project.listener.SimulationEventType;
import project.listener.SimulationSaveStatistics;
import project.model.Vector2d;
import project.model.maps.*;
import project.model.worldElements.*;
import project.statistics.SimulationStatistics;
import project.statistics.StatisticsRecord;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Simulation implements Runnable {

    private final ArrayList<SimulationChangeListener> listeners = new ArrayList<>();
    private final WorldMap worldMap;
    private final SimulationParameters simulationParameters;
    private final SimulationStatistics statistics = new SimulationStatistics();
    private int currentDay = 1;
    private int coolDown = 200;
    private final int initialAnimalsEnergy;

    private CountDownLatch countDownLatch;
    private boolean running = true;

    private final Random rand = new Random();

    public Simulation(SimulationParameters simulationParameters) {
        this.simulationParameters = simulationParameters;
        this.initialAnimalsEnergy = simulationParameters.initialAnimalsEnergy();

        this.worldMap = switch (simulationParameters.growthGrassVariant()) {
            case EQUATOR_MAP -> new EquatorMap(simulationParameters.mapHeight(), simulationParameters.mapWidth());
            case MOVING_JUNGLE_MAP ->
                    new MovingJungleMap(simulationParameters.mapHeight(), simulationParameters.mapWidth());
        };
        worldMap.spawnGrass(simulationParameters.numberOfGrassOnMap());

        MutationStrategy mutationStrategy = switch (simulationParameters.mutationVariant()) {
            case RANDOM ->
                    new RandomMutationStrategyVariant(simulationParameters.minimalNumberOfMutation(), simulationParameters.maximumNumberOfMutation());
            case INCREMENT_DECREMENT ->
                    new IncrementDecrementMutationStrategyVariant(simulationParameters.minimalNumberOfMutation(), simulationParameters.maximumNumberOfMutation());
        };

        try {
            spawnFirstAnimals(simulationParameters.startNumberOfAnimals(), simulationParameters.initialAnimalsEnergy(), simulationParameters.energyNeedToReproduce(), simulationParameters.energyUsedToReproduce(), mutationStrategy, simulationParameters.numberOfGenes());
        } catch (IncorrectPositionException e) {
            System.err.printf("Error while creating Simulation: %s%n", e.getMessage());
        }

        statistics.updateStatistics(worldMap, currentDay);
    }

    public SimulationStatistics getStatistics() {
        return statistics;
    }

    public WorldMap getWorldMap() {
        return worldMap;
    }

    public int getInitialAnimalsEnergy() {
        return initialAnimalsEnergy;
    }

    public List<Animal> resolveAnimalsConflicts(List<Animal> animals) {
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

    public void countDown() {
        countDownLatch.countDown();
    }

    public void setCoolDown(int coolDown) {
        this.coolDown = coolDown;
    }

    public void stopSimulation() {
        running = false;
        countDownLatch.countDown();

        for (SimulationChangeListener observer : listeners) {
            if (observer instanceof SimulationSaveStatistics) {
                ((SimulationSaveStatistics) observer).close();
            }
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
        int mapWidth = mapBounds.upperRight().x() + 1;
        int mapHeight = mapBounds.upperRight().y() + 1;

        for (int i = 0; i < numberOfAnimalsToSpawn; i++) {
            Vector2d positionToSpawnAnimal = new Vector2d(rand.nextInt(mapWidth), rand.nextInt(mapHeight));
            Animal animal = new Animal(positionToSpawnAnimal, numberOfGenes, initialAnimalsEnergy, energyNeedToReproduce, energyUsedToReproduce, mutationStrategy);
            worldMap.place(animal);
        }
    }

    private void rotateAnimals() {
        Collection<Animal> animals = worldMap.getOrderedAnimals();

        for (Animal animal : animals) {
            animal.rotate();
        }
    }

    private void moveAnimals() {
        Collection<Animal> animals = worldMap.getOrderedAnimals();

        for (Animal animal : animals) {
            worldMap.move(animal);
        }
    }

    private void removeDeadAnimals() {
        Collection<Animal> allAnimalsOnMap = worldMap.getOrderedAnimals();
        for (Animal animal : allAnimalsOnMap) {
            if (!animal.isAnimalAlive()) {
                statistics.registerDeadAnimal(animal);
                worldMap.removeAnimal(animal);
            }
        }
    }

    private void consumePlantsAndReproduce() throws IncorrectPositionException {
        for (Vector2d position : worldMap.getAllAnimalsPositions()) {

            Optional<List<Animal>> animalsAtPosition = worldMap.animalsAt(position);
            List<Animal> resolvedConflictsAnimals = resolveAnimalsConflicts(animalsAtPosition.get());

            if (worldMap.isGrassAt(position)) {
                worldMap.removeGrass(position);
                resolvedConflictsAnimals.getFirst().eat(simulationParameters.energyFromGrass());
            }

            if (resolvedConflictsAnimals.size() < 2) {
                continue;
            }

            Animal parent1 = resolvedConflictsAnimals.get(0);
            Animal parent2 = resolvedConflictsAnimals.get(1);

            if (parent1.getCurrentEnergy() >= simulationParameters.energyNeedToReproduce() && parent2.getCurrentEnergy() >= simulationParameters.energyNeedToReproduce()) {
                worldMap.place(Animal.reproduce(parent1,parent2));
            }
        }
    }

    private void SimulationChangeEvent(SimulationEventType eventType) {
        countDownLatch = new CountDownLatch(1);
        StatisticsRecord statisticsRecord = statistics.getStatisticsRecord();

        for (SimulationChangeListener observer : listeners) {
            observer.handleChangeEvent(worldMap, eventType, statisticsRecord);
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                removeDeadAnimals();
                SimulationChangeEvent(SimulationEventType.ANIMALS_REMOVED);
                Thread.sleep(coolDown);
                countDownLatch.await();
                rotateAnimals();
                SimulationChangeEvent(SimulationEventType.ANIMALS_ROTATED);
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
                worldMap.spawnGrass(simulationParameters.numberOfGrassGrowingEveryDay());
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