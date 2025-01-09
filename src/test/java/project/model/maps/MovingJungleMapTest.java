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

import static org.junit.jupiter.api.Assertions.*;

class MovingJungleMapTest {

    private MovingJungleMap movingJungleMap;

    @BeforeEach
    void setUp() {
        int mapHeight = 10;
        int mapWidth = 10;
        movingJungleMap = new MovingJungleMap(mapHeight, mapWidth);
    }

    @Test
    void addingGrassUpdatesPreferredAndNotPreferredPositions() {
        // Given
        Vector2d position = new Vector2d(3, 3);
        Grass grass = new Grass(position);

        try {
            // When
            movingJungleMap.place(grass);

            // Then
            assertTrue(movingJungleMap.getElements().contains(grass));
            assertFalse(movingJungleMap.getFreeGrassPreferredPositions().contains(position));
            assertFalse(movingJungleMap.getFreeGrassNotPreferredPositions().contains(position));

            for (Vector2d adjacent : getAdjacentPositions(position)) {
                assertTrue(movingJungleMap.getFreeGrassPreferredPositions().contains(adjacent));
                assertFalse(movingJungleMap.getFreeGrassNotPreferredPositions().contains(adjacent));
            }
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for valid grass position.");
        }
    }

    @Test
    void removingGrassUpdatesPreferredAndNotPreferredPositions() {
        // Given
        Vector2d position = new Vector2d(3, 3);
        Grass grass = new Grass(position);

        try {
            movingJungleMap.place(grass);
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for valid grass position.");
        }

        // When
        movingJungleMap.removeGrass(position);

        // Then
        assertFalse(movingJungleMap.getElements().contains(grass));
        assertTrue(movingJungleMap.getFreeGrassNotPreferredPositions().contains(position));
        assertFalse(movingJungleMap.getFreeGrassPreferredPositions().contains(position));

        for (Vector2d adjacent : getAdjacentPositions(position)) {
            assertFalse(movingJungleMap.getFreeGrassPreferredPositions().contains(adjacent));
            assertTrue(movingJungleMap.getFreeGrassNotPreferredPositions().contains(adjacent));
        }
    }

    @Test
    void adjacentGrassMakesPositionPreferred() {
        // Given
        Vector2d grassPosition = new Vector2d(4, 4);
        Vector2d adjacentPosition = new Vector2d(4, 5); // Pozycja obok
        Grass grass = new Grass(grassPosition);

        try {
            movingJungleMap.place(grass);
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for valid grass position.");
        }

        // Then
        assertTrue(movingJungleMap.getFreeGrassPreferredPositions().contains(adjacentPosition));
        assertFalse(movingJungleMap.getFreeGrassNotPreferredPositions().contains(adjacentPosition));
    }

    @Test
    void removingAdjacentGrassMakesPositionNotPreferred() {
        // Given
        Vector2d grassPosition = new Vector2d(4, 4);
        Vector2d adjacentPosition = new Vector2d(4, 5);
        Grass grass = new Grass(grassPosition);

        try {
            movingJungleMap.place(grass);
        } catch (IncorrectPositionException e) {
            fail("Exception should not be thrown for valid grass position.");
        }

        // When
        movingJungleMap.removeGrass(grassPosition);

        // Then
        assertTrue(movingJungleMap.getFreeGrassNotPreferredPositions().contains(adjacentPosition));
        assertFalse(movingJungleMap.getFreeGrassPreferredPositions().contains(adjacentPosition));
    }

    private List<Vector2d> getAdjacentPositions(Vector2d position) {
        List<Vector2d> adjacentPositions = new ArrayList<>();
        for (MapDirection direction : MapDirection.values()) {
            adjacentPositions.add(position.add(direction.toUnitVector()));
        }
        return adjacentPositions;
    }

