package starlords.console;

import starlords.presentation.Presentation;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.controllers.FiefController;
import starlords.controllers.LordController;
import starlords.controllers.RelationController;
import starlords.person.Lord;
import starlords.util.Utils;

public class AddLordFief implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        String[] argsArr = args.split(" ");
        if (argsArr.length != 2) {
            Console.showMessage(Presentation.text("add_lord_fief.addlordfief_requires_2_arguments"));
            return CommandResult.BAD_SYNTAX;
        }
        Lord lord = LordController.getLordByFirstName(argsArr[0]);
        if (lord == null) {
            lord = LordController.getLordOrPlayerById(argsArr[0]);
        }
        if (lord == null) {
            Console.showMessage(Presentation.text("add_lord_fief.lord_not_found"));
            return CommandResult.ERROR;
        }

        MarketAPI market = Global.getSector().getEconomy().getMarket(argsArr[1]);
        if (market == null) {
            Console.showMessage(Presentation.text("add_lord_fief.invalid_market_id_trying_name"));
        }

        market = Utils.getFactionMarket(argsArr[1], lord.getFaction().getId());
        if (market == null) {
            Console.showMessage(Presentation.text("add_lord_fief.invalid_market_id_or_name"));
            return CommandResult.ERROR;
        }

        if (!market.getFaction().equals(lord.getFaction())) {
            Console.showMessage(Presentation.text("add_lord_fief.cannot_grant_market_to_a_lord_of_a_different_faction"));
            return CommandResult.ERROR;
        }
        FiefController.setOwner(market, lord.getLordAPI().getId());
        Console.showMessage(Presentation.text("add_lord_fief.changed_owner_of") + market.getName() + Presentation.text("add_lord_fief.to") + lord.getLordAPI().getNameString());
        return CommandResult.SUCCESS;
    }
}
