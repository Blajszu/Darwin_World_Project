package project.presenter;

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
import project.listener.SimulationSaveStatistics;
import project.model.maps.WorldMap;


import java.io.IOException;
import java.util.List;

public class SimulationStartPresenter {
    @FXML
    private CheckBox collectStatistics;
    @FXML
    private TextField numberOfGenes;
    @FXML
    private ComboBox<MutationVariant> mutationVariant;
    @FXML
    private TextField maximumNumberOfMutation;
    @FXML
    private TextField minimalNumberOfMutation;
    @FXML
    private TextField energyUsedToReproduce;
    @FXML
    private TextField energyNeedToReproduce;
    @FXML
    private TextField initialAnimalsEnergy;
    @FXML
    private TextField startNumberOfAnimals;
    @FXML
    private TextField numberOfGrassGrowingEveryDay;
    @FXML
    private TextField energyFromGrass;
    @FXML
    private TextField startNumberOfGrass;
    @FXML
    private ComboBox<GrowthGrassVariant> growthGrassVariant;
    @FXML
    private TextField width;
    @FXML
    private TextField height;
    @FXML
    private Label errors;
    @FXML
    private ComboBox<String> chooseParameters;
    @FXML
    private Button saveParameters;
    @FXML
    private TextField fileName;

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

        numericInputs.forEach(input -> input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                input.setText(newValue.replaceAll("\\D", ""));
            }
        }));

        mutationVariant.getItems().addAll(MutationVariant.values());
        growthGrassVariant.getItems().addAll(GrowthGrassVariant.values());

        try {
            chooseParameters.getItems().addAll(SimulationPresets.getCorrectFilesNames());
        } catch (IOException e) {
            errors.setText(e.getMessage());
        }

        fileName.textProperty().addListener((observable, oldValue, newValue) -> saveParameters.setDisable(newValue.isEmpty()));
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

        String chosenParameters = chooseParameters.getValue();
        if(chosenParameters == null)
            return;

        SimulationParameters simulationParameters = SimulationPresets.loadParameters(chosenParameters);

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