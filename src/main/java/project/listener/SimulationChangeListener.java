package project.listener;

import project.model.maps.WorldMap;

@FunctionalInterface
public interface SimulationChangeListener {

    void mapChanged(WorldMap worldMap, String message);
}