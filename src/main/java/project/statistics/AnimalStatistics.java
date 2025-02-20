package project.statistics;

import project.Simulation;
import project.model.worldElements.Animal;

import java.util.HashSet;
import java.util.Set;

public class AnimalStatistics {
    private Integer whenDied;

    private AnimalStatisticsRecord record;
    private final Simulation simulation;
    private final Animal animal;

    public AnimalStatistics(Animal animal, Simulation simulation) {
        this.animal = animal;
        this.simulation = simulation;

        updateStatistics();
    }

    public void updateStatistics() {
        if (whenDied == null)
            this.whenDied = (animal.isAnimalAlive() ? null : simulation.getStatistics().getStatisticsRecord().day() + 1);

        record = new AnimalStatisticsRecord(
                animal.getAnimalGenesString(),
                animal.getActivePartOfGenome(),
                animal.getCurrentEnergy(),
                animal.getNumberOfEatenPlants(),
                animal.getAnimalsKids().size(),
                countDescendants(),
                animal.getLengthOfLife(),
                whenDied
        );
    }

    public AnimalStatisticsRecord getRecord() {
        return record;
    }

    private int countDescendants() {
        Set<Animal> allDescendants = new HashSet<>();
        findDescendants(animal, allDescendants);
        return allDescendants.size();
    }

    private void findDescendants(Animal animal, Set<Animal> allDescendants) {
        for (Animal child : animal.getAnimalsKids()) {
            if (allDescendants.add(child)) {
                findDescendants(child, allDescendants);
            }
        }
    }
}
