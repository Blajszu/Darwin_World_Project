package project.presenter;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import project.model.worldElements.Animal;
import project.model.worldElements.WorldElement;

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
            Node elementBox = createElementBox(element);
            container.getChildren().clear();
            container.getChildren().add(elementBox);
    }

    private Node createElementBox(WorldElement element) {

        Image image;

        if(selected) {
            image = getOrCreateImage(element.getResourceFileName().replace(".png", "_selected.png"));
        } else {
            image = getOrCreateImage(element.getResourceFileName());
        }

        ImageView imageView = new ImageView(image);

        if(element instanceof Animal) {
            imageView.setFitHeight(size * 0.8);
            imageView.setFitWidth(size * 0.8);
        } else {
            imageView.setFitHeight(size);
            imageView.setFitWidth(size);
        }

        StackPane stackPane = new StackPane();
        stackPane.setMinWidth(size);
        stackPane.setMinHeight(size);
        stackPane.setAlignment(Pos.CENTER);
        stackPane.getChildren().add(imageView);

        if (element instanceof Animal) {
            VBox animalLabel = createAnimalEnergyBar();
            stackPane.getChildren().add(animalLabel);
        }

        return stackPane;
    }

    private VBox createAnimalEnergyBar() {
        Label label = new Label();

        int energy = ((Animal) element).getCurrentEnergy();
        double width = Math.min(size, ((double)energy / (2 * (double)initialAnimalEnergy)) * size);

        label.setMinWidth(width);
        label.setMaxWidth(width);

        label.setMinHeight(0.1 * size);
        label.setMaxHeight(0.1 * size);
        label.setStyle("-fx-background-color: #F99090;");

        VBox animalVBox = new VBox();
        animalVBox.setMinWidth(size);
        animalVBox.setMinHeight(size);
        animalVBox.getChildren().add(label);

        animalVBox.setAlignment(Pos.BOTTOM_LEFT);

        return animalVBox;
    }

    private Image getOrCreateImage(String resourceFileName) {
        return imageCache.computeIfAbsent(resourceFileName, Image::new);
    }
}