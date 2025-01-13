package project;

public enum MutationVariant {
    RANDOM("Random"),
    INCREMENT_DECREMENT("Increment/Decrement");

    private final String displayName;

    MutationVariant(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

