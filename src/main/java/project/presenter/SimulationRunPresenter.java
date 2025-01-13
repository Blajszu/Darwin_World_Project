package project.presenter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import project.model.maps.Boundary;
import project.model.maps.MapChangeListener;
import project.model.maps.WorldMap;
import project.model.worldElements.WorldElementBox;
import project.model.Vector2d;
import project.model.worldElements.Grass;

import java.util.List;
import java.util.stream.Stream;

public class SimulationRunPresenter implements MapChangeListener {

    private final int CELL_WIDTH = 55;
    private final int CELL_HEIGHT = 55;

    private WorldMap worldMap;
    private Boundary currentBounds;
    private List<WorldElementBox> grassBoxes;
    private List<WorldElementBox> animalBoxes;

    @FXML
    private Label moveLabel;
    @FXML
    public GridPane mapGrid;

    private void clearGrid() {
        if(mapGrid.getChildren().isEmpty()) return;

        mapGrid.getChildren().retainAll(mapGrid.getChildren().getFirst()); // hack to retain visible grid lines
        mapGrid.getColumnConstraints().clear();
        mapGrid.getRowConstraints().clear();
    }

    private void addLabels(int mapHeight, int mapWidth) {
        Label label = new Label("x/y");
        GridPane.setHalignment(label, HPos.CENTER);
        mapGrid.getColumnConstraints().add(new ColumnConstraints(CELL_WIDTH));
        mapGrid.getRowConstraints().add(new RowConstraints(CELL_HEIGHT));
        mapGrid.add(label, 0, 0);

        for(int i = 0; i <= mapHeight; i++) {
            Label labelY = new Label(String.valueOf(currentBounds.upperRight().getY() - i));
            GridPane.setHalignment(labelY, HPos.CENTER);
            mapGrid.getRowConstraints().add(new RowConstraints(CELL_HEIGHT));
            mapGrid.add(labelY, 0, i+1);
        }

        for(int i = 0; i <= mapWidth; i++) {
            Label labelX = new Label(String.valueOf( currentBounds.lowerLeft().getX() + i));
            GridPane.setHalignment(labelX, HPos.CENTER);
            mapGrid.getColumnConstraints().add(new ColumnConstraints(CELL_WIDTH));
            mapGrid.add(labelX, i+1, 0);
        }
    }

    private void addWorldElements() {

        List<WorldElementBox> worldElementBoxes;
        animalBoxes.forEach(WorldElementBox::update);

        worldElementBoxes = Stream.concat(grassBoxes.stream(), animalBoxes.stream()).toList();

        worldElementBoxes.forEach(worldElementBox -> {
            Vector2d position = worldElementBox.getElement().getPosition();
            mapGrid.add(worldElementBox.getGraphicBox(),
                    position.getX() + 1 - currentBounds.lowerLeft().getX(),
                    currentBounds.upperRight().getY() - position.getY() + 1);
        });
    }

    public void setWorldMap(WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    private void drawMap() {
        grassBoxes = worldMap.getElements().stream()
                .filter(worldElement -> worldElement instanceof Grass)
                .map(WorldElementBox::new)
                .toList();

        animalBoxes = worldMap.getOrderedAnimals().stream().map(WorldElementBox::new).toList();

        clearGrid();
        currentBounds = worldMap.getMapBounds();
        int mapHeight = currentBounds.upperRight().getY() - currentBounds.lowerLeft().getY();
        int mapWidth = currentBounds.upperRight().getX() - currentBounds.lowerLeft().getX();

        addLabels(mapHeight, mapWidth);
        addWorldElements();
    }

    @Override
    public void mapChanged(WorldMap worldMap, String message) {
        setWorldMap(worldMap);
        Platform.runLater(() -> {
            drawMap();
            moveLabel.setText(message);
        });
    }
}
