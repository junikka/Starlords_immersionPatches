package starlords.console;

import starlords.presentation.Presentation;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.util.Utils;

public class PrintLordsPrisonerProblems implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {

        Utils.printLordsWithPrisonerProblems(false, true);

        Console.showMessage(Presentation.text("print_lords_prisoner_problems.printed_prisoner_issues_to_log"));
        return CommandResult.SUCCESS;
    }
}
