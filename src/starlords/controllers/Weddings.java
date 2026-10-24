package starlords.controllers;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import starlords.person.Lord;
import starlords.presentation.Presentation;
import starlords.util.dialogControler.dialogAddon.DialogAddon_wedPlayerToLord;

import java.util.HashMap;
import java.util.Map;

/**
 * Weddings happen at a venue, not at a rave. Accepting a proposal makes the player engaged; the fiance names a
 * venue (their own holding, else their faction's largest world, else the largest world friendly to the player),
 * and the ceremony is attended from that market's own menu (rules.csv: starlords_weddingOpt, StarlordsWedding).
 */
public final class Weddings {
    private static final String DATA_KEY = "starlords_engagement";

    private Weddings() {}

    @SuppressWarnings("unchecked")
    private static Map<String, String> data() {
        Map<String, Object> persistent = Global.getSector().getPersistentData();
        Object data = persistent.get(DATA_KEY);
        if (!(data instanceof Map)) {
            data = new HashMap<String, String>();
            persistent.put(DATA_KEY, data);
        }
        return (Map<String, String>) data;
    }

    /** Accepting a proposal: records the engagement and tells the player where the ceremony will be. */
    public static void engage(Lord fiance, TextPanelAPI text) {
        MarketAPI venue = chooseVenue(fiance);
        Map<String, String> data = data();
        data.put("fiance", fiance.getLordAPI().getId());
        if (venue == null) {
            data.remove("venue");
            return;
        }
        data.put("venue", venue.getId());
        String message = Presentation.format("wedding.engaged",
                "commander", fiance.getLordAPI().getNameString(), "venue", venue.getName());
        if (text != null) text.addPara(message);
        Global.getSector().getCampaignUI().addMessage(message, fiance.getFaction().getBaseUIColor());
    }

    /** The fiance, while the engagement still stands; ends it (with a message) if it can no longer happen. */
    public static Lord fiance() {
        Map<String, String> data = data();
        String id = data.get("fiance");
        if (id == null) return null;
        Lord fiance = LordController.getLordById(id);
        Lord player = LordController.getPlayerLord();
        if (fiance == null || fiance.isMarried() || (player != null && player.isMarried())) {
            data.clear();
            if (fiance != null) Global.getSector().getCampaignUI().addMessage(
                    Presentation.format("wedding.cancelled", "commander", fiance.getLordAPI().getNameString()));
            return null;
        }
        return fiance;
    }

    public static boolean isEngaged() {
        return fiance() != null;
    }

    /** The venue market, picked again if the old one no longer exists or changed hands. */
    public static MarketAPI venue() {
        Lord fiance = fiance();
        if (fiance == null) return null;
        Map<String, String> data = data();
        MarketAPI venue = data.get("venue") == null ? null : Global.getSector().getEconomy().getMarket(data.get("venue"));
        if (venue == null || venue.isHidden() || venue.getFaction().isHostileTo(Global.getSector().getPlayerFaction())) {
            venue = chooseVenue(fiance);
            if (venue != null) data.put("venue", venue.getId());
        }
        return venue;
    }

    public static boolean isVenue(MarketAPI market) {
        MarketAPI venue = market == null ? null : venue();
        return venue != null && venue.getId().equals(market.getId());
    }

    /** The ceremony, from the venue's market menu. */
    public static void hold(InteractionDialogAPI dialog) {
        Lord fiance = fiance();
        MarketAPI venue = venue();
        if (fiance == null || venue == null) return;
        dialog.getTextPanel().addPara(Presentation.format("wedding.ceremony",
                "commander", fiance.getLordAPI().getNameString(), "venue", venue.getName()));
        new DialogAddon_wedPlayerToLord(true).apply(dialog.getTextPanel(), dialog.getOptionPanel(), dialog, fiance);
        data().clear();
        Global.getSector().getCampaignUI().addMessage(Presentation.format("wedding.married",
                "commander", fiance.getLordAPI().getNameString()), fiance.getFaction().getBaseUIColor());
    }

    // their own holding, else their faction's largest world, else the largest world friendly to the player
    static MarketAPI chooseVenue(Lord fiance) {
        FactionAPI player = Global.getSector().getPlayerFaction();
        for (SectorEntityToken fief : fiance.getFiefs()) {
            MarketAPI market = fief.getMarket();
            if (usable(market, player)) return market;
        }
        MarketAPI best = null;
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!market.getFaction().equals(fiance.getFaction()) || !usable(market, player)) continue;
            if (best == null || market.getSize() > best.getSize()) best = market;
        }
        if (best != null) return best;
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!usable(market, player)) continue;
            if (best == null || market.getSize() > best.getSize()) best = market;
        }
        return best;
    }

    private static boolean usable(MarketAPI market, FactionAPI player) {
        return market != null && !market.isHidden() && market.getPrimaryEntity() != null
                && !market.getFaction().isHostileTo(player);
    }
}
