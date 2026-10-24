package starlords.controllers;

import com.fs.starfarer.api.campaign.FactionAPI;
import starlords.person.Lord;
import starlords.person.LordAction;
import starlords.person.LordEvent;
import starlords.util.Utils;

/**
 * Who gets past the door of a rave. Raves are for the host faction's elite: the player needs a seat on that
 * faction's council (any standing, as long as their reputation still honours it) or must be its ruler. Anyone
 * else meets the bodyguard and can try to slip in, at the risk of being thrown out, now or later.
 * The state lives on the rave (LordEvent), so it lasts exactly as long as that rave.
 */
public final class RaveAccess {
    public static final float SNEAK_CHANCE = 0.5f;
    /** Each time a gatecrasher re-joins the party, the chance a steward recognises them. */
    public static final float SPOTTED_CHANCE = 0.25f;

    public enum Outcome { ADMITTED, AT_THE_DOOR, TURNED_AWAY, SNUCK_IN, CAUGHT_SNEAKING, SPOTTED_INSIDE }

    private RaveAccess() {}

    /** The rave this commander is at, or null. */
    public static LordEvent raveOf(Lord lord) {
        if (lord == null || lord.getCurrAction() != LordAction.FEAST) return null;
        return EventController.getCurrentFeast(lord.getLordAPI().getFaction());
    }

    public static boolean isInside(LordEvent rave) {
        return rave != null && rave.isPlayerInside() && !rave.isThrownOut();
    }

    /** A seat on the host faction's council, or rule over it. */
    public static boolean isInvited(LordEvent rave) {
        FactionAPI host = rave == null ? null : rave.getFaction();
        if (host == null) return false;
        if (host.isPlayerFaction()) return true;
        FactionAPI council = PlayerProgression.getFaction();
        return council != null && council.getId().equals(host.getId())
                && PlayerProgression.getEffectiveStanding() >= 0;
    }

    /** "Join the rave." */
    public static Outcome join(LordEvent rave) {
        if (rave.isThrownOut()) return Outcome.TURNED_AWAY;
        if (rave.isPlayerInside()) {
            if (rave.isGatecrasher() && !isInvited(rave) && roll(SPOTTED_CHANCE)) {
                throwOut(rave);
                return Outcome.SPOTTED_INSIDE;
            }
            return Outcome.ADMITTED;
        }
        if (!isInvited(rave)) return Outcome.AT_THE_DOOR;
        rave.setPlayerInside(true);
        rave.setGatecrasher(false);
        return Outcome.ADMITTED;
    }

    /** "Slip in anyway." */
    public static Outcome sneak(LordEvent rave) {
        if (rave.isThrownOut()) return Outcome.TURNED_AWAY;
        if (!roll(SNEAK_CHANCE)) {
            throwOut(rave);
            return Outcome.CAUGHT_SNEAKING;
        }
        rave.setPlayerInside(true);
        rave.setGatecrasher(true);
        return Outcome.SNUCK_IN;
    }

    private static void throwOut(LordEvent rave) {
        rave.setPlayerInside(false);
        rave.setThrownOut(true);
    }

    private static boolean roll(float chance) {
        return Utils.rand.nextFloat() < chance;
    }
}
