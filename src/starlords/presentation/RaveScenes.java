package starlords.presentation;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONArray;
import org.json.JSONObject;
import starlords.controllers.PoliticsController;
import starlords.faction.Lawset;
import starlords.person.LordEvent;
import starlords.util.Constants;

import java.util.*;

/**
 * The look and feel of a rave, by host faction (data/lords/presentation/raves.json): the venue picture,
 * an arrival scene above the first conversation at each rave, and a crowd line in every rave conversation.
 */
public final class RaveScenes {
    public static final String DEFAULT = "default";
    /** Rave Licensing levels in LawLevel order: VERY_LOW is Anything Goes, VERY_HIGH is Banned. */
    public static final List<String> LEVELS = List.of("anything_goes", "permissive", "licensed", "strict", "banned");

    private static final class Venue {
        String image;
        final Map<String, String> arrival = new HashMap<>(); // level -> text; key DEFAULT for a single text
        final List<String> crowd = new ArrayList<>();
    }

    private static Map<String, Venue> venues;
    private static final Random random = new Random();

    private RaveScenes() {}

    public static synchronized void reload() {
        try {
            JSONObject json = Global.getSettings().getMergedJSONForMod("data/lords/presentation/raves.json", Constants.MOD_ID);
            venues = parse(json);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load data/lords/presentation/raves.json: " + ex.getMessage(), ex);
        }
    }

    static Map<String, Venue> parse(JSONObject json) throws Exception {
        if (json.getInt("schemaVersion") != 1) throw new IllegalArgumentException("Unsupported raves schemaVersion");
        JSONObject factions = json.getJSONObject("factions");
        if (!factions.has(DEFAULT)) throw new IllegalArgumentException("factions.default is required");
        Map<String, Venue> loaded = new HashMap<>();
        for (Iterator<?> it = factions.keys(); it.hasNext();) {
            String id = (String) it.next();
            String path = "factions." + id;
            JSONObject entry = factions.getJSONObject(id);
            Venue venue = new Venue();
            venue.image = entry.optString("image", null);
            Object arrival = entry.opt("arrival");
            if (arrival instanceof String) {
                venue.arrival.put(DEFAULT, text(arrival, path + ".arrival"));
            } else if (arrival instanceof JSONObject) {
                JSONObject levels = (JSONObject) arrival;
                for (Iterator<?> keys = levels.keys(); keys.hasNext();) {
                    String level = (String) keys.next();
                    if (!LEVELS.contains(level)) throw new IllegalArgumentException(path + ".arrival." + level + " is not a Rave Licensing level " + LEVELS);
                    venue.arrival.put(level, text(levels.get(level), path + ".arrival." + level));
                }
            } else if (arrival != null) {
                throw new IllegalArgumentException(path + ".arrival must be text or an object of levels");
            }
            JSONArray crowd = entry.optJSONArray("crowd");
            if (crowd != null) for (int i = 0; i < crowd.length(); i++) venue.crowd.add(text(crowd.get(i), path + ".crowd[" + i + "]"));
            loaded.put(id, venue);
        }
        Venue fallback = loaded.get(DEFAULT);
        if (fallback.image == null || fallback.arrival.get(DEFAULT) == null)
            throw new IllegalArgumentException("factions.default needs an image and a single arrival text");
        return loaded;
    }

    private static String text(Object value, String path) {
        if (!(value instanceof String) || ((String) value).trim().isEmpty()) throw new IllegalArgumentException(path + " must be nonempty text");
        return (String) value;
    }

    private static Map<String, Venue> venues() {
        if (venues == null) reload();
        return venues;
    }

    private static Venue venue(String factionId) {
        Venue venue = factionId == null ? null : venues().get(factionId);
        return venue != null ? venue : venues().get(DEFAULT);
    }

    /** The illustration key for a rave held by this faction. */
    public static String image(String factionId) {
        String image = venue(factionId).image;
        return image != null ? image : venues().get(DEFAULT).image;
    }

    /** The arrival scene for this faction at a Rave Licensing level (LawLevel ordinal). */
    public static String arrival(String factionId, int licensingLevel) {
        Venue venue = venue(factionId);
        String level = LEVELS.get(Math.max(0, Math.min(LEVELS.size() - 1, licensingLevel)));
        String text = venue.arrival.get(level);
        if (text == null) text = venue.arrival.get(DEFAULT);
        return text != null ? text : venues().get(DEFAULT).arrival.get(DEFAULT);
    }

    /** All crowd lines a rave of this faction can show: its own plus the default ones. */
    public static List<String> crowd(String factionId) {
        List<String> lines = new ArrayList<>(venues().get(DEFAULT).crowd);
        Venue venue = venues().get(factionId);
        if (venue != null && !DEFAULT.equals(factionId)) lines.addAll(venue.crowd);
        return lines;
    }

    /** First conversation at this rave: the venue picture and the arrival scene, once per rave. */
    public static void showArrival(TextPanelAPI text, LordEvent rave) {
        if (rave == null || rave.isArrivalShown()) return;
        rave.setArrivalShown(true);
        FactionAPI host = rave.getFaction();
        String id = host == null ? null : host.getId();
        try {
            text.addImage("illustrations", image(id));
        } catch (RuntimeException e) {
            Global.getLogger(RaveScenes.class).warn("Missing rave illustration " + image(id), e);
        }
        int level = host == null ? 0 : PoliticsController.getLaws(host).getLawLevel(Lawset.LawType.FEAST_LAW).ordinal();
        text.addPara(arrival(id, level));
    }

    /** One line of background life, in the muted colour vanilla uses for scene notes. */
    public static void showCrowdLine(TextPanelAPI text, LordEvent rave) {
        if (rave == null) return;
        List<String> lines = crowd(rave.getFaction() == null ? null : rave.getFaction().getId());
        if (lines.isEmpty()) return;
        text.addPara(lines.get(random.nextInt(lines.size())), Misc.getGrayColor());
    }

    /** The venue picture's sprite path, loaded so intel panels can draw it; null if it can't be found. */
    public static String imageSprite(String factionId) {
        try {
            String sprite = Global.getSettings().getSpriteName("illustrations", image(factionId));
            Global.getSettings().loadTexture(sprite);
            return sprite;
        } catch (Exception e) {
            Global.getLogger(RaveScenes.class).warn("Missing rave illustration " + image(factionId), e);
            return null;
        }
    }
}
