package project;

import project.model.Vector2d;
import project.model.maps.GrassField;
import project.model.maps.RectangularMap;

import java.util.ArrayList;
import java.util.List;

public class World {
    public static void main(String[] args) {

        try {
            List<MoveDirection> directions = OptionsParser.parse(args);
            List<Vector2d> positions = List.of(new Vector2d(2, 2), new Vector2d(3, 4));
            List<Simulation> simulations = new ArrayList<>();
            ConsoleMapDisplay display = new ConsoleMapDisplay();

            for(int i = 0; i < 500; i++) {
                GrassField gf = new GrassField(10);
                RectangularMap rm = new RectangularMap(10, 10);

                gf.addObserver(display);
                rm.addObserver(display);

                Simulation simulation1 = new Simulation(positions, directions, gf);
                Simulation simulation2 = new Simulation(positions, directions, rm);

                simulations.add(simulation1);
                simulations.add(simulation2);
            }

            SimulationEngine engine = new SimulationEngine(simulations);
            engine.runAsyncInThreadPool();
            engine.awaitSimulationsEnd();
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace(System.err);
        }
        catch (InterruptedException e) {
            System.out.printf("Action interrupted: %s%n", e.getMessage());
        }

        System.out.println("System zakonczyl dzialanie!");
    }
}