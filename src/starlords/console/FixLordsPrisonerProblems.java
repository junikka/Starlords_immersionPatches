package starlords.console;

import starlords.presentation.Presentation;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.util.Utils;

public class FixLordsPrisonerProblems implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {

        Utils.printLordsWithPrisonerProblems(true, false);

        Console.showMessage(Presentation.text("fix_lords_prisoner_problems.fixed_prisoner_problems"));
        return CommandResult.SUCCESS;
    }
}
