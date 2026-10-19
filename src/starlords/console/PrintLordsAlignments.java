package starlords.console;

import starlords.presentation.Presentation;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.util.Utils;

public class PrintLordsAlignments implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {


        Utils.printLordsAlignments();
        Console.showMessage(Presentation.text("print_lords_alignments.print_full_lord_list_alignments_to_log"));
        return CommandResult.SUCCESS;



    }
}
