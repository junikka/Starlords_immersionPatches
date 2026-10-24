package starlords.util.dialogControler.dialogAddon;

import starlords.controllers.Weddings;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import starlords.person.Lord;

public class DialogAddon_startWedding extends DialogAddon_Base{
    @Override
    public void apply(TextPanelAPI textPanel, OptionPanelAPI options, InteractionDialogAPI dialog, Lord lord) {
        // the wedding is held at a venue named by the fiance, not at a rave (see Weddings)
        Weddings.engage(lord, textPanel);
    }
}
