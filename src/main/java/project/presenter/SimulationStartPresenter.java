package project.presenter;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import project.GrowthGrassVariant;
import project.MutationVariant;
import project.Simulation;
import project.SimulationEngine;
import project.listener.SimulationMapDisplay;
import project.listener.SimulationSaveStatistics;
import project.model.maps.WorldMap;


import java.io.IOException;
import java.util.List;

public class SimulationStartPresenter {
    @FXML
    public CheckBox collectStatistics;
    @FXML
    public TextField numberOfGenes;
    @FXML
    public ComboBox<MutationVariant> mutationVariant;
    @FXML
    public TextField maximumNumberOfMutation;
    @FXML
    public TextField minimalNumberOfMutation;
    @FXML
    public TextField energyUsedToReproduce;
    @FXML
    public TextField energyNeedToReproduce;
    @FXML
    public TextField initialAnimalsEnergy;
    @FXML
    public TextField startNumberOfAnimals;
    @FXML
    public TextField numberOfGrassGrowingEveryDay;
    @FXML
    public TextField energyFromGrass;
    @FXML
    public TextField startNumberOfGrass;
    @FXML
    public ComboBox<GrowthGrassVariant> growthGrassVariant;
    @FXML
    public TextField width;
    @FXML
    public TextField height;
    @FXML
    public Button startSimulation;
    @FXML
    public Label errors;
    @FXML
    public ComboBox<String> chooseParameters;
    @FXML
    public Button saveParameters;
    @FXML
    public TextField fileName;

    private final SimulationMapDisplay simulationMapDisplay = new SimulationMapDisplay();

    @FXML
    public void initialize() {
        List<TextField> numericInputs = List.of(
                width,
                height,
                startNumberOfGrass,
                energyFromGrass,
                numberOfGrassGrowingEveryDay,
                startNumberOfAnimals,
                initialAnimalsEnergy,
                energyNeedToReproduce,
                energyUsedToReproduce,
                minimalNumberOfMutation,
                maximumNumberOfMutation,
                numberOfGenes
        );

        numericInputs.forEach(input -> {
            input.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (!newValue.matches("\\d*")) {
                        input.setText(newValue.replaceAll("\\D", ""));
                    }
                }
            });
        });

        mutationVariant.getItems().addAll(MutationVariant.values());
        growthGrassVariant.getItems().addAll(GrowthGrassVariant.values());

        try {
            chooseParameters.getItems().addAll(SimulationPresets.getCorrectFilesNames());
        } catch (IOException e) {
            errors.setText(e.getMessage());
        }

        fileName.textProperty().addListener((observable, oldValue, newValue) -> {
            saveParameters.setDisable(newValue.isEmpty());
        });
    }

    public void onSimulationStartClicked() {
        try {
            SimulationParameters simulationParameters = getParameters();

            Stage simulationStage = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("simulationRun.fxml"));
            BorderPane viewRoot = loader.load();
            SimulationRunPresenter simulationRunPresenter = loader.getController();

            var scene = new Scene(viewRoot);
            simulationStage.setScene(scene);
            simulationStage.setTitle("Simulation app");
            simulationStage.minWidthProperty().bind(viewRoot.minWidthProperty());
            simulationStage.minHeightProperty().bind(viewRoot.minHeightProperty());

            simulationStage.show();

            Simulation simulation = new Simulation(simulationParameters);

            simulation.addObserver(simulationRunPresenter);
            simulation.addObserver(simulationMapDisplay);
            WorldMap worldMap = simulation.getWorldMap();
            simulationRunPresenter.setSimulation(simulation);

            if(collectStatistics.isSelected()) {
                try (SimulationSaveStatistics stats = new SimulationSaveStatistics(worldMap)) {
                    simulation.addObserver(stats);
                    SimulationEngine engine = new SimulationEngine(List.of(simulation));
                    engine.runAsync();
                }
            }
            else {
                SimulationEngine engine = new SimulationEngine(List.of(simulation));
                engine.runAsync();
            }
        }
        catch (IllegalArgumentException | IOException e) {
            errors.setText(e.getMessage());
        }
    }

    public void onChooseParameters() {
        SimulationParameters simulationParameters = SimulationPresets.loadParameters(chooseParameters.getValue());

        height.setText(String.valueOf(simulationParameters.mapHeight()));
        width.setText(String.valueOf(simulationParameters.mapWidth()));
        growthGrassVariant.setValue(simulationParameters.growthGrassVariant());
        startNumberOfGrass.setText(String.valueOf(simulationParameters.startNumberOfAnimals()));
        energyFromGrass.setText(String.valueOf(simulationParameters.energyFromGrass()));
        numberOfGrassGrowingEveryDay.setText(String.valueOf(simulationParameters.numberOfGrassGrowingEveryDay()));
        startNumberOfAnimals.setText(String.valueOf(simulationParameters.startNumberOfAnimals()));
        initialAnimalsEnergy.setText(String.valueOf(simulationParameters.initialAnimalsEnergy()));
        energyNeedToReproduce.setText(String.valueOf(simulationParameters.energyNeedToReproduce()));
        energyUsedToReproduce.setText(String.valueOf(simulationParameters.energyUsedToReproduce()));
        minimalNumberOfMutation.setText(String.valueOf(simulationParameters.minimalNumberOfMutation()));
        maximumNumberOfMutation.setText(String.valueOf(simulationParameters.maximumNumberOfMutation()));
        mutationVariant.setValue(simulationParameters.mutationVariant());
        numberOfGenes.setText(String.valueOf(simulationParameters.numberOfGenes()));
        collectStatistics.setSelected(simulationParameters.collectStatistics());
    }

    public void saveInputParameters() {
        try {
            SimulationParameters simulationParameters = getParameters();
            SimulationPresets.saveParameters(simulationParameters, fileName.getText() + ".csv");
            errors.setText("Zapisano konfigurację!");
            fileName.setText("");
            chooseParameters.getItems().clear();
            chooseParameters.getItems().addAll(SimulationPresets.getCorrectFilesNames());
        } catch(IllegalArgumentException | IOException e) {
            errors.setText(e.getMessage());
        }

    }

    private SimulationParameters getParameters(){
        return SimulationChecker.checkParameters(
            height.getText(),
            width.getText(),
            growthGrassVariant.getValue(),
            startNumberOfGrass.getText(),
            energyFromGrass.getText(),
            numberOfGrassGrowingEveryDay.getText(),
            startNumberOfAnimals.getText(),
            initialAnimalsEnergy.getText(),
            energyNeedToReproduce.getText(),
            energyUsedToReproduce.getText(),
            minimalNumberOfMutation.getText(),
            maximumNumberOfMutation.getText(),
            mutationVariant.getValue(),
            numberOfGenes.getText(),
            collectStatistics.isSelected()
        );
    }
}