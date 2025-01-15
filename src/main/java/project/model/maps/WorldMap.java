package project.model.maps;

import project.listener.SimulationChangeListener;
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

    boolean isGrassAt(Vector2d position);

    Collection<WorldElement> getElements();

    UUID getId();

    Collection<Animal> getOrderedAnimals();

    Collection<Vector2d> getAllAnimalsPositions();

    List<Vector2d> getFreeGrassPreferredPositions();

    List<Vector2d> getFreeGrassNotPreferredPositions();

    void removeAnimal(Animal animal);

    void removeGrass(Vector2d position);

    Boundary getMapBounds();

    void addObserver(SimulationChangeListener simulationChangeListener);

    void removeObserver(SimulationChangeListener simulationChangeListener);

    void mapChangeEvent(String message);
}