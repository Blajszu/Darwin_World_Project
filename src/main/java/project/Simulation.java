package project;

import project.model.Vector2d;
import project.model.maps.IncorrectPositionException;
import project.model.maps.WorldMap;
import project.model.worldElements.Animal;
import project.model.worldElements.MapDirection;

import java.util.ArrayList;
import java.util.List;

public class Simulation implements Runnable {

    private final List<Animal> animals;
    private final List<MoveDirection> moves;
    private final WorldMap worldMap;

    public Simulation(List<Vector2d> animalPositions, List<MoveDirection> moveDirections, WorldMap map) {
        moves = moveDirections;
        animals = new ArrayList<>();
        worldMap = map;

        for(Vector2d position : animalPositions) {
            Animal newAnimal = new Animal(position);

            try {
                map.place(newAnimal);
                animals.add(newAnimal);
            }
            catch (IncorrectPositionException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    List<Vector2d> getAnimalsPositionsAfterSimulation() {
        List<Vector2d> positions = new ArrayList<>();
        for(Animal animal : animals) {
            positions.add(animal.getPosition());
        }

        return positions;
    }

    List<MapDirection> getAnimalOrientationsAfterSimulation() {
        List<MapDirection> orientations = new ArrayList<>();
        for(Animal animal : animals) {
            orientations.add(animal.getCurrentOrientation());
        }

        return orientations;
    }

    public void run() {
        int numberOfAnimals = animals.size();
        int numberOfMoves = moves.size();

        if(numberOfAnimals == 0)
            return;

        try {
            for (int i = 0; i < numberOfMoves; i++) {
                Animal currentAnimal = animals.get(i % numberOfAnimals);

                worldMap.move(currentAnimal, moves.get(i));
                Thread.sleep(500);
            }
        }
        catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}