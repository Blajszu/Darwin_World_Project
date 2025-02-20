package project.presenter;

public enum GrowthGrassVariant {
    EQUATOR_MAP("Equator map"),
    MOVING_JUNGLE_MAP("Moving jungle map");

    private final String displayName;

    GrowthGrassVariant(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
