package project;

import java.util.ArrayList;
import java.util.List;
public class SimulationEngine {

    private final List<Simulation> simulations;
    private final List<Thread> simulationThreads = new ArrayList<>();

    public SimulationEngine(List<Simulation> simulations) {

        this.simulations = simulations;
    }

    public void runAsync() {
        simulationThreads.clear();

        simulations.forEach(simulation -> simulationThreads.add(new Thread(simulation)));

        simulationThreads.forEach(Thread::start);
    }
}
