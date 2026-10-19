package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import starlords.controllers.LordController;
import starlords.controllers.PlayerProgression;
import starlords.presentation.FleetGreetings;
import starlords.util.Utils;

import java.util.List;
import java.util.Map;

// Fleets of the faction whose council the player sits on greet them by rank (data/campaign/rules.csv).
// "StarlordsRankGreeting applies" is the rule condition; "StarlordsRankGreeting print" adds the greeting.
public class StarlordsRankGreeting extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || !(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
        CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
        FactionAPI faction = PlayerProgression.getFaction();
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (faction == null || player == null || !player.isTransponderOn()) return false;
        if (!faction.equals(fleet.getFaction()) || fleet.isHostileTo(player)) return false;
        // commanders have their own conversation
        if (fleet.getCommander() != null && LordController.getLordById(fleet.getCommander().getId()) != null) return false;
        // the council's honours follow the reputation the rank requires, not just the rank
        int tier = PlayerProgression.getEffectiveStanding();
        if (tier < 0) return false;

        String mode = params.isEmpty() ? "applies" : params.get(0).getString(memoryMap);
        if (mode.equals("print")) {
            dialog.getTextPanel().addPara(FleetGreetings.greeting(faction.getId(), tier, Utils.getTitle(faction, tier)));
        }
        return true;
    }
}
