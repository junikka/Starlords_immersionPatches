package starlords.controllers;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.util.Misc;
import starlords.faction.LawProposal;
import starlords.faction.Lawset;
import starlords.person.Lord;
import starlords.presentation.Presentation;
import starlords.util.Utils;
import starlords.util.factionUtils.FactionTemplate;
import starlords.util.factionUtils.FactionTemplateController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// The player's climb through the council of the faction they hold a commission with:
// Commissioned (no seat), then tiers 0, 1 and 2. Requirements come from the faction's "progression" block in
// data/lords/faction.json. The player's own faction is not affected: there the player rules from the start.
public class PlayerProgression {
    public static final int COMMISSIONED = -1;
    private static final String DATA_KEY = "starlords_playerProgression";
    private static final int CHECK_INTERVAL_DAYS = 30;

    private static Map<String, Object> data() {
        Map<String, Object> data = (Map<String, Object>) Global.getSector().getPersistentData().get(DATA_KEY);
        if (data == null) {
            data = new HashMap<>();
            Global.getSector().getPersistentData().put(DATA_KEY, data);
        }
        return data;
    }

    // starts over whenever the player's commission changes
    private static Map<String, Object> sync() {
        Map<String, Object> data = data();
        FactionAPI faction = getFaction();
        String id = faction == null ? null : faction.getId();
        Object tracked = data.get("faction");
        if (id == null ? tracked == null : id.equals(tracked)) return data;
        data.clear();
        data.put("faction", id);
        if (faction != null) {
            data.put("since", Global.getSector().getClock().getTimestamp());
            data.put("rankSince", Global.getSector().getClock().getTimestamp());
            data.put("seated", false);
            data.put("wins", 0);
            LordController.getPlayerLord().setRanking(0);
        }
        return data;
    }

    // the faction whose council the player is climbing, or null when progression doesn't apply
    public static FactionAPI getFaction() {
        FactionAPI faction = Misc.getCommissionFaction();
        if (faction == null || faction.isPlayerFaction()) return null;
        if (!FactionTemplateController.getTemplate(faction).isCanPreformPolicy()) return null;
        return faction;
    }

    // COMMISSIONED or the player's council tier. Without a commission the player has full powers.
    public static int getStanding() {
        if (getFaction() == null) return 2;
        if (!Boolean.TRUE.equals(sync().get("seated"))) return COMMISSIONED;
        return LordController.getPlayerLord().getRanking();
    }

    // The standing the council honours: the rank held, capped by the reputation each rank requires.
    // Falling reputation suspends privileges without taking the rank away; they return with the reputation.
    public static int getEffectiveStanding() {
        FactionAPI faction = getFaction();
        int standing = getStanding();
        if (faction == null) return standing;
        FactionTemplate rules = FactionTemplateController.getTemplate(faction);
        for (int tier = standing; tier >= 0; tier--) {
            if (faction.getRelToPlayer().isAtWorst(getRequiredRep(rules, tier))) return tier;
        }
        return COMMISSIONED;
    }

    private static RepLevel getRequiredRep(FactionTemplate rules, int tier) {
        if (tier >= 2) return rules.getTier2Rep();
        if (tier == 1) return rules.getTier1Rep();
        return rules.getSeatRep();
    }

    // empty unless reputation has fallen below what the held rank requires
    public static String getSuspensionText() {
        FactionAPI faction = getFaction();
        int standing = getStanding();
        int effective = getEffectiveStanding();
        if (faction == null || effective >= standing) return "";
        String title = effective == COMMISSIONED
                ? Presentation.text("progression.commissioned_title") : Utils.getTitle(faction, effective);
        return Presentation.format("progression.suspended", "faction", faction.getDisplayName(),
                "level", getRequiredRep(FactionTemplateController.getTemplate(faction), standing).getDisplayName(),
                "title", title);
    }

