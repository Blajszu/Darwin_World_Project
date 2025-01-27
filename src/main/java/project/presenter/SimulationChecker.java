package project.presenter;

import project.GrowthGrassVariant;
import project.MutationVariant;

public class SimulationChecker { // czy ta klasa coś wnosi? czemu to nie jest w SimulationParameters?

    public static SimulationParameters checkParameters(
            String mapHeight,
            String mapWidth,
            GrowthGrassVariant growthGrassVariant, // czemu wszystko oprócz 3 parametrów jest stringiem?
            String numberOfGrassOnMap,
            String energyFromGrass,
            String numberOfGrassGrowingEveryDay,
            String startNumberOfAnimals,
            String initialAnimalsEnergy,
            String energyNeedToReproduce,
            String energyUsedToReproduce,
            String minimalNumberOfMutation,
            String maximumNumberOfMutation,
            MutationVariant mutationVariant,
            String numberOfGenes,
            boolean collectStatistics
    ) {
        if (mapHeight.isEmpty() ||
                mapWidth.isEmpty() ||
                numberOfGrassOnMap.isEmpty() ||
                energyFromGrass.isEmpty() ||
                numberOfGrassGrowingEveryDay.isEmpty() ||
                startNumberOfAnimals.isEmpty() ||
                initialAnimalsEnergy.isEmpty() ||
                energyNeedToReproduce.isEmpty() ||
                energyUsedToReproduce.isEmpty() ||
                minimalNumberOfMutation.isEmpty() ||
                maximumNumberOfMutation.isEmpty() ||
                numberOfGenes.isEmpty()
        ) {
            throw new IllegalArgumentException("Missing Simulation parameters");
        }

        SimulationParameters simulationParameters = new SimulationParameters(
                Integer.parseInt(mapHeight),
                Integer.parseInt(mapWidth),
                growthGrassVariant,
                Integer.parseInt(numberOfGrassOnMap),
                Integer.parseInt(energyFromGrass),
                Integer.parseInt(numberOfGrassGrowingEveryDay),
                Integer.parseInt(startNumberOfAnimals),
                Integer.parseInt(initialAnimalsEnergy),
                Integer.parseInt(energyNeedToReproduce),
                Integer.parseInt(energyUsedToReproduce),
                Integer.parseInt(minimalNumberOfMutation),
                Integer.parseInt(maximumNumberOfMutation),
                mutationVariant,
                Integer.parseInt(numberOfGenes),
                collectStatistics
        );

        if (
                simulationParameters.mapHeight() <= 0 ||    // nie wygodniej by to było pogrupować po parametrze?
                        simulationParameters.mapWidth() <= 0 ||
                        simulationParameters.numberOfGrassOnMap() < 0 ||
                        simulationParameters.energyFromGrass() <= 0 ||
                        simulationParameters.numberOfGrassGrowingEveryDay() <= 0 ||
                        simulationParameters.startNumberOfAnimals() < 0 ||
                        simulationParameters.initialAnimalsEnergy() <= 0 ||
                        simulationParameters.energyNeedToReproduce() <= 0 ||
                        simulationParameters.energyUsedToReproduce() <= 0 ||
                        simulationParameters.minimalNumberOfMutation() < 0 ||
                        simulationParameters.maximumNumberOfMutation() < 0 ||
                        simulationParameters.numberOfGenes() <= 0
        ) {
            throw new IllegalArgumentException("Invalid Simulation parameters");
        }

        if (
                simulationParameters.mapHeight() > 200 ||
                        simulationParameters.mapWidth() > 200 ||
                        simulationParameters.energyNeedToReproduce() <= simulationParameters.energyUsedToReproduce() ||
                        simulationParameters.minimalNumberOfMutation() > simulationParameters.maximumNumberOfMutation() ||
                        simulationParameters.maximumNumberOfMutation() > simulationParameters.numberOfGenes() ||
                        simulationParameters.numberOfGrassOnMap() > simulationParameters.mapHeight() * simulationParameters.mapWidth() ||
                        simulationParameters.startNumberOfAnimals() > simulationParameters.mapHeight() * simulationParameters.mapWidth() ||
                        simulationParameters.numberOfGrassGrowingEveryDay() > simulationParameters.mapHeight() * simulationParameters.mapWidth()
        ) {
            throw new IllegalArgumentException("Invalid Simulation parameters");
        }

        return simulationParameters;
    }
}
