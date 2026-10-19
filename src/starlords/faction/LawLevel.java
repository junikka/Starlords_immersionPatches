package starlords.faction;

import starlords.presentation.Presentation;

public enum LawLevel {
    VERY_LOW(Presentation.text("law_level.v_low")),
    LOW(Presentation.text("law_level.low")),
    MEDIUM(Presentation.text("law_level.medium")),
    HIGH(Presentation.text("law_level.high")),
    VERY_HIGH(Presentation.text("law_level.v_high"));

    public final String displayName;

    LawLevel(String s) {
        displayName = s;
    }

    public boolean isAtLeast(LawLevel level) {
        return ordinal() - level.ordinal() >= 0;
    }
}
