package starlords.presentation;

import com.fs.starfarer.api.Global;
import org.json.*;
import starlords.util.Constants;
import java.util.*;

/** Neutral presentation facade. No gameplay configuration is mutated here. */
public final class Presentation {
    private static Map<String, String> messages;
    private static TitleResolver titles;
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
            titles = new TitleResolver(neutral, explicit, legacy);
            messages = Collections.unmodifiableMap(loaded);
            warned.clear();
            Global.getLogger(Presentation.class).info("Loaded neutral presentation: " + messages.size() + " messages");
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load data/lords/presentation: " + ex.getMessage(), ex);
        }
    }

    private static List<String> ranks(JSONArray array, String path) throws JSONException {
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

    public static String format(String key, String... pairs) {
        if (pairs.length % 2 != 0) throw new IllegalArgumentException("Expected token/value pairs for " + key);
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) values.put("{" + pairs[i] + "}", pairs[i + 1]);
        return render(key, text(key), values);
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

    public static String lawName(com.fs.starfarer.api.campaign.FactionAPI authority,
                                 starlords.faction.Lawset.LawType law) {
        // The authority argument is the seam for Phase 3's faction profiles.
        return text("policy.name." + law.name().toLowerCase(java.util.Locale.ROOT));
    }
}
