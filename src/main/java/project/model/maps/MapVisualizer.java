package project.model.maps;

import project.model.Vector2d;
import project.model.worldElements.WorldElement;

public class MapVisualizer {  // czy to jest potrzebne?
    private static final String EMPTY_CELL = " ";
    private static final String FRAME_SEGMENT = "-";
    private static final String CELL_SEGMENT = "|";
    private final WorldMap map;

    private final Vector2d lowerLeft;
    private final Vector2d upperRight;

    String[][] grid;

    public MapVisualizer(WorldMap map) {
        this.map = map;

        Boundary bounds = map.getMapBounds();
        lowerLeft = bounds.lowerLeft();
        upperRight = bounds.upperRight();

        int width = upperRight.x() - lowerLeft.x() + 1;
        int height = upperRight.y() - lowerLeft.y() + 1;

        grid = new String[height][width];

        for (int y = lowerLeft.y(); y <= upperRight.y(); y++) {
            for (int x = lowerLeft.x(); x <= upperRight.x(); x++) {
                grid[y][x] = EMPTY_CELL;
            }
        }
    }

    public String draw() {
        updateMap();
        StringBuilder builder = new StringBuilder();
        for (int i = upperRight.y() + 1; i >= lowerLeft.y() - 1; i--) {
            if (i == upperRight.y() + 1) {
                builder.append(drawHeader(lowerLeft, upperRight));
            }
            builder.append(String.format("%3d: ", i));
            for (int j = lowerLeft.x(); j <= upperRight.x() + 1; j++) {
                if (i < lowerLeft.y() || i > upperRight.y()) {
                    builder.append(drawFrame(j <= upperRight.x()));
                } else {
                    builder.append(CELL_SEGMENT);
                    if (j <= upperRight.x()) {
                        builder.append(grid[i][j]);
                    }
                }
            }
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String drawFrame(boolean innerSegment) {
        if (innerSegment) {
            return FRAME_SEGMENT + FRAME_SEGMENT;
        } else {
            return FRAME_SEGMENT;
        }
    }

    private String drawHeader(Vector2d lowerLeft, Vector2d upperRight) {
        StringBuilder builder = new StringBuilder();
        builder.append(" y\\x ");
        for (int j = lowerLeft.x(); j < upperRight.x() + 1; j++) {
            builder.append(String.format("%2d", j));
        }
        builder.append(System.lineSeparator());
        return builder.toString();
    }

    private void updateMap() {
        for (int y = lowerLeft.y(); y <= upperRight.y(); y++) {
            for (int x = lowerLeft.x(); x <= upperRight.x(); x++) {
                grid[y][x] = EMPTY_CELL;
            }
        }

        for (WorldElement element : map.getElements()) {
            Vector2d position = element.getPosition();

            if (grid[position.y()][position.x()].equals(EMPTY_CELL)) {
                grid[position.y()][position.x()] = element.toString();
            }
        }
    }
}