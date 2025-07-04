package project.model.worldElements;

import project.model.Vector2d;

public class Grass implements WorldElement {

    private final Vector2d position;

    public Grass(Vector2d pos) {
        position = pos;
    }

    public Vector2d getPosition() {
        return position;
    }

    @Override
    public String getResourceFileName() {
        return "images/grass.png";
    }

    @Override
    public String toString() {
        return "*";
    }
}
