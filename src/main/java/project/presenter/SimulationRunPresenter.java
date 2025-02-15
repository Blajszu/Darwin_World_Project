package project.presenter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import project.Simulation;
import project.listener.SimulationChangeListener;
import project.listener.SimulationEventType;
import project.model.maps.Boundary;
import project.model.maps.WorldMap;
import project.model.worldElements.Animal;
import project.model.worldElements.WorldElementBox;
import project.model.Vector2d;
import project.model.worldElements.Grass;
import project.statistics.AnimalStatistics;
import project.statistics.AnimalStatisticsRecord;
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
    private final HashMap<Animal, WorldElementBox> animalBoxes = new HashMap<>();

    private XYChart.Series<Number, Number> animalsSeries;
    private XYChart.Series<Number, Number> grassesSeries;

    private int initialAnimalEnergy;
    private Animal selectedAnimal;
    private WorldElementBox selectedAnimalBox;
    private AnimalStatistics selectedAnimalStatistics = null;

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
    private Label animalStatisticGenomLabel;
    @FXML
    private Label animalStatisticActiveGenLabel;
    @FXML
    private Label animalStatisticEnergyLabel;
    @FXML
    private Label animalStatisticEatenGrassesLabel;
    @FXML
    private Label animalStatisticChildrenLabel;
    @FXML
    private Label animalStatisticDescendantsLabel;
    @FXML
    private Label animalStatisticLengthOfLifeLabel;
    @FXML
    private Label animalStatisticDeathLabel;

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

        simulationDelay.valueProperty().addListener((observable, oldValue, newValue) -> simulation.setCoolDown(newValue.intValue()));
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        this.worldMap = simulation.getWorldMap();
        this.initialAnimalEnergy = simulation.getInitialAnimalsEnergy();

        cellSize = Math.min(500 / (worldMap.getMapHeight() + 1), 500 / (worldMap.getMapWidth() + 1));
        mapGrid.setMaxWidth(cellSize * worldMap.getMapWidth());
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

    public void setSimulationState(boolean state) {
        isSimulationStopped = state;
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
            Label labelY = new Label(String.valueOf(currentBounds.upperRight().y() - i));
            labelY.setId("axisLabel");
            labelY.setStyle("-fx-font-size: %dpx;".formatted(cellSize / 2));
            labelY.setStyle("-fx-min-height: %dpx;".formatted(cellSize));

            GridPane.setHalignment(labelY, HPos.CENTER);
            mapGrid.getRowConstraints().add(new RowConstraints(cellSize));
            mapGrid.add(labelY, 0, i + 1);
        }

        for (int i = 0; i <= mapWidth; i++) {
            Label labelX = new Label(String.valueOf(currentBounds.lowerLeft().x() + i));
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

        worldElementBoxes = Stream.concat(grassBoxes.stream(), animalBoxes.values().stream()).toList();

        worldElementBoxes.forEach(worldElementBox -> {
            Vector2d position = worldElementBox.getElement().getPosition();
            mapGrid.add(worldElementBox.getGraphicBox(),
                    position.x() + 1 - currentBounds.lowerLeft().x(),
                    currentBounds.upperRight().y() - position.y() + 1);
        });

        if(selectedAnimal != null && selectedAnimal.isAnimalAlive()) {
            WorldElementBox selectAnimalBox = new WorldElementBox(selectedAnimal, cellSize, initialAnimalEnergy, true);
            this.selectedAnimalBox = selectAnimalBox;
            Vector2d position = selectAnimalBox.getElement().getPosition();
            mapGrid.add(selectAnimalBox.getGraphicBox(),
                    position.x() + 1 - currentBounds.lowerLeft().x(),
                    currentBounds.upperRight().y() - position.y() + 1);

            animalBoxes.put(selectedAnimal, selectAnimalBox);
        }

        for (Node node : mapGrid.getChildren()) {
            if (node instanceof Region) {
                node.setOnMouseClicked(event -> handleCellClick(node));
            }
        }
    }

    private void handleCellClick(Node node) {
        if(!isSimulationStopped)
            return;

        if(selectedAnimalBox != null) {
            selectedAnimalBox.setSelected(false);
            selectedAnimalBox.fillContent();
        }

        int rowIndex = worldMap.getMapHeight() - GridPane.getRowIndex(node);
        int columnIndex = GridPane.getColumnIndex(node) - 1;

        Vector2d position = new Vector2d(columnIndex, rowIndex);

        Optional<List<Animal>> animalsAtPosition = worldMap.animalsAt(position);
        if (animalsAtPosition.isEmpty())
            return;

        Animal newSelectedAnimal = simulation.resolveAnimalsConflicts(animalsAtPosition.get()).getFirst();
        if(selectedAnimal == newSelectedAnimal) {
            selectedAnimalBox.setSelected(true);
            selectedAnimalBox.fillContent();
            return;
        }

        selectedAnimal = newSelectedAnimal;
        selectedAnimalBox = animalBoxes.get(selectedAnimal);

        selectedAnimalBox.setSelected(true);
        selectedAnimalBox.fillContent();

        selectedAnimalStatistics = new AnimalStatistics(selectedAnimal, simulation);
        writeAnimalStatistics();
    }

    private void writeAnimalStatistics() {
        AnimalStatisticsRecord record = selectedAnimalStatistics.getRecord();

        animalStatisticGenomLabel.setText(String.format("Genom: %s", record.animalGene()));
        animalStatisticActiveGenLabel.setText(String.format("Aktywna część genomu: %s", record.activePartOfGenome()));
        animalStatisticEnergyLabel.setText(String.format("Ilość energii: %d", record.currentEnergy()));
        animalStatisticEatenGrassesLabel.setText(String.format("Liczba zjedzonych roślin: %d", record.numberOfEatenPlants()));
        animalStatisticChildrenLabel.setText(String.format("Liczba dzieci: %d", record.numberOfKids()));
        animalStatisticDescendantsLabel.setText(String.format("Liczba potomków: %d", record.numberOfDescendants()));
        animalStatisticLengthOfLifeLabel.setText(String.format("Ile dni żyje/żył: %d", record.lengthOfLife()));
        animalStatisticDeathLabel.setText(String.format("Którego dnia umarł: %s",
                (record.whenDied() == null) ? "Jeszcze żyje" : record.whenDied().toString()));
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

        if(statisticsRecord.genotypesCount().isEmpty() || Collections.max(statisticsRecord.genotypesCount().values()) == 1){
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
                    .filter(entry -> entry.getKey() > 1).stream().flatMap(entry -> entry.getValue().stream().limit(5))
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

    private void drawMap() {

        ArrayList<Animal> conflictedAnimals = new ArrayList<>();
        animalBoxes.clear();

        ArrayList<Animal> animalsOnGrid = new ArrayList<>(worldMap.getOrderedAnimals());

        if(selectedAnimal != null)
            animalsOnGrid.remove(selectedAnimal);

        for (Animal animal : animalsOnGrid) {
            if (conflictedAnimals.isEmpty() || (conflictedAnimals.getFirst().getPosition().equals(animal.getPosition()))) {
                conflictedAnimals.add(animal);
            } else {
                List<Animal> resolvedConflicts = simulation.resolveAnimalsConflicts(conflictedAnimals);
                animalBoxes.put(resolvedConflicts.getFirst(), new WorldElementBox(resolvedConflicts.getFirst(), cellSize, initialAnimalEnergy));

                conflictedAnimals.clear();
                conflictedAnimals.add(animal);
            }
        }
        if(!conflictedAnimals.isEmpty()) {
            List<Animal> resolvedConflicts = simulation.resolveAnimalsConflicts(conflictedAnimals);
            animalBoxes.put(resolvedConflicts.getFirst(), new WorldElementBox(resolvedConflicts.getFirst(), cellSize, initialAnimalEnergy));
        }

        grassBoxes = worldMap.getElements().stream()
                .filter(worldElement -> worldElement instanceof Grass)
                .map(element -> new WorldElementBox(element, cellSize, initialAnimalEnergy))
                .toList();

        clearGrid();
        currentBounds = worldMap.getMapBounds();
        int mapHeight = currentBounds.upperRight().y() - currentBounds.lowerLeft().y();
        int mapWidth = currentBounds.upperRight().x() - currentBounds.lowerLeft().x();

        addLabels(mapHeight, mapWidth);
        addWorldElements();
    }

    private void colorPreferredGrassPositions() {

        List<Vector2d> grassPositionsToColor = worldMap.getFreeGrassPreferredPositions();
        Image image = new Image("x.png");

        for (Vector2d position : grassPositionsToColor ) {
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(cellSize);
            imageView.setFitWidth(cellSize);
            grassPreferredPositionsImageView.add(imageView);

            mapGrid.add(imageView, position.x()+1, worldMap.getMapHeight() - position.y());
            imageView.toBack();
        }
    }

    private void removeColoredElements() {
        mapGrid.getChildren().removeAll(grassPreferredPositionsImageView);
        mapGrid.getChildren().removeAll(animalsTopGenotypesLabels);
        grassPreferredPositionsImageView.clear();
        animalsTopGenotypesLabels.clear();
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
            label.setStyle("-fx-background-color: #d968dc");
            label.setMinHeight(cellSize);
            label.setMinWidth(cellSize);
            animalsTopGenotypesLabels.add(label);

            mapGrid.add(label, position.x()+1, worldMap.getMapHeight() - position.y());
            label.toBack();
        }
    }

    @Override
    public void handleChangeEvent(WorldMap worldMap, SimulationEventType eventType, StatisticsRecord statisticsRecord) {
        Platform.runLater(() -> {
            drawMap();
            moveLabel.setText("%s%n Day: %s".formatted(eventType, statisticsRecord.day()));
            writeStatistics(eventType, statisticsRecord);

            if(selectedAnimal != null) {
                selectedAnimalStatistics.updateStatistics();
                writeAnimalStatistics();
            }

            if (!isSimulationStopped) {
                simulation.countDown();
            } else {
                colorPreferredGrassPositions();
                colorMostPopularGenotype();
            }
        });
    }
}
