package project.model.worldElements;

import project.model.Vector2d;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.stream.Collectors;

public class Animal implements WorldElement{
    private MapDirection currentOrientation;
    private Vector2d currentPosition;
    private int currentEnergy;
    private final ArrayList<Integer> animalGenes = new ArrayList<>();
    private String animalGenesString;
    private int currentActiveGene;
    private final MutationStrategy mutationStrategy;
    private final int energyOfWellFedAnimal;
    private final int energyUsedToReproduce;
    private final LinkedList<Animal> animalsKids = new LinkedList<>();
    private int lengthOfLife = 0;

    Random random = new Random();

    public Animal(Vector2d position, ArrayList<Integer> genes, int initialEnergy,  int energyOfWellFedAnimal, int energyUsedToReproduce, MutationStrategy mutationStrategy) {
        this(position, initialEnergy, energyOfWellFedAnimal, energyUsedToReproduce, mutationStrategy);

        for (int gene : genes) {
            if (gene < 0 || gene >= 8) {
                throw new IllegalArgumentException("Invalid gene value: " + gene);
            }
        }

        animalGenes.addAll(genes);
        currentActiveGene = random.nextInt(0, animalGenes.size());

        animalGenesString = animalGenes.stream()
            .map(Object::toString)
            .collect(Collectors.joining(""));
    }

    public Animal(Vector2d position, int numberOfGenes, int initialEnergy,  int energyOfWellFedAnimal, int energyUsedToReproduce, MutationStrategy mutationStrategy) {
        this(position, initialEnergy, energyOfWellFedAnimal, energyUsedToReproduce, mutationStrategy);

        if (numberOfGenes <= 0) {
            throw new IllegalArgumentException("Number of genes must be greater than zero");
        }

        for(int i = 0; i < numberOfGenes; i++) {
            animalGenes.add(random.nextInt(0,8));
        }

        currentActiveGene = random.nextInt(0, animalGenes.size());

        animalGenesString = animalGenes.stream()
            .map(Object::toString)
            .collect(Collectors.joining(""));
    }

    private Animal(Vector2d position, int initialEnergy,  int energyOfWellFedAnimal, int energyUsedToReproduce, MutationStrategy mutationStrategy) {

        if (initialEnergy <= 0 || energyOfWellFedAnimal <= 0 || energyUsedToReproduce <= 0) {
            throw new IllegalArgumentException("InitialEnergy, energyOfWellFedAnimal, energyUsedToReproduce must all be greater than zero");
        }

        Random random = new Random();

        currentPosition = position;
        currentEnergy = initialEnergy;
        this.energyOfWellFedAnimal = energyOfWellFedAnimal;
        this.energyUsedToReproduce = energyUsedToReproduce;
        this.mutationStrategy = mutationStrategy;

        currentOrientation = MapDirection.values()[random.nextInt(0,8)];
    }

    public int getCurrentEnergy() {
        return currentEnergy;
    }

    public MapDirection getCurrentOrientation() {
        return currentOrientation;
    }

    public Vector2d getPosition() {
        return currentPosition;
    }

    public ArrayList<Integer> getAnimalGenes() {
        return new ArrayList<>(animalGenes);
    }

    public String getAnimalGenesString() { return animalGenesString; }

    public Vector2d getNextPosition() {
        return currentPosition.add(currentOrientation.toUnitVector());
    }

    public MapDirection getNextOrientation() {
        return MapDirection.values()[(currentOrientation.ordinal() + animalGenes.get(currentActiveGene)) % 8];
    }

    public int getLengthOfLife() {
        return lengthOfLife;
    }

    public LinkedList<Animal> getAnimalsKids() {
        return new LinkedList<>(animalsKids);
    }

    public String getResourceName() {
        return "Z %s".formatted(currentPosition.toString());
    }

    public String getResourceFileName() {
        return switch (currentOrientation) {
            case NORTH -> "north.png";
            case SOUTH -> "south.png";
            case EAST -> "east.png";
            case WEST -> "west.png";
            case NORTHEAST -> "northeast.png";
            case SOUTHEAST -> "southeast.png";
            case SOUTHWEST -> "southwest.png";
            case NORTHWEST -> "northwest.png";
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
    }

    public void rotate(int rotateAngle) {
        if (!this.isAnimalAlive()) {
            throw new AnimalDeadException("Animal can't rotate, because it is dead");
        }

        currentOrientation = currentOrientation.rotate(rotateAngle);
        currentActiveGene = (currentActiveGene + 1) % animalGenes.size();
        lengthOfLife++;
    }

    public void rotate() {
        this.rotate(animalGenes.get(currentActiveGene));
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

    public Animal reproduce(Animal secondParent) {

        if(this.currentEnergy < energyOfWellFedAnimal || secondParent.getCurrentEnergy() < energyOfWellFedAnimal){
            throw new IllegalArgumentException ("At least one animal has not enough energy to reproduce");
        }
        if(!this.currentPosition.equals(secondParent.currentPosition)) {
            throw new IllegalArgumentException("Animals cannot reproduce because they have different positions");
        }
        int numberOfGenesFromStrongerAnimal = (this.getCurrentEnergy() > secondParent.getCurrentEnergy())
                ? (int) Math.round((double) this.getCurrentEnergy() / (this.getCurrentEnergy() + secondParent.getCurrentEnergy()) * animalGenes.size())
                : (int) Math.round((double) secondParent.getCurrentEnergy() / (this.getCurrentEnergy() + secondParent.getCurrentEnergy()) * animalGenes.size());
        int numberOfGenesFromWeakerAnimal = animalGenes.size() - numberOfGenesFromStrongerAnimal;

        ArrayList<Integer> strongerGenes = (this.currentEnergy > secondParent.getCurrentEnergy()) ? this.animalGenes: secondParent.animalGenes;
        ArrayList<Integer> weakerGenes = (this.currentEnergy <= secondParent.getCurrentEnergy()) ? this.animalGenes: secondParent.animalGenes;

        ArrayList<Integer> genesFromBothParents = new ArrayList<>();

        boolean strongerParentSide = random.nextBoolean();

        if (strongerParentSide) {
            genesFromBothParents.addAll(strongerGenes.subList(0, numberOfGenesFromStrongerAnimal));
            genesFromBothParents.addAll(weakerGenes.subList(numberOfGenesFromStrongerAnimal, this.animalGenes.size()));
        }
        else {
            genesFromBothParents.addAll(weakerGenes.subList(0, numberOfGenesFromWeakerAnimal));
            genesFromBothParents.addAll(strongerGenes.subList(numberOfGenesFromWeakerAnimal, this.animalGenes.size()));
        }
        mutationStrategy.mutateGenes(genesFromBothParents);

        this.currentEnergy = currentEnergy - energyUsedToReproduce;
        secondParent.currentEnergy -= energyUsedToReproduce;

        Animal babyAnimal = new Animal(this.currentPosition, genesFromBothParents, energyUsedToReproduce*2, energyOfWellFedAnimal, energyUsedToReproduce, mutationStrategy);
        animalsKids.add(babyAnimal);

        return babyAnimal;
    }
}
