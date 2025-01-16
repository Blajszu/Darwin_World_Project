package project.statistics;

import project.model.Vector2d;
import project.model.maps.WorldMap;
import project.model.worldElements.Animal;
import project.model.worldElements.WorldElement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SimulationStatistics {
    private int numberOfDeadAnimals = 0;
    private int countOfDaysForDeadAnimals = 0;

    private final HashMap<String, Integer> genotypesCount = new HashMap<>();

    private StatisticsRecord statisticsRecord;

    public void registerDeadAnimal(Animal deadAnimal) {
        numberOfDeadAnimals++;
        countOfDaysForDeadAnimals += deadAnimal.getLengthOfLife();
    }

    public void updateStatistics(WorldMap map, int day) {
        int animalsCount = map.getNumberOfAnimals();
        int plantsCount = map.getNumberOfGrass();

        double averageLengthOfLife = (numberOfDeadAnimals != 0) ? (double) countOfDaysForDeadAnimals / numberOfDeadAnimals : 0.0;

        int allPositionsCount = map.getMapHeight() * map.getMapWidth();
        Set<Vector2d> occupiedPositions = new HashSet<>();

        int allChildrenCount = 0;
        int allEnergyCount = 0;

        for(WorldElement element : map.getElements()) {
            occupiedPositions.add(element.getPosition());

            if(element instanceof Animal animal) {
                allChildrenCount += animal.getAnimalsKids().size();
                allEnergyCount += animal.getCurrentEnergy();

                int genotypeCount = genotypesCount.computeIfAbsent(animal.getAnimalGenesString(), k -> 0);
                genotypesCount.put(animal.getAnimalGenesString(), ++genotypeCount);
            }
        }

        int numberOfFreePositions = allPositionsCount - occupiedPositions.size();
        double averageAnimalsEnergy = (animalsCount != 0) ? (double) allEnergyCount / animalsCount : 0.0;
        double averageChildrenCount = (animalsCount != 0) ? (double) allChildrenCount / animalsCount : 0.0;

        statisticsRecord = new StatisticsRecord(
                day,
                animalsCount,
                plantsCount,
                numberOfFreePositions,
                genotypesCount,
                averageAnimalsEnergy,
                averageLengthOfLife,
                averageChildrenCount
        );
    }

    public StatisticsRecord getStatisticsRecord() {
        return statisticsRecord;
    }
}
