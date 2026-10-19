package starlords.console;

import starlords.presentation.Presentation;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.controllers.LordController;
import starlords.person.Lord;
import starlords.util.Utils;

public class PrintLordTravel implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        String[] argsArr = args.split(" ");
        Lord lord = LordController.getLordByFirstName(argsArr[0]);
        if (lord == null) {
            lord = LordController.getLordOrPlayerById(argsArr[0]);
        }
        if (lord == null) {
            lord = LordController.getPlayerLord();
            Console.showMessage(Presentation.text("print_lord_travel.no_lord_provided_using_player"));
        }
        SectorEntityToken token;

        if (lord.isPlayer()) {
             token = lord.getFleet().getInteractionTarget();
        }
        else {
             token = lord.getTarget();
        }

        if (token == null) {
            Console.showMessage(Presentation.text("print_lord_travel.lord_has_no_current_target"));
            return CommandResult.ERROR;
        }

        float distanceLY = Misc.getDistanceLY(lord.getFleet(),token);
        float speed = lord.getFleet().getTravelSpeed();

        Console.showMessage(Presentation.text("print_lord_travel.lord") + lord.getLordAPI().getNameString()
                + Presentation.text("print_lord_travel.traveling_to") + token.getName() + " "
                + Presentation.text("print_lord_travel.distance") + distanceLY + " "
                + Presentation.text("print_lord_travel.speed") + speed + " "
                + Presentation.text("print_lord_travel.time_at_current_speed") + (distanceLY / speed) * 200 + Presentation.text("print_lord_travel.days")
                + Presentation.text("print_lord_travel.time_at_average_speed") + Utils.getTravelTime(lord.getFleet(),token) + Presentation.text("print_lord_travel.days"));
        return CommandResult.SUCCESS;
    }
}
