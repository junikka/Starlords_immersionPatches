package starlords.console;

import starlords.presentation.Presentation;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.util.Utils;

public class PrintLordsRequests implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {

        Utils.printRequests();

        Console.showMessage(Presentation.text("print_lords_requests.print_existing_lord_requests_to_log"));
        return CommandResult.SUCCESS;
    }
}
