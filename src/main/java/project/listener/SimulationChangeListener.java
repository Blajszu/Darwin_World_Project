package project.listener;

import project.model.maps.WorldMap;

@FunctionalInterface
public interface SimulationChangeListener {

    void handleChangeEvent(WorldMap worldMap, SimulationEventType eventType, int day);
}