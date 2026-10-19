package starlords.faction;

public enum LawLevel {
    VERY_LOW("v_low"),
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    VERY_HIGH("v_high");

    /** Suffix of the law_level.* message; display names depend on the faction, see Presentation.levelName. */
    public final String key;

    LawLevel(String key) {
        this.key = key;
    }

    public boolean isAtLeast(LawLevel level) {
        return ordinal() - level.ordinal() >= 0;
    }
}
