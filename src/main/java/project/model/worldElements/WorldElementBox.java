package project.model.worldElements;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldElementBox {
    private final VBox container = new VBox();
    private final WorldElement element;
    private static final Map<String, Image> imageCache = new HashMap<>();

    private final int size;
    private final int initialAnimalEnergy;

    private final List<String> animalsColors = List.of(
            "#ffffff",
            "#b1e3d9",
            "#84f3dc",
            "#5febfa",
            "#00d8fe",
            "#3f85ee",
            "#1261a1",
            "#062195"
    );

    public WorldElementBox(WorldElement element, int size, int initialAnimalEnergy) {
        this.element = element;
        this.size = size;
        this.initialAnimalEnergy = initialAnimalEnergy;

        fillContent();
    }

    public VBox getGraphicBox() {
        return container;
    }

    public WorldElement getElement() {
        return element;
    }

    private void fillContent() {
        Label label = new Label();
        if (element instanceof Animal) {
            label.setId("animalLabel");
            label.setMinWidth(0.8 * size);
            label.setMinHeight(0.8 * size);

            int backgroundIndex = (int) Math.min(Math.floor(((double) ((Animal) element).getCurrentEnergy() / (2 * initialAnimalEnergy)) * 8), 7);
            label.setStyle("-fx-background-color: " + animalsColors.get(backgroundIndex) + ";");

            label.setAlignment(Pos.CENTER);
            label.setPadding(new Insets(10));

            Image image = getOrCreateImage(element.getResourceFileName());
            ImageView imageView = new ImageView(image);

            imageView.setFitHeight(size);
            imageView.setFitWidth(size);

            StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(label, imageView);
            stackPane.setAlignment(Pos.CENTER);

            container.getChildren().add(stackPane);
        } else {
            label.setAlignment(Pos.CENTER);
            label.setId("grassLabel");
            label.setMinWidth(size);
            label.setMinHeight(size);

            container.getChildren().add(label);
        }
        container.setAlignment(Pos.CENTER);
    }


    private Image getOrCreateImage(String resourceFileName) {
        return imageCache.computeIfAbsent(resourceFileName, Image::new);
    }
}