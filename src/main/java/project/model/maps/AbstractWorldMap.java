package project.model.maps;

import project.model.Vector2d;
import project.model.worldElements.Animal;
import project.model.worldElements.Grass;
import project.model.worldElements.WorldElement;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractWorldMap implements WorldMap {

    protected final int height;
    protected final int width;

    protected final Boundary mapBoundary;

    protected final Map<Vector2d, Animal> animalsOnMap = new HashMap<>();
    protected final Map<Vector2d, Grass> grassOnMap = new HashMap<>();

    private final List<MapChangeListener> observers = new ArrayList<>();
    private final UUID uuid = UUID.randomUUID();
    private final MapVisualizer visualizer;

    public AbstractWorldMap(int height, int width) {
        this.height = height;
        this.width = width;

        this.visualizer = new MapVisualizer(this);
        mapBoundary = new Boundary(new Vector2d(0,0), new Vector2d(width - 1, height - 1));
    }

    public UUID getId() {
        return uuid;
    }

    public void addObserver(MapChangeListener observer) {
        observers.add(observer);
    }

    public void removeObserver(MapChangeListener observer) {
        observers.remove(observer);
    }

    protected void mapChangeEvent(String message) {
        for(MapChangeListener observer : observers) {
            observer.mapChanged(this, message);
        }
    }

    @Override
    public boolean isOccupied(Vector2d position) {
        return objectAt(position).isPresent();
    }

    @Override
    public Optional<WorldElement> objectAt(Vector2d position) {
        if(animalsOnMap.containsKey(position)) {
            return Optional.of(animalsOnMap.get(position));
        }
        return Optional.ofNullable(grassOnMap.get(position));
    }

    @Override
    public Collection<WorldElement> getElements() {
        return new ArrayList<>(Stream.concat(animalsOnMap.values().stream(), grassOnMap.values().stream()).toList());
    }

    @Override
    public Collection<Animal> getOrderedAnimals() {
        Comparator<Animal> animalComparator = Comparator.comparing(animal -> "%s %s".formatted(animal.getPosition().getX(), animal.getPosition().getY()));
        return animalsOnMap.values().stream()
                .sorted(animalComparator)
                .toList();
    }

    public Boundary getMapBounds() {
        return mapBoundary;
    }

    @Override
    public void removeAnimal(Animal animal) {
        animalsOnMap.remove(animal.getPosition(), animal);
    }

    @Override
    public String toString() {
        return visualizer.draw(mapBoundary.lowerLeft(), mapBoundary.upperRight());
    }
}