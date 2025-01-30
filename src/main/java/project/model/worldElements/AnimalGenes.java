package project.model.worldElements;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class AnimalGenes {
    private final ArrayList<Integer> animalGenes = new ArrayList<>();
    private final static Random random = new Random();
    private int currentActiveGene;
    private String animalGenesString;
    private final MutationStrategy mutationStrategy;
    public AnimalGenes(int numberOfGenes, MutationStrategy mutationStrategy) {
        this.mutationStrategy = mutationStrategy;

        if (numberOfGenes <= 0) {
            throw new IllegalArgumentException("Number of genes must be greater than zero");
        }

        for (int i = 0; i < numberOfGenes; i++) {
            animalGenes.add(random.nextInt(0, 8));
        }
        setGenesParameters();
    }

    public AnimalGenes(ArrayList<Integer> genes, MutationStrategy mutationStrategy) {
        for (int gene : genes) {
            if (gene < 0 || gene >= 8) {
                throw new IllegalArgumentException("Invalid gene value: " + gene);
            }
        }

        this.mutationStrategy = mutationStrategy;
        animalGenes.addAll(genes);
        setGenesParameters();
    }

    private void setGenesParameters() {
        currentActiveGene = random.nextInt(0, animalGenes.size());

        animalGenesString = animalGenes.stream()
                .map(Object::toString)
                .collect(Collectors.joining(""));
    }

    public int getIndexOfActiveGene() {
        return currentActiveGene;
    }

    public String getAnimalGenesString() {
        return animalGenesString;
    }

    public int getCurrentActiveGeneValue() {
        return animalGenes.get(currentActiveGene);
    }

    public ArrayList<Integer> getAnimalGenes() {
        return animalGenes;
    }

    public int getNumberOfGenes() {
        return animalGenes.size();
    }
    public void nextActiveGeneIndex() {
        currentActiveGene = (currentActiveGene + 1) % animalGenes.size();
    }

    public static ArrayList<Integer> kidsGenes(Animal firstParent, Animal secondParent) {
        int numberOfGenesFromStrongerAnimal = (firstParent.getCurrentEnergy() > secondParent.getCurrentEnergy())
                ? (int) Math.round((double) firstParent.getCurrentEnergy() / (firstParent.getCurrentEnergy() + secondParent.getCurrentEnergy()) * firstParent.getAnimalGenesList().size())
                : (int) Math.round((double) secondParent.getCurrentEnergy() / (firstParent.getCurrentEnergy() + secondParent.getCurrentEnergy()) * firstParent.getAnimalGenesList().size());
        int numberOfGenesFromWeakerAnimal = firstParent.getAnimalGenesList().size() - numberOfGenesFromStrongerAnimal;

        ArrayList<Integer> strongerGenes = (firstParent.getCurrentEnergy() > secondParent.getCurrentEnergy()) ? firstParent.getAnimalGenesList() : secondParent.getAnimalGenesList();
        ArrayList<Integer> weakerGenes = (firstParent.getCurrentEnergy() <= secondParent.getCurrentEnergy()) ? firstParent.getAnimalGenesList() : secondParent.getAnimalGenesList();

        ArrayList<Integer> genesFromBothParents = new ArrayList<>();

        boolean strongerParentSide = random.nextBoolean();

        if (strongerParentSide) {
            genesFromBothParents.addAll(strongerGenes.subList(0, numberOfGenesFromStrongerAnimal));
            genesFromBothParents.addAll(weakerGenes.subList(numberOfGenesFromStrongerAnimal, firstParent.getAnimalGenesList().size()));
        } else {
            genesFromBothParents.addAll(weakerGenes.subList(0, numberOfGenesFromWeakerAnimal));
            genesFromBothParents.addAll(strongerGenes.subList(numberOfGenesFromWeakerAnimal, firstParent.getAnimalGenesList().size()));
        }
        firstParent.getAnimalGenes().mutationStrategy.mutateGenes(genesFromBothParents);

        return genesFromBothParents;
    }
}
