package starlords.util.dialogControler.dialogRull;

import starlords.controllers.PlayerProgression;
import starlords.person.Lord;

// true while the player holds a council seat in a commissioning faction and can still be promoted
public class DialogRule_playerCanSeekPromotion extends DialogRule_Base {
    boolean value;
    public DialogRule_playerCanSeekPromotion(boolean value){
        this.value = value;
    }

    @Override
    public boolean condition(Lord lord) {
        return value == PlayerProgression.canSeekPromotion();
    }
}
