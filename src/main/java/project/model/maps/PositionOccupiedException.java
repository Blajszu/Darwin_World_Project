package project.model.maps;

import project.model.worldElements.WorldElement;

public class PositionOccupiedException extends RuntimeException {
    public PositionOccupiedException(WorldElement element) {
        super("Position %s is already occupied by other %s".formatted(element.getPosition(), element.getClass().getName()));
    }
}
