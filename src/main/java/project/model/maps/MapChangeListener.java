package project.model.maps;

@FunctionalInterface
public interface MapChangeListener {

    void mapChanged(WorldMap worldMap, String message);
}