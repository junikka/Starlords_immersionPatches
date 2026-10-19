package starlords.console;

import starlords.presentation.Presentation;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import starlords.controllers.LordController;
import starlords.controllers.RequestController;
import starlords.person.Lord;
import starlords.person.LordRequest;

public class AddLordRequest implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        String[] argsArr = args.split(" ");
        if (argsArr.length != 2) {
            Console.showMessage(Presentation.text("add_lord_request.addlordrequest_requires_2_arguments"));
            return CommandResult.BAD_SYNTAX;
        }
        Lord lord = LordController.getLordById(argsArr[0]);
        if (lord == null || lord.isPlayer()) {
            Console.showMessage(Presentation.text("add_lord_request.lord_not_found"));
            return CommandResult.ERROR;
        }

        String request_type = String.valueOf(argsArr[1]);;
        switch (request_type){
            case LordRequest.PRISON_BREAK:
                RequestController.addRequest(new LordRequest(LordRequest.PRISON_BREAK, lord));
                Console.showMessage(Presentation.text("add_lord_request.added") + request_type + Presentation.text("add_lord_request.request_to") + lord.getLordAPI().getNameString());
                return CommandResult.SUCCESS;
            case LordRequest.FIEF_FOR_DEFECTION:
                RequestController.addRequest(new LordRequest(LordRequest.FIEF_FOR_DEFECTION, lord));
                Console.showMessage(Presentation.text("add_lord_request.added") + request_type + Presentation.text("add_lord_request.request_to") + lord.getLordAPI().getNameString());
                return CommandResult.SUCCESS;
            default:
                Console.showMessage(Presentation.text("add_lord_request.incorrect_request_type"));
                return CommandResult.ERROR;
        }


    }
}
