package project.statistics;

public record AnimalStatisticsRecord (
    String animalGene,
    int activePartOfGenome,
    int currentEnergy,
    int numberOfEatenPlants,
    int numberOfKids,
    int numberOfDescendants,
    int lengthOfLife,
    Integer whenDied
) {}
