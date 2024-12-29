package project.model.maps;

import project.model.Vector2d;
import project.model.worldElements.Grass;
import project.model.worldElements.WorldElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Math.sqrt;

public class GrassField extends AbstractWorldMap {

    private final Map<Vector2d, Grass> grassOnMap = new HashMap<>();

    public GrassField(int numberOfGrassPieces) {

        RandomPositionGenerator generator = new RandomPositionGenerator((int) sqrt(numberOfGrassPieces * 10), (int) sqrt(numberOfGrassPieces * 10), numberOfGrassPieces);

        for(Vector2d grassPosition : generator) {
            grassOnMap.put(grassPosition, new Grass(grassPosition));
        }
    }

    @Override
    public Optional<WorldElement> objectAt(Vector2d position) {

        Optional<WorldElement> animal = super.objectAt(position);
        if(animal.isEmpty())
            return Optional.ofNullable(grassOnMap.get(position));

        return animal;
    }

    @Override
    public Collection<WorldElement> getElements() {
        return Stream.concat(super.getElements().stream(), grassOnMap.values().stream()).toList();
    }

    @Override
    public Boundary getCurrentBounds() {

        Collection<WorldElement> mapElements = this.getElements();

        if(mapElements == null || mapElements.isEmpty()) {
            return new Boundary(new Vector2d(0,0), new Vector2d(0,0));
        }

        Vector2d leftDown = new Vector2d(Integer.MAX_VALUE, Integer.MAX_VALUE);
        Vector2d rightUp = new Vector2d(Integer.MIN_VALUE, Integer.MIN_VALUE);

        for (WorldElement element : mapElements) {
            Vector2d position = element.getPosition();

            leftDown = leftDown.lowerLeft(position);
            rightUp = rightUp.upperRight(position);
        }

        return new Boundary(leftDown, rightUp);
    }
}