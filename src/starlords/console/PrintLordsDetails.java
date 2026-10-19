package starlords.console;

import starlords.presentation.Presentation;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.util.Utils;

public class PrintLordsDetails implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {

        Utils.printLordsDetails();

        Console.showMessage(Presentation.text("print_lords_details.print_full_lord_list_details_to_log"));
        return CommandResult.SUCCESS;
    }
}
