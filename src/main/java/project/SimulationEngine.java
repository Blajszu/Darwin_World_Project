package project; // pakiet powinien mieć formę odwrotnej nazwy domenowej

import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {

    private final List<Simulation> simulations;
    private final List<Thread> simulationThreads = new ArrayList<>();

    public SimulationEngine(List<Simulation> simulations) {

        this.simulations = simulations;
    }

    public void runAsync() {
        simulationThreads.clear(); // czy to dobry pomysł?

        simulations.forEach(simulation -> simulationThreads.add(new Thread(simulation)));

        simulationThreads.forEach(Thread::start);
    }
}
