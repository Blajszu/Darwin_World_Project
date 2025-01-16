package project.listener;

import project.model.maps.WorldMap;
import project.statistics.StatisticsRecord;

public class SimulationMapDisplay implements SimulationChangeListener {

    private int eventsCounter = 1;

    @Override
    public void handleChangeEvent(WorldMap worldMap, SimulationEventType eventType, StatisticsRecord statisticsRecord) {
        synchronized (System.out) {
            System.out.println("Map ID: " + worldMap.getId());
            System.out.println(eventType);
            System.out.println(worldMap);
            System.out.println("Events counter: " + eventsCounter);
            System.out.println("Day: " + statisticsRecord.day());
            System.out.println("Animals count: " + statisticsRecord.animalsCount());
            System.out.println("Plants Count: " + statisticsRecord.plantsCount());
            System.out.println("Empty fields count: " + statisticsRecord.emptyFieldsCount());
            System.out.println("Genotypes count: " + statisticsRecord.genotypesCount());
            System.out.println("Average animals energy: " + statisticsRecord.averageEnergy());
            System.out.println("Average animals life length: " + statisticsRecord.averageLifeLength());
            System.out.println("Average animals children count: " + statisticsRecord.averageChildrenCount());
            System.out.println("================\n");
            eventsCounter++;
        }
    }
}
