package project.presenter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.util.List;
import java.util.stream.Collectors;
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
    private List<String> topGenotypes;

    private final ArrayList<Label> animalsTopGenotypesLabels = new ArrayList<>();
    private final ArrayList<ImageView> grassPreferredPositionsImageView = new ArrayList<>();

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

        if(Collections.max(statisticsRecord.genotypesCount().values()) == 1){
            mostPopularGenotypesLabel.setText("Najpopularniejsze Genotypy\n" + "Brak najpopularniejszego genotypu");
        }
        else {

            topGenotypes = statisticsRecord.genotypesCount().entrySet()
                    .stream()
                    .collect(Collectors.groupingBy(
                            Map.Entry::getValue,
                            TreeMap::new,
                            Collectors.toList()
                    ))
                    .descendingMap()
                    .entrySet().stream()
                    .findFirst()
                    .filter(entry -> entry.getKey() > 1)
                    .map(entry -> entry.getValue().stream().limit(5))
                    .orElse(Stream.empty())
                    .map(Map.Entry::getKey)
                    .toList();

            List<String> topGenotypesToDisplay = topGenotypes.stream()
                    .map(genotpe -> "%s (%d)".formatted(genotpe, statisticsRecord.genotypesCount().get(genotpe)))
                    .toList();

            mostPopularGenotypesLabel.setText("Najpopularniejsze Genotypy\n" + String.join(", ", topGenotypesToDisplay).replace(',', '\n'));
        }


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
        if(!conflictedAnimals.isEmpty()) {
            List<Animal> resolvedConflicts = simulation.resolveAnimalsConflicts(conflictedAnimals);
            animalBoxes.add(new WorldElementBox(resolvedConflicts.getFirst(), cellSize));
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
            } else {
                colorPreferredGrassPositions();
                colorMostPopularGenotype();
            }
        });
    }

    private void colorMostPopularGenotype() {
        if(topGenotypes == null)
            return;

        HashSet<Vector2d> topGenotypesAnimalsPositions = worldMap.getOrderedAnimals().stream()
                .filter(animal -> topGenotypes.contains(animal.getAnimalGenesString()))
                .map(Animal::getPosition)
                .collect(Collectors.toCollection(HashSet::new));

        for (Vector2d position : topGenotypesAnimalsPositions) {
            Label label = new Label();
            label.setStyle("-fx-background-color: #7742eb");
            label.setMinHeight(cellSize);
            label.setMinWidth(cellSize);
            animalsTopGenotypesLabels.add(label);

            mapGrid.add(label, position.getX()+1, worldMap.getMapHeight() - position.getY());
            label.toBack();
        }
    }

    private void colorPreferredGrassPositions() {

        List<Vector2d> grassPositionsToColor = worldMap.getFreeGrassPreferredPositions();
        Image image = new Image("x.png");


        for (Vector2d position : grassPositionsToColor ) {
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(cellSize);
            imageView.setFitWidth(cellSize);
            grassPreferredPositionsImageView.add(imageView);

            mapGrid.add(imageView, position.getX()+1, worldMap.getMapHeight() - position.getY());
            imageView.toBack();
        }
    }

    private void removeColoredElements() {
        mapGrid.getChildren().removeAll(grassPreferredPositionsImageView);
        mapGrid.getChildren().removeAll(animalsTopGenotypesLabels);
        grassPreferredPositionsImageView.clear();
        animalsTopGenotypesLabels.clear();
    }

    public void stopRestartSimulation() {

        if (!isSimulationStopped) {
            stopRestartSimulationButton.setText("RESTART SIMULATION");

        } else {
            stopRestartSimulationButton.setText("STOP SIMULATION");
            removeColoredElements();
            simulation.countDown();
        }

        isSimulationStopped = !isSimulationStopped;
    }

    public void endSimulation() {
        simulation.stopSimulation();
        Stage stage = (Stage) mapGrid.getScene().getWindow();
        stage.close();
    }
}
