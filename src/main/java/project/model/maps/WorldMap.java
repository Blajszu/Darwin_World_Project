package project.model.maps;

import project.model.Vector2d;
import project.model.worldElements.Animal;
import project.model.worldElements.WorldElement;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorldMap {

    void place(WorldElement element) throws IncorrectPositionException;

    void move(Animal animal);

    Optional<List<Animal>> animalsAt(Vector2d position);

    Collection<WorldElement> getElements();

    UUID getId();

    Collection<Animal> getOrderedAnimals();

    List<Vector2d> getGrassPreferredPositions();

    List<Vector2d> getGrassNotPreferredPositions();

    void removeAnimal(Animal animal);

    void removeGrass(Vector2d position);
}