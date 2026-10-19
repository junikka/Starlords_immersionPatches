package starlords.presentation;

import java.util.*;
import java.util.function.Function;

/** Rank wording only; political tier, income and voting weight are unchanged. */
public final class TitleResolver {
    private final List<String> neutral;
    private final Map<String, List<String>> bindings;
    private final Map<String, List<String>> legacy;
    private final Function<String, List<String>> profiles;

    public TitleResolver(List<String> neutral, Map<String, List<String>> bindings,
                         Map<String, List<String>> legacy) {
        this(neutral, bindings, legacy, factionId -> null);
    }

    /** Order: titles.json binding, then legacy faction.json ranks, then the faction's profile, then neutral. */
    public TitleResolver(List<String> neutral, Map<String, List<String>> bindings,
                         Map<String, List<String>> legacy, Function<String, List<String>> profiles) {
        this.profiles = profiles;
        this.neutral = List.copyOf(neutral);
        if (neutral.size() != 3) throw new IllegalArgumentException("Three neutral ranks are required");
        this.bindings = copy(bindings);
        this.legacy = copy(legacy);
    }

    private static Map<String, List<String>> copy(Map<String, List<String>> input) {
        Map<String, List<String>> out = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : input.entrySet())
            out.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        return Collections.unmodifiableMap(out);
    }

    public String title(String factionId, int tier) {
        int index = Math.max(0, Math.min(2, tier));
        List<String> ranks = bindings.get(factionId);
        if (ranks == null) ranks = legacy.get(factionId);
        String value = ranks != null && index < ranks.size() ? ranks.get(index) : null;
        if (value == null || value.trim().isEmpty()) {
            List<String> profile = profiles.apply(factionId);
            value = profile != null ? profile.get(index) : null;
        }
        return value == null || value.trim().isEmpty() ? neutral.get(index) : value;
    }
}
