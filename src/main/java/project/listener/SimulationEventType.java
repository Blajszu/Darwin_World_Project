package project.listener;

public enum SimulationEventType {
    ANIMALS_REMOVED("Usunięto martwe zwierzęta"),
    ANIMALS_ROTATED("Zwierzaki się obróciły"),
    ANIMALS_MOVED("Zwierzęta wykonały ruch"),
    FOOD_CONSUMED("Zwierzęta zjadły trawę i się rozmnożyły"),
    GRASS_SPAWNED("Wyrosła nowa trawa"),
    DAY_ENDED("Zakończono dzień symulacji");

    private final String message;

    SimulationEventType(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
