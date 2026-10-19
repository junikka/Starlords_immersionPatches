package starlords.console;

import starlords.presentation.Presentation;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.controllers.LordController;
import starlords.person.Lord;

public class AddLordRelation implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        String[] argsArr = args.split(" ");
        if (argsArr.length != 2) {
            Console.showMessage(Presentation.text("add_lord_relation.addlordrelation_requires_2_arguments"));
            return CommandResult.BAD_SYNTAX;
        }
        Lord lord = LordController.getLordByFirstName(argsArr[0]);
        if (lord == null) {
            lord = LordController.getLordById(argsArr[0]);
        }
        if (lord == null || lord.isPlayer()) {
            Console.showMessage(Presentation.text("add_lord_relation.lord_not_found"));
            return CommandResult.ERROR;
        }

        int amount;
        try {
            amount = Integer.parseInt(argsArr[1]);
        } catch (Exception e) {
            Console.showMessage(Presentation.text("add_lord_relation.could_not_parse_relations_to_add"));
            return CommandResult.ERROR;
        }

        lord.getLordAPI().getRelToPlayer().adjustRelationship(
                (float) amount / 100, null);
        Console.showMessage(Presentation.text("add_lord_relation.player_relation_with") + lord.getLordAPI().getNameString()
                + Presentation.text("add_lord_relation.is_now") + lord.getLordAPI().getRelToPlayer().getRepInt());
        return CommandResult.SUCCESS;
    }
}
