package project.model.worldElements;

import org.junit.jupiter.api.Test;
import project.model.Vector2d;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnimalTest {

    @Test
    void testInitializationWithGenes() {
        // Given
        ArrayList<Integer> genes = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7));
        MutationStrategy mutationStrategy = new IncrementDecrementMutationStrategyVariant(2, 4);

        // When
        Animal animal = new Animal(new Vector2d(2, 2), genes, 50, 80, 10, mutationStrategy);

        // Then
        assertEquals(new Vector2d(2, 2), animal.getPosition());
        assertEquals(50, animal.getCurrentEnergy());
        assertEquals(genes, animal.getAnimalGenesList());
    }

    @Test
    void testInitializationWithRandomGenes() {
        // Given
        int numberOfGenes = 8;
        MutationStrategy mutationStrategy = new RandomMutationStrategyVariant(2, 4);

        // When
        Animal animal = new Animal(new Vector2d(1, 1), numberOfGenes, 40, 70, 10, mutationStrategy);

        // Then
        assertEquals(numberOfGenes, animal.getAnimalGenesList().size());
        animal.getAnimalGenesList().forEach(gene -> assertTrue(gene >= 0 && gene <= 7));
    }

    @Test
    void testEnergyReductionOnMove() {
        // Given
        MutationStrategy mutationStrategy = new RandomMutationStrategyVariant(2, 4);
        Animal animal = new Animal(new Vector2d(2, 2), 8, 50, 80, 10, mutationStrategy);
        int initialEnergy = animal.getCurrentEnergy();

        // When
        animal.move();

        // Then
        assertEquals(initialEnergy - 1, animal.getCurrentEnergy());
    }

    @Test
    void testRotation() {
        // Given
        MutationStrategy mutationStrategy = new IncrementDecrementMutationStrategyVariant(2, 4);
        Animal animal1 = new Animal(new Vector2d(3, 3), 8, 50, 80, 10, mutationStrategy);
        Animal animal2 = new Animal(new Vector2d(3, 3), 8, 50, 80, 10, mutationStrategy);
        MapDirection initialOrientation1 = animal1.getCurrentOrientation();
        MapDirection initialOrientation2 = animal2.getCurrentOrientation();

        // When
        animal1.rotate(2);
        animal2.rotate(10);
        MapDirection newOrientation = initialOrientation2.rotate(2);

        // Then
        assertEquals(initialOrientation1.rotate(2), animal1.getCurrentOrientation());
        assertEquals(newOrientation, animal2.getCurrentOrientation());
    }

    @Test
    void testDeadAnimalCannotMove() {
        // Given
        MutationStrategy mutationStrategy = new IncrementDecrementMutationStrategyVariant(2, 4);
        Animal animal = new Animal(new Vector2d(2, 2), 8, 1, 80, 10, mutationStrategy);

        // When
        animal.move();

        // Then
        assertThrows(AnimalDeadException.class, animal::move);
    }

    @Test
    void testDeadAnimalCannotRotate() {
        // Given
        MutationStrategy mutationStrategy = new RandomMutationStrategyVariant(2, 4);
        Animal animal = new Animal(new Vector2d(2, 2), 8, 1, 80, 10, mutationStrategy);

        // When
        animal.move();

        // Then
        assertThrows(AnimalDeadException.class, () -> animal.rotate(3));
    }

    @Test
    void testReproduction() {
        // Given
        MutationStrategy mutationStrategy = new RandomMutationStrategyVariant(2, 4);
        Animal parent1 = new Animal(new Vector2d(4, 4), 8, 50, 25, 10, mutationStrategy);
        Animal parent2 = new Animal(new Vector2d(4, 4), 8, 60, 25, 10, mutationStrategy);

        // When
        Animal offspring = Animal.reproduce(parent1, parent2);

        // Then
        assertEquals(40, parent1.getCurrentEnergy());
        assertEquals(50, parent2.getCurrentEnergy());
        assertEquals(20, offspring.getCurrentEnergy());
        assertEquals(parent1.getPosition(), offspring.getPosition());
        assertEquals(8, offspring.getAnimalGenesList().size());
    }

    @Test
    void testMutationWithIncrementDecrementStrategy() {
        // Given
        ArrayList<Integer> genes = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7));
        MutationStrategy mutationStrategy = new IncrementDecrementMutationStrategyVariant(2, 4);

        // When
        mutationStrategy.mutateGenes(genes);

        // Then
        assertEquals(8, genes.size());
        genes.forEach(gene -> assertTrue(gene >= 0 && gene <= 7));
    }

    @Test
    void testMutationWithRandomStrategy() {
        // Given
        ArrayList<Integer> genes = new ArrayList<>(List.of(0, 0, 0, 0, 0, 0, 0, 0));
        MutationStrategy mutationStrategy = new RandomMutationStrategyVariant(2, 4);

        // When
        mutationStrategy.mutateGenes(genes);

        // Then
        assertEquals(8, genes.size());
        assertTrue(genes.stream().anyMatch(gene -> gene != 0));
    }

    @Test
    void testEdgeCaseEnergy() {
        // Given
        MutationStrategy mutationStrategy = new IncrementDecrementMutationStrategyVariant(2, 4);
        Animal animal = new Animal(new Vector2d(2, 2), 8, 1, 80, 10, mutationStrategy);

        // When
        animal.move();

        // Then
        assertFalse(animal.isAnimalAlive());
    }

    @Test
    void testEdgeCaseReproduction() {
        // Given
        MutationStrategy mutationStrategy = new RandomMutationStrategyVariant(2, 4);
        Animal parent1 = new Animal(new Vector2d(4, 4), 8, 10, 80, 10, mutationStrategy);
        Animal parent2 = new Animal(new Vector2d(4, 4), 8, 5, 80, 10, mutationStrategy);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> Animal.reproduce(parent1, parent2));
    }

    @Test
    void testEnergyAfterEating() {
        // Given
        MutationStrategy mutationStrategy = new IncrementDecrementMutationStrategyVariant(2, 4);
        Animal animal = new Animal(new Vector2d(2, 2), 8, 70, 80, 10, mutationStrategy);

        // When
        animal.eat(20);

        // Then
        assertEquals(90, animal.getCurrentEnergy());
    }

    @Test
    void testRandomMutationStrategyChangesGeneCount() {
        // Given
        ArrayList<Integer> genes = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7));
        MutationStrategy mutationStrategy = new RandomMutationStrategyVariant(3, 5);
        ArrayList<Integer> originalGenes = new ArrayList<>(genes);

        // When
        mutationStrategy.mutateGenes(genes);
        int changedGenes = 0;
        for (int i = 0; i < genes.size(); i++) {
            if (!genes.get(i).equals(originalGenes.get(i))) {
                changedGenes += 1;
            }
        }

        // Then
        assertTrue(changedGenes >= 3 && changedGenes <= 5);
        assertEquals(8, genes.size());
    }

    @Test
    void testIncrementDecrementMutationStrategyChangesGeneCount() {
        // Given
        ArrayList<Integer> genes = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7));
        MutationStrategy mutationStrategy = new IncrementDecrementMutationStrategyVariant(3, 5);

        // When
        ArrayList<Integer> newGenes = new ArrayList<>(genes);
        mutationStrategy.mutateGenes(genes);

        // Then
        int changedGenes = 0;
        for (int i = 0; i < genes.size(); i++) {
            if (!genes.get(i).equals(newGenes.get(i))) {
                changedGenes += 1;
            }
        }
        assertTrue(changedGenes >= 3 && changedGenes <= 5);
        assertEquals(8, genes.size());
    }

    @Test
    void testIncrementDecrementMutationStrategyChangesAreByOne() {
        // Given
        ArrayList<Integer> genes = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7));
        MutationStrategy mutationStrategy = new IncrementDecrementMutationStrategyVariant(3, 3);
        ArrayList<Integer> originalGenes = new ArrayList<>(genes);

        // When
        mutationStrategy.mutateGenes(genes);

        // Then
        long changedGenesCount = 0;
        for (int i = 0; i < genes.size(); i++) {
            if (!originalGenes.get(i).equals(genes.get(i))) {
                int difference = genes.get(i) - originalGenes.get(i);
                assertTrue(difference == 1 || difference == -1 || difference == -7 || difference == 7);
                changedGenesCount++;
            }
        }
        assertEquals(changedGenesCount, 3);
        assertEquals(8, genes.size());
    }

    @Test
    void testAnimalRotationWithInvalidGene() {
        // Given
        ArrayList<Integer> genes = new ArrayList<>(List.of(8, 9, -1));
        MutationStrategy mutationStrategy = new RandomMutationStrategyVariant(2, 4);

        // Then
        assertThrows(IllegalArgumentException.class, () -> new Animal(new Vector2d(1, 1), genes, 50, 80, 10, mutationStrategy));
    }

    @Test
    void testBabyAnimalGenesAfterReproduction() {
        // Given
        ArrayList<Integer> genesParent1 = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7));
        ArrayList<Integer> genesParent2 = new ArrayList<>(List.of(7, 6, 5, 4, 3, 2, 1, 0));
        MutationStrategy mutationStrategy = new RandomMutationStrategyVariant(0, 0);

        Animal parent1 = new Animal(new Vector2d(0, 0), genesParent1, 100, 30, 10, mutationStrategy);
        Animal parent2 = new Animal(new Vector2d(0, 0), genesParent2, 50, 30, 10, mutationStrategy);

        // When
        Animal babyAnimal = Animal.reproduce(parent1, parent2);

        ArrayList<Integer> expectedGenes1 = new ArrayList<>(List.of(7, 6, 2, 3, 4, 5, 6, 7));
        ArrayList<Integer> expectedGenes2 = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 1, 0));

        System.out.println(babyAnimal.getAnimalGenesString());
        assertTrue(babyAnimal.getAnimalGenesList().equals(expectedGenes1) || babyAnimal.getAnimalGenesList().equals(expectedGenes2));
    }

    @Test
    void testIfNextOrientationIsCorrect() {
        // Given
        ArrayList<Integer> genes1 = new ArrayList<>(List.of(1, 1));
        MutationStrategy mutationStrategy = new RandomMutationStrategyVariant(0, 0);

        Animal animal1 = new Animal(new Vector2d(0, 0), genes1, 100, 30, 10, mutationStrategy);

        // Then
        assertSame(animal1.getCurrentOrientation().rotate(1), animal1.getNextOrientation());
    }

    @Test
    void testIfNextPositionIsCorrect() {
        // Given
        ArrayList<Integer> genes1 = new ArrayList<>(List.of(1, 1));
        MutationStrategy mutationStrategy = new RandomMutationStrategyVariant(0, 0);

        Animal animal1 = new Animal(new Vector2d(0, 0), genes1, 100, 30, 10, mutationStrategy);
        MapDirection orientation = animal1.getCurrentOrientation();

        // When & Then
        assertEquals(animal1.getPosition().add(orientation.toUnitVector()), animal1.getNextPosition());
    }
}
