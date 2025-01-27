package project; // czy to się nadaje do głównego pakietu?

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

