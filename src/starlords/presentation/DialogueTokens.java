package starlords.presentation;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import starlords.controllers.*;
import starlords.faction.LawProposal;
import starlords.person.*;
import starlords.util.*;
import java.util.*;

/** Live actor context. Legacy markers and new named tokens share the same values. */
public final class DialogueTokens {
    private DialogueTokens() {}

    public static Map<String, String> values(Lord speaker, Lord recipient, MarketAPI market) {
        Map<String, String> out = new LinkedHashMap<>();
        Lord player = LordController.getPlayerLord();
        FactionAPI destination = Utils.getRecruitmentFaction();
        person(out, "PLAYER_", "player", player, Global.getSector().getPlayerPerson(), destination);
        person(out, "LORD_", "speaker", speaker);
        person(out, "SECOND_LORD_", "recipient", recipient);
        person(out, "PLAYER_SPOUSE_", "playerPartner", partner(player));
        person(out, "LORD_SPOUSE_", "speakerPartner", partner(speaker));
        person(out, "SECOND_LORD_SPOUSE_", "recipientPartner", partner(recipient));
        LordEvent event = speaker == null ? null : EventController.getCurrentFeast(speaker.getFaction());
        Lord host = event == null ? null : event.getOriginator();
        Lord wedding = event == null ? null : event.getWeddingCeremonyTarget();
        person(out, "LORD_HOST_", "host", host);
        person(out, "LORD_HOST_SPOUSE_", "hostPartner", partner(host));
        person(out, "WEDDING_TARGET_", "weddingPartner", wedding);
        person(out, "WEDDING_TARGET_SPOUSE_", "weddingPartnerSpouse", partner(wedding));
        String marketName = market == null ? Presentation.text("context.market") : market.getName();
        out.put("%TARGET_MARKET_NAME", marketName);
        out.put("{market.name}", marketName);
        out.put("%TARGET_MARKET_FACTION", factionName(market == null ? null : market.getFaction()));
        out.put("%TARGET_MARKET_SYSTEM", market == null || market.getStarSystem() == null
                ? Presentation.text("context.location") : market.getStarSystem().getName());
        out.put("%TARGET_MARKET_SIZE", market == null ? "" : Integer.toString(market.getSize()));
        out.put("%TARGET_MARKET_STABILITY", market == null ? "" : Float.toString(market.getStabilityValue()));
        out.put("{destination.name}", factionName(destination));
        for (int i = 0; i < 3; i++) out.put("{destination.rank" + i + "}",
                Presentation.title(destination == null ? null : destination.getId(), i));
        return out;
    }

    private static Lord partner(Lord lord) {
        return lord == null || lord.getSpouse() == null ? null : LordController.getLordOrPlayerById(lord.getSpouse());
    }

    private static String factionName(FactionAPI faction) {
        return faction == null ? Presentation.text("context.faction") : faction.getDisplayName();
    }

    private static void person(Map<String, String> out, String prefix, String alias, Lord lord) {
        person(out, prefix, alias, lord, lord == null ? null : lord.getLordAPI(), lord == null ? null : lord.getFaction());
    }

    private static void person(Map<String, String> out, String prefix, String alias, Lord lord,
                               PersonAPI person, FactionAPI faction) {
        String name = person == null ? Presentation.text("context.person") : person.getNameString();
        String title = Presentation.title(faction == null ? null : faction.getId(), lord == null ? 0 : lord.getRanking());
        out.put("%" + prefix + "NAME", name);
        out.put("%" + prefix + "NAME_FIRST", person == null ? name : person.getName().getFirst());
        out.put("%" + prefix + "NAME_LAST", person == null ? name : person.getName().getLast());
        out.put("%" + prefix + "TITLE", title);
        out.put("%" + prefix + "FACTION_NAME", factionName(faction));
        FactionAPI starting = lord == null || lord.getTemplate() == null ? null
                : Global.getSector().getFaction(lord.getTemplate().factionId);
        out.put("%" + prefix + "STARTING_FACTION_NAME", factionName(starting));
        out.put("{" + alias + ".fullName}", name);
        out.put("{" + alias + ".formalName}", title + " " + name);
        out.put("{" + alias + ".title}", title);
        out.put("{" + alias + ".factionName}", factionName(faction));
        out.put("%" + prefix + "GENDER_MAN_OR_WOMEN", person == null ? "person" : person.getManOrWoman());
        out.put("%" + prefix + "GENDER_HE_OR_SHE", person == null ? "they" : person.getHeOrShe());
        out.put("%" + prefix + "GENDER_HIM_OR_HER", person == null ? "them" : person.getHimOrHer());
        out.put("%" + prefix + "GENDER_HIS_OR_HER", person == null ? "their" : person.getHisOrHer());
        out.put("%" + prefix + "GENDER_SUIT_OR_DRESS", lord == null ? Presentation.text("context.formalWear") : lord.getFormalWear());
        out.put("%" + prefix + "GENDER_SIR_OR_MAAM", person == null ? title : GenderUtils.sirOrMaam(person, false));
        out.put("%" + prefix + "GENDER_NAME", person == null ? "ANY" : person.getGender().name());
        out.put("%" + prefix + "GENDER_HUSBAND_OR_WIFE", person == null ? "partner" : GenderUtils.husbandOrWife(person, false));
        CampaignFleetAPI fleet = lord == null ? null : lord.getFleet();
        FleetMemberAPI ship = flownShip(person, fleet);
        out.put("%" + prefix + "FLAGSHIP_HULLNAME", ship == null ? Presentation.text("context.ship") : ship.getHullSpec().getHullName());
        out.put("%" + prefix + "FLAGSHIP_NAME", ship == null ? Presentation.text("context.ship") : ship.getShipName());
        LawProposal proposal = lord == null ? null : PoliticsController.getProposal(lord);
        out.put("%" + prefix + "PROPOSAL_NAME", proposal == null ? Presentation.text("context.proposal") : proposal.getTitle());
        String location = fleet == null ? null : Utils.getNearbyDescription(fleet);
        out.put("%" + prefix + "FLEET_LOCATION", location == null ? Presentation.text("context.location") : location);
        String leader = lord == null ? null : lord.getLiegeName();
        // A faction may be governed by an institution without a named person.
        out.put("%" + prefix + "LIEGE_NAME", leader == null || leader.trim().isEmpty()
                ? Presentation.text("context.leadership") : leader);
        for (int i = 0; i < 3; i++) out.put("%" + prefix + "FACTION_RANK_TITLE" + i,
                Presentation.title(faction == null ? null : faction.getId(), i));
    }

    private static FleetMemberAPI flownShip(PersonAPI person, CampaignFleetAPI fleet) {
        if (person == null || fleet == null) return null;
        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy())
            if (ship.getCaptain() != null && person.getId().equals(ship.getCaptain().getId())) return ship;
        return null;
    }
}