    // whether the player has a vote in the council
    public static boolean hasSeat() {
        return getEffectiveStanding() != COMMISSIONED;
    }

    public static String getStandingTitle() {
        int standing = getStanding();
        if (standing == COMMISSIONED) return Presentation.text("progression.commissioned_title");
        return Utils.getTitle(Utils.getRecruitmentFaction(), standing);
    }

    // shown on proposals the player can't make yet
    public static String getRequirementText(int requiredStanding) {
        if (getStanding() >= requiredStanding) return getSuspensionText();
        if (requiredStanding <= 0) return Presentation.text("progression.requires_seat");
        return Presentation.format("progression.requires_rank",
                "title", Utils.getTitle(Utils.getRecruitmentFaction(), requiredStanding));
    }

    // called daily by PoliticsController
    public static void advance() {
        FactionAPI faction = getFaction();
        if (faction == null) {
            sync();
            return;
        }
        if (getStanding() == COMMISSIONED) {
            if (getMissingRequirements().isEmpty()) grantSeat(faction);
            return;
        }
        Map<String, Object> data = sync();
        Long waitFrom = (Long) data.get("waitFrom");
        Integer waitDays = (Integer) data.get("waitDays");
        if (waitFrom != null && waitDays != null && Utils.getDaysSince(waitFrom) < waitDays) return;
        data.put("waitFrom", Global.getSector().getClock().getTimestamp());
        data.put("waitDays", CHECK_INTERVAL_DAYS);
        sponsorPromotion(false);
    }

    // what still stands between the player and the next step; empty when it's within reach or there is none
    public static List<String> getMissingRequirements() {
        List<String> missing = new ArrayList<>();
        FactionAPI faction = getFaction();
        if (faction == null) return missing;
        FactionTemplate rules = FactionTemplateController.getTemplate(faction);
        Map<String, Object> data = sync();
        switch (getStanding()) {
            case COMMISSIONED:
                int days = (int) Utils.getDaysSince((Long) data.get("since"));
                if (days < rules.getSeatAfterDays()) {
                    missing.add(Presentation.format("progression.need_days",
                            "days", Integer.toString(rules.getSeatAfterDays()), "current", Integer.toString(days)));
                }
                addMissingRep(missing, faction, rules.getSeatRep());
                break;
            case 0:
                addMissingDaysInRank(missing, faction, 0, rules.getTier1AfterDays());
                addMissingRep(missing, faction, rules.getTier1Rep());
                int wins = getJointVictories();
                if (LordController.getPlayerLord().getFiefs().isEmpty() && wins < rules.getTier1Battles()) {
                    missing.add(Presentation.format("progression.need_holding_or_battles",
                            "battles", Integer.toString(rules.getTier1Battles()), "current", Integer.toString(wins)));
                }
                break;
            case 1:
                addMissingDaysInRank(missing, faction, 1, rules.getTier2AfterDays());
                addMissingRep(missing, faction, rules.getTier2Rep());
                int friends = 0;
                for (Lord lord : LordController.getLordsList()) {
                    if (!lord.isPlayer() && faction.equals(lord.getFaction())
                            && lord.getLordAPI().getRelToPlayer().getRepInt() >= rules.getTier2FriendRelation()) {
                        friends++;
                    }
                }
                if (friends < rules.getTier2Friends()) {
                    missing.add(Presentation.format("progression.need_friends",
                            "count", Integer.toString(rules.getTier2Friends()),
                            "relation", Integer.toString(rules.getTier2FriendRelation()),
                            "current", Integer.toString(friends)));
                }
                break;
        }
        return missing;
    }

    private static void addMissingDaysInRank(List<String> missing, FactionAPI faction, int tier, int needed) {
        Map<String, Object> data = sync();
        Long rankSince = (Long) data.get("rankSince");
        if (rankSince == null) rankSince = (Long) data.get("since");
        int days = (int) Utils.getDaysSince(rankSince);
        if (days >= needed) return;
        missing.add(Presentation.format("progression.need_days_in_rank", "days", Integer.toString(needed),
                "title", Utils.getTitle(faction, tier), "current", Integer.toString(days)));
    }

