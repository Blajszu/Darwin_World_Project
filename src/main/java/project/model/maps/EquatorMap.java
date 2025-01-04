package project.model.maps;

import project.model.Vector2d;
import project.model.worldElements.Grass;

import java.util.ArrayList;

public class EquatorMap extends AbstractWorldMap {

    private final Boundary equatorBoundary;

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

            if(isPositionPreferredByGrass(positions.getFirst())){
                freeGrassPreferredPositions.addAll(positions);
            }
            else {
                freeGrassNotPreferredPositions.addAll(positions);
            }
        }
    }

    public Boundary getEquatorBounds() {
        return equatorBoundary;
    }

    @Override
    public void removeGrass(Vector2d position) {
        grassOnMap.remove(position);
        if(isPositionPreferredByGrass(position)) {
            freeGrassPreferredPositions.add(position);
        }
        else {
            freeGrassNotPreferredPositions.add(position);
        }
    }

    protected void placeGrass(Grass grass) {
        Vector2d position = grass.getPosition();

        if(grassOnMap.containsKey(position)) {
            throw new PositionOccupiedException(grass);
        }
        grassOnMap.put(position, grass);

        if(isPositionPreferredByGrass(position)) {
            freeGrassPreferredPositions.remove(position);
        }
        else {
            freeGrassNotPreferredPositions.remove(position);
        }
    }

    private boolean isPositionPreferredByGrass(Vector2d position) {
        return (position.follows(equatorBoundary.lowerLeft()) &&
                position.precedes(equatorBoundary.upperRight()));
    }
}