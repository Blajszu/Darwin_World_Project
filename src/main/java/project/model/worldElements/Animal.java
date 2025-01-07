package project.model.worldElements;

import project.model.Vector2d;

import java.util.ArrayList;
import java.util.Random;

public class Animal {
    private MapDirection currentOrientation;
    private Vector2d currentPosition;
    private int currentEnergy;
    private final ArrayList<Integer> animalGenes = new ArrayList<>();
    private int currentActiveGene;
    private final MutationStrategy mutationStrategy;
    private final int energyOfWellFedAnimal;
    private final int energyUsedToReproduce;
    Random random = new Random();

    public Animal(Vector2d position, ArrayList<Integer> genes, int initialEnergy,  int energyOfWellFedAnimal, int energyUsedToReproduce, MutationStrategy mutationStrategy) {
        this(position, initialEnergy, energyOfWellFedAnimal, energyUsedToReproduce, mutationStrategy);
        animalGenes.addAll(genes);
        currentActiveGene = random.nextInt(0, genes.size());
    }

    public Animal(Vector2d position, int numberOfGenes, int initialEnergy,  int energyOfWellFedAnimal, int energyUsedToReproduce, MutationStrategy mutationStrategy) {
        this(position, initialEnergy, energyOfWellFedAnimal, energyUsedToReproduce, mutationStrategy);

        for(int i = 0; i < numberOfGenes; i++) {
            animalGenes.add(random.nextInt(0,8));
        }
    }

    private Animal(Vector2d position, int initialEnergy,  int energyOfWellFedAnimal, int energyUsedToReproduce, MutationStrategy mutationStrategy) {
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

    public Vector2d getNextPosition() {
        return currentPosition.add(MapDirection.values()[currentActiveGene].toUnitVector());
    }

    public String getResourceName() {
        return "Z %s".formatted(currentPosition.toString());
    }

    public String getResourceFileName() {
        return switch (currentOrientation) {
            case NORTH -> "up.png";
            case SOUTH -> "down.png";
            case EAST -> "right.png";
            case WEST -> "left.png";
            case NORTHEAST -> "northeast.png";
            case SOUTHEAST -> "southeast.png";
            case SOUTHWEST -> "southwest.png";
            case NORTHWEST -> "northwest.png";
        };
    }

    @Override
    public String toString() {
        return switch (currentOrientation) {
            case NORTH -> "↑";
            case NORTHEAST -> "↗";
            case EAST -> "→";
            case SOUTHEAST -> "↘";
            case SOUTH -> "↓";
            case SOUTHWEST -> "↙";
            case WEST -> "←";
            case NORTHWEST -> "↖";
        };
    }

    public boolean isAnimalAlive(){
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
    }

    public void move(Vector2d position) {
        if (!this.isAnimalAlive()) {
            throw new AnimalDeadException("Animal can't move, because it is dead");
        }

        this.rotate(currentActiveGene);
        currentPosition = position;
        currentEnergy--;
        currentActiveGene = (currentActiveGene + 1) % animalGenes.size();
    }

    public void move() {
        move(getNextPosition());
    }

    public Animal reproduce(Animal secondParent) {

        int numberOfGenesFromStrongerAnimal = (this.getCurrentEnergy() > secondParent.getCurrentEnergy())
                ? (int) Math.round((double) this.getCurrentEnergy() / (this.getCurrentEnergy() + secondParent.getCurrentEnergy()) * 4)
                : (int) Math.round((double) secondParent.getCurrentEnergy() / (this.getCurrentEnergy() + secondParent.getCurrentEnergy()) * 4);
        int numberOfGenesFromWeakerAnimal = animalGenes.size() - numberOfGenesFromStrongerAnimal;

        ArrayList<Integer> strongerGenes = (this.currentEnergy > secondParent.getCurrentEnergy()) ? this.animalGenes: secondParent.animalGenes;
        ArrayList<Integer> weakerGenes = (this.currentEnergy <= secondParent.getCurrentEnergy()) ? this.animalGenes: secondParent.animalGenes;

        ArrayList<Integer> genesFromBothParents = new ArrayList<>();

        boolean strongerParentSide = random.nextBoolean();

        if (strongerParentSide){
            genesFromBothParents.addAll(strongerGenes.subList(0, numberOfGenesFromStrongerAnimal));
            genesFromBothParents.addAll(weakerGenes.subList(0, numberOfGenesFromWeakerAnimal));
        }
        else {
            genesFromBothParents.addAll(weakerGenes.subList(0, numberOfGenesFromWeakerAnimal));
            genesFromBothParents.addAll(strongerGenes.subList(0, numberOfGenesFromStrongerAnimal));
        }
        mutationStrategy.mutateGenes(genesFromBothParents);

        this.currentEnergy = currentEnergy - energyUsedToReproduce;
        secondParent.currentEnergy -= energyUsedToReproduce;

        return new Animal(this.currentPosition, genesFromBothParents, energyUsedToReproduce*2, energyOfWellFedAnimal, energyUsedToReproduce, mutationStrategy);
    }
}
