package starlords.console;

import starlords.presentation.Presentation;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.controllers.FiefController;
import starlords.controllers.LordController;
import starlords.person.Lord;

public class RemoveLordFief implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        String[] argsArr = args.split(" ");
        if (argsArr.length != 2) {
            Console.showMessage(Presentation.text("remove_lord_fief.removelordfief_requires_2_arguments"));
            return CommandResult.BAD_SYNTAX;
        }
        Lord lord = LordController.getLordByFirstName(argsArr[0]);
        if (lord == null) {
            lord = LordController.getLordOrPlayerById(argsArr[0]);
        }
        if (lord == null) {
            Console.showMessage(Presentation.text("remove_lord_fief.lord_not_found"));
            return CommandResult.ERROR;
        }

        MarketAPI market = Global.getSector().getEconomy().getMarket(argsArr[1]);
        if (market == null) {
            Console.showMessage(Presentation.text("remove_lord_fief.invalid_market_id"));
            return CommandResult.ERROR;
        }
        if (!lord.equals(FiefController.getOwner(market))) {
            Console.showMessage(lord.getLordAPI().getNameString() + Presentation.text("remove_lord_fief.already_doesn_t_own") + market.getName());
            return CommandResult.SUCCESS;
        }
        FiefController.setOwner(market, null);
        Console.showMessage(Presentation.text("remove_lord_fief.revoked") + market.getName() + Presentation.text("remove_lord_fief.from") + lord.getLordAPI().getNameString());
        return CommandResult.SUCCESS;
    }
}
