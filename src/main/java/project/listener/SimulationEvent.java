package project.listener;

import project.model.maps.WorldMap;

public record SimulationEvent(WorldMap worldMap, SimulationEventType eventType, int day) {

    public String getMessage() {
        return eventType.toString();
    }
}