package starlords.presentation;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import org.json.JSONArray;
import org.json.JSONObject;
import starlords.util.Constants;

import java.util.*;

/** Comm-link greetings from ordinary fleets of the player's council faction, from data/lords/presentation/greetings.json. */
public final class FleetGreetings {
    private static List<String> fallback;
    private static Map<String, List<String>> byFaction;
    private FleetGreetings() {}

    private static synchronized void load() {
        try {
            JSONObject json = Global.getSettings().getMergedJSONForMod(
                    "data/lords/presentation/greetings.json", Constants.MOD_ID);
            if (json.getInt("schemaVersion") != 1) throw new IllegalArgumentException("Unsupported greetings schemaVersion");
            fallback = lines(json.getJSONArray("default"), "default");
            Map<String, List<String>> loaded = new HashMap<>();
            JSONObject factions = json.getJSONObject("factions");
            for (Iterator<?> it = factions.keys(); it.hasNext();) {
                String id = (String) it.next();
                loaded.put(id, lines(factions.getJSONArray(id), "factions." + id));
            }
            byFaction = loaded;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load data/lords/presentation/greetings.json: " + ex.getMessage(), ex);
        }
    }

    private static List<String> lines(JSONArray array, String path) throws Exception {
        if (array.length() != 3) throw new IllegalArgumentException(path + " requires three greetings, one per tier");
        List<String> out = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String line = array.getString(i);
            if (line.trim().isEmpty()) throw new IllegalArgumentException(path + "[" + i + "] must be nonempty text");
            out.add(line);
        }
        return out;
    }

    public static String greeting(String factionId, int tier, String title) {
        if (byFaction == null) load();
        List<String> lines = byFaction.containsKey(factionId) ? byFaction.get(factionId) : fallback;
        PersonAPI player = Global.getSector().getPlayerPerson();
        FullName name = player.getName();
        Map<String, String> values = new LinkedHashMap<>();
        values.put("{title}", title);
        values.put("{lastName}", name.getLast() == null || name.getLast().isEmpty() ? name.getFirst() : name.getLast());
        values.put("{fullName}", name.getFullName());
        values.put("{sirOrMadam}", name.getGender() == FullName.Gender.FEMALE ? "ma'am" : "sir");
        int index = Math.max(0, Math.min(2, tier));
        return Presentation.render("greetings." + factionId + "." + index, lines.get(index), values);
    }
}
