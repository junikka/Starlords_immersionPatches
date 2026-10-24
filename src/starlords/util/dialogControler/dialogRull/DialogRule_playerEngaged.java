package starlords.util.dialogControler.dialogRull;

import starlords.controllers.Weddings;
import starlords.person.Lord;

/** Whether the player is engaged to a commander and the wedding hasn't happened yet (see Weddings). */
public class DialogRule_playerEngaged extends DialogRule_Base {
    boolean engaged;
    public DialogRule_playerEngaged(boolean engaged){
        this.engaged = engaged;
    }

    @Override
    public boolean condition(Lord lord) {
        return engaged == Weddings.isEngaged();
    }
}
