package project.model.worldElements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class RandomMutationStrategyVariant implements MutationStrategy {
    private final Random random = new Random();
    private final int minNumberOfMutations;
    private final int maxNumberOfMutations;

    public RandomMutationStrategyVariant(int minNumberOfMutations, int maxNumberOfMutations) {
        this.minNumberOfMutations = minNumberOfMutations;
        this.maxNumberOfMutations = maxNumberOfMutations;
    }

    @Override
    public void mutateGenes(ArrayList<Integer> genes) {
        int numberOfMutations = random.nextInt(minNumberOfMutations, maxNumberOfMutations + 1);
        ArrayList<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < genes.size(); i++) {
            indexes.add(i);
        }
        Collections.shuffle(indexes); // wygląda znajomo

        for (int i = 0; i < numberOfMutations; i++) {

            int geneIndex = indexes.get(i);
            int geneValue = genes.get(geneIndex);

            int newGeneValue = random.nextInt(0, 8);
            if (newGeneValue == geneValue) {
                newGeneValue = (newGeneValue + 1) % 8;
            }

            genes.remove(geneIndex);
            genes.add(geneIndex, newGeneValue); // lista nie ma metody set?
        }
    }
}
