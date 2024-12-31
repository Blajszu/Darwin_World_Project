package project.model.worldElements;

import project.model.Vector2d;

public enum MapDirection {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    private static final Vector2d[] directions = {
            new Vector2d(0, 1),
            new Vector2d(1, 0),
            new Vector2d(0, -1),
            new Vector2d(-1, 0)
    };

    private static final String[] names = {"Polnoc", "Wschod", "Poludnie", "Zachod"};

    @Override
    public String toString() {
        return names[this.ordinal()];
    }

    public MapDirection next() {
        return MapDirection.values()[((this.ordinal()+1)%4)];
    }

    public MapDirection previous() {
        int index = this.ordinal()-1;
        if (index < 0) index = 3;
        return MapDirection.values()[index];
    }

    public Vector2d toUnitVector () {
        return directions[this.ordinal()];
    }
}
