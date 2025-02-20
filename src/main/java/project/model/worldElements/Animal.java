package project.model.worldElements;

import project.model.Vector2d;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Animal implements WorldElement {
    private MapDirection currentOrientation;
    private Vector2d currentPosition;
    private int currentEnergy;
    private AnimalGenes animalGenes;
    private final int energyOfWellFedAnimal;
    private final MutationStrategy mutationStrategy;
    private final int energyUsedToReproduce;
    private final LinkedList<Animal> animalsKids = new LinkedList<>();
    private int lengthOfLife = 0;
    private int numberOfEatenPlants = 0;

    public Animal(Vector2d position, ArrayList<Integer> genes, int initialEnergy, int energyOfWellFedAnimal, int energyUsedToReproduce, MutationStrategy mutationStrategy) {
        this(position, initialEnergy, energyOfWellFedAnimal, energyUsedToReproduce, mutationStrategy);
        animalGenes = new AnimalGenes(genes, mutationStrategy);
    }

    public Animal(Vector2d position, int numberOfGenes, int initialEnergy, int energyOfWellFedAnimal, int energyUsedToReproduce, MutationStrategy mutationStrategy) {
        this(position, initialEnergy, energyOfWellFedAnimal, energyUsedToReproduce, mutationStrategy);
        animalGenes = new AnimalGenes(numberOfGenes, mutationStrategy);
    }

    private Animal(Vector2d position, int initialEnergy, int energyOfWellFedAnimal, int energyUsedToReproduce, MutationStrategy mutationStrategy) {
        this.mutationStrategy = mutationStrategy;
        if (initialEnergy <= 0 || energyOfWellFedAnimal <= 0 || energyUsedToReproduce <= 0) {
            throw new IllegalArgumentException("InitialEnergy, energyOfWellFedAnimal, energyUsedToReproduce must all be greater than zero");
        }

        currentPosition = position;
        currentEnergy = initialEnergy;
        this.energyOfWellFedAnimal = energyOfWellFedAnimal;
        this.energyUsedToReproduce = energyUsedToReproduce;

        Random random = new Random();
        currentOrientation = MapDirection.values()[random.nextInt(0, 8)];
    }

    public int getCurrentEnergy() {
        return currentEnergy;
    }

    public int getActivePartOfGenome() {
        return animalGenes.getIndexOfActiveGene();
    }

    public int getNumberOfEatenPlants() {
        return numberOfEatenPlants;
    }

    public MapDirection getCurrentOrientation() {
        return currentOrientation;
    }

    public Vector2d getPosition() {
        return currentPosition;
    }

    public ArrayList<Integer> getAnimalGenesList() {
        return new ArrayList<>(animalGenes.getAnimalGenes());
    }

    AnimalGenes getAnimalGenes() {
        return animalGenes;
    }

    public String getAnimalGenesString() {
        return animalGenes.getAnimalGenesString();
    }

    public Vector2d getNextPosition() {
        return currentPosition.add(currentOrientation.toUnitVector());
    }

    public MapDirection getNextOrientation() {
        return MapDirection.values()[(currentOrientation.ordinal() + animalGenes.getCurrentActiveGeneValue()) % 8];
    }

    public int getLengthOfLife() {
        return lengthOfLife;
    }

    public LinkedList<Animal> getAnimalsKids() {
        return new LinkedList<>(animalsKids);
    }

    public String getResourceFileName() {
        return switch (currentOrientation) {
            case NORTH -> "images/north.png";
            case SOUTH -> "images/south.png";
            case EAST -> "images/east.png";
            case WEST -> "images/west.png";
            case NORTHEAST -> "images/northeast.png";
            case SOUTHEAST -> "images/southeast.png";
            case SOUTHWEST -> "images/southwest.png";
            case NORTHWEST -> "images/northwest.png";
        };
    }

    @Override
    public String toString() {
        return switch (currentOrientation) {
            case NORTH -> "N";
            case NORTHEAST -> "NE";
            case EAST -> "E";
            case SOUTHEAST -> "SE";
            case SOUTH -> "S";
            case SOUTHWEST -> "SW";
            case WEST -> "W";
            case NORTHWEST -> "NW";
        };
    }

    public boolean isAnimalAlive() {
        return currentEnergy > 0;
    }

    public void eat(int energyFromGrass) {
        currentEnergy += energyFromGrass;
        numberOfEatenPlants++;
    }

    public void rotate(int rotateAngle) {
        if (!this.isAnimalAlive()) {
            throw new AnimalDeadException("Animal can't rotate, because it is dead");
        }

        currentOrientation = currentOrientation.rotate(rotateAngle);
        animalGenes.nextActiveGeneIndex();
        lengthOfLife++;
    }

    public void rotate() {
        this.rotate(animalGenes.getCurrentActiveGeneValue());
    }

    public void move(Vector2d position) {
        if (!this.isAnimalAlive()) {
            throw new AnimalDeadException("Animal can't move, because it is dead");
        }

        currentPosition = position;
        currentEnergy--;
    }

    public void move() {
        move(getNextPosition());
    }


    public static Animal reproduce(Animal firstParent, Animal secondParent) {

        if (firstParent.currentEnergy < firstParent.energyOfWellFedAnimal || secondParent.getCurrentEnergy() < firstParent.energyOfWellFedAnimal) {
            throw new IllegalArgumentException("At least one animal has not enough energy to reproduce");
        }
        if (!firstParent.currentPosition.equals(secondParent.currentPosition)) {
            throw new IllegalArgumentException("Animals cannot reproduce because they have different positions");
        }

        firstParent.currentEnergy = firstParent.currentEnergy - firstParent.energyUsedToReproduce;
        secondParent.currentEnergy -= firstParent.energyUsedToReproduce;

        ArrayList<Integer> genesFromBothParents = AnimalGenes.kidsGenes(firstParent, secondParent);

        Animal babyAnimal = new Animal(firstParent.currentPosition, genesFromBothParents, firstParent.energyUsedToReproduce * 2, firstParent.energyOfWellFedAnimal, firstParent.energyUsedToReproduce, firstParent.mutationStrategy);
        firstParent.animalsKids.add(babyAnimal);
        secondParent.animalsKids.add(babyAnimal);

        return babyAnimal;
    }
}
