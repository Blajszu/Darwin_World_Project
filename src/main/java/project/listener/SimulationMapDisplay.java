package project.listener;

import project.model.maps.WorldMap;

public class SimulationMapDisplay implements SimulationChangeListener {

    private int eventsCounter = 1;

    @Override
    public void handleChangeEvent(WorldMap worldMap, SimulationEventType eventType, int day) {
        synchronized (System.out) {
            System.out.println("Map ID: " + worldMap.getId());
            System.out.println(eventType);
            System.out.println(worldMap);
            System.out.println("Events counter: " + eventsCounter);
            System.out.println("================\n");
            eventsCounter++;
        }
    }
}
