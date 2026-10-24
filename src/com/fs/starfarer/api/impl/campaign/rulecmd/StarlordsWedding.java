package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import starlords.controllers.Weddings;
import starlords.person.Lord;
import starlords.presentation.Presentation;

import java.util.List;
import java.util.Map;

/**
 * The wedding on the venue's market menu (rules.csv starlords_weddingOpt / starlords_weddingSel).
 * "here": true at the engagement's venue; "option": adds the menu option; "hold": the ceremony.
 */
public class StarlordsWedding extends BaseCommandPlugin {
    public static final String OPTION = "starlords_wedding";

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || dialog.getInteractionTarget() == null) return false;
        MarketAPI market = dialog.getInteractionTarget().getMarket();
        if (!Weddings.isVenue(market)) return false;
        String mode = params.isEmpty() ? "here" : params.get(0).getString(memoryMap);
        switch (mode) {
            case "option":
                Lord fiance = Weddings.fiance();
                dialog.getOptionPanel().addOption(Presentation.format("wedding.option",
                        "commander", fiance.getLordAPI().getNameString()), OPTION);
                return true;
            case "hold":
                Weddings.hold(dialog);
                return true;
            default:
                return true;
        }
    }
}