    private static void addMissingRep(List<String> missing, FactionAPI faction, RepLevel needed) {
        if (faction.getRelToPlayer().isAtWorst(needed)) return;
        missing.add(Presentation.format("progression.need_rep", "level", needed.getDisplayName(),
                "faction", faction.getDisplayName(), "current", faction.getRelToPlayer().getLevel().getDisplayName()));
    }

    public static int getJointVictories() {
        Integer wins = (Integer) sync().get("wins");
        return wins == null ? 0 : wins;
    }

    // a won battle counts when any fleet of the player's faction fought on the player's side
    public static void recordBattle(BattleAPI battle, CampaignFleetAPI primaryWinner) {
        FactionAPI faction = getFaction();
        if (faction == null || !battle.isPlayerInvolved() || !battle.isOnPlayerSide(primaryWinner)) return;
        for (CampaignFleetAPI fleet : battle.getSnapshotSideFor(primaryWinner)) {
            if (fleet.isPlayerFleet() || !faction.equals(fleet.getFaction())) continue;
            sync().put("wins", getJointVictories() + 1);
            return;
        }
    }

    private static void grantSeat(FactionAPI faction) {
        sync().put("seated", true);
        sync().put("rankSince", Global.getSector().getClock().getTimestamp());
        LordController.getPlayerLord().setRanking(0);
        Global.getSector().getCampaignUI().addMessage(Presentation.format("progression.seated",
                "faction", faction.getDisplayName(), "title", Utils.getTitle(faction, 0)), faction.getBaseUIColor());
    }

    // Puts the player forward for the next tier. The sponsor is the commander who likes the player most, and the
    // council votes on it like any other rank change. Returns what happened, for the console.
    public static String sponsorPromotion(boolean ignoreRequirements) {
        FactionAPI faction = getFaction();
        if (faction == null) return Presentation.text("progression.no_commission");
        int standing = getStanding();
        if (standing == COMMISSIONED) return Presentation.text("progression.no_vote_seat");
        if (standing >= 2) return Presentation.text("progression.no_vote_top");
        List<String> missing = getMissingRequirements();
        if (!ignoreRequirements && !missing.isEmpty()) {
            return Presentation.format("progression.no_vote_requirements", "requirements", joinRequirements(missing));
        }
        if (PoliticsController.hasPendingPromotion(LordController.getPlayerLord())) return Presentation.text("progression.no_vote_pending");

        Lord sponsor = null;
        int bestRelation = 0;
        LawProposal debated = PoliticsController.getCurrProposal(faction);
        for (Lord lord : LordController.getLordsList()) {
            if (lord.isPlayer() || !faction.equals(lord.getFaction())) continue;
            // don't pull a proposal out of the debate
            LawProposal own = PoliticsController.getProposal(lord);
            if (own != null && own.equals(debated)) continue;
            int relation = lord.getLordAPI().getRelToPlayer().getRepInt();
            if (relation > bestRelation || (sponsor != null && relation == bestRelation
                    && lord.getLordAPI().getId().compareTo(sponsor.getLordAPI().getId()) < 0)) {
                sponsor = lord;
                bestRelation = relation;
            }
        }
        if (sponsor == null) return Presentation.text("progression.no_vote_sponsor");
        return propose(sponsor, faction, standing + 1);
    }

    private static String propose(Lord sponsor, FactionAPI faction, int targetTier) {
        LawProposal proposal = new LawProposal(Lawset.LawType.CHANGE_RANK, sponsor.getLordAPI().getId(),
                LordController.getPlayerLord().getLordAPI().getId(), null, null, targetTier);
        proposal.setPlayerSupports(true);
        PoliticsController.addProposal(sponsor, proposal);
        PoliticsController.updateProposal(proposal);
        String message = Presentation.format("progression.sponsored",
                "commander", sponsor.getTitle() + " " + sponsor.getLordAPI().getNameString(),
                "title", Utils.getTitle(faction, targetTier));
        Global.getSector().getCampaignUI().addMessage(message, faction.getBaseUIColor());
        return message;
    }

