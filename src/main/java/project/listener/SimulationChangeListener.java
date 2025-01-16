package project.listener;

import project.model.maps.WorldMap;
import project.statistics.StatisticsRecord;

@FunctionalInterface
public interface SimulationChangeListener {

    void handleChangeEvent(WorldMap worldMap, SimulationEventType eventType, StatisticsRecord statisticsRecord);
}