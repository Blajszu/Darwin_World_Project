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
import project.model.maps.EquatorMap;
import project.model.maps.MovingJungleMap;
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
                        input.setText(newValue.replaceAll("[^\\d]", ""));
                    }
                }
            });
        });

        mutationVariant.getItems().addAll(MutationVariant.values());
        growthGrassVariant.getItems().addAll(GrowthGrassVariant.values());
    }

    public void onSimulationStartClicked() {
            int mapHeight = Integer.parseInt(height.getText());
            int mapWidth = Integer.parseInt(width.getText());
            int startNumberOfGrassOnMap = Integer.parseInt(startNumberOfGrass.getText());
            int givenEnergyFromGrass = Integer.parseInt(energyFromGrass.getText());
            int givenNumberOfGrassGrowingEveryDay = Integer.parseInt(numberOfGrassGrowingEveryDay.getText());
            int startNumberOfAnimalsOnMap = Integer.parseInt(startNumberOfAnimals.getText());
            int givenInitialAnimalsEnergy = Integer.parseInt(initialAnimalsEnergy.getText());
            int givenEnergyNeedToReproduce = Integer.parseInt(energyNeedToReproduce.getText());
            int givenEnergyUsedToReproduce = Integer.parseInt(energyUsedToReproduce.getText());
            int givenMinimalNumberOfMutation = Integer.parseInt(minimalNumberOfMutation.getText());
            int givenMaximumNumberOfMutation = Integer.parseInt(maximumNumberOfMutation.getText());
            int numberOfGenesOfAnimal = Integer.parseInt(numberOfGenes.getText());


        try {
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

            WorldMap worldMap = switch(growthGrassVariant.getValue()) {
                case EQUATOR_MAP -> new EquatorMap(mapHeight, mapWidth);
                case MOVING_JUNGLE_MAP -> new MovingJungleMap(mapHeight, mapWidth);
            };

            simulationRunPresenter.setWorldMap(worldMap);
            worldMap.addObserver(simulationRunPresenter);
            worldMap.addObserver(new SimulationMapDisplay());

            Simulation simulation = new Simulation(
                    worldMap,
                    startNumberOfGrassOnMap,
                    givenEnergyFromGrass,
                    givenNumberOfGrassGrowingEveryDay,
                    startNumberOfAnimalsOnMap,
                    givenInitialAnimalsEnergy,
                    givenEnergyNeedToReproduce,
                    givenEnergyUsedToReproduce,
                    givenMinimalNumberOfMutation,
                    givenMaximumNumberOfMutation,
                    mutationVariant.getValue(),
                    numberOfGenesOfAnimal,
                    collectStatistics.isSelected()
            );
            SimulationEngine engine = new SimulationEngine(List.of(simulation));
            engine.runAsync();
        }
        catch (IllegalArgumentException | IOException e) {
            errors.setText(e.getMessage());
        }
    }
}