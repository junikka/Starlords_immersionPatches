package starlords.console;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.controllers.LordController;
import starlords.controllers.Weddings;
import starlords.person.Lord;
import starlords.presentation.Presentation;

/** Testing: skip the courtship and get engaged to a commander, as if they had accepted a proposal. */
public class EngageLord implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage(Presentation.text("start_rave.campaign_only"));
            return CommandResult.WRONG_CONTEXT;
        }
        String name = args.trim();
        if (name.isEmpty()) return CommandResult.BAD_SYNTAX;
        Lord lord = LordController.getLordByFirstName(name);
        if (lord == null) lord = LordController.getLordById(name);
        if (lord == null || lord.isPlayer()) {
            Console.showMessage(Presentation.text("engage_lord.not_found"));
            return CommandResult.ERROR;
        }
        if (lord.isMarried() || LordController.getPlayerLord().isMarried()) {
            Console.showMessage(Presentation.text("engage_lord.married"));
            return CommandResult.ERROR;
        }
        Weddings.engage(lord, null);
        if (Weddings.venue() == null) {
            Console.showMessage(Presentation.text("engage_lord.no_venue"));
            return CommandResult.ERROR;
        }
        Console.showMessage(Presentation.format("engage_lord.done",
                "commander", lord.getLordAPI().getNameString(), "venue", Weddings.venue().getName()));
        return CommandResult.SUCCESS;
    }
}
