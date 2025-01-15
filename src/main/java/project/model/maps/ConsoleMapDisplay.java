package project.model.maps;

public class ConsoleMapDisplay implements MapChangeListener{

    private int eventsCounter = 1;

    @Override
    public void mapChanged(WorldMap worldMap, String message) {
        synchronized (System.out) {
            System.out.println("Map ID: " + worldMap.getId());
            System.out.println(message);
            System.out.println(worldMap);
            System.out.println("Events counter: " + eventsCounter);
            System.out.println("================\n");
            eventsCounter++;
        }
    }
}
