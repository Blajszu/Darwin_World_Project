package project;

import org.junit.jupiter.api.Test;
import project.presenter.SimulationChecker;
import project.presenter.SimulationParameters;


import static org.junit.jupiter.api.Assertions.*;
class SimulationTest {
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
        SimulationParameters simulationParameters1 = new SimulationParameters(Integer.parseInt(parameter0), Integer.parseInt(parameter0), growthGrassVariant, Integer.parseInt(parameter1), Integer.parseInt(parameter1), Integer.parseInt(parameter1),Integer.parseInt(parameter1), Integer.parseInt(parameter1), Integer.parseInt(parameter1), Integer.parseInt(parameter5),Integer.parseInt(parameter2),Integer.parseInt(parameter5), mutationVariant,Integer.parseInt(parameter5),collectStatistics);

        //then
        assertEquals(simulationParameters1,SimulationChecker.checkParameters(parameter0,parameter0,growthGrassVariant,parameter1,parameter1,parameter1,parameter1,parameter1,parameter1,parameter5,parameter2,parameter5,mutationVariant,parameter5,collectStatistics));
        assertThrows(IllegalArgumentException.class,() ->SimulationChecker.checkParameters(parameter0,parameter0,growthGrassVariant,parameter1,parameter1,parameter1,parameter4,parameter1,parameter0,parameter1,parameter2,parameter5,mutationVariant,parameter5,collectStatistics));
        assertThrows(IllegalArgumentException.class,() ->SimulationChecker.checkParameters(parameter0,parameter0,growthGrassVariant,parameter1,parameter1,parameter1,parameter1,parameter1,parameter0,parameter1,parameter2,parameter3,mutationVariant,parameter5,collectStatistics));
        assertThrows(IllegalArgumentException.class,() ->SimulationChecker.checkParameters(parameter0,parameter0,growthGrassVariant,parameter1,parameter1,parameter1,parameter1,parameter1,parameter0,parameter1,parameter1,parameter2,mutationVariant,parameter1,collectStatistics));
        assertThrows(IllegalArgumentException.class,() ->SimulationChecker.checkParameters(parameter2,parameter0,growthGrassVariant,parameter1,parameter1,parameter1,parameter1,parameter1,parameter0,parameter1,parameter2,parameter1,mutationVariant,parameter1,collectStatistics));

    }
}