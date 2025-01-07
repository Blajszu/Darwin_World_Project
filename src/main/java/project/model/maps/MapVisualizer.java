package project.model.maps;

import project.model.Vector2d;
import project.model.worldElements.WorldElement;

public class MapVisualizer {
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

        int width = upperRight.getX() - lowerLeft.getX() + 1;
        int height = upperRight.getY() - lowerLeft.getY() + 1;

        grid = new String[height][width];

        for (int y = lowerLeft.getY(); y <= upperRight.getY(); y++) {
            for (int x = lowerLeft.getX(); x <= upperRight.getX(); x++) {
                grid[y][x] = EMPTY_CELL;
            }
        }
    }

    public String draw() {
        updateMap();
        StringBuilder builder = new StringBuilder();
        for (int i = upperRight.getY() + 1; i >= lowerLeft.getY() - 1; i--) {
            if (i == upperRight.getY() + 1) {
                builder.append(drawHeader(lowerLeft, upperRight));
            }
            builder.append(String.format("%3d: ", i));
            for (int j = lowerLeft.getX(); j <= upperRight.getX() + 1; j++) {
                if (i < lowerLeft.getY() || i > upperRight.getY()) {
                    builder.append(drawFrame(j <= upperRight.getX()));
                } else {
                    builder.append(CELL_SEGMENT);
                    if (j <= upperRight.getX()) {
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
        for (int j = lowerLeft.getX(); j < upperRight.getX() + 1; j++) {
            builder.append(String.format("%2d", j));
        }
        builder.append(System.lineSeparator());
        return builder.toString();
    }

    private void updateMap() {
        for (int y = lowerLeft.getY(); y <= upperRight.getY(); y++) {
            for (int x = lowerLeft.getX(); x <= upperRight.getX(); x++) {
                grid[y][x] = EMPTY_CELL;
            }
        }

        for (WorldElement element : map.getElements()) {
            Vector2d position = element.getPosition();

            if(grid[position.getY()][position.getX()].equals(EMPTY_CELL)) {
                grid[position.getY()][position.getX()] = element.toString();
            }
        }
    }
}