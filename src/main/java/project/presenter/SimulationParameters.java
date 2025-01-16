package project.presenter;

import project.GrowthGrassVariant;
import project.MutationVariant;

public record SimulationParameters(
        int mapHeight,
        int mapWidth,
        GrowthGrassVariant growthGrassVariant,
        int numberOfGrassOnMap,
        int energyFromGrass,
        int numberOfGrassGrowingEveryDay,
        int startNumberOfAnimals,
        int initialAnimalsEnergy,
        int energyNeedToReproduce,
        int energyUsedToReproduce,
        int minimalNumberOfMutation,
        int maximumNumberOfMutation,
        MutationVariant mutationVariant,
        int numberOfGenes,
        boolean collectStatistics
) {
}
