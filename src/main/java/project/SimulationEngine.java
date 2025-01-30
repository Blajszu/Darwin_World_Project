package project;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimulationEngine implements AutoCloseable {
    private final Simulation simulation;
    private final ExecutorService executor;

    public SimulationEngine(Simulation simulation) {
        this.simulation = simulation;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void runAsync() {
        executor.submit(simulation);
    }
    @Override
    public void close() {
        executor.shutdown();
    }
}