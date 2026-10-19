package starlords.console;

import starlords.presentation.Presentation;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.controllers.LordController;
import starlords.controllers.RelationController;
import starlords.person.Lord;

public class AddLordLoyalty implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        String[] argsArr = args.split(" ");
        if (argsArr.length != 2) {
            Console.showMessage(Presentation.text("add_lord_loyalty.addlordloyalty_requires_2_arguments"));
            return CommandResult.BAD_SYNTAX;
        }
        Lord lord = LordController.getLordByFirstName(argsArr[0]);
        if (lord == null) {
            lord = LordController.getLordById(argsArr[0]);
        }
        if (lord == null || lord.isPlayer()) {
            Console.showMessage(Presentation.text("add_lord_loyalty.lord_not_found"));
            return CommandResult.ERROR;
        }

        int amount;
        try {
            amount = Integer.parseInt(argsArr[1]);
        } catch (Exception e) {
            Console.showMessage(Presentation.text("add_lord_loyalty.could_not_parse_loyalty_to_add"));
            return CommandResult.ERROR;
        }

        RelationController.modifyLoyalty(lord, amount);
        Console.showMessage(Presentation.text("add_lord_loyalty.faction_loyalty_of") + lord.getLordAPI().getNameString()
                + Presentation.text("add_lord_loyalty.is_now") + RelationController.getLoyalty(lord));
        return CommandResult.SUCCESS;
    }
}
