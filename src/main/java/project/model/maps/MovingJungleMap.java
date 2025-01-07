package project.model.maps;

import project.model.Vector2d;
import project.model.worldElements.Grass;
import project.model.worldElements.MapDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class MovingJungleMap extends AbstractWorldMap {

    public MovingJungleMap(int height, int width) {
        super(height, width);

        ArrayList<Vector2d> positions;
        for(int i = 0; i < height; i++) {
            positions = new ArrayList<>();

            for(int j = 0; j < width; j++) {
                positions.add(new Vector2d(j, i));
            }

            freeGrassNotPreferredPositions.addAll(positions);
        }
    }

    @Override
    protected void placeGrass(Grass grass) {
        Vector2d position = grass.getPosition();

        if(grassOnMap.containsKey(position)) {
            throw new PositionOccupiedException(grass);
        }
        grassOnMap.put(position, grass);

        for(Vector2d freePosition : getFreePositionsAroundPosition(position)) {
            if(!freeGrassPreferredPositions.contains(freePosition)) {
                freeGrassPreferredPositions.add(freePosition);
                freeGrassNotPreferredPositions.remove(freePosition);
            }
        }

        freeGrassPreferredPositions.remove(position);
        freeGrassNotPreferredPositions.remove(position);
    }

    @Override
    public void removeGrass(Vector2d position) {
        if(!grassOnMap.containsKey(position)){
            throw new NoSuchElementException("Cannot remove grass: no grass found at this location.");
        }
        grassOnMap.remove(position);

        for(Vector2d freePosition : getFreePositionsAroundPosition(position)) {
            if(!checkIfPositionIsPreferred(freePosition)) {
                freeGrassPreferredPositions.remove(freePosition);
                freeGrassNotPreferredPositions.add(freePosition);
            }
        }

        if(checkIfPositionIsPreferred(position)) {
            freeGrassPreferredPositions.add(position);
        }
        else {
            freeGrassNotPreferredPositions.add(position);
        }
    }

    private List<Vector2d> getFreePositionsAroundPosition(Vector2d position) {
        List<Vector2d> freePositions = new ArrayList<>();
        for(MapDirection direction : MapDirection.values()) {
            Vector2d positionToCheck = position.add(direction.toUnitVector());
            if(!grassOnMap.containsKey(positionToCheck) && isPositionCorrect(positionToCheck)) {
                freePositions.add(positionToCheck);
            }
        }
        return freePositions;
    }

    private boolean checkIfPositionIsPreferred(Vector2d position) {
        for(MapDirection direction : MapDirection.values()) {
            Vector2d positionToCheck = position.add(direction.toUnitVector());
            if(grassOnMap.containsKey(positionToCheck)) {
                return true;
            }
        }
        return false;
    }
}
