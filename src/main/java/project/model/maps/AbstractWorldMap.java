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

    protected final List<Vector2d> freeGrassPreferredPositions = new ArrayList<>();
    protected final List<Vector2d> freeGrassNotPreferredPositions = new ArrayList<>();

    protected final Map<Vector2d, LinkedList<Animal>> animalsOnMap = new HashMap<>();
    protected final Map<Vector2d, Grass> grassOnMap = new HashMap<>();

    private final UUID uuid = UUID.randomUUID();

    public AbstractWorldMap(int height, int width) {
        if (height <= 0 || width <= 0) {
            throw new IllegalArgumentException("Height and width must be greater than 0");
        }

        this.height = height;
        this.width = width;

        mapBoundary = new Boundary(new Vector2d(0, 0), new Vector2d(width - 1, height - 1));
    }

    public UUID getId() {
        return uuid;
    }

    @Override
    public Collection<WorldElement> getElements() {
        return new ArrayList<>(
                Stream.concat(
                        animalsOnMap.values().stream().flatMap(Collection::stream),
                        grassOnMap.values().stream()
                ).toList()
        );
    }

    @Override
    public Collection<Animal> getOrderedAnimals() {
        Comparator<Animal> animalComparator = Comparator.comparing(animal ->
                "%s %s".formatted(animal.getPosition().x(), animal.getPosition().y())
        );

        return animalsOnMap.values().stream()
                .flatMap(Collection::stream)
                .sorted(animalComparator)
                .toList();
    }

    @Override
    public Collection<Vector2d> getAllAnimalsPositions() {
        return animalsOnMap.keySet();
    }

    @Override
    public Boundary getMapBounds() {
        return mapBoundary;
    }

    @Override
    public List<Vector2d> getFreeGrassPreferredPositions() {
        return new ArrayList<>(freeGrassPreferredPositions);
    }

    @Override
    public List<Vector2d> getFreeGrassNotPreferredPositions() {
        return new ArrayList<>(freeGrassNotPreferredPositions);
    }

    @Override
    public int getNumberOfAnimals() {
        return animalsOnMap.size();
    }

    @Override
    public int getNumberOfGrass() {
        return grassOnMap.size();
    }

    @Override
    public int getMapHeight() {
        return height;
    }

    @Override
    public int getMapWidth() {
        return width;
    }

    @Override
    public boolean isGrassAt(Vector2d position) {
        return grassOnMap.containsKey(position);
    }

    @Override
    public Optional<List<Animal>> animalsAt(Vector2d position) {
        return Optional.ofNullable(animalsOnMap.get(position));
    }

    @Override
    public void place(WorldElement element) throws IncorrectPositionException {
        Vector2d position = element.getPosition();

        if (!isPositionCorrect(position)) {
            throw new IncorrectPositionException(position);
        }

        if (element instanceof Animal) {

            LinkedList<Animal> animalsAtPosition = animalsOnMap.computeIfAbsent(position, currentPosition -> new LinkedList<>());

            if (animalsAtPosition.contains(element)) {
                throw new IllegalArgumentException("The animal is already present on the map.");
            }

            animalsOnMap.computeIfAbsent(position, currentPosition -> new LinkedList<>()).add((Animal) element);
        } else {
            placeGrass((Grass) element);
        }
    }

    @Override
    public void move(Animal animal) {
        Vector2d nextPosition = animal.getNextPosition();

        if (isPositionCorrect(nextPosition)) {
            removeAnimal(animal);
            animalsOnMap.computeIfAbsent(nextPosition, k -> new LinkedList<>()).add(animal);
            animal.move();
            return;
        }

        if (mapBoundary.lowerLeft().y() > nextPosition.y() || mapBoundary.upperRight().y() < nextPosition.y()) {
            animal.rotate(4);
            nextPosition = new Vector2d(nextPosition.x(), animal.getPosition().y());
        }

        if (mapBoundary.lowerLeft().x() > nextPosition.x() || mapBoundary.upperRight().x() < nextPosition.x()) {
            nextPosition = new Vector2d((nextPosition.x() + width) % width, nextPosition.y());
            removeAnimal(animal);
            animalsOnMap.computeIfAbsent(nextPosition, k -> new LinkedList<>()).add(animal);
            animal.move(nextPosition);
        }
    }

    @Override
    public void removeAnimal(Animal animal) {
        Vector2d position = animal.getPosition();

        LinkedList<Animal> list = animalsOnMap.get(position);

        if (list == null || !list.remove(animal)) {
            throw new NoSuchElementException("Cannot remove animal: this animal not found at this location.");
        }

        if (list.isEmpty()) {
            animalsOnMap.remove(position);
        }
    }

    protected boolean isPositionCorrect(Vector2d position) {
        return (position.follows(mapBoundary.lowerLeft()) &&
                position.precedes(mapBoundary.upperRight()));
    }

    abstract protected void placeGrass(Grass grass);

    @Override
    public void spawnGrass(int numberOfGrassToSpawn) {
        int grassLeft = numberOfGrassToSpawn;
        int numberOfGrassToSpawnOnPreferredPositions = (int) Math.round(numberOfGrassToSpawn * 0.8);
        int numberOfGrassToSpawnOnNotPreferredPositions = numberOfGrassToSpawn - numberOfGrassToSpawnOnPreferredPositions;
        Random random = new Random();

        while (numberOfGrassToSpawnOnPreferredPositions > 0) {
            List<Vector2d> preferredPositions = this.getFreeGrassPreferredPositions();

            if (preferredPositions.isEmpty()) {
                break;
            }

            Vector2d positionToSpawnGrass = preferredPositions.get(random.nextInt(preferredPositions.size()));

            this.placeGrass(new Grass(positionToSpawnGrass));
            numberOfGrassToSpawnOnPreferredPositions--;
            grassLeft--;
        }

        while (numberOfGrassToSpawnOnNotPreferredPositions > 0) {
            List<Vector2d> notPreferredPositions = this.getFreeGrassNotPreferredPositions();

            if (notPreferredPositions.isEmpty()) {
                break;
            }

            Vector2d positionToSpawnGrass = notPreferredPositions.get(random.nextInt(notPreferredPositions.size()));

            this.placeGrass(new Grass(positionToSpawnGrass));
            numberOfGrassToSpawnOnNotPreferredPositions--;
            grassLeft--;
        }

        while (grassLeft > 0) {
            List<Vector2d> preferredPositions = this.getFreeGrassPreferredPositions();
            List<Vector2d> notPreferredPositions = this.getFreeGrassNotPreferredPositions();

            if (preferredPositions.isEmpty() && notPreferredPositions.isEmpty()) {
                break;
            }

            List<Vector2d> availablePositions = new ArrayList<>();
            availablePositions.addAll(preferredPositions);
            availablePositions.addAll(notPreferredPositions);

            Vector2d positionToSpawnGrass = availablePositions.get(random.nextInt(availablePositions.size()));

            this.placeGrass(new Grass(positionToSpawnGrass));
            grassLeft--;
        }
    }
}