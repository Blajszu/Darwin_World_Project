package project.model.maps;

import project.model.Vector2d;
import project.model.worldElements.Animal;
import project.model.worldElements.Grass;
import project.model.worldElements.WorldElement;

import java.util.ArrayList;
import java.util.List;

public class EquatorMap extends AbstractWorldMap {

    private final Boundary equatorBoundary;

    private final List<Vector2d> freeGrassPositionsOnEquator = new ArrayList<>();
    private final List<Vector2d> freeGrassPositionsOnSteppe = new ArrayList<>();

    public EquatorMap(int height, int width) {
        super(height, width);

        int equatorHeight = (int) Math.round(height*0.2);
        int equatorStartHeight = height/2 - equatorHeight/2;
        int equatorEndHeight = equatorStartHeight + equatorHeight - 1;
        equatorBoundary = new Boundary(new Vector2d(0, equatorStartHeight), new Vector2d(width - 1, equatorEndHeight));

        ArrayList<Vector2d> positions;

        for(int i = 0; i < height; i++) {
            positions = new ArrayList<>();

            for(int j = 0; j < width; j++) {
                positions.add(new Vector2d(j, i));
            }

            if(isPositionOnEquator(positions.getFirst())){
                freeGrassPositionsOnEquator.addAll(positions);
            }
            else {
                freeGrassPositionsOnSteppe.addAll(positions);
            }
        }
    }

    @Override
    public void place(WorldElement element) throws IncorrectPositionException {
        Vector2d position = element.getPosition();

        if(!isPositionCorrect(position)) {
            throw new IncorrectPositionException(position);
        }

        if(element instanceof Animal) {
            animalsOnMap.get(position).add((Animal) element);
        }
        else {
            if(grassOnMap.containsKey(position)) {
                throw new PositionOccupiedException(element);
            }
            grassOnMap.put(position, (Grass) element);

            if(isPositionOnEquator(position)) {
                freeGrassPositionsOnEquator.remove(position);
            }
            else {
                freeGrassPositionsOnSteppe.remove(position);
            }
        }
    }

    @Override
    public void move(Animal animal) {
        Vector2d currentPosition = animal.getPosition();
        Vector2d nextPosition = animal.nextPosition();

        if(isPositionCorrect(nextPosition)) {
            removeAnimal(animal);
            animalsOnMap.get(nextPosition).add(animal);
            animal.move();
            mapChangeEvent("Animal moved from %s to %s ".formatted(currentPosition, nextPosition));
            return;
        }

        if(mapBoundary.lowerLeft().getY() > nextPosition.getY() || mapBoundary.upperRight().getY() < nextPosition.getY()) {
            animal.rotate(4);
            nextPosition = new Vector2d(nextPosition.getX(), animal.getPosition().getY());
        }

        if(mapBoundary.lowerLeft().getX() > nextPosition.getX() || mapBoundary.upperRight().getX() < nextPosition.getX()) {
            nextPosition = new Vector2d((nextPosition.getX() + width) % width, nextPosition.getY());
            removeAnimal(animal);
            animalsOnMap.get(nextPosition).add(animal);
            animal.move(nextPosition);
            mapChangeEvent("Animal moved from %s to %s ".formatted(currentPosition, nextPosition));
        }
    }

    public Boundary getEquatorBounds() {
        return equatorBoundary;
    }

    public List<Vector2d> getGrassPreferredPositions() {
        return freeGrassPositionsOnEquator;
    }

    public List<Vector2d> getGrassNotPreferredPositions() {
        return freeGrassPositionsOnSteppe;
    }

    @Override
    public void removeGrass(Vector2d position) {
        grassOnMap.remove(position);
        if(isPositionOnEquator(position)) {
            freeGrassPositionsOnEquator.add(position);
        }
        else {
            freeGrassPositionsOnSteppe.add(position);
        }
    }

    private boolean isPositionCorrect(Vector2d position) {
        return (position.follows(mapBoundary.lowerLeft()) &&
                position.precedes(mapBoundary.upperRight()));
    }

    private boolean isPositionOnEquator(Vector2d position) {
        return (position.follows(equatorBoundary.lowerLeft()) &&
                position.precedes(equatorBoundary.upperRight()));
    }
}