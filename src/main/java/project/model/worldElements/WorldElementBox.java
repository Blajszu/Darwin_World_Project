package project.model.worldElements; // to jest element świata?

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldElementBox {
    private final VBox container = new VBox();
    private final WorldElement element;
    private static final Map<String, Image> imageCache = new HashMap<>();

    private final int size;
    private final int initialAnimalEnergy;
    private boolean selected;

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
        this(element, size, initialAnimalEnergy, false);
    }

    public WorldElementBox(WorldElement element, int size, int initialAnimalEnergy, boolean selected) {
        this.element = element;
        this.size = size;
        this.initialAnimalEnergy = initialAnimalEnergy;
        this.selected = selected;

        fillContent();
    }

    public VBox getGraphicBox() {
        return container;
    }

    public WorldElement getElement() {
        return element;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void fillContent() {
        Label label = new Label();
        if (element instanceof Animal) {
            label.setId("animalLabel");
            label.setMinWidth(0.8 * size);
            label.setMinHeight(0.8 * size);

            if (!selected) {
                int backgroundIndex = (int) Math.min(Math.floor(((double) ((Animal) element).getCurrentEnergy() / (2 * initialAnimalEnergy)) * 8), 7);
                label.setStyle("-fx-background-color: " + animalsColors.get(backgroundIndex) + ";");
            } else {
                label.setStyle("-fx-background-color: yellow;");
            }

            label.setAlignment(Pos.CENTER);
            label.setPadding(new Insets(10));

            Image image = getOrCreateImage(element.getResourceFileName());
            ImageView imageView = new ImageView(image);

            imageView.setFitHeight(size);
            imageView.setFitWidth(size);

            StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(label, imageView);
            stackPane.setAlignment(Pos.CENTER);

            container.getChildren().clear();
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