package project.presenter;

import project.GrowthGrassVariant;
import project.MutationVariant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimulationPresets {

    private final static HashMap<String, SimulationParameters> fileNameWithFileContent = new HashMap<>();

    public static ArrayList<String> getCorrectFilesNames() throws IOException {

        File folder = new File("src/main/java/project/presenter/presetParameters");
        if (!folder.exists()) {
            throw new IOException("Błąd odczytu z folderu");
        }
        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".csv"));

        if (listOfFiles == null) {
            return new ArrayList<>();
        }

        for (File file : listOfFiles) {
            validateFileParameters(file);
        }

        return new ArrayList<>(
                fileNameWithFileContent.keySet().stream()
                        .map(name -> name.replace(".csv", ""))
                        .toList()
        );
    }

    public static SimulationParameters loadParameters(String fileName) {
        return fileNameWithFileContent.get(fileName + ".csv");
    }

    public static void saveParameters(SimulationParameters simulationParameters, String fileName) throws IOException {
        File folder = new File("src/main/java/project/presenter/presetParameters");
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IOException("Błąd tworzenia folderu");
            }
        }

        File file = new File(folder, fileName);
        String data = "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s".formatted(
                simulationParameters.mapHeight(),
                simulationParameters.mapWidth(),
                simulationParameters.growthGrassVariant().name(),
                simulationParameters.numberOfGrassOnMap(),
                simulationParameters.energyFromGrass(),
                simulationParameters.numberOfGrassGrowingEveryDay(),
                simulationParameters.startNumberOfAnimals(),
                simulationParameters.initialAnimalsEnergy(),
                simulationParameters.energyNeedToReproduce(),
                simulationParameters.energyUsedToReproduce(),
                simulationParameters.minimalNumberOfMutation(),
                simulationParameters.maximumNumberOfMutation(),
                simulationParameters.mutationVariant().name(),
                simulationParameters.numberOfGenes(),
                simulationParameters.collectStatistics()
        );

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(data);
        } catch (IOException e) {
            throw new IOException("Błąd zapisu do pliku");
        }
    }

    private static void validateFileParameters(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        String[] params = lines.getFirst().split(",");
        if (params.length != 15) {
            return;
        }

        SimulationParameters parameters = SimulationChecker.checkParameters(params[0], params[1], GrowthGrassVariant.valueOf(params[2]), params[3], params[4], params[5], params[6], params[7], params[8], params[9], params[10], params[11], MutationVariant.valueOf(params[12]), params[13], Boolean.parseBoolean(params[14]));
        fileNameWithFileContent.put(file.getName(), parameters);
    }
}
