package project.model.worldElements;

import project.model.Vector2d;

public enum MapDirection {
    NORTH,
    NORTHEAST,
    EAST,
    SOUTHEAST,
    SOUTHWEST,
    SOUTH,
    WEST,
    NORTHWEST;

    private static final Vector2d[] directions = {
            new Vector2d(0, 1),
            new Vector2d(1,1),
            new Vector2d(1, 0),
            new Vector2d(1,-1),
            new Vector2d(0, -1),
            new Vector2d(-1,-1),
            new Vector2d(-1, 0),
            new Vector2d(-1,1)
    };

    private static final String[] names = {"Polnoc","PolnocnyWschod", "Wschod", "PoludniowyWschod", "Poludnie", "PoludniowyZachod", "Zachod","PolnocnyZachod"};

    @Override
    public String toString() {
        return names[this.ordinal()];
    }

    public Vector2d toUnitVector () {
        return directions[this.ordinal()];
    }

    public MapDirection rotate(int numberOfRotations) {
        return MapDirection.values()[((this.ordinal()+numberOfRotations)%8)];
    }
}
