package starlords.plugins;

import starlords.util.RaveMusic;
import starlords.presentation.Presentation;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import starlords.controllers.LordController;
import org.lwjgl.input.Keyboard;
import starlords.person.Lord;
import starlords.person.LordEvent;
import starlords.util.LordFleetFactory;
import starlords.util.Utils;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TournamentDialogPlugin extends FleetInteractionDialogPluginImpl {

    public static enum TournamentOptionId {
        LIST_PARTICIPANTS,
        PLACE_WAGER,
        PROCESS_WAGER1,
        PROCESS_WAGER2,
        PROCESS_WAGER3,
        PROCESS_WAGER4,
        CHOOSE_SHIP,
        UNCHOOSE_SHIP,
        REWARD_SHIP,
        REWARD_BP
    }

    // Total possible winnings that should be expected from a wager, by round
    private static final float[] wagerRatio = new float[]{2.5f, 2, 1.8f, 1.5f};

    private final LordEvent feast;
    private boolean placedWager;
    private int wager;
    private int winnings;
    private float initCR;
    private float initHull;
    private FleetMemberAPI rewardShip;
    private FleetMemberAPI playerShip;
    private CampaignFleetAPI origPlayerFleet;
    private ArrayList<PersonAPI> participants;
    private MarketAPI market;
    private ShipAPI.HullSize currRoundHullSize;
    private ArrayList<String> currRoundOptions;
    private String shipCategory;
    private PersonAPI alsoRemove; // if the player passes a round due to kills, remove this guy instead
    private boolean playerTeamLost;

    public TournamentDialogPlugin(LordEvent feast) {
        if (feast == null) {
            // for testing only
            MarketAPI market = Misc.findNearestLocalMarket(Global.getSector().getPlayerFleet(), 1e5f, null);
            feast = new LordEvent(LordEvent.FEAST, LordController.getLordsList().get(0), market.getPrimaryEntity());
            for (int i = 0; i < 7; i++) {
                feast.getParticipants().add(LordController.getLordsList().get(1 + i));
            }
        }

        this.feast = feast;
        market = feast.getTarget().getMarket();
        config = new FIDConfig();
        config.pullInEnemies = false;
        config.pullInAllies = false;
        config.pullInStations = false;
        config.showFleetAttitude = false;
        config.showCommLinkOption = false;
        config.showTransponderStatus = false;
        config.alwaysAttackVsAttack = true;
        origPlayerFleet = Global.getSector().getPlayerFleet();
        participants = new ArrayList<>();
        currRoundOptions = new ArrayList<>();
        participants.add(feast.getOriginator().getLordAPI());
        for (Lord participant : feast.getParticipants()) {
            if (participants.size() < 15) participants.add(participant.getLordAPI());
        }
        feast.setHeldTournament(true);
        populateParticipants();
        setShipCategory();
    }


    @Override
    public void init(InteractionDialogAPI dialog) {
        RaveMusic.start();
        this.dialog = dialog;
        dialog.setInteractionTarget(feast.getOriginator().getFleet());
        textPanel = dialog.getTextPanel();
        options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();
        conversationDelegate = new RuleBasedInteractionDialogPluginImpl();
        conversationDelegate.setEmbeddedMode(true);
        conversationDelegate.init(dialog);
        Global.getSector().setPaused(true);
        advanceTournamentStage(false);
        addText(Presentation.text("tournament.the_tournament_is_about_to_start_you_eye_your_fifteen_competitor") +
                Presentation.text("tournament.competition_sponsored_crafts_each_of_them_seems_determined_to_ta") +
                Presentation.text("tournament.and_lay_claim_to_the_title_of_best_pilot_in") + market.getName());
    }

    public void advanceTournamentStage(boolean eliminate) {
        // clean up old fleets
        if (playerFleet != null && playerFleet != origPlayerFleet) playerFleet.despawn();
        if (otherFleet != null) otherFleet.despawn();
        // eliminate loser contestants
        if (eliminate) {
            int toRemove = 1 + participants.size() / 2;
            if (playerTeamLost) {
                for (int i = 0; i < toRemove - 1; i++) {
                    participants.remove(0);
                }
            } else {
                for (int i = 0; i < toRemove; i++) {
                    participants.remove(participants.size() - 1);
                }
            }
            if (alsoRemove != null) {
                participants.remove(alsoRemove);
                alsoRemove = null;
            }
            Collections.shuffle(participants);
            if (wager > 0) {
                addText(Presentation.text("tournament.gained") + wager + Presentation.text("tournament.credits_from_wagers"), Color.GREEN);
            }
            winnings += wager;
            placedWager = false;
            playerTeamLost = false;
            addText(Presentation.text("tournament.you_ve_reached_the_round_of") + (participants.size() + 1));
        }

        // create temporary fleets for next battle
        updateHullSize();
        CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(
                Utils.getRecruitmentFaction().getId(), FleetTypes.TASK_FORCE,
                market);
        market.getContainingLocation().addEntity(fleet);
        fleet.setName(Presentation.text("tournament.your_fleet"));
        fleet.setHidden(false);
        fleet.setExpired(false);
        fleet.setCommander(Global.getSector().getPlayerPerson());
        fleet.setLocation(market.getPrimaryEntity().getLocation().x,
                market.getPrimaryEntity().getLocation().y);
        addContestant(fleet, Global.getSector().getPlayerPerson(), chooseHull(Global.getSector().getPlayerPerson()));
        for (int i = 0; i < participants.size() / 2; i++) {
            addContestant(fleet, participants.get(i), chooseHull(participants.get(i)));
        }
        Global.getSector().setPlayerFleet(fleet);
        fleet.setAIMode(true);
        fleet.inflateIfNeeded();
        fleet.getFleetData().setSyncNeeded();
        fleet.addTag("$starlords_tournament");
        fleet.getFleetData().takeSnapshot();
        fleet.forceSync();
        fleet.getCargo().addCrew((int) fleet.getFleetData().getMinCrew());

        CampaignFleetAPI fleet2 = FleetFactoryV3.createEmptyFleet(
                Utils.getRecruitmentFaction().getId(), FleetTypes.TASK_FORCE,
                market);
        fleet2.setAIMode(true);
        fleet2.setNoFactionInName(true);
        fleet2.setName(Presentation.text("tournament.opposing_fleet"));
        fleet2.addTag("$starlords_tournament");
        fleet2.setCommander(participants.get(participants.size() / 2));
        fleet2.setLocation(market.getPrimaryEntity().getLocation().x,
                market.getPrimaryEntity().getLocation().y);
        for (int i = participants.size() / 2; i < participants.size(); i++) {
            addContestant(fleet2, participants.get(i), chooseHull(participants.get(i)));
        }
        fleet2.forceSync();
        playerFleet = fleet;
        otherFleet = fleet2;
        BattleAPI battle = Global.getFactory().createBattle(playerFleet, otherFleet);
        context.setBattle(battle);
        context.getBattle().genCombined();
        context.getBattle().takeSnapshots();
        visual.setVisualFade(0.25f, 0.25f);
        showFleetInfo();
        conversationDelegate.fireBest("BeginFleetEncounter");
        optionSelected(null, OptionId.INIT);
    }

    private void addContestant(CampaignFleetAPI fleet, PersonAPI person, String variantId) {
        if (person.isPlayer() && playerShip != null) {
            fleet.getFleetData().addFleetMember(playerShip);
            if (shipCategory != null && !playerShip.getHullSpec().getManufacturer().equals(shipCategory)) {
                addText(Presentation.text("tournament.your_ship_does_not_match_the_tournament_theme") + shipCategory,
                        shipCategory, market.getFaction().getBaseUIColor());
                addText(Presentation.text("tournament.cr_will_be_significantly_reduced"), Color.YELLOW);
                playerShip.getRepairTracker().setCR(0.4f);
            }
        } else {
            LordFleetFactory.addToLordFleet(new ShipRolePick(variantId), fleet, new Random());
        }
        for (FleetMemberAPI member : fleet.getFleetData().getMembersInPriorityOrder()) {
            if (member.getCaptain().getNameString().isEmpty()) {
                member.getRepairTracker().setCR(1f);
                member.setCaptain(person);
            }
        }
        fleet.getFleetData().ensureHasFlagship();

    }

    private String chooseHull(PersonAPI person) {
        long seed = Global.getSector().getClock().getTimestamp() * person.getId().hashCode();
        return currRoundOptions.get(new Random(seed).nextInt(currRoundOptions.size()));
    }

    private void updateHullSize() {
        if (playerShip != null) {
            currRoundHullSize = playerShip.getHullSpec().getHullSize();
        } else {
            long seed = Global.getSector().getClock().getTimestamp() * market.getId().hashCode() * participants.size();
            int choice = new Random(seed).nextInt(3);
            switch (choice) {
                case 0:
                    currRoundHullSize = ShipAPI.HullSize.CRUISER;
                    break;
                case 1:
                    currRoundHullSize = ShipAPI.HullSize.DESTROYER;
                    break;
                default:
                    currRoundHullSize = ShipAPI.HullSize.FRIGATE;
                    break;
            }
        }

        currRoundOptions.clear();
        for (String variantId : Global.getSettings().getAllVariantIds()) {
            ShipVariantAPI variant = Global.getSettings().getVariant(variantId);
            if (variant.getHullSize() != currRoundHullSize) continue;
            if (variant.hasHullMod(HullMods.CIVGRADE)) continue;
            if (variant.isCarrier()) continue;
            if (variant.getWeaponGroups().isEmpty()) continue;
            if (variant.getHullSpec().getManufacturer().equals(shipCategory)) currRoundOptions.add(variantId);
        }
        if (currRoundOptions.isEmpty()) {
            // backup: guess we're doing pirates
            for (String variantId : Global.getSettings().getAllVariantIds()) {
                ShipVariantAPI variant = Global.getSettings().getVariant(variantId);
                if (variant.getHullSize() != currRoundHullSize) continue;
                if (variant.hasHullMod(HullMods.CIVGRADE)) continue;
                if (variant.getWeaponGroups().isEmpty()) continue;
                if (variant.getHullSpec().getManufacturer().equals("Pirate")) currRoundOptions.add(variantId);
            }
        }
    }

    @Override
    public void optionSelected(String text, Object optionData) {
        if (optionData == null) return;
        boolean doSuper = true;
        if (optionData instanceof OptionId) {
            OptionId option = (OptionId) optionData;
            switch (option) {
                case INIT:
                    options.clearOptions();
                    options.addOption(Presentation.text("tournament.proceed_to_the_next_round"), OptionId.ENGAGE, getString("tooltipEngage"));
                    options.addOption(Presentation.text("tournament.check_the_tournament_roster"), TournamentOptionId.LIST_PARTICIPANTS);
                    options.addOption(Presentation.text("tournament.place_a_wager"), TournamentOptionId.PLACE_WAGER);
                    if (playerShip == null) {
                        options.addOption(Presentation.text("tournament.pilot_your_own_ship"), TournamentOptionId.CHOOSE_SHIP);
                    } else {
                        options.addOption(Presentation.text("tournament.pilot_a_standardized_ship"), TournamentOptionId.UNCHOOSE_SHIP);
                    }
                    options.addOption(Presentation.text("tournament.withdraw_from_the_tournament"), OptionId.LEAVE, null);
                    options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
                    if (placedWager) options.setEnabled(TournamentOptionId.PLACE_WAGER, false);
                    doSuper = false;
                    break;
                case CONTINUE_INTO_BATTLE:
                    BattleCreationContext bcc;
                    playerFleet.getFleetData().clear();
                    bcc = new BattleCreationContext(playerFleet, FleetGoal.ATTACK, otherFleet, FleetGoal.ATTACK);
                    bcc.setPlayerCommandPoints((int) Global.getSector().getPlayerFleet().getCommanderStats().getCommandPoints().getModifiedValue());
                    if (config.delegate != null) {
                        config.delegate.battleContextCreated(dialog, bcc);
                    }
                    doSuper = false;
                    visual.fadeVisualOut();
                    dialog.startBattle(bcc);
                    break;
                case LEAVE:
                    doSuper = false;
                    if (playerFleet != null && playerFleet != origPlayerFleet) playerFleet.despawn();
                    if (otherFleet != null) otherFleet.despawn();
                    Global.getSector().setPlayerFleet(origPlayerFleet);
                    origPlayerFleet.getCargo().getCredits().add(winnings);
                    RaveMusic.stop();
                    for (CampaignFleetAPI fleet : context.getBattle().getBothSides()) {
                        context.getBattle().leave(fleet, false);
                    }
                    context.getBattle().finish(BattleAPI.BattleSide.ONE);
                    // sometimes original fleet gets stuck in a battle somehow
                    if (origPlayerFleet.getBattle() != null) {
                        BattleAPI battle = origPlayerFleet.getBattle();
                        for (CampaignFleetAPI fleet : battle.getBothSides()) {
                            battle.leave(fleet, false);
                        }
                        battle.finish(BattleAPI.BattleSide.ONE);
                    }
                    dialog.dismiss();
                    break;
            }
        } else {
            TournamentOptionId option = (TournamentOptionId) optionData;
            doSuper = false;
            int roundsLeft;
            float baseWinPerRound;
            switch (option) {
                case LIST_PARTICIPANTS:
                    addText(Presentation.text("tournament.tournament_roster"));
                    addText(Global.getSector().getPlayerPerson().getNameString(),
                            Utils.getRecruitmentFaction().getBaseUIColor());
                    for (int i = 0; i < participants.size(); i++) {
                        Lord lord = LordController.getLordById(participants.get(i).getId());
                        String title;
                        if (lord != null) {
                            title = lord.getTitle();
                        } else {
                            title = participants.get(i).getPost();
                        }
                        addText(title + " " + participants.get(i).getNameString(),
                                participants.get(i).getFaction().getBaseUIColor());
                    }
                    break;
                case PLACE_WAGER:
                    addText(Presentation.text("tournament.choose_the_amount_you_d_like_to_wager") +
                            Presentation.text("tournament.you_will_pay_this_amount_upfront_and_receive_winnings_for_each_r") +
                            Presentation.text("tournament.you_can_wager_once_per_round_and_winnings_stack"));
                    int creditsLeft = (int) origPlayerFleet.getCargo().getCredits().get() + winnings;
                    roundsLeft = (int) Math.round(Math.log(participants.size() + 1) / Math.log(2));
                    baseWinPerRound = wagerRatio[4 - roundsLeft] / roundsLeft;
                    // 2.5, 2, 1.8, 1.5
                    options.clearOptions();
                    options.addOption(Presentation.text("tournament.2000_credits"), TournamentOptionId.PROCESS_WAGER1);
                    if (creditsLeft < 2000) options.setEnabled(TournamentOptionId.PROCESS_WAGER1, false);
                    options.setTooltip(TournamentOptionId.PROCESS_WAGER1,
                            Presentation.text("tournament.gain") + ((int) (baseWinPerRound * 2000)) + Presentation.text("tournament.credits_per_win"));
                    options.addOption(Presentation.text("tournament.5000_credits"), TournamentOptionId.PROCESS_WAGER2);
                    if (creditsLeft < 5000) options.setEnabled(TournamentOptionId.PROCESS_WAGER2, false);
                    options.setTooltip(TournamentOptionId.PROCESS_WAGER2,
                            Presentation.text("tournament.gain") + ((int) (baseWinPerRound * 5000)) + Presentation.text("tournament.credits_per_win"));
                    options.addOption(Presentation.text("tournament.10000_credits"), TournamentOptionId.PROCESS_WAGER3);
                    if (creditsLeft < 10000) options.setEnabled(TournamentOptionId.PROCESS_WAGER3, false);
                    options.setTooltip(TournamentOptionId.PROCESS_WAGER3,
                            Presentation.text("tournament.gain") + ((int) (baseWinPerRound * 10000)) + Presentation.text("tournament.credits_per_win"));
                    options.addOption(Presentation.text("tournament.20000_credits"), TournamentOptionId.PROCESS_WAGER4);
                    if (creditsLeft < 20000) options.setEnabled(TournamentOptionId.PROCESS_WAGER4, false);
                    options.setTooltip(TournamentOptionId.PROCESS_WAGER4,
                            Presentation.text("tournament.gain") + ((int) (baseWinPerRound * 20000)) + Presentation.text("tournament.credits_per_win"));
                    options.addOption(Presentation.text("tournament.never_mind"), OptionId.INIT);
                    break;
                case PROCESS_WAGER1:
                case PROCESS_WAGER2:
                case PROCESS_WAGER3:
                case PROCESS_WAGER4:
                    int credits = Integer.parseInt(text.split(" ")[0]);
                    roundsLeft = (int) Math.round(Math.log(participants.size() + 1) / Math.log(2));
                    baseWinPerRound = wagerRatio[4 - roundsLeft] / roundsLeft;
                    wager += baseWinPerRound * credits;
                    winnings -= credits;
                    placedWager = true;
                    addText(Presentation.text("tournament.wagered") + credits + Presentation.text("tournament.credits"), Color.RED);
                    optionSelected(null, OptionId.INIT);
                    break;
                case CHOOSE_SHIP:
                    List<FleetMemberAPI> members = origPlayerFleet.getFleetData().getMembersListCopy();
                    List<FleetMemberAPI> toRemove = new ArrayList<>();
                    for (FleetMemberAPI member : members) {
                        if (member.getHullSpec().getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) toRemove.add(member);
                    }
                    members.removeAll(toRemove);
                    if (members.isEmpty()) {
                        addText(Presentation.text("tournament.no_eligible_ships_to_pilot"), Color.RED);
                        return;
                    }
                    dialog.showFleetMemberPickerDialog(Presentation.text("tournament.select_craft_to_pilot"), Presentation.text("tournament.ok"), Presentation.text("tournament.cancel"),
                            4, 8, 58f, false, false, members,
                            new FleetMemberPickerListener() {
                                public void pickedFleetMembers(List<FleetMemberAPI> members) {
                                    if (members != null && !members.isEmpty()) {
                                        if (playerShip != null) playerShip.getRepairTracker().setCR(initCR);
                                        playerShip = members.get(0);
                                        initCR = playerShip.getRepairTracker().getCR();
                                        initHull = playerShip.getStatus().getHullFraction();
                                        advanceTournamentStage(false);
                                    }
                                }
                                public void cancelledFleetMemberPicking() {

                                }
                            });
                    break;
                case UNCHOOSE_SHIP:
                    playerShip = null;
                    advanceTournamentStage(false);
                    break;
                case REWARD_SHIP:
                    origPlayerFleet.getFleetData().addFleetMember(rewardShip);
                    addText(Presentation.text("tournament.gained") + rewardShip.getShipName(), Color.GREEN);
                    options.clearOptions();
                    options.addOption(Presentation.text("tournament.leave"), OptionId.LEAVE, null);
                    options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
                    break;
                case REWARD_BP:
                    FactionAPI playerFaction = Global.getSector().getPlayerFaction();
                    FactionAPI rewardFaction = market.getFaction();
                    ArrayList<Pair<String, String>> knownBp = new ArrayList<>();
                    ArrayList<Pair<String, String>> unknownBp = new ArrayList<>();
                    for (String id : rewardFaction.getKnownShips()) {
                        if (playerFaction.knowsShip(id)) {
                            knownBp.add(new Pair<>(id, "ship_bp"));
                        } else {
                            unknownBp.add(new Pair<>(id, "ship_bp"));
                        }
                    }
                    for (String id : rewardFaction.getKnownFighters()) {
                        if (playerFaction.knowsFighter(id)) {
                            knownBp.add(new Pair<>(id, "fighter_bp"));
                        } else {
                            unknownBp.add(new Pair<>(id, "fighter_bp"));
                        }
                    }
                    for (String id : rewardFaction.getKnownWeapons()) {
                        if (playerFaction.knowsWeapon(id)) {
                            knownBp.add(new Pair<>(id, "weapon_bp"));
                        } else {
                            unknownBp.add(new Pair<>(id, "weapon_bp"));
                        }
                    }
                    Pair<String, String> result = null;
                    if (!unknownBp.isEmpty()) {
                        result = unknownBp.get(Utils.rand.nextInt(unknownBp.size()));
                    } else if (!knownBp.isEmpty()) {
                        result = knownBp.get(Utils.rand.nextInt(knownBp.size()));
                    }
                    if (result != null) {
                        String name = null;
                        switch (result.two) {
                            case "weapon_bp":
                                name = Global.getSettings().getWeaponSpec(result.one).getWeaponName();
                                break;
                            case "fighter_bp":
                                name = Global.getSettings().getFighterWingSpec(result.one).getWingName();
                                break;
                            case "ship_bp":
                                name = Global.getSettings().getHullSpec(result.one).getHullName();
                                break;
                        }
                        origPlayerFleet.getCargo().addSpecial(new SpecialItemData(result.two, result.one), 1);
                        addText(Presentation.text("tournament.gained_blueprint_for") + name, Color.GREEN);
                    } else {
                        addText(Presentation.text("tournament.this_faction_has_no_more_blueprints_to_offer"), Color.RED);
                    }
                    options.clearOptions();
                    options.addOption(Presentation.text("tournament.leave"), OptionId.LEAVE, null);
                    options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
                    break;
            }
        }
        if (doSuper) {
            super.optionSelected(text, optionData);
        }

        if (options.hasOption(OptionId.SELECT_FLAGSHIP)) {
            options.setEnabled(OptionId.SELECT_FLAGSHIP, false);
        }
    }

    @Override
    public void backFromEngagement(EngagementResultAPI result) {
        RaveMusic.resume();
        result.setBattle(context.getBattle());
        lastResult = result;
        boolean advance = false;
        if (result.didPlayerWin()) {
            advance = true;
        } else {
            int playerKills = getSoloKills(Global.getSector().getPlayerPerson(), result);
            if (playerKills >= 1) {
                advance = true;
                playerTeamLost = true;
                // if player passes from merit, then eliminate the lowest damage dealing member on winning team
                alsoRemove = null;
                int alsoRemoveDmg = Integer.MAX_VALUE;
                for (int i = participants.size() / 2; i < participants.size(); i++) {
                    int damage = getTotalDamage(participants.get(i), result);
                    if (damage < alsoRemoveDmg) {
                        alsoRemove = participants.get(i);
                        alsoRemoveDmg = damage;
                    }
                }
            }
        }
        if (playerShip != null) {
            playerShip.getStatus().setHullFraction(initHull);
            playerShip.getStatus().repairArmorAllCells(1f);
        }
        if (!advance) {
            showFleetInfo();
            addText(Presentation.text("tournament.you_ve_been_eliminated_from_the_tournament"), Color.RED);
            PersonAPI winner = getRandWinner();
            feast.setTournamentWinner(winner);
            addText(winner.getNameString() + Presentation.text("tournament.goes_on_to_win_the_tournament"));
            options.clearOptions();
            options.addOption(Presentation.text("tournament.leave"), OptionId.LEAVE, null);
            options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
        } else if (participants.size() == 1) {
            showFleetInfo();
            // choose between faction ship or rare blueprint
            addText(Presentation.text("tournament.the_crowd_erupts_in_cheers_you_have_won_the_tournament"), Color.GREEN);
            addText(Presentation.text("tournament.gained_25000_credits"), Color.GREEN);
            winnings += 25000;
            ArrayList<String> cruiserOptions = new ArrayList<>();
            ArrayList<String> capitalOptions = new ArrayList<>();
            for (String variantId : Global.getSettings().getAllVariantIds()) {
                ShipVariantAPI variant = Global.getSettings().getVariant(variantId);
                if (variant.hasHullMod(HullMods.CIVGRADE)) continue;
                if (variant.getWeaponGroups().isEmpty()) continue;
                if (!variant.getHullSpec().getManufacturer().equals(shipCategory)) continue;
                if (variant.getHullSize() == ShipAPI.HullSize.CRUISER) cruiserOptions.add(variantId);
                if (variant.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) capitalOptions.add(variantId);
            }
            String variant = "atlas_standard";
            if ((cruiserOptions.isEmpty() ||Utils.rand.nextInt(3) == 0) && !capitalOptions.isEmpty()) {
                variant = capitalOptions.get(Utils.rand.nextInt(capitalOptions.size()));
            } else if (!cruiserOptions.isEmpty()) {
                variant = cruiserOptions.get(Utils.rand.nextInt(cruiserOptions.size()));
            }
            // as fallback, just give an atlas
            rewardShip = Global.getFactory().createFleetMember(
                    FleetMemberType.SHIP, Global.getSettings().getVariant(variant));
            rewardShip.setShipName(origPlayerFleet.getFleetData().pickShipName(rewardShip, Utils.rand));
            dialog.getVisualPanel().showFleetMemberInfo(rewardShip, false);
            feast.setTournamentWinner(Global.getSector().getPlayerPerson());
            options.clearOptions();
            options.addOption(Presentation.text("tournament.claim_the_prize_ship"), TournamentOptionId.REWARD_SHIP, null);
            options.addOption(Presentation.text("tournament.claim_new_faction_blueprint"), TournamentOptionId.REWARD_BP, null);
            options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
        } else {
            showFleetInfo();
            if (playerTeamLost) {
                addText(Presentation.text("tournament.though_your_team_was_defeated_you_have_been_recognized_for") +
                        Presentation.text("tournament.achieving_a_solo_takedown_and_advance_to_the_next_round"), Color.GREEN);
            } else {
                addText(Presentation.text("tournament.you_advance_to_the_next_round"), Color.GREEN);
            }
            advanceTournamentStage(true);
        }
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
        if (optionData instanceof TournamentOptionId) return;
        super.optionMousedOver(optionText, optionData);
    }

    private void populateParticipants() {
        List<OfficerDataAPI> playerOfficers = origPlayerFleet.getFleetData().getOfficersCopy();
        Collections.shuffle(playerOfficers);
        List<PersonAPI> marketPeople = market.getPeopleCopy();
        Collections.shuffle(marketPeople);
        boolean memed = false;
        while (participants.size() < 15) {
            ArrayList<Integer> options = new ArrayList<>();
            ArrayList<Integer> weights = new ArrayList<>();

            // 0 - player officers
            if (!playerOfficers.isEmpty() && playerOfficers.get(0).getPerson().isPlayer()) {
                playerOfficers.remove(0);
            }
            if (!playerOfficers.isEmpty()) {
                options.add(0);
                weights.add(50);
            }

            // 1 - Some meme characters
            if (!memed) {
                options.add(1);
                weights.add(5);
            }

            // 2 - pather and pirate characters
            options.add(2);
            weights.add(10);

            // 3 - station characters
            while (!marketPeople.isEmpty() && (
                    participants.contains(marketPeople.get(0)) || marketPeople.get(0).getNameString().isEmpty())) {
                marketPeople.remove(0);
            }
            if (!marketPeople.isEmpty()) {
                options.add(3);
                weights.add(50);
            }

            // 4 - Random faction character
            options.add(4);
            weights.add(25);
            int option = Utils.weightedSample(options, weights, Utils.rand);
            boolean addSkills = false;
            boolean elite = false;
            PersonAPI person = null;
            switch (option) {
                case 0:
                    participants.add(playerOfficers.remove(0).getPerson());
                    break;
                case 1:
                    memed = true;
                    person = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson();
                    person.setPersonality(Personalities.RECKLESS);
                    person.setGender(FullName.Gender.MALE);
                    person.getStats().setLevel(15);
                    person.setName(new FullName("John", "Starsector", FullName.Gender.MALE));
                    person.setPortraitSprite("graphics/portraits/portrait_pirate07.png");
                    elite = true;
                    addSkills = true;
                    participants.add(person);
                    break;
                case 2:
                    if (Utils.rand.nextBoolean()) {
                        person = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson();
                    } else {
                        person = Global.getSector().getFaction(Factions.LUDDIC_PATH).createRandomPerson();
                    }
                    person.getStats().setLevel(6);
                    addSkills = true;
                    participants.add(person);
                    break;
                case 3:
                    person = Utils.clonePerson(marketPeople.remove(0));
                    if (person.getStats().getLevel() < 5) {
                        person.getStats().setLevel(5);
                        addSkills = true;
                        elite = true;
                    }
                    participants.add(person);
                    break;
                case 4:
                    person = market.getFaction().createRandomPerson();
                    person.getStats().setLevel(5);
                    addSkills = true;
                    participants.add(person);
                    break;
            }
            if (addSkills && person != null) {
                for (int i = 0; i < person.getStats().getLevel(); i++) {
                    LordFleetFactory.upskillOfficer(person, elite);
                }
            }
        }
        Collections.shuffle(participants);
    }

    private void setShipCategory() {
        switch (market.getFaction().getId()) {
            case Factions.HEGEMONY:
                shipCategory = Presentation.text("tournament.low_tech");
                break;
            case Factions.TRITACHYON:
                shipCategory = Presentation.text("tournament.high_tech");
                break;
            case Factions.LIONS_GUARD:
            case Factions.DIKTAT:
                shipCategory = Presentation.text("tournament.lion_s_guard");
                break;
            case Factions.LUDDIC_CHURCH:
                shipCategory = Presentation.text("tournament.luddic_church");
                break;
            case Factions.LUDDIC_PATH:
                shipCategory = Presentation.text("tournament.luddic_path");
                break;
            case Factions.PERSEAN:
                shipCategory = Presentation.text("tournament.midline");
                break;
            case Factions.REMNANTS:
                shipCategory = Presentation.text("tournament.remnant");
                break;
            case Factions.PIRATES:
            default:
                shipCategory = Presentation.text("tournament.pirate");
                break;
        }
    }

    // for when player is eliminated
    private PersonAPI getRandWinner() {
        ArrayList<Integer> weights = new ArrayList<>();
        for (PersonAPI person : participants) {
            weights.add((int) Math.pow(person.getStats().getLevel(), 2));
        }
        return Utils.weightedSample(participants, weights, Utils.rand);
    }

    private int getSoloKills(PersonAPI officer, EngagementResultAPI result) {
        int kills = 0;

        // figure out which ships were solo killed
        HashSet<FleetMemberAPI> killed = new HashSet<>();
        for (FleetMemberAPI member : otherFleet.getFleetData().getMembersListCopy()) {
            if (member.getStatus().getHullFraction() == 0.0f) {
                int numDamaging = 0;
                Map<FleetMemberAPI, CombatDamageData.DealtByFleetMember> damageDealt =
                        result.getLastCombatDamageData().getDealt();
                for (FleetMemberAPI dealer : damageDealt.keySet()) {
                    CombatDamageData.DamageToFleetMember damage = damageDealt.get(dealer).getDamageTo(member);
                    if (damage != null && damage.hullDamage > 100) {
                        numDamaging += 1;
                    }
                }
                if (numDamaging == 1) killed.add(member);
            }
        }

        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getStatus().getHullFraction() == 0.0f) {
                int numDamaging = 0;
                Map<FleetMemberAPI, CombatDamageData.DealtByFleetMember> damageDealt =
                        result.getLastCombatDamageData().getDealt();
                for (FleetMemberAPI dealer : damageDealt.keySet()) {
                    CombatDamageData.DamageToFleetMember damage = damageDealt.get(dealer).getDamageTo(member);
                    if (damage != null && damage.hullDamage > 100) {
                        numDamaging += 1;
                    }
                }
                if (numDamaging == 1) killed.add(member);
            }
        }


        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getCaptain().equals(officer)) {
                Map<FleetMemberAPI, CombatDamageData.DamageToFleetMember>  damageDealt =
                        result.getLastCombatDamageData().getDealtBy(member).getDamage();
                for (FleetMemberAPI target : damageDealt.keySet()) {
                    if (killed.contains(target) && damageDealt.get(target).hullDamage > 100) kills += 1;
                }
            }
        }

        for (FleetMemberAPI member : otherFleet.getFleetData().getMembersListCopy()) {
            if (member.getCaptain().equals(officer)) {
                Map<FleetMemberAPI, CombatDamageData.DamageToFleetMember> damageDealt =
                        result.getLastCombatDamageData().getDealtBy(member).getDamage();
                for (FleetMemberAPI target : damageDealt.keySet()) {
                    if (killed.contains(target) && damageDealt.get(target).hullDamage > 100) kills += 1;
                }
            }
        }
        return kills;
    }

    private int getTotalDamage(PersonAPI officer, EngagementResultAPI result) {
        int damage = 0;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getCaptain().equals(officer)) {
                Map<FleetMemberAPI, CombatDamageData.DamageToFleetMember> damageDealt =
                        result.getLastCombatDamageData().getDealtBy(member).getDamage();
                for (FleetMemberAPI target : damageDealt.keySet()) {
                    damage += damageDealt.get(target).hullDamage;
                }
                return damage;
            }
        }
        for (FleetMemberAPI member : otherFleet.getFleetData().getMembersListCopy()) {
            if (member.getCaptain().equals(officer)) {
                Map<FleetMemberAPI, CombatDamageData.DamageToFleetMember> damageDealt =
                        result.getLastCombatDamageData().getDealtBy(member).getDamage();
                for (FleetMemberAPI target : damageDealt.keySet()) {
                    damage += damageDealt.get(target).hullDamage;
                }
                return damage;
            }
        }
        return damage;
    }
}
