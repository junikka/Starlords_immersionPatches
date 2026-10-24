import java.awt.Color;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiFunction;
import org.json.*;
import com.fs.starfarer.api.*;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.*;
import starlords.controllers.*;
import starlords.faction.*;
import starlords.person.*;
import starlords.presentation.*;
import starlords.util.*;
import starlords.util.dialogControler.*;
import starlords.util.dialogControler.dialogAddon.DialogAddon_Base;

/** Controlled game context, invoking production code against the real API JAR. */
public class PresentationChecks {
    static Path root;
    static JSONObject titleData, neutralData, legacyData, profileData, overridableData, raveData;
    static final Map<String, FactionAPI> factions = new HashMap<>();
    static final Map<String, Object> persistent = new HashMap<>();
    static String commission;
    static FactionAPI speakerFaction;
    static Lord player, speaker, recipient, host;
    static MarketAPI market;

    @SuppressWarnings("unchecked")
    static <T> T proxy(Class<T> type, BiFunction<String, Object[], Object> handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (obj, method, args) -> {
            if (method.getName().equals("equals")) return obj == args[0];
            if (method.getName().equals("hashCode")) return System.identityHashCode(obj);
            if (method.getName().equals("toString")) return "API proxy " + type.getSimpleName();
            Object value = handler.apply(method.getName(), args == null ? new Object[0] : args);
            if (value != null || !method.getReturnType().isPrimitive()) return value;
            if (method.getReturnType() == boolean.class) return false;
            if (method.getReturnType() == float.class) return 0f;
            if (method.getReturnType() == double.class) return 0d;
            if (method.getReturnType() == long.class) return 0L;
            return 0;
        });
    }
    static void eq(Object actual, Object expected) {
        if (!Objects.equals(actual, expected)) throw new AssertionError("Expected " + expected + ", got " + actual);
    }
    static void yes(boolean condition) { if (!condition) throw new AssertionError(); }
    static JSONObject json(String path) throws Exception { return new JSONObject(Files.readString(root.resolve(path))); }
    static Object field(Class<?> type, Object instance, String name) throws Exception {
        Field f = type.getDeclaredField(name); f.setAccessible(true); return f.get(instance);
    }
    static void set(Class<?> type, Object instance, String name, Object value) throws Exception {
        Field f = type.getDeclaredField(name); f.setAccessible(true); f.set(instance, value);
    }
    static <T> T construct(Class<T> type) throws Exception {
        Constructor<T> ctor = type.getDeclaredConstructor(); ctor.setAccessible(true); return ctor.newInstance();
    }
    static FactionAPI faction(String id) {
        FactionAPI f = proxy(FactionAPI.class, (m, a) -> switch(m) {
            case "getId" -> id;
            case "getDisplayName", "getDisplayNameWithArticle" -> id;
            case "isPlayerFaction" -> id.equals("player");
            case "getBaseUIColor", "getBrightUIColor", "getDarkUIColor" -> Color.WHITE;
            default -> null;
        });
        factions.put(id, f); return f;
    }
    static Lord person(String id, String first, String last, String factionId, int tier) throws Exception {
        FullName name = new FullName(first, last, FullName.Gender.MALE);
        PersonAPI p = proxy(PersonAPI.class, (m, a) -> switch(m) {
            case "getId" -> id;
            case "getNameString" -> first + " " + last;
            case "getName" -> name;
            case "getGender" -> FullName.Gender.MALE;
            case "getFaction" -> id.equals("speaker") ? speakerFaction : factions.get(factionId);
            case "getManOrWoman" -> "man";
            case "getHeOrShe" -> "he";
            case "getHisOrHer", "getHimOrHer" -> "his";
            default -> null;
        });
        Constructor<Lord> ctor = Lord.class.getDeclaredConstructor(PersonAPI.class);
        ctor.setAccessible(true);
        Lord lord = ctor.newInstance(p);
        set(Lord.class, lord, "ranking", tier);
        set(Lord.class, lord, "formalWear", "uniform");
        return lord;
    }
    static void setup() throws Exception {
        neutralData = json("data/lords/presentation/neutral.json");
        titleData = json("data/lords/presentation/titles.json");
        legacyData = json("data/lords/faction.json");
        profileData = json("data/lords/presentation/profiles.json");
        overridableData = json("data/lords/presentation/overridable_keys.json");
        raveData = json("data/lords/presentation/raves.json");
        JSONObject strings = json("data/strings/strings.json");
        ModManagerAPI modManager = proxy(ModManagerAPI.class, (m,a) -> null);
        Global.setSettings(proxy(SettingsAPI.class, (m, a) -> {
            try {
                if (m.equals("getMergedJSONForMod")) return switch((String)a[0]) {
                    case "data/lords/presentation/neutral.json" -> neutralData;
                    case "data/lords/presentation/titles.json" -> titleData;
                    case "data/lords/faction.json" -> legacyData;
                    case "data/lords/presentation/profiles.json" -> profileData;
                    case "data/lords/presentation/raves.json" -> raveData;
                    default -> throw new AssertionError("Unexpected file: " + a[0]);
                };
                if (m.equals("loadJSON")) {
                    if (!a[0].equals("data/lords/presentation/overridable_keys.json")) throw new AssertionError("Unexpected file: " + a[0]);
                    return overridableData;
                }
                if (m.equals("getString")) return strings.getJSONObject((String)a[0]).getString((String)a[1]);
                if (m.equals("getModManager")) return modManager;
                if (m.endsWith("Color")) return Color.WHITE;
                return null;
            } catch (JSONException e) { throw new RuntimeException(e); }
        }));
        for (String id : List.of("player", "naval", "raider", "corporate", "unknown_mod",
                "hegemony", "pirates", "sindrian_diktat", "luddic_path", "modded_corp")) faction(id);
        speakerFaction = factions.get("raider");
        titleData.getJSONObject("factions").put("naval", new JSONArray(List.of("Lieutenant", "Captain", "Admiral")));
        titleData.getJSONObject("factions").put("raider", new JSONArray(List.of("Captain", "Warlord", "Overlord")));
        titleData.getJSONObject("factions").put("corporate", new JSONArray(List.of("Agent", "Manager", "Director")));
        persistent.put(Constants.LORD_TABLE_KEY, new HashMap<>());
        MemoryAPI memory = proxy(MemoryAPI.class, (m,a) -> m.equals("getString") && a[0].equals("$fcm_faction") ? commission : null);
        CharacterDataAPI character = proxy(CharacterDataAPI.class, (m,a) -> m.equals("getMemoryWithoutUpdate") ? memory : null);
        CampaignClockAPI clock = proxy(CampaignClockAPI.class, (m,a) -> null);
        market = proxy(MarketAPI.class, (m,a) -> switch(m) {
            case "getId" -> "kestrel"; case "getName" -> "Kestrel $5\\Dock";
            case "getFaction" -> factions.get("naval"); default -> null;
        });
        EconomyAPI economy = proxy(EconomyAPI.class, (m,a) -> m.equals("getMarket") && a[0].equals("kestrel") ? market : null);
        Global.setSector(proxy(SectorAPI.class, (m,a) -> switch(m) {
            case "getFaction" -> factions.get(a[0]);
            case "getPlayerFaction" -> factions.get("player");
            case "getPlayerPerson" -> player == null ? null : player.getLordAPI();
            case "getCharacterData" -> character;
            case "getClock" -> clock;
            case "getEconomy" -> economy;
            case "getPersistentData" -> persistent;
            case "getAllFactions", "getScripts" -> new ArrayList<>();
            default -> null;
        }));
        Presentation.reload();
        player = person("player", "Mara$1", "\\Venn", "player", 1);
        set(Lord.class, player, "isPlayer", true);
        set(LordController.class, null, "playerLord", player);
        speaker = person("speaker", "Rook", "Tann", "raider", 1);
        recipient = person("recipient", "Ira", "Sun", "corporate", 2);
        host = person("host", "Dana", "Rell", "raider", 0);
        set(LordController.class, null, "lordsMap", new HashMap<>(Map.of("speaker", speaker, "recipient", recipient, "host", host)));
        PoliticsController.instance = construct(PoliticsController.class);
        set(EventController.class, null, "instance", construct(EventController.class));
    }
    static void renderer() {
        Map<String,String> values = new HashMap<>(Map.of("%PLAYER_NAME", "Mara$1\\Venn %PLAYER_NAME_FIRST {host.fullName}",
                "%PLAYER_NAME_FIRST", "Mara", "%c1", "one", "%c10", "ten"));
        eq(TextRenderer.render("%PLAYER_NAME_FIRST / %PLAYER_NAME / %c10 / %c1 / %c100", values),
                "Mara / Mara$1\\Venn %PLAYER_NAME_FIRST {host.fullName} / ten / one / %c100");
        eq(TextRenderer.render("%c10", Map.of("%c1", "one")), "%c10");
        eq(TextRenderer.positional("$string / $string", "$1\\test $string", "two"), "$1\\test $string / two");
        eq(TextRenderer.positional("$string / $string", (String)null), "unavailable / unavailable");
        eq(TextRenderer.missingNamedTokens("{commander} {host.name}", Map.of("{commander}", "{invented}")), Set.of("{host.name}"));
        eq(Presentation.format("holding.assign", "market", "Kestrel"), "Information unavailable");
        eq(Presentation.format("holding.assign", "market", "Kestrel", "commander", "$5\\{host.name}"),
                "Grant Kestrel to $5\\{host.name} as a holding");
    }
    static void titles() {
        List<String> bound = new ArrayList<>(List.of("Ensign", "Captain", "Admiral"));
        TitleResolver resolver = new TitleResolver(List.of("Commander", "Commander", "Commander"),
                Map.of("naval", bound), Map.of("naval", List.of("Lord", "Duke", "King"),
                        "legacy", Arrays.asList("Captain", null, "Warlord")));
        bound.set(1, "Changed");
        eq(resolver.title("naval", 1), "Captain");
        eq(resolver.title("legacy", 0), "Captain");
        eq(resolver.title("legacy", 1), "Commander");
        eq(resolver.title("legacy", 2), "Warlord");
        eq(resolver.title("absent", 2), "Commander"); eq(resolver.title(null, -5), "Commander");
        eq(resolver.title("naval", 20), "Admiral");
        eq(Utils.getTitle(factions.get("raider"), 1), speaker.getTitle());
    }
    @SuppressWarnings("unchecked")
    static void context() throws Exception {
        commission = "naval";
        List<LordEvent> feasts = (List<LordEvent>)field(EventController.class,
                field(EventController.class, null, "instance"), "feasts");
        LordEvent event = new LordEvent(LordEvent.FEAST, host);
        event.setWeddingCeremonyTarget(recipient); feasts.add(event);
        Map<String,String> v = DialogueTokens.values(speaker, recipient, market);
        eq(v.get("%PLAYER_FACTION_RANK_TITLE2"), "Admiral");
        eq(v.get("%PLAYER_TITLE"), "Captain"); eq(v.get("%LORD_TITLE"), "Warlord");
        eq(v.get("%SECOND_LORD_TITLE"), "Director"); eq(v.get("{destination.rank2}"), "Admiral");
        eq(v.get("{host.formalName}"), "Captain Dana Rell");
        eq(v.get("%WEDDING_TARGET_TITLE"), "Director");
        eq(v.get("%LORD_LIEGE_NAME"), "the faction leadership");
        eq(v.get("%PLAYER_NAME_FIRST"), "Mara$1");
        eq(v.get("%TARGET_MARKET_NAME"), "Kestrel $5\\Dock");
        speakerFaction = factions.get("naval");
        eq(speaker.getTitle(), "Captain");
        eq(DialogueTokens.values(speaker, recipient, null).get("%LORD_TITLE"), "Captain");
        commission = null; speakerFaction = factions.get("unknown_mod");
        v = DialogueTokens.values(speaker, null, null);
        eq(v.get("%LORD_TITLE"), "Commodore"); eq(v.get("%PLAYER_TITLE"), "Commodore");
        eq(v.get("%SECOND_LORD_NAME"), "that person"); eq(v.get("%TARGET_MARKET_NAME"), "the market");
        eq(v.get("{host.fullName}"), "that person");
        for (String value : v.values()) yes(value == null || !value.contains("ERROR:"));
    }
    @SuppressWarnings("unchecked")
    static void proposals() throws Exception {
        commission = "naval";
        LawProposal award = new LawProposal(Lawset.LawType.AWARD_FIEF, "player", "speaker", "kestrel", null, 0);
        eq(award.getSummary(), "Grant Kestrel $5\\Dock to Warlord Rook Tann as a holding");
        LawProposal rank = new LawProposal(Lawset.LawType.CHANGE_RANK, "player", "speaker", null, null, 2);
        eq(rank.getSummary(), "Change Warlord Rook Tann's rank to Admiral");
        for (Lawset.LawType law : Lawset.LawType.values()) {
            LawProposal proposal = new LawProposal(law, "player", "speaker", "kestrel", "corporate", 1);
            yes(!proposal.getSummary().contains("Information unavailable"));
            yes(!proposal.getTitle().contains("Information unavailable"));
            yes(!proposal.getSummary().contains("{"));
        }
        LawProposal policy = new LawProposal(Lawset.LawType.CROWN_AUTHORITY, "player", null, null, null, 1);
        eq(policy.getTitle(), "Decrease Central Authority");
        Map<String,LawProposal> proposals = (Map<String,LawProposal>)field(PoliticsController.class, PoliticsController.instance, "lordProposalsMap");
        proposals.put("player", policy); proposals.put("speaker", award);
        Map<String,String> values = DialogueTokens.values(speaker, null, null);
        eq(values.get("%PLAYER_PROPOSAL_NAME"), policy.getTitle());
        eq(values.get("%LORD_PROPOSAL_NAME"), award.getTitle());
    }
    @SuppressWarnings("unchecked")
    static void dialogue() throws Exception {
        LordDialog owner = new LordDialog("test", new JSONObject("{\"rules\":{},\"lines\":{}}"));
        JSONObject line = new JSONObject().put("line", "%PLAYER_NAME_FIRST / %c10 / %c1 / {recipient.title}")
                .put("hint", "%c10 / %c1 / {recipient.title}")
                .put("highlight", new JSONObject().put("highlight", new JSONArray(List.of("%c10", "%c1"))))
                .put("color", "GREEN")
                .put("customInserts", new JSONObject().put("replaced", "%c10").put("dialogValue", 42));
        DialogSet set = new DialogSet("test", new JSONObject().put("lines", new JSONObject().put("test", line)), owner);
        List<Object[]> paras = new ArrayList<>(), options = new ArrayList<>(), hints = new ArrayList<>();
        List<String> order = new ArrayList<>();
        TextPanelAPI text = proxy(TextPanelAPI.class, (m,a) -> { if(m.equals("addPara")) { paras.add(a); order.add("text"); } return null; });
        OptionPanelAPI panel = proxy(OptionPanelAPI.class, (m,a) -> {
            if(m.equals("addOption")) options.add(a); if(m.equals("setTooltip")) hints.add(a); return null;
        });
        Map<String,ArrayList<DialogAddon_Base>> addons = (Map<String,ArrayList<DialogAddon_Base>>)field(DialogSet.class, set, "addons");
        addons.put("test", new ArrayList<>(List.of(new DialogAddon_Base() {
            public void apply(TextPanelAPI t, OptionPanelAPI o, InteractionDialogAPI d, Lord l, Lord r, MarketAPI m) { order.add("effect"); }
        })));
        HashMap<String,String> extra = new HashMap<>(Map.of("%c1", "$1\\literal %c10"));
        set.applyLine("test", speaker, recipient, market, text, panel, null, false, extra);
        eq(paras.get(0)[0], "Mara$1 / 42 / $1\\literal %c10 / Director");
        eq(Arrays.asList((String[])paras.get(0)[3]), List.of("42", "$1\\literal %c10"));
        eq(order, List.of("text", "effect"));
        set.applyOptionSingle("test", speaker, recipient, market, text, null, panel, null, extra);
        eq(options.get(0)[0], paras.get(0)[0]);
        eq(hints.get(0)[1], "42 / $1\\literal %c10 / Director");
        eq(order, List.of("text", "effect")); // Rendering options must not trigger effects.
    }
    static void validation() throws Exception {
        titleData.getJSONObject("factions").put("optional_mod_not_installed", new JSONArray(List.of("Captain","Warlord","Admiral")));
        Presentation.reload(); eq(Presentation.title("unknown_mod", 1), "Commodore");
        titleData.getJSONObject("factions").put("bad", new JSONArray(List.of("one","two")));
        boolean failed = false;
        try { Presentation.reload(); } catch (IllegalStateException e) { failed = e.getMessage().contains("factions.bad"); }
        yes(failed);
        titleData.getJSONObject("factions").remove("bad");
        neutralData.put("schemaVersion", 77); failed = false;
        try { Presentation.reload(); } catch (IllegalStateException e) { failed = e.getMessage().contains("schemaVersion"); }
        yes(failed);
    }
    static void expectFailure(String fragment) {
        try {
            Presentation.reload();
        } catch (IllegalStateException e) {
            if (e.getMessage().contains(fragment)) return;
            throw new AssertionError("Expected '" + fragment + "' in: " + e.getMessage());
        }
        throw new AssertionError("Expected a load failure containing '" + fragment + "'");
    }
    @SuppressWarnings("unchecked")
    static void profiles() throws Exception {
        FactionAPI heg = factions.get("hegemony"), pirates = factions.get("pirates"),
                diktat = factions.get("sindrian_diktat"), path = factions.get("luddic_path");
        // per-key inheritance: faction profile -> institutional profile -> default
        eq(Presentation.textFor(heg, "laws.award_fief"), "Grant command");
        eq(Presentation.textFor(diktat, "laws.current_marshal"), "Marshal of the Fleet:");
        eq(Presentation.lawName(diktat, Lawset.LawType.CROWN_AUTHORITY), "Chain of Command");
        eq(Presentation.textFor(path, "laws.current_marshal"), "Knight-Commander:");
        eq(Presentation.lawName(path, Lawset.LawType.TRADE_LAW), "Tithes");
        eq(Presentation.lawName(path, Lawset.LawType.FEAST_LAW), "Rave Licensing");
        eq(Presentation.textFor(pirates, "laws.days"), Presentation.text("laws.days"));
        eq(Presentation.textFor(factions.get("unknown_mod"), "laws.award_fief"), "Grant holding");
        eq(Presentation.textFor((FactionAPI) null, "laws.award_fief"), "Grant holding");
        // two factions back to back must not leak into each other (the old shared-enum problem)
        eq(Presentation.lawName(heg, Lawset.LawType.CROWN_AUTHORITY), "Chain of Command");
        eq(Presentation.lawName(pirates, Lawset.LawType.CROWN_AUTHORITY), "Boss's Word");
        eq(Presentation.lawName(heg, Lawset.LawType.CROWN_AUTHORITY), "Chain of Command");
        eq(Presentation.levelName(pirates, LawLevel.VERY_HIGH), "V. High");
        eq(Presentation.levelEffects(heg, Lawset.LawType.TRADE_LAW, 0), "Free Trade|-50% command revenue |+150% trade income");
        eq(Presentation.levelEffects(pirates, Lawset.LawType.FEAST_LAW, 4), "Banned|No raves");
        eq(Presentation.formatFor(heg, "holding.assign", "market", "Kestrel", "commander", "Ada"), "Grant Kestrel to Ada as a command");
        eq(Presentation.formatFor(pirates, "holding.revoke", "market", "Kestrel", "commander", "Ada"), "Take Kestrel back from Ada");
        // proposals use the deciding faction's words
        Lord admiral = person("admiral", "Ada", "Kane", "hegemony", 1);
        ((Map<String, Lord>) field(LordController.class, null, "lordsMap")).put("admiral", admiral);
        eq(new LawProposal(Lawset.LawType.AWARD_FIEF, "admiral", "speaker", "kestrel", null, 0).getSummary(),
                "Grant Kestrel $5\\Dock to Warlord Rook Tann as a command");
        eq(new LawProposal(Lawset.LawType.CROWN_AUTHORITY, "admiral", null, null, null, 1).getTitle(), "Decrease Chain of Command");
        // the player's own faction follows the setting; unknown names fall back to default
        Presentation.setPlayerProfile("corporate");
        eq(Presentation.textFor(factions.get("player"), "laws.award_fief"), "Grant concession");
        Presentation.setPlayerProfile("no_such_profile");
        eq(Presentation.textFor(factions.get("player"), "laws.award_fief"), "Grant holding");
        Presentation.setPlayerProfile("default");
        // titles: titles.json wins; profile ladders cover factions without an entry
        eq(Presentation.title("hegemony", 0), "Commodore");
        profileData.getJSONObject("factions").put("modded_corp", "corporate");
        Presentation.reload();
        eq(Presentation.title("modded_corp", 2), "Executive Vice President");
        eq(Presentation.textFor(factions.get("modded_corp"), "council.council"), " Board");
        // the League's own council over the aristocratic profile's High Council; the rest is inherited
        profileData.getJSONObject("factions").put("modded_house", "aristocratic");
        Presentation.reload();
        eq(Presentation.textFor("modded_house", "progression.requires_seat"), "You need a place on the High Council to propose this.");
        eq(Presentation.textFor("persean", "progression.requires_seat"), "You need a place on the League Council to propose this.");
        eq(Presentation.textFor("persean", "council.council"), " Council");
        eq(Presentation.textFor("persean", "laws.current_marshal"), "Strategos:");
        eq(Presentation.textFor("pirates", "event.campaign.heading"), "Ongoing armada");
    }
    static void profileValidation() throws Exception {
        JSONObject all = profileData.getJSONObject("profiles");
        all.put("loop_a", new JSONObject().put("extends", "loop_b"));
        all.put("loop_b", new JSONObject().put("extends", "loop_a"));
        expectFailure("inheritance cycle");
        all.remove("loop_a"); all.remove("loop_b");
        all.put("orphan", new JSONObject().put("extends", "missing"));
        expectFailure("profiles.orphan extends unknown profile missing");
        all.remove("orphan");
        all.put("typo", new JSONObject().put("messages", new JSONObject().put("laws.days", "sols")));
        expectFailure("profiles.typo.messages.laws.days is not an overridable message");
        all.put("typo", new JSONObject().put("messages", new JSONObject().put("holding.assign", "Grant {market} to {host.name}")));
        expectFailure("uses tokens its context doesn't supply: [{host.name}]");
        all.put("typo", new JSONObject().put("messages", new JSONObject().put("holding.assign", " ")));
        expectFailure("must be nonempty text");
        all.remove("typo");
        all.put("default", new JSONObject());
        expectFailure("cannot be redefined");
        all.remove("default");
        profileData.getJSONObject("factions").put("hegemony", "nonexistent");
        expectFailure("factions.hegemony names unknown profile nonexistent");
        profileData.getJSONObject("factions").put("hegemony", "naval");
        overridableData.getJSONArray("keys").put("no.such.message");
        expectFailure("lists unknown message no.such.message");
    }
    static void raves() throws Exception {
        RaveScenes.reload();
        // the Church's scene follows its Rave Licensing: 0 is Anything Goes, 3 Strict, 4 Banned
        yes(RaveScenes.arrival("luddic_church", 3).startsWith("A curate stands at the speaker stacks"));
        yes(RaveScenes.arrival("luddic_church", 0).startsWith("The icons are wearing glowsticks"));
        yes(RaveScenes.arrival("luddic_church", 4).startsWith("A masked acolyte"));
        yes(RaveScenes.arrival("luddic_church", 99).startsWith("A masked acolyte"));
        // a single text serves every level; unknown factions get the default venue
        eq(RaveScenes.arrival("hegemony", 0), RaveScenes.arrival("hegemony", 4));
        eq(RaveScenes.arrival("unknown_mod", 2), RaveScenes.arrival("default", 2));
        eq(RaveScenes.image("luddic_path"), "teahouse");
        eq(RaveScenes.image("unknown_mod"), "space_bar");
        eq(RaveScenes.image(null), "space_bar");
        // crowd lines: the default pool, plus a faction's own when it has some
        int shared = RaveScenes.crowd("default").size();
        yes(shared > 0);
        eq(RaveScenes.crowd("pirates").size(), shared);
        raveData.getJSONObject("factions").getJSONObject("pirates").put("crowd", new JSONArray(List.of("One more.")));
        RaveScenes.reload();
        eq(RaveScenes.crowd("pirates").size(), shared + 1);
        // a level that isn't one names its path
        raveData.getJSONObject("factions").getJSONObject("pirates").put("arrival", new JSONObject().put("loud", "x"));
        boolean failed = false;
        try { RaveScenes.reload(); } catch (IllegalStateException e) { failed = e.getMessage().contains("factions.pirates.arrival.loud"); }
        yes(failed);
    }
    @SuppressWarnings("unchecked")
    static void absentFactions() throws Exception {
        // a commander written for a faction mod that isn't installed is skipped at new game, not created
        JSONObject victor = json("data/lords/lords.json").getJSONObject("Victor Wallis");
        LordTemplate installed = new LordTemplate("Installed", new JSONObject(victor.toString()).put("faction", "naval"));
        LordTemplate absent = new LordTemplate("Absent", new JSONObject(victor.toString()).put("faction", "not_installed_mod"));
        set(LordController.class, null, "lordTemplates", new HashMap<>(Map.of("Installed", installed, "Absent", absent)));
        eq(LordController.templatesOfInstalledFactions(), List.of(installed));
        // faction.json settings for an absent faction are skipped instead of crashing game load
        legacyData.put("not_installed_mod", new JSONObject().put("startingPolicies", new JSONObject().put("trade_law", "HIGH")));
        starlords.util.factionUtils.FactionTemplateController.init();
        Map<String, ?> built = (Map<String, ?>) field(starlords.util.factionUtils.FactionTemplateController.class, null, "templates");
        yes(!built.containsKey("not_installed_mod"));
        yes(built.containsKey("hegemony"));
        // presentation data for an absent faction is inert: nothing asks for it, and asking falls back
        profileData.getJSONObject("factions").put("not_installed_mod", "corporate");
        titleData.getJSONObject("factions").put("not_installed_mod", new JSONArray(List.of("A", "B", "C")));
        raveData.getJSONObject("factions").put("not_installed_mod", new JSONObject().put("image", "space_bar").put("arrival", "x"));
        Presentation.reload(); RaveScenes.reload();
        eq(Presentation.textFor("hegemony", "laws.award_fief"), "Grant command");
    }
    static void raveDoor() throws Exception {
        commission = null;
        // a stranger to the host faction meets the bodyguard; nothing changes until they act
        LordEvent rave = new LordEvent(LordEvent.FEAST, host);
        yes(!RaveAccess.isInvited(rave));
        eq(RaveAccess.join(rave), RaveAccess.Outcome.AT_THE_DOOR);
        yes(!RaveAccess.isInside(rave));
        // sneaking either gets you in as a gatecrasher, or out for the rest of this rave
        for (int i = 0; i < 20; i++) {
            LordEvent attempt = new LordEvent(LordEvent.FEAST, host);
            RaveAccess.Outcome sneak = RaveAccess.sneak(attempt);
            if (sneak == RaveAccess.Outcome.SNUCK_IN) {
                yes(RaveAccess.isInside(attempt) && attempt.isGatecrasher());
                RaveAccess.Outcome again = RaveAccess.join(attempt);
                yes(again == RaveAccess.Outcome.ADMITTED || again == RaveAccess.Outcome.SPOTTED_INSIDE);
                if (again == RaveAccess.Outcome.SPOTTED_INSIDE) yes(!RaveAccess.isInside(attempt));
            } else {
                eq(sneak, RaveAccess.Outcome.CAUGHT_SNEAKING);
                yes(!RaveAccess.isInside(attempt));
                eq(RaveAccess.join(attempt), RaveAccess.Outcome.TURNED_AWAY);
                eq(RaveAccess.sneak(attempt), RaveAccess.Outcome.TURNED_AWAY);
            }
        }
        // your own faction's raves always let you in
        LordEvent own = new LordEvent(LordEvent.FEAST, player);
        yes(RaveAccess.isInvited(own));
        eq(RaveAccess.join(own), RaveAccess.Outcome.ADMITTED);
        yes(RaveAccess.isInside(own));
        eq(RaveAccess.join(own), RaveAccess.Outcome.ADMITTED);
    }
    /** Prints faction|tier0|tier1|tier2 for the Python test that compares with tools/sync_ranks.py. */
    static void ranks() throws Exception {
        Set<String> ids = new TreeSet<>();
        for (Iterator<?> it = titleData.getJSONObject("factions").keys(); it.hasNext();) ids.add((String) it.next());
        for (Iterator<?> it = profileData.getJSONObject("factions").keys(); it.hasNext();) ids.add((String) it.next());
        for (String id : ids)
            System.out.println("RANKS " + id + "|" + Presentation.title(id, 0) + "|" + Presentation.title(id, 1) + "|" + Presentation.title(id, 2));
    }
    /** Prints every overridable message as each bound faction sees it, for comparison with tools/preview_profiles.py. */
    static void messages() throws Exception {
        JSONObject out = new JSONObject();
        JSONArray keys = overridableData.getJSONArray("keys");
        for (Iterator<?> it = profileData.getJSONObject("factions").keys(); it.hasNext();) {
            String faction = (String) it.next();
            JSONObject resolved = new JSONObject();
            for (int i = 0; i < keys.length(); i++) resolved.put(keys.getString(i), Presentation.textFor(faction, keys.getString(i)));
            out.put(faction, resolved);
        }
        System.out.println("MESSAGES " + out);
    }
    public static void main(String[] args) throws Exception {
        root = Path.of(args[0]); setup();
        switch(args[1]) {
            case "renderer" -> renderer(); case "titles" -> titles(); case "context" -> context();
            case "proposals" -> proposals(); case "dialogue" -> dialogue(); case "validation" -> validation();
            case "profiles" -> profiles(); case "profile_validation" -> profileValidation(); case "ranks" -> ranks(); case "messages" -> messages(); case "raves" -> raves(); case "absent_factions" -> absentFactions(); case "rave_door" -> raveDoor();
            default -> throw new IllegalArgumentException(args[1]);
        }
        System.out.println("PASS " + args[1]);
    }
}
