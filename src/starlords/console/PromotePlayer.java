package starlords.console;

import starlords.presentation.Presentation;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.controllers.LordController;
import starlords.controllers.PlayerProgression;
import starlords.controllers.PoliticsController;

import java.util.List;

public class PromotePlayer implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (PlayerProgression.getFaction() == null) {
            Console.showMessage(Presentation.text("progression.no_commission"));
            return CommandResult.ERROR;
        }
        String arg = args.trim().toLowerCase();
        if (arg.equals("vote")) {
            // starts a real promotion vote, skipping the requirements but not the council
            Console.showMessage(PlayerProgression.sponsorPromotion(true));
        } else if (!arg.equals("status")) {
            int standing;
            if (arg.isEmpty()) {
                standing = Math.min(2, PlayerProgression.getStanding() + 1);
            } else if (arg.equals("commissioned")) {
                standing = PlayerProgression.COMMISSIONED;
            } else {
                try {
                    standing = Integer.parseInt(arg);
                } catch (NumberFormatException e) {
                    standing = -2;
                }
                if (standing < 0 || standing > 2) {
                    Console.showMessage(Presentation.text("promote_player.usage"));
                    return CommandResult.BAD_SYNTAX;
                }
            }
            PlayerProgression.setStanding(standing);
        }
        Console.showMessage(Presentation.format("promote_player.status",
                "faction", PlayerProgression.getFaction().getDisplayName(),
                "title", PlayerProgression.getStandingTitle(),
                "battles", Integer.toString(PlayerProgression.getJointVictories())));
        String suspension = PlayerProgression.getSuspensionText();
        if (!suspension.isEmpty()) Console.showMessage(suspension);
        List<String> missing = PlayerProgression.getMissingRequirements();
        if (PoliticsController.hasPendingPromotion(LordController.getPlayerLord())) {
            Console.showMessage(Presentation.text("progression.no_vote_pending"));
        } else if (PlayerProgression.getStanding() >= 2) {
            Console.showMessage(Presentation.text("promote_player.top"));
        } else if (missing.isEmpty()) {
            Console.showMessage(Presentation.text("promote_player.ready"));
        } else {
            for (String requirement : missing) Console.showMessage("  " + requirement);
        }
        return CommandResult.SUCCESS;
    }
}
