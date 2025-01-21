package project.model;

import static java.lang.Math.max;
import static java.lang.Math.min;

public record Vector2d(int x, int y) {

    @Override
    public String toString() {
        return "(%d, %d)".formatted(x, y);
    }

    public boolean precedes(Vector2d other) {
        return x <= other.x && y <= other.y;
    }

    public boolean follows(Vector2d other) {
        return x >= other.x && y >= other.y;
    }

    public Vector2d add(Vector2d other) {
        return new Vector2d(x + other.x, y + other.y);
    }

    public Vector2d subtract(Vector2d other) {
        return new Vector2d(x - other.x, y - other.y);
    }

    public Vector2d upperRight(Vector2d other) {
        return new Vector2d(max(x, other.x), max(y, other.y));
    }

    public Vector2d lowerLeft(Vector2d other) {
        return new Vector2d(min(x, other.x), min(y, other.y));
    }

    public Vector2d opposite() {
        return new Vector2d(-x, -y);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Vector2d(int x1, int y1)))
            return false;

        return x == x1 && y == y1;
    }

}
