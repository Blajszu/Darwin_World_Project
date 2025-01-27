package project.model.worldElements;

import project.model.Vector2d;

public interface WorldElement {

    Vector2d getPosition();

    String getResourceName();

    String getResourceFileName(); // czym się różnią te dwie metody?
}