    // whether the "ask for a promotion" dialogue option makes sense right now
    public static boolean canSeekPromotion() {
        int standing = getStanding();
        return getFaction() != null && standing >= 0 && standing < 2;
    }

    // The player asks this commander in person. Same requirements as the monthly check, but no waiting for it.
    // Returns the dialogue line key for the commander's answer.
    public static String askForSponsor(Lord lord) {
        FactionAPI faction = getFaction();
        if (!canSeekPromotion() || !faction.equals(lord.getFaction())) return "promotion_not_ready";
        if (!getMissingRequirements().isEmpty()) return "promotion_not_ready";
        if (PoliticsController.hasPendingPromotion(LordController.getPlayerLord())) return "promotion_pending";
        Long failedAt = (Long) sync().get("failedAt");
        int cooldown = FactionTemplateController.getTemplate(faction).getPromotionCooldownDays();
        if (failedAt != null && Utils.getDaysSince(failedAt) < cooldown) return "promotion_cooldown";
        if (lord.getLordAPI().getRelToPlayer().getRepInt() <= 0) return "promotion_refused";
        LawProposal own = PoliticsController.getProposal(lord);
        if (own != null && own.equals(PoliticsController.getCurrProposal(faction))) return "promotion_busy";
        propose(lord, faction, getStanding() + 1);
        return "promotion_sponsored";
    }

    public static boolean isPlayerPromotion(LawProposal proposal) {
        return proposal != null && proposal.law == Lawset.LawType.CHANGE_RANK
                && Global.getSector().getPlayerPerson().getId().equals(proposal.targetLord)
                && !proposal.getFaction().isPlayerFaction();
    }

    // a failed vote only starts the cooldown; it costs nothing else
    public static void onVoteResolved(LawProposal proposal) {
        FactionAPI faction = getFaction();
        if (faction == null || !isPlayerPromotion(proposal)) return;
        Map<String, Object> data = sync();
        int cooldown = FactionTemplateController.getTemplate(faction).getPromotionCooldownDays();
        data.put("waitFrom", Global.getSector().getClock().getTimestamp());
        data.put("waitDays", proposal.isPassed() ? CHECK_INTERVAL_DAYS : cooldown);
        if (proposal.isPassed()) data.put("rankSince", Global.getSector().getClock().getTimestamp());
        if (!proposal.isPassed()) {
            data.put("failedAt", Global.getSector().getClock().getTimestamp());
            Global.getSector().getCampaignUI().addMessage(Presentation.format("progression.vote_failed",
                    "days", Integer.toString(cooldown)), faction.getBaseUIColor());
        }
    }

    // console only: jumps straight to a standing, skipping the votes and the time requirements. Reputation is
    // raised to what the standing needs, so the council and fleets honour it.
    public static void setStanding(int standing) {
        FactionAPI faction = getFaction();
        if (faction != null && standing >= 0) {
            faction.getRelToPlayer().ensureAtWorst(getRequiredRep(FactionTemplateController.getTemplate(faction), standing));
        }
        Map<String, Object> data = sync();
        data.put("seated", standing != COMMISSIONED);
        data.put("rankSince", Global.getSector().getClock().getTimestamp());
        LordController.getPlayerLord().setRanking(Math.max(0, Math.min(2, standing)));
        data.remove("waitFrom");
    }

    public static String joinRequirements(List<String> requirements) {
        StringBuilder out = new StringBuilder();
        for (String requirement : requirements) {
            if (out.length() > 0) out.append("; ");
            out.append(requirement);
        }
        return out.toString();
    }
}
