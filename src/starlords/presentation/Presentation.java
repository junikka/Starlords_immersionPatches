package starlords.presentation;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import org.json.*;
import starlords.util.Constants;
import java.util.*;

/** Presentation facade: default wording plus faction profiles. No gameplay configuration is mutated here. */
public final class Presentation {
    private static Map<String, String> messages;
    private static TitleResolver titles;
    private static ProfileRegistry profiles = new ProfileRegistry();
    private static String playerProfile = ProfileRegistry.DEFAULT;
    private static final Set<String> warned = new HashSet<>();
    private Presentation() {}

    public static synchronized void reload() {
        try {
            JSONObject json = Global.getSettings().getMergedJSONForMod(
                    "data/lords/presentation/neutral.json", Constants.MOD_ID);
            if (json.getInt("schemaVersion") != 1) throw new IllegalArgumentException("Unsupported neutral schemaVersion");
            Map<String, String> loaded = new HashMap<>();
            JSONObject entries = json.getJSONObject("messages");
            for (Iterator<?> it = entries.keys(); it.hasNext();) {
                String key = (String) it.next();
                Object value = entries.get(key);
                if (!(value instanceof String) || ((String)value).trim().isEmpty())
                    throw new IllegalArgumentException("messages." + key + " must be nonempty text");
                loaded.put(key, (String) value);
            }
            JSONObject rankData = Global.getSettings().getMergedJSONForMod(
                    "data/lords/presentation/titles.json", Constants.MOD_ID);
            if (rankData.getInt("schemaVersion") != 1) throw new IllegalArgumentException("Unsupported titles schemaVersion");
            List<String> neutral = ranks(rankData.getJSONArray("neutralRanks"), "neutralRanks");
            Map<String, List<String>> explicit = new HashMap<>(), legacy = new HashMap<>();
            JSONObject factions = rankData.getJSONObject("factions");
            for (Iterator<?> it = factions.keys(); it.hasNext();) {
                String id = (String) it.next();
                explicit.put(id, ranks(factions.getJSONArray(id), "factions." + id));
            }
            JSONObject old = Global.getSettings().getMergedJSONForMod("data/lords/faction.json", Constants.MOD_ID);
            for (Iterator<?> it = old.keys(); it.hasNext();) {
                String id = (String) it.next();
                JSONObject faction = old.getJSONObject(id);
                List<String> list = new ArrayList<>();
                for (int tier = 0; tier < 3; tier++) {
                    Object value = faction.opt("rank_" + tier);
                    list.add(value instanceof String ? (String) value : null);
                }
                legacy.put(id, list);
            }
            Set<String> overridable = new HashSet<>();
            JSONArray keys = Global.getSettings().loadJSON(
                    "data/lords/presentation/overridable_keys.json", Constants.MOD_ID).getJSONArray("keys");
            for (int i = 0; i < keys.length(); i++) {
                String key = keys.getString(i);
                if (!loaded.containsKey(key)) throw new IllegalArgumentException("overridable_keys.json lists unknown message " + key);
                overridable.add(key);
            }
            ProfileRegistry loadedProfiles = new ProfileRegistry(Global.getSettings().getMergedJSONForMod(
                    "data/lords/presentation/profiles.json", Constants.MOD_ID), loaded, overridable);
            titles = new TitleResolver(neutral, explicit, legacy,
                    factionId -> loadedProfiles.titles(loadedProfiles.profileOf(factionId)));
            profiles = loadedProfiles;
            messages = Collections.unmodifiableMap(loaded);
            warned.clear();
            Global.getLogger(Presentation.class).info("Loaded neutral presentation: " + messages.size() + " messages");
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load data/lords/presentation: " + ex.getMessage(), ex);
        }
    }

    static List<String> ranks(JSONArray array, String path) throws JSONException {
        if (array.length() != 3) throw new IllegalArgumentException(path + " requires three ranks");
        List<String> out = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Object value = array.get(i);
            if (!(value instanceof String) || ((String) value).trim().isEmpty())
                throw new IllegalArgumentException(path + "[" + i + "] must be nonempty text");
            out.add((String) value);
        }
        return out;
    }

    public static String text(String key) {
        if (messages == null) reload();
        String value = messages.get(key);
        if (value != null) return value;
        if (warned.add(key)) Global.getLogger(Presentation.class).warn("Missing presentation message: " + key);
        return "Information unavailable";
    }

    /** Wording as the given faction's profile has it; institutional keys may differ per faction. */
    public static String textFor(FactionAPI faction, String key) {
        return textFor(faction == null ? null : faction.getId(), key);
    }

    public static String textFor(String factionId, String key) {
        if (messages == null) reload();
        String value = profiles.message(profileOf(factionId), key);
        return value != null ? value : text(key);
    }

    public static String format(String key, String... pairs) {
        return formatFor((String) null, key, pairs);
    }

    public static String formatFor(FactionAPI faction, String key, String... pairs) {
        return formatFor(faction == null ? null : faction.getId(), key, pairs);
    }

    public static String formatFor(String factionId, String key, String... pairs) {
        if (pairs.length % 2 != 0) throw new IllegalArgumentException("Expected token/value pairs for " + key);
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) values.put("{" + pairs[i] + "}", pairs[i + 1]);
        return render(key, textFor(factionId, key), values);
    }

    /** The profile a faction's wording comes from. The player's own faction uses the chosen setting. */
    public static String profileOf(String factionId) {
        if (Factions.PLAYER.equals(factionId) && !ProfileRegistry.DEFAULT.equals(playerProfile)) return playerProfile;
        return profiles.profileOf(factionId);
    }

    /** Set from the settings; an unknown name falls back to the default profile with a warning. */
    public static void setPlayerProfile(String name) {
        if (messages == null) reload();
        String profile = name == null ? ProfileRegistry.DEFAULT : name.trim();
        if (!profiles.isProfile(profile)) {
            Global.getLogger(Presentation.class).warn("Unknown player faction profile " + name + ", using default");
            profile = ProfileRegistry.DEFAULT;
        }
        playerProfile = profile;
    }

    public static String render(String key, String template, Map<String, String> values) {
        Set<String> missing = TextRenderer.missingNamedTokens(template, values);
        if (!missing.isEmpty()) {
            String warning = key + ": " + missing;
            if (warned.add(warning)) Global.getLogger(Presentation.class).warn(
                    "Missing presentation context for " + warning);
            return text("context.unavailable");
        }
        return TextRenderer.render(template, values);
    }

    public static String title(String factionId, int tier) {
        if (titles == null) reload();
        return titles.title(factionId, tier);
    }

    public static String lawName(FactionAPI authority, starlords.faction.Lawset.LawType law) {
        return textFor(authority, "policy.name." + law.name().toLowerCase(Locale.ROOT));
    }

    public static String levelName(FactionAPI authority, starlords.faction.LawLevel level) {
        return textFor(authority, "law_level." + level.key);
    }

    /** What one level of a policy does; "|" separates the tooltip's lines, the first being the level's name. */
    public static String levelEffects(FactionAPI authority, starlords.faction.Lawset.LawType law, int level) {
        return textFor(authority, "policy.level." + law.name().toLowerCase(Locale.ROOT) + "." + level);
    }

    public static String policyDescription(FactionAPI authority, starlords.faction.Lawset.LawType law) {
        return textFor(authority, "policy.description." + law.name().toLowerCase(Locale.ROOT));
    }
}
