package project.presenter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import project.Simulation;
import project.SimulationEngine;
import project.model.Vector2d;
import project.model.maps.GrassField;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SimulationStartPresenter {

    @FXML
    private Label infoLabel;
    @FXML
    private TextField moveList;

    public void onSimulationStartClicked() {

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

            GrassField map = new GrassField(10);
            simulationRunPresenter.setWorldMap(map);

            map.addObserver(simulationRunPresenter);
            map.addObserver(new FileMapDisplay());
            map.addObserver((worldMap, message) -> {
                LocalTime localTime = LocalTime.now();

                String time = localTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                LocalDate date = LocalDate.now();

                System.out.printf("%s %s %s%n", date, time, message);
            });

            List<MoveDirection> directions = OptionsParser.parse(moveList.getText().split(" "));
            List<Vector2d> positions = List.of(new Vector2d(0, 0), new Vector2d(2, 2));

            Simulation simulation = new Simulation(positions, directions, map);
            SimulationEngine engine = new SimulationEngine(List.of(simulation));
            engine.runAsync();
        }
        catch (IllegalArgumentException | IOException e) {
            infoLabel.setText(e.getMessage());
        }
    }
}