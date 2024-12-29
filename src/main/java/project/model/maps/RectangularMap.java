package project.model.maps;

import project.model.Vector2d;

public class RectangularMap extends AbstractWorldMap {

    private final Vector2d leftDownMapCorner = new Vector2d(0,0);
    private final Vector2d rightUpMapCorner;
    private final Boundary boundary;

    public RectangularMap(int height, int width) {

        rightUpMapCorner = new Vector2d(width - 1, height - 1);
        boundary = new Boundary(leftDownMapCorner, rightUpMapCorner);
    }

    @Override
    public boolean canMoveTo(Vector2d position) {
        return (super.canMoveTo(position) &&
                position.follows(leftDownMapCorner) &&
                position.precedes(rightUpMapCorner));
    }

    @Override
    public Boundary getCurrentBounds() {
        return boundary;
    }
}