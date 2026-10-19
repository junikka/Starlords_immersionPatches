package starlords.faction;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import starlords.controllers.LordController;
import starlords.controllers.PlayerProgression;
import starlords.controllers.PoliticsController;
import lombok.Getter;
import lombok.Setter;
import starlords.person.Lord;
import starlords.util.Utils;
import starlords.presentation.Presentation;

import java.util.ArrayList;
import java.util.HashSet;

@Getter
public class LawProposal {

    public final String originator; // lord id
    public final String targetLord;
    public final String targetFief;
    public final String targetFaction;
    public final int targetLevel; // target law level or rank
    public final Lawset.LawType law;
    public final long creationTimestamp;
    @Setter
    public FactionAPI faction;

    @Setter
    private boolean shouldShow;
    private ArrayList<String> supportersCached; // stores supporters from last round of compute
    private ArrayList<String> supporters;
    private ArrayList<Integer> supporterVals; // stores each supporter's total opinion of proposal
    private ArrayList<ArrayList<String>> supporterReasons; // stores itemized breakdown of supporter reasons
    private ArrayList<String> opposersCached; // stores opposers from last round of compute
    private ArrayList<String> opposers;
    private ArrayList<Integer> opposersVals; // stores each opposer's total opinion of proposal
    private ArrayList<ArrayList<String>> opposerReasons; // stores itemized breakdown of opposer reasons
    private boolean playerSupports;
    @Setter
    private boolean liegeSupports;
    @Setter
    private int liegeVal;
    @Setter
    private ArrayList<String> liegeReasons;
    @Setter
    private boolean passed; // whether this proposal was ever passed
    @Setter
    private boolean forcePassed; // whether this proposal was ever force-passed
    private boolean alive;  // if not alive, intel entry is removed. Proposals in the council are considered not alive.
    @Getter
    private HashSet<String> pledgedFor; // stores lords who were swayed to support this proposal
    @Getter
    private HashSet<String> pledgedAgainst; // stores lords who were swayed to opposes this proposal


    public LawProposal(Lawset.LawType law, String originator,
                       String targetLord, String targetFief, String targetFaction, int targetLevel) {
        this.originator = originator;
        this.targetLord = targetLord;
        this.targetFief = targetFief;
        this.targetFaction = targetFaction;
        this.targetLevel = targetLevel;
        this.law = law;
        this.faction = LordController.getLordOrPlayerById(originator).getFaction();
        creationTimestamp = Global.getSector().getClock().getTimestamp();
        supporters = new ArrayList<>();
        opposers = new ArrayList<>();
        supporterReasons = new ArrayList<>();
        opposerReasons = new ArrayList<>();
        supporterVals = new ArrayList<>();
        opposersVals = new ArrayList<>();
        pledgedAgainst = new HashSet<>();
        pledgedFor = new HashSet<>();
        alive = true;
    }

    public LawProposal(Lawset.LawType law, String originator,
                       String targetLord, String targetFief, String targetFaction, int targetLevel,boolean isTemp) {
        //this is a different function for the sole reason that I want to beable to get a crash report if LawProposal gets a null lord as its input.
        this.originator = originator;
        this.targetLord = targetLord;
        this.targetFief = targetFief;
        this.targetFaction = targetFaction;
        this.targetLevel = targetLevel;
        this.law = law;
        if (LordController.getLordOrPlayerById(originator) != null) this.faction = LordController.getLordOrPlayerById(originator).getFaction();
        creationTimestamp = Global.getSector().getClock().getTimestamp();
        supporters = new ArrayList<>();
        opposers = new ArrayList<>();
        supporterReasons = new ArrayList<>();
        opposerReasons = new ArrayList<>();
        supporterVals = new ArrayList<>();
        opposersVals = new ArrayList<>();
        pledgedAgainst = new HashSet<>();
        pledgedFor = new HashSet<>();
        alive = true;
    }



    public void cacheSupporters() {
        supporterReasons.clear();
        opposerReasons.clear();
        supporterVals.clear();
        opposersVals.clear();
        supportersCached = supporters;
        opposersCached = opposers;
        supporters = new ArrayList<>();
        opposers = new ArrayList<>();
    }

    // ignores effect of non-player ruler. TODO this is a duplicate function
    public int getTotalSupport() {
        int ctr = 0;
        for (String lordStr : supporters) {
            Lord lord = LordController.getLordOrPlayerById(lordStr);
            if (lord == null) continue;
            ctr += PoliticsController.getPoliticalWeight(lord);
        }
        if (playerSupports) {
            if (!faction.equals(Global.getSector().getPlayerFaction())) {
                if (PlayerProgression.hasSeat()) ctr += PoliticsController.getPoliticalWeight(LordController.getPlayerLord());
            } else  {
                ctr += PoliticsController.PLAYER_EXTRA_COUNCIL_WEIGHT;
                ctr *= PoliticsController.getLiegeMultiplier(Global.getSector().getPlayerFaction());
            }
        }
        return ctr;
    }

    public void setPlayerSupports(boolean supports) {
        playerSupports = supports;
        if (faction.equals(Global.getSector().getPlayerFaction())) {
            liegeSupports = supports;
        }
    }

    public void kill() {
        alive = false;
    }

    // Presentation only: the proposal's authority faction is explicit.
    public String getSummary() {
        Lord target = targetLord == null ? null : LordController.getLordOrPlayerById(targetLord);
        String name = target == null ? Presentation.text("context.person")
                : target.getTitle() + " " + target.getLordAPI().getNameString();
        com.fs.starfarer.api.campaign.econ.MarketAPI market = targetFief == null ? null : Global.getSector().getEconomy().getMarket(targetFief);
        String marketName = market == null ? Presentation.text("context.market") : market.getName();
        FactionAPI other = targetFaction == null ? null : Global.getSector().getFaction(targetFaction);
        String otherName = other == null ? Presentation.text("context.faction") : other.getDisplayNameWithArticle();
        switch (law) {
            case CROWN_AUTHORITY: case NOBLE_AUTHORITY: case TRADE_LAW: case FEAST_LAW:
                return Presentation.format("policy.change", "policy", Presentation.lawName(faction, law),
                        "level", LawLevel.values()[targetLevel].displayName);
            case APPOINT_MARSHAL:
                return Presentation.format("policy.appoint", "commander", name);
            case AWARD_FIEF:
                return Presentation.format("holding.assign", "market", marketName, "commander", name);
            case REVOKE_FIEF:
                return Presentation.format("holding.revoke", "market", marketName, "commander", name);
            case DECLARE_WAR:
                return Presentation.format("policy.war", "faction", otherName);
            case SUE_FOR_PEACE:
                return Presentation.format("policy.peace", "faction", otherName);
            case CHANGE_RANK:
                return Presentation.format("standing.change", "commander", name,
                        "title", Presentation.title(faction == null ? null : faction.getId(), targetLevel),
                        "tier", Integer.toString(targetLevel + 1));
            case EXILE_LORD:
                return Presentation.format("policy.expel", "commander", name);
            default:
                return Presentation.text("policy.unavailable");
        }
    }

    public String getTitle() {
        switch (law) {
            case CROWN_AUTHORITY: case NOBLE_AUTHORITY: case TRADE_LAW: case FEAST_LAW:
                int current = PoliticsController.getLaws(faction).getLawLevel(law).ordinal();
                return Presentation.format(current > targetLevel ? "policy.decrease" : "policy.increase",
                        "policy", Presentation.lawName(faction, law));
            default:
                return getSummary();
        }
    }
}
