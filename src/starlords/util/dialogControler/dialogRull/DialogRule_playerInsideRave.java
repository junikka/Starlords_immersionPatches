package starlords.util.dialogControler.dialogRull;

import starlords.controllers.RaveAccess;
import starlords.person.Lord;

/** Whether the player is inside the rave this commander is at (see RaveAccess). */
public class DialogRule_playerInsideRave extends DialogRule_Base {
    boolean inside;
    public DialogRule_playerInsideRave(boolean inside){
        this.inside = inside;
    }

    @Override
    public boolean condition(Lord lord) {
        return inside == RaveAccess.isInside(RaveAccess.raveOf(lord));
    }
}
