package project.presenter;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import project.Simulation;
import project.listener.SimulationChangeListener;
import project.listener.SimulationEventType;
import project.model.maps.Boundary;
import project.model.maps.WorldMap;
import project.model.worldElements.Animal;
import project.model.worldElements.WorldElementBox;
import project.model.Vector2d;
import project.model.worldElements.Grass;
import project.statistics.StatisticsRecord;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;

public class SimulationRunPresenter implements SimulationChangeListener {

    private int cellSize;

    private Simulation simulation;
    private WorldMap worldMap;
    private Boundary currentBounds;
    private List<WorldElementBox> grassBoxes = new ArrayList<>();
    private final List<WorldElementBox> animalBoxes = new ArrayList<>();

    private XYChart.Series<Number, Number> animalsSeries;
    private XYChart.Series<Number, Number> grassesSeries;

    private boolean isSimulationStopped = false;

    @FXML
    private Label moveLabel;
    @FXML
    private GridPane mapGrid;
    @FXML
    private LineChart<Number, Number> simulationChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private Slider simulationDelay;
    @FXML
    private Button stopRestartSimulationButton;

    @FXML
    private Label animalsCountLabel;
    @FXML
    private Label grassCountLabel;
    @FXML
    private Label emptyFieldsCountLabel;
    @FXML
    private Label averageEnergyLabel;
    @FXML
    private Label averageLifeLengthLabel;
    @FXML
    private Label averageChildrenCountLabel;
    @FXML
    private Label mostPopularGenotypesLabel;


