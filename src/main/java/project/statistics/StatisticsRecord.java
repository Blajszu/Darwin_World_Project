package project.statistics;

import java.util.HashMap;

public record StatisticsRecord(
        int day,
        int animalsCount,
        int plantsCount,
        int emptyFieldsCount,
        HashMap<String, Integer> genotypesCount,
        double averageEnergy,
        double averageLifeLength,
        double averageChildrenCount
) {
}
