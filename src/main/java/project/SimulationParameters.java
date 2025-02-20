package project;

import project.presenter.GrowthGrassVariant;
import project.presenter.MutationVariant;

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
    public SimulationParameters(
            String mapHeight,
            String mapWidth,
            GrowthGrassVariant growthGrassVariant,
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
        this(
                checkMapSize(mapHeight),
                checkMapSize(mapWidth),
                growthGrassVariant,
                checkParametersGraterOrEqualThanZero(numberOfGrassOnMap, "Initial number of Grass"),
                checkParametersGraterThanZero(energyFromGrass, "Energy from grass"),
                checkParametersGraterThanZero(numberOfGrassGrowingEveryDay, "Number of grass growing every day"),
                checkParametersGraterThanZero(startNumberOfAnimals, "Start number of animals"),
                checkParametersGraterThanZero(initialAnimalsEnergy, "Initial animal's energy"),
                checkParametersGraterThanZero(energyNeedToReproduce, "Energy need to reproduce"),
                checkParametersGraterThanZero(energyUsedToReproduce, "Energy used to reproduce"),
                checkParametersGraterOrEqualThanZero(minimalNumberOfMutation, "Minimal number of mutations"),
                checkParametersGraterOrEqualThanZero(maximumNumberOfMutation, "Maximum number of Mutations"),
                mutationVariant,
                checkParametersGraterThanZero(numberOfGenes, "Number of Genes"),
                collectStatistics
        );

        checkDependenceBetweenParameters();
    }
    private static int checkMapSize(String mapSize) {
        if (mapSize.isEmpty())
            throw new IllegalArgumentException("Missing Map Size");

        int size = Integer.parseInt(mapSize);

        if (size < 0 || size > 200)
            throw new IllegalArgumentException("Map Size must be between 1 and 200");

        return size;
    }

    private static int checkParametersGraterThanZero(String parameter, String name) {
        if (parameter.isEmpty())
            throw new IllegalArgumentException("Missing %s".formatted(name));

        int parsedParameter = Integer.parseInt(parameter);

        if (parsedParameter <= 0)
            throw new IllegalArgumentException("%s must be grater than 0".formatted(name));

        return parsedParameter;
    }

    private static int checkParametersGraterOrEqualThanZero(String parameter, String name) {
        if (parameter.isEmpty())
            throw new IllegalArgumentException("Missing %s".formatted(name));

        int parsedParameter = Integer.parseInt(parameter);

        if (parsedParameter < 0)
            throw new IllegalArgumentException("%s must be grater or equal 0".formatted(name));

        return parsedParameter;
    }

    private void checkDependenceBetweenParameters() {
        if (
        energyNeedToReproduce <= energyUsedToReproduce ||
        minimalNumberOfMutation > maximumNumberOfMutation ||
        maximumNumberOfMutation > numberOfGenes ||
        numberOfGrassOnMap > mapHeight * mapWidth ||
        startNumberOfAnimals > mapHeight * mapWidth ||
        numberOfGrassGrowingEveryDay > mapHeight * mapWidth
        )
            throw new IllegalArgumentException("Invalid dependence between parameters");
    }
}