    @FXML
    public void initialize() {
        animalsSeries = new XYChart.Series<>();
        animalsSeries.setName("Animals");

        grassesSeries = new XYChart.Series<>();
        grassesSeries.setName("Grasses");

        List<XYChart.Series<Number, Number>> series = List.of(animalsSeries, grassesSeries);
        simulationChart.getData().addAll(series);

        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(40);

        simulationDelay.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.setCoolDown(newValue.intValue());
        });
    }

    private void clearGrid() {
        if (mapGrid.getChildren().isEmpty()) return;

        mapGrid.getChildren().retainAll(mapGrid.getChildren().getFirst()); // hack to retain visible grid lines
        mapGrid.getColumnConstraints().clear();
        mapGrid.getRowConstraints().clear();
    }

    private void addLabels(int mapHeight, int mapWidth) {
        Label label = new Label("x/y");
        label.setId("axisLabel");
        label.setStyle("-fx-font-size: %dpx;".formatted(cellSize / 2));
        label.setStyle("-fx-min-height: %dpx;".formatted(cellSize));

        GridPane.setHalignment(label, HPos.CENTER);
        mapGrid.getColumnConstraints().add(new ColumnConstraints(cellSize));
        mapGrid.getRowConstraints().add(new RowConstraints(cellSize));
        mapGrid.add(label, 0, 0);

        for (int i = 0; i <= mapHeight; i++) {
            Label labelY = new Label(String.valueOf(currentBounds.upperRight().getY() - i));
            labelY.setId("axisLabel");
            labelY.setStyle("-fx-font-size: %dpx;".formatted(cellSize / 2));
            labelY.setStyle("-fx-min-height: %dpx;".formatted(cellSize));

            GridPane.setHalignment(labelY, HPos.CENTER);
            mapGrid.getRowConstraints().add(new RowConstraints(cellSize));
            mapGrid.add(labelY, 0, i + 1);
        }

        for (int i = 0; i <= mapWidth; i++) {
            Label labelX = new Label(String.valueOf(currentBounds.lowerLeft().getX() + i));
            labelX.setId("axisLabel");
            labelX.setStyle("-fx-font-size: %dpx;".formatted(cellSize / 2));
            labelX.setStyle("-fx-min-height: %dpx;".formatted(cellSize));

            GridPane.setHalignment(labelX, HPos.CENTER);
            mapGrid.getColumnConstraints().add(new ColumnConstraints(cellSize));
            mapGrid.add(labelX, i + 1, 0);
        }
    }

    private void addWorldElements() {

        List<WorldElementBox> worldElementBoxes;

        worldElementBoxes = Stream.concat(grassBoxes.stream(), animalBoxes.stream()).toList();

        worldElementBoxes.forEach(worldElementBox -> {
            Vector2d position = worldElementBox.getElement().getPosition();
            mapGrid.add(worldElementBox.getGraphicBox(),
                    position.getX() + 1 - currentBounds.lowerLeft().getX(),
                    currentBounds.upperRight().getY() - position.getY() + 1);
        });
    }

    private void writeStatistics(SimulationEventType eventType, StatisticsRecord statisticsRecord) {

        if (eventType != SimulationEventType.DAY_ENDED) {
            return;
        }

        animalsCountLabel.setText("Liczba zwierząt: %s".formatted(statisticsRecord.animalsCount()));
        grassCountLabel.setText("Liczba traw: %s".formatted(statisticsRecord.plantsCount()));
        emptyFieldsCountLabel.setText("Liczba pustych pól: %s".formatted(statisticsRecord.emptyFieldsCount()));

        DecimalFormat df = new DecimalFormat("#.##");

        averageEnergyLabel.setText("Średnia energia zwierząt: %s".formatted(df.format(statisticsRecord.averageEnergy())));
        averageLifeLengthLabel.setText("Średnia długość życia: %s".formatted(df.format(statisticsRecord.averageLifeLength())));
        averageChildrenCountLabel.setText("Średnia liczba dzieci: %s".formatted(df.format(statisticsRecord.averageChildrenCount())));

        List<String> topGenotypes = statisticsRecord.genotypesCount().entrySet()
                .stream()
                .sorted((genotype, count) -> count.getValue().compareTo(genotype.getValue()))
                .limit(5)
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .toList();

        mostPopularGenotypesLabel.setText("Najpopularniejsze Genotypy\n" + String.join(", ", topGenotypes).replace(',', '\n'));

        animalsSeries.getData().add(new XYChart.Data<>(statisticsRecord.day(), statisticsRecord.animalsCount()));
        grassesSeries.getData().add(new XYChart.Data<>(statisticsRecord.day(), statisticsRecord.plantsCount()));

        if (animalsSeries.getData().size() > 40) {
            animalsSeries.getData().removeFirst();
            grassesSeries.getData().removeFirst();

            double firstDay = animalsSeries.getData().getFirst().getXValue().doubleValue();
            double lastDay = statisticsRecord.day();

            xAxis.setLowerBound(firstDay);
            xAxis.setUpperBound(lastDay);

            double range = lastDay - firstDay;
            xAxis.setTickUnit(range / 10);
        }
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        this.worldMap = simulation.getWorldMap();

        cellSize = Math.min(500 / (worldMap.getMapHeight() + 1), 500 / (worldMap.getMapWidth() + 1));
        mapGrid.setMaxWidth(cellSize * worldMap.getMapWidth());
    }

    private void drawMap() {

        ArrayList<Animal> conflictedAnimals = new ArrayList<>();
        animalBoxes.clear();

        for (Animal animal : worldMap.getOrderedAnimals()) {
            if (conflictedAnimals.isEmpty() || (conflictedAnimals.getFirst().getPosition().equals(animal.getPosition()))) {
                conflictedAnimals.add(animal);
            } else {
                List<Animal> resolvedConflicts = simulation.resolveAnimalsConflicts(conflictedAnimals);
                animalBoxes.add(new WorldElementBox(resolvedConflicts.getFirst(), cellSize));

                conflictedAnimals.clear();
                conflictedAnimals.add(animal);
            }
        }

        grassBoxes = worldMap.getElements().stream()
                .filter(worldElement -> worldElement instanceof Grass)
                .map(element -> new WorldElementBox(element, cellSize))
                .toList();

        clearGrid();
        currentBounds = worldMap.getMapBounds();
        int mapHeight = currentBounds.upperRight().getY() - currentBounds.lowerLeft().getY();
        int mapWidth = currentBounds.upperRight().getX() - currentBounds.lowerLeft().getX();

        addLabels(mapHeight, mapWidth);
        addWorldElements();
    }

    @Override
    public void handleChangeEvent(WorldMap worldMap, SimulationEventType eventType, StatisticsRecord statisticsRecord) {
        Platform.runLater(() -> {
            drawMap();

            moveLabel.setText("%s%n Day: %s".formatted(eventType, statisticsRecord.day()));
            writeStatistics(eventType, statisticsRecord);
            if (!isSimulationStopped) {
                simulation.countDown();
            }
        });
    }

    public void stopRestartSimulation(ActionEvent event) {

        if (!isSimulationStopped) {
            stopRestartSimulationButton.setText("RESTART SIMULATION");
        } else {
            stopRestartSimulationButton.setText("STOP SIMULATION");
            simulation.countDown();
        }

        isSimulationStopped = !isSimulationStopped;
    }

    public void endSimulation(ActionEvent event) {
        simulation.stopSimulation();
        Stage stage = (Stage) mapGrid.getScene().getWindow();
        stage.close();
    }
}
