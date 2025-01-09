package project.model.maps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project.model.Vector2d;
import project.model.worldElements.Animal;
import project.model.worldElements.Grass;
import project.model.worldElements.MapDirection;
import project.model.worldElements.RandomMutationStrategyVariant;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EquatorMapTest {

    private EquatorMap equatorMap;
    private final int mapHeight = 10;
    private final int mapWidth = 10;

    @BeforeEach
    void setUp() {
        equatorMap = new EquatorMap(mapHeight, mapWidth);
    }

    @Test
    void placeAnimalSuccessfully() {
        // Given
        Vector2d position = new Vector2d(3, 3);
        Animal animal = new Animal(position, 1, 1, 1, 1, new RandomMutationStrategyVariant(0, 0));

        // When
        try {
            equatorMap.place(animal);
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for a valid position");
        }

        // Then
        Optional<List<Animal>> animalsAtPosition = equatorMap.animalsAt(position);
        assertTrue(animalsAtPosition.isPresent());
        assertTrue(animalsAtPosition.get().contains(animal));
    }

    @Test
    void placeAnimalOutOfBoundsThrowsException() {
        // Given
        Vector2d position = new Vector2d(-1, 3);
        Animal animal = new Animal(position, 1, 1, 1, 1, new RandomMutationStrategyVariant(0, 0));

        // When & Then
        assertThrows(IncorrectPositionException.class, () -> equatorMap.place(animal));
    }

    @Test
    void placeGrassSuccessfully() {
        // Given
        Vector2d position = new Vector2d(4, 5);
        Grass grass = new Grass(position);

        // When
        try {
            equatorMap.place(grass);
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for a valid position");
        }

        // Then
        assertTrue(equatorMap.getElements().contains(grass));
        assertFalse(equatorMap.getFreeGrassPreferredPositions().contains(position));
    }

    @Test
    void removeNonexistentGrassThrowsException() {
        // Given
        Vector2d position = new Vector2d(5, 5);

        // When & Then
        assertThrows(NoSuchElementException.class, () -> equatorMap.removeGrass(position));
    }

    @Test
    void removeExistingGrassSuccessfully() {
        // Given
        Vector2d position = new Vector2d(2, 2);
        Grass grass = new Grass(position);

        try {
            equatorMap.place(grass);
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for a valid position");
        }

        // When
        equatorMap.removeGrass(position);

        // Then
        assertFalse(equatorMap.getElements().contains(grass));
        assertTrue(equatorMap.getFreeGrassNotPreferredPositions().contains(position));
    }

    @Test
    void moveAnimalUpdatesPosition() {
        // Given
        Vector2d initialPosition = new Vector2d(2, 2);
        Vector2d nextPosition = new Vector2d(3, 2);
        ArrayList<Integer> genes = new ArrayList<>();
        genes.add(0);
        Animal animal = new Animal(initialPosition, genes, 10, 1, 1, new RandomMutationStrategyVariant(0, 0));

        while (animal.getCurrentOrientation() != MapDirection.EAST) {
            animal.rotate(1);
        }
        try {
            equatorMap.place(animal);
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for a valid position");
        }

        // When
        equatorMap.move(animal);

        // Then
        Optional<List<Animal>> animalsAtNextPosition = equatorMap.animalsAt(nextPosition);
        assertTrue(animalsAtNextPosition.isPresent());
        assertTrue(animalsAtNextPosition.get().contains(animal));

        Optional<List<Animal>> animalsAtInitialPosition = equatorMap.animalsAt(initialPosition);
        assertTrue(animalsAtInitialPosition.isEmpty());
    }

    @Test
    void moveAnimalOutOfBoundsWrapsAround() {
        // Given
        Vector2d initialPosition = new Vector2d(5, 5);
        ArrayList<Integer> genes = new ArrayList<>();
        genes.add(0);
        Animal animal = new Animal(initialPosition, genes, 10, 1, 1, new RandomMutationStrategyVariant(0, 0));

        while (animal.getCurrentOrientation() != MapDirection.NORTH) {
            animal.rotate(1);
        }
        try {
            equatorMap.place(animal);
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for a valid position");
        }

        // When
        equatorMap.move(animal);

        // Then
        Optional<List<Animal>> animalsAtWrappedPosition = equatorMap.animalsAt(new Vector2d(5, 6));
        assertTrue(animalsAtWrappedPosition.isPresent());
        assertTrue(animalsAtWrappedPosition.get().contains(animal));

        Optional<List<Animal>> animalsAtInitialPosition = equatorMap.animalsAt(initialPosition);
        assertTrue(animalsAtInitialPosition.isEmpty());
    }

    @Test
    void getMapBoundsReturnsCorrectValues() {
        // Given & When
        var bounds = equatorMap.getMapBounds();

        // Then
        assertNotNull(bounds);
        assertEquals(new Vector2d(0, 0), bounds.lowerLeft());
        assertEquals(new Vector2d(mapWidth - 1, mapHeight - 1), bounds.upperRight());
    }

    @Test
    void getEquatorBoundsReturnsCorrectValues() {
        // Given & When
        var equatorBounds = equatorMap.getEquatorBounds();

        // Then
        assertNotNull(equatorBounds);
        int equatorHeight = (int) Math.round(mapHeight * 0.2);
        int equatorStartHeight = mapHeight / 2 - equatorHeight / 2;
        int equatorEndHeight = equatorStartHeight + equatorHeight - 1;
        assertEquals(new Vector2d(0, equatorStartHeight), equatorBounds.lowerLeft());
        assertEquals(new Vector2d(mapWidth - 1, equatorEndHeight), equatorBounds.upperRight());
    }

    @Test
    void addGrassToPreferredRemovesPositionFromAvailable() {
        // Given
        Vector2d preferredPosition = new Vector2d(5, 5); // Zakładamy, że jest w strefie preferowanej
        Grass grass = new Grass(preferredPosition);

        try {
            // When
            equatorMap.place(grass);

            // Then
            assertFalse(equatorMap.getFreeGrassPreferredPositions().contains(preferredPosition));
            assertTrue(equatorMap.getElements().contains(grass));
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for a valid position");
        }
    }

    @Test
    void addGrassToNonPreferredRemovesPositionFromAvailable() {
        // Given
        Vector2d notPreferredPosition = new Vector2d(1, 1); // Zakładamy, że jest poza strefą preferowaną
        Grass grass = new Grass(notPreferredPosition);

        try {
            // When
            equatorMap.place(grass);

            // Then
            assertFalse(equatorMap.getFreeGrassNotPreferredPositions().contains(notPreferredPosition));
            assertTrue(equatorMap.getElements().contains(grass));
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for a valid position");
        }
    }

    @Test
    void removeGrassFromPreferredAddsPositionToAvailable() {
        // Given
        Vector2d preferredPosition = new Vector2d(4, 5); // Zakładamy, że jest w strefie preferowanej
        Grass grass = new Grass(preferredPosition);

        try {
            equatorMap.place(grass);
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for a valid position");
        }

        // When
        equatorMap.removeGrass(preferredPosition);

        // Then
        assertTrue(equatorMap.getFreeGrassPreferredPositions().contains(preferredPosition));
        assertFalse(equatorMap.getElements().contains(grass));
    }

    @Test
    void removeGrassFromNonPreferredAddsPositionToAvailable() {
        // Given
        Vector2d notPreferredPosition = new Vector2d(0, 1); // Zakładamy, że jest poza strefą preferowaną
        Grass grass = new Grass(notPreferredPosition);

        try {
            equatorMap.place(grass);
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for a valid position");
        }

        // When
        equatorMap.removeGrass(notPreferredPosition);

        // Then
        assertTrue(equatorMap.getFreeGrassNotPreferredPositions().contains(notPreferredPosition));
        assertFalse(equatorMap.getElements().contains(grass));
    }
}
