package starlords.presentation;

import org.json.*;
import java.util.*;

/**
 * Faction wording profiles: default (neutral.json) -> institutional profile -> faction profile.
 * A profile may only override the institutional message keys listed in overridable_keys.json,
 * and may only use tokens its default message already receives. Wording only; no mechanics.
 */
public final class ProfileRegistry {
    public static final String DEFAULT = "default";

    private static final class Profile {
        final String parent;
        final Map<String, String> messages;
        final List<String> titles;
        Profile(String parent, Map<String, String> messages, List<String> titles) {
            this.parent = parent;
            this.messages = messages;
            this.titles = titles;
        }
    }

    private final Map<String, Profile> profiles = new HashMap<>();
    private final Map<String, String> bindings = new HashMap<>();
    private final Map<String, List<String>> chains = new HashMap<>();

    /** An empty registry: everything resolves to the default profile. */
    public ProfileRegistry() {}

    public ProfileRegistry(JSONObject json, Map<String, String> defaults, Set<String> overridable) throws JSONException {
        if (json.getInt("schemaVersion") != 1) throw new IllegalArgumentException("Unsupported profiles schemaVersion");
        JSONObject entries = json.getJSONObject("profiles");
        for (Iterator<?> it = entries.keys(); it.hasNext();) {
            String name = (String) it.next();
            String path = "profiles." + name;
            if (name.equals(DEFAULT)) throw new IllegalArgumentException(path + ": the default profile is neutral.json and cannot be redefined");
            JSONObject entry = entries.getJSONObject(name);
            String parent = entry.optString("extends", DEFAULT);
            Map<String, String> messages = new HashMap<>();
            JSONObject overrides = entry.optJSONObject("messages");
            if (overrides != null) {
                for (Iterator<?> keys = overrides.keys(); keys.hasNext();) {
                    String key = (String) keys.next();
                    String keyPath = path + ".messages." + key;
                    if (!overridable.contains(key)) throw new IllegalArgumentException(keyPath + " is not an overridable message");
                    Object value = overrides.get(key);
                    if (!(value instanceof String) || ((String) value).trim().isEmpty())
                        throw new IllegalArgumentException(keyPath + " must be nonempty text");
                    Set<String> extra = TextRenderer.namedTokens((String) value);
                    extra.removeAll(TextRenderer.namedTokens(defaults.get(key)));
                    if (!extra.isEmpty()) throw new IllegalArgumentException(keyPath + " uses tokens its context doesn't supply: " + extra);
                    messages.put(key, (String) value);
                }
            }
            List<String> titles = null;
            if (entry.has("titles")) titles = Presentation.ranks(entry.getJSONArray("titles"), path + ".titles");
            profiles.put(name, new Profile(parent, messages, titles));
        }
        for (String name : profiles.keySet()) chains.put(name, chain(name));
        JSONObject factions = json.getJSONObject("factions");
        for (Iterator<?> it = factions.keys(); it.hasNext();) {
            String faction = (String) it.next();
            String profile = factions.getString(faction);
            if (!isProfile(profile)) throw new IllegalArgumentException("factions." + faction + " names unknown profile " + profile);
            bindings.put(faction, profile);
        }
    }

    private List<String> chain(String name) {
        List<String> chain = new ArrayList<>();
        String current = name;
        while (!current.equals(DEFAULT)) {
            if (chain.contains(current)) throw new IllegalArgumentException("profiles." + name + " has an inheritance cycle: " + chain);
            Profile profile = profiles.get(current);
            if (profile == null) throw new IllegalArgumentException("profiles." + chain.get(chain.size() - 1) + " extends unknown profile " + current);
            chain.add(current);
            current = profile.parent;
        }
        return Collections.unmodifiableList(chain);
    }

    public boolean isProfile(String name) {
        return DEFAULT.equals(name) || profiles.containsKey(name);
    }

    public String profileOf(String factionId) {
        String profile = factionId == null ? null : bindings.get(factionId);
        return profile == null ? DEFAULT : profile;
    }

    /** The most specific override of a message for this profile, or null for the default wording. */
    public String message(String profile, String key) {
        for (String name : chains.getOrDefault(profile, List.of())) {
            String value = profiles.get(name).messages.get(key);
            if (value != null) return value;
        }
        return null;
    }

    /** The most specific title ladder for this profile, or null when only the default ranks apply. */
    public List<String> titles(String profile) {
        for (String name : chains.getOrDefault(profile, List.of())) {
            List<String> titles = profiles.get(name).titles;
            if (titles != null) return titles;
        }
        return null;
    }
}
