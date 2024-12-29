package project;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimulationEngine {

    private final List<Simulation> simulations;
    private final List<Thread> simulationThreads = new ArrayList<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public SimulationEngine(List<Simulation> simulations) {

        this.simulations = simulations;
    }

    public void runSync() {
        simulations.forEach(Simulation::run);
    }

    public void runAsync() {
        simulationThreads.clear();

        simulations.forEach(simulation -> {
            simulationThreads.add(new Thread(simulation));
        });

        simulationThreads.forEach(Thread::start);
    }

    public void runAsyncInThreadPool() {
        for(Thread thread : simulationThreads) {
            executorService.submit(thread);
        }
    }

    public void awaitSimulationsEnd() throws InterruptedException {
        for (Thread thread : simulationThreads) {
            thread.join();
        }

        executorService.shutdown();
        if(!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
    }
}