    @Test
    void animalBouncesBackFromBottomEdgeWhenMoving() {
        // Given
        Vector2d initialPosition = new Vector2d(5, 0); // Pozycja na dolnej krawędzi
        ArrayList<Integer> genes = new ArrayList<>(List.of(0)); // Kierunek SOUTH
        Animal animal = new Animal(initialPosition, genes, 10, 100, 20, new RandomMutationStrategyVariant(0,0));

        while(animal.getCurrentOrientation() != MapDirection.SOUTH) {
            animal.rotate(1);
        }

        // When
        try {
            movingJungleMap.place(animal);
        } catch (IncorrectPositionException e) {
            fail("Exception while placing animal on the map.");
        }
        movingJungleMap.move(animal); // Próba ruchu na południe (poza mapę)

        // Then
        assertEquals(initialPosition, animal.getPosition()); // Zwierzę pozostaje na tej samej pozycji
        assertEquals(MapDirection.NORTH, animal.getCurrentOrientation()); // Obraca się o 180 stopni (SOUTH -> NORTH)
    }

    @Test
    void animalBouncesBackFromTopEdgeWhenMoving() {
        // Given
        Vector2d initialPosition = new Vector2d(5, 9); // Pozycja na górnej krawędzi
        ArrayList<Integer> genes = new ArrayList<>(List.of(0)); // Kierunek NORTH
        Animal animal = new Animal(initialPosition, genes, 10, 100, 20, new RandomMutationStrategyVariant(0,0));

        while(animal.getCurrentOrientation() != MapDirection.NORTH) {
            animal.rotate(1);
        }

        // When
        try {
            movingJungleMap.place(animal);
        } catch (IncorrectPositionException e) {
            fail("Exception while placing animal on the map.");
        }
        movingJungleMap.move(animal); // Próba ruchu na północ (poza mapę)

        // Then
        assertEquals(initialPosition, animal.getPosition()); // Zwierzę pozostaje na tej samej pozycji
        assertEquals(MapDirection.SOUTH, animal.getCurrentOrientation()); // Obraca się o 180 stopni (NORTH -> SOUTH)
    }

    @Test
    void animalWrapsAroundFromLeftEdgeWhenMoving() {
        // Given
        Vector2d initialPosition = new Vector2d(0, 5); // Pozycja na lewej krawędzi
        ArrayList<Integer> genes = new ArrayList<>(List.of(0)); // Kierunek WEST
        Animal animal = new Animal(initialPosition, genes, 10, 100, 20, new RandomMutationStrategyVariant(0,0));

        while(animal.getCurrentOrientation() != MapDirection.WEST) {
            animal.rotate(1);
        }

        // When
        try {
            movingJungleMap.place(animal);
        } catch (IncorrectPositionException e) {
            fail("Exception while placing animal on the map.");
        }
        movingJungleMap.move(animal); // Próba ruchu na zachód (poza mapę)

        // Then
        Vector2d expectedPosition = new Vector2d(9, 5); // Pozycja na prawej krawędzi
        assertEquals(expectedPosition, animal.getPosition()); // Zwierzę przenosi się na drugą stronę mapy
    }

    @Test
    void animalWrapsAroundFromRightEdgeWhenMoving() {
        // Given
        Vector2d initialPosition = new Vector2d(9, 5); // Pozycja na prawej krawędzi
        ArrayList<Integer> genes = new ArrayList<>(List.of(0)); // Kierunek EAST
        Animal animal = new Animal(initialPosition, genes, 10, 100, 20, new RandomMutationStrategyVariant(0,0));

        while(animal.getCurrentOrientation() != MapDirection.EAST) {
            animal.rotate(1);
        }

        // When
        try {
            movingJungleMap.place(animal);
        } catch (IncorrectPositionException e) {
            fail("Exception while placing animal on the map.");
        }
        movingJungleMap.move(animal);

        // Then
        Vector2d expectedPosition = new Vector2d(0, 5);
        assertEquals(expectedPosition, animal.getPosition());
    }
}