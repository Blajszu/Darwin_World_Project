package project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import project.model.Vector2d;
import project.model.worldElements.Animal;
import project.model.worldElements.RandomMutationStrategyVariant;
import project.presenter.GrowthGrassVariant;
import project.presenter.MutationVariant;

import java.util.List;

class SimulationTest {

    @Test
    void shouldCorrectlyResolveConflictsByEnergyAndAge() {
        // given
        Simulation simulation = new Simulation(createDefaultParameters());
        Animal strongerAnimal = new Animal(new Vector2d(2, 2), 8, 100, 50, 30, new RandomMutationStrategyVariant(1, 3));
        Animal weakerAnimal = new Animal(new Vector2d(2, 2), 8, 50, 50, 30, new RandomMutationStrategyVariant(1, 3));
        List<Animal> animals = List.of(weakerAnimal, strongerAnimal);

        // when
        List<Animal> resolvedAnimals = simulation.resolveAnimalsConflicts(animals);

        // then
        assertEquals(strongerAnimal, resolvedAnimals.get(0));
        assertEquals(weakerAnimal, resolvedAnimals.get(1));
    }

    @Test
    void shouldSpawnCorrectNumberOfInitialAnimals() {
        // given
        SimulationParameters params = createParametersWithAnimals(10);

        // when
        Simulation simulation = new Simulation(params);

        // then
        assertEquals(10, simulation.getWorldMap().getOrderedAnimals().size());
    }

    @Test
    void shouldSetCorrectInitialEnergy() {
        // given
        int expectedEnergy = 100;
        SimulationParameters params = createParametersWithEnergy(expectedEnergy);

        // when
        Simulation simulation = new Simulation(params);

        // then
        assertEquals(expectedEnergy, simulation.getInitialAnimalsEnergy());
    }

    @Test
    public void checkSimulationConstruction() {
        //given
        String parameter0 = "10";
        String parameter1 = "5";
        GrowthGrassVariant growthGrassVariant = GrowthGrassVariant.MOVING_JUNGLE_MAP;
        MutationVariant mutationVariant = MutationVariant.INCREMENT_DECREMENT;
        String parameter2 = "0";
        String parameter3 = "11";
        String parameter4 = "101";
        String parameter5 = "2";
        boolean collectStatistics = true;

        //when
        SimulationParameters simulationParameters1 = new SimulationParameters(Integer.parseInt(parameter0), Integer.parseInt(parameter0), growthGrassVariant, Integer.parseInt(parameter1), Integer.parseInt(parameter1), Integer.parseInt(parameter1), Integer.parseInt(parameter1), Integer.parseInt(parameter1), Integer.parseInt(parameter1), Integer.parseInt(parameter5), Integer.parseInt(parameter2), Integer.parseInt(parameter5), mutationVariant, Integer.parseInt(parameter5), collectStatistics);

        //then
        assertEquals(simulationParameters1, new SimulationParameters(parameter0, parameter0, growthGrassVariant, parameter1, parameter1, parameter1, parameter1, parameter1, parameter1, parameter5, parameter2, parameter5, mutationVariant, parameter5, collectStatistics));
        assertThrows(IllegalArgumentException.class, () -> new SimulationParameters(parameter0, parameter0, growthGrassVariant, parameter1, parameter1, parameter1, parameter4, parameter1, parameter0, parameter1, parameter2, parameter5, mutationVariant, parameter5, collectStatistics));
        assertThrows(IllegalArgumentException.class, () -> new SimulationParameters(parameter0, parameter0, growthGrassVariant, parameter1, parameter1, parameter1, parameter1, parameter1, parameter0, parameter1, parameter2, parameter3, mutationVariant, parameter5, collectStatistics));
        assertThrows(IllegalArgumentException.class, () -> new SimulationParameters(parameter0, parameter0, growthGrassVariant, parameter1, parameter1, parameter1, parameter1, parameter1, parameter0, parameter1, parameter1, parameter2, mutationVariant, parameter1, collectStatistics));
        assertThrows(IllegalArgumentException.class, () -> new SimulationParameters(parameter2, parameter0, growthGrassVariant, parameter1, parameter1, parameter1, parameter1, parameter1, parameter0, parameter1, parameter2, parameter1, mutationVariant, parameter1, collectStatistics));
    }

    private SimulationParameters createDefaultParameters() {
        return new SimulationParameters(20, 20, GrowthGrassVariant.EQUATOR_MAP, 10, 10, 2, 10, 100, 50, 30, 1, 3, MutationVariant.RANDOM, 8, true);
    }

    private SimulationParameters createParametersWithAnimals(int numberOfAnimals) {
        return new SimulationParameters(20, 20, GrowthGrassVariant.EQUATOR_MAP, 10, 10, 2, numberOfAnimals, 100, 50, 30, 1, 3, MutationVariant.RANDOM, 8, true);
    }

    private SimulationParameters createParametersWithEnergy(int energy) {
        return new SimulationParameters(20, 20, GrowthGrassVariant.EQUATOR_MAP, 10, 10, 2, 10, energy, 50, 30, 1, 3, MutationVariant.RANDOM, 8, true);
    }
}
