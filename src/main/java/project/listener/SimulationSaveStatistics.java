package project.listener;

import project.model.maps.WorldMap;
import project.statistics.StatisticsRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimulationSaveStatistics implements SimulationChangeListener, AutoCloseable {
    private static final String HEADER = "day,animals_count,grass_count,average_energy,average_lifetime,average_kids_number,most_popular_genotype";
    private final List<String> buffer = new ArrayList<>();
    private final Path filePath;

    public SimulationSaveStatistics(WorldMap worldMap) throws IOException {
        Path directoryPath = Path.of("simulation_statistics");
        filePath = directoryPath.resolve("%s.csv".formatted(worldMap.getId()));

        Files.createDirectories(directoryPath);

        if (!Files.exists(filePath)) {
            Files.write(filePath, List.of(HEADER), StandardOpenOption.CREATE);
        }
    }

    @Override
    public void handleChangeEvent(WorldMap worldMap, SimulationEventType eventType, StatisticsRecord statisticsRecord) {
        if (eventType != SimulationEventType.DAY_ENDED) {
            return;
        }

        Optional<Map.Entry<String, Integer>> maxGenotype = statisticsRecord.genotypesCount().entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());

        String csvLine = String.format("%d,%d,%d,%.2f,%.2f,%s,%s",
                statisticsRecord.day(),
                statisticsRecord.animalsCount(),
                statisticsRecord.plantsCount(),
                statisticsRecord.averageEnergy(),
                statisticsRecord.averageLifeLength(),
                statisticsRecord.averageChildrenCount(),
                (maxGenotype.isPresent()) ? maxGenotype.get().getKey() : ""
        );

        buffer.add(csvLine);

        if (buffer.size() >= 20) {
            flushBuffer();
        }
    }

    private void flushBuffer() {
        if (buffer.isEmpty()) return;
        try {
            Files.write(
                    filePath,
                    buffer,
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE
            );
            buffer.clear();
        } catch (IOException e) {
            System.err.println("Error writing to statistics file: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        flushBuffer();
    }
}