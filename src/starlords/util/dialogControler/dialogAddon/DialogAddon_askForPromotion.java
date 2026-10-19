package starlords.util.dialogControler.dialogAddon;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import starlords.controllers.PlayerProgression;
import starlords.person.Lord;
import starlords.presentation.Presentation;
import starlords.util.dialogControler.DialogSet;

import java.util.List;

// the player asks this commander to put them forward for promotion; see PlayerProgression.askForSponsor
public class DialogAddon_askForPromotion extends DialogAddon_Base {
    public DialogAddon_askForPromotion(){

    }

    @Override
    public void apply(TextPanelAPI textPanel, OptionPanelAPI options, InteractionDialogAPI dialog, Lord lord, Lord targetLord, MarketAPI targetMarket) {
        List<String> missing = PlayerProgression.getMissingRequirements();
        String answer = PlayerProgression.askForSponsor(lord);
        DialogSet.addParaWithInserts(answer, lord, targetLord, targetMarket, textPanel, options, dialog);
        if (answer.equals("promotion_not_ready") && !missing.isEmpty()) {
            textPanel.addPara(Presentation.format("progression.next_step",
                    "requirements", PlayerProgression.joinRequirements(missing)));
        }
    }
}
