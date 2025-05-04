package project.listener;

import project.model.maps.WorldMap;
import project.statistics.StatisticsRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimulationSaveStatistics implements SimulationChangeListener, AutoCloseable {
    private static final String HEADER = "day,animals_count,grass_count,average_energy,average_lifetime,average_kids_number,most_popular_genotype";
    private static final String STATS_DIR = "simulationStatistics";

    private final List<String> buffer = new ArrayList<>();
    private final Path filePath;

    public SimulationSaveStatistics(WorldMap worldMap) throws IOException {
        Path directoryPath = Paths.get(STATS_DIR);
        Files.createDirectories(directoryPath);

        filePath = directoryPath.resolve(String.format("%s.csv", worldMap.getId()));

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

        String csvLine = String.format("%d,%d,%d,%.2f,%.2f,%.2f,%s",
                statisticsRecord.day(),
                statisticsRecord.animalsCount(),
                statisticsRecord.plantsCount(),
                statisticsRecord.averageEnergy(),
                statisticsRecord.averageLifeLength(),
                statisticsRecord.averageChildrenCount(),
                maxGenotype.map(Map.Entry::getKey).orElse("")
        );

        buffer.add(csvLine);

        if (buffer.size() >= 20) {
            flushBuffer();
        }
    }

    @Override
    public void close() {
        flushBuffer();
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
}