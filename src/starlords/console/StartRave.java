package starlords.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.ai.LordAI;
import starlords.controllers.EventController;
import starlords.controllers.LordController;
import starlords.person.Lord;
import starlords.person.LordAction;
import starlords.person.LordEvent;
import starlords.presentation.Presentation;
import starlords.util.Utils;

/** Testing: a commander throws a rave now, ignoring Rave Licensing, the cooldown and the cost. */
public class StartRave implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage(Presentation.text("start_rave.campaign_only"));
            return CommandResult.WRONG_CONTEXT;
        }
        String name = args.trim();
        Lord lord = name.isEmpty() ? nearestCommander() : LordController.getLordByFirstName(name);
        if (lord == null && !name.isEmpty()) lord = LordController.getLordById(name);
        if (lord == null || lord.isPlayer() || lord.getFleet() == null || !lord.getFleet().isAlive()) {
            Console.showMessage(Presentation.text("start_rave.no_commander"));
            return CommandResult.ERROR;
        }
        LordEvent current = EventController.getCurrentFeast(lord.getFaction());
        if (current != null) {
            Console.showMessage(Presentation.format("start_rave.already_raving",
                    "faction", lord.getFaction().getDisplayName(), "location", current.getTarget().getName()));
            return CommandResult.ERROR;
        }
        // the nearest friendly world, not the commander's own holding, so the rave happens close by
        SectorEntityToken venue = lord.getClosestBase(false);
        if (venue == null || venue.getMarket() == null) {
            Console.showMessage(Presentation.text("start_rave.no_venue"));
            return CommandResult.ERROR;
        }
        // same steps as LordAI's FEAST_TRANSIT, minus the licensing, cooldown and wealth checks
        EventController.removeFromAllEvents(lord);
        EventController.addFeast(new LordEvent(LordEvent.FEAST, lord, venue));
        lord.setFeastInteracted(false);
        LordAI.beginAssignment(lord, LordAction.FEAST_TRANSIT, venue, null, false);
        Console.showMessage(Presentation.format("start_rave.started",
                "commander", lord.getLordAPI().getNameString(), "location", venue.getName()));
        return CommandResult.SUCCESS;
    }

    // a commander in your own star system wins; otherwise the closest one through hyperspace
    private static Lord nearestCommander() {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        Lord best = null;
        boolean bestLocal = false;
        float bestDistance = Float.MAX_VALUE;
        for (Lord lord : LordController.getLordsList()) {
            CampaignFleetAPI fleet = lord.getFleet();
            if (lord.isPlayer() || fleet == null || !fleet.isAlive()) continue;
            boolean local = fleet.getContainingLocation() == player.getContainingLocation();
            float distance = local ? Misc.getDistance(fleet, player) : Utils.getHyperspaceDistance(fleet, player);
            if (local && !bestLocal || local == bestLocal && distance < bestDistance) {
                best = lord;
                bestLocal = local;
                bestDistance = distance;
            }
        }
        return best;
    }
}
