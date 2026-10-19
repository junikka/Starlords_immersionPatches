package starlords.console;

import starlords.presentation.Presentation;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.controllers.LordController;
import starlords.person.Lord;
import starlords.util.DefectionUtils;
import starlords.util.Utils;

public class PrintLordsDefectionWeights implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {

        for (Lord lord : LordController.getLordsList())
            DefectionUtils.getLordPreferredFaction(lord,true);

        Console.showMessage(Presentation.text("print_lords_defection_weights.print_full_lord_list_defection_weights_to_log"));
        return CommandResult.SUCCESS;

    }
}
