package starlords.console;

import starlords.presentation.Presentation;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.ai.LordAI;
import starlords.controllers.EventController;
import starlords.controllers.LordController;
import starlords.person.Lord;
import starlords.person.LordAction;
import starlords.person.LordEvent;

public class AddLordRaid implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        String[] argsArr = args.split(" ");
        if (argsArr.length != 2) {
            Console.showMessage(Presentation.text("add_lord_raid.addlordraid_requires_2_arguments"));
            return CommandResult.BAD_SYNTAX;
        }
        Lord lord = LordController.getLordOrPlayerById(argsArr[0]);
        if (lord == null) {
            lord = LordController.getLordByFirstName(argsArr[0]);
        }
        if (lord == null || lord.isPlayer()) {
            Console.showMessage(Presentation.text("add_lord_raid.lord_not_found"));
            return CommandResult.ERROR;
        }

        String marketName = argsArr[1];

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy())
            if (market.getName().equalsIgnoreCase(marketName)) {
                LordAI.beginAssignment(lord, LordAction.RAID_TRANSIT, market.getPrimaryEntity(),null, false);
                Console.showMessage(Presentation.text("add_lord_raid.added_raid_for_market") + marketName + Presentation.text("add_lord_raid.with_originator_lord") + lord.getLordAPI().getNameString());
                return CommandResult.SUCCESS;
            }

        Console.showMessage(Presentation.text("add_lord_raid.market_not_found"));
        return CommandResult.ERROR;
    }
}
