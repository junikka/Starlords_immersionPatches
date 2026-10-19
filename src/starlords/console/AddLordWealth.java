package starlords.console;

import starlords.presentation.Presentation;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.controllers.LordController;
import starlords.person.Lord;

public class AddLordWealth implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        String[] argsArr = args.split(" ");
        if (argsArr.length != 2) {
            Console.showMessage(Presentation.text("add_lord_wealth.addlordwealth_requires_2_arguments"));
            return CommandResult.BAD_SYNTAX;
        }
        Lord lord = LordController.getLordByFirstName(argsArr[0]);
        if (lord == null) {
            lord = LordController.getLordById(argsArr[0]);
        }
        if (lord == null || lord.isPlayer()) {
            Console.showMessage(Presentation.text("add_lord_wealth.lord_not_found"));
            return CommandResult.ERROR;
        }

        int amount;
        try {
            amount = Integer.parseInt(argsArr[1]);
        } catch (Exception e) {
            Console.showMessage(Presentation.text("add_lord_wealth.could_not_parse_wealth_to_add"));
            return CommandResult.ERROR;
        }

        lord.addWealth(amount);

        if (amount > 0) {
            Console.showMessage(Presentation.text("add_lord_wealth.added") + amount + Presentation.text("add_lord_wealth.wealth_to") + lord.getLordAPI().getNameString());
        } else if (amount < 0) {
            Console.showMessage(Presentation.text("add_lord_wealth.removed") + amount + Presentation.text("add_lord_wealth.wealth_from") + lord.getLordAPI().getNameString());
        }
        return CommandResult.SUCCESS;
    }
}
