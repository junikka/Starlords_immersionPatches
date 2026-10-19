package starlords.ui;

import starlords.presentation.Presentation;

import com.fs.starfarer.api.Script;
import starlords.ai.LordAI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import starlords.controllers.EventController;
import starlords.controllers.FiefController;
import starlords.controllers.LordController;
import starlords.controllers.PlayerProgression;
import starlords.controllers.PoliticsController;
import starlords.faction.LawLevel;
import starlords.faction.LawProposal;
import starlords.faction.Lawset;
import starlords.person.Lord;
import starlords.person.LordEvent;
import starlords.plugins.SelectItemDialogPlugin;
import starlords.util.StringUtil;
import starlords.util.Utils;
import starlords.util.factionUtils.FactionTemplateController;

import java.awt.*;
import java.util.*;
import java.util.List;

import static starlords.util.Constants.CATEGORY_UI;
import static starlords.util.Constants.DARK_GOLD;

public class LawsIntelPlugin extends BaseIntelPlugin {

   static class ButtonObject {
        String type;
        int level;
        public ButtonObject(String s) {
            type = s;
        }

        public ButtonObject clone(int i) {
            ButtonObject ret = new ButtonObject(type);
            ret.level = i;
            return ret;
        }
    }

    private static ButtonObject CROWN_AUTHORITY_BUTTON = new ButtonObject(
            Lawset.LawType.CROWN_AUTHORITY.toString());
    private static ButtonObject NOBLE_AUTHORITY_BUTTON = new ButtonObject(
            Lawset.LawType.NOBLE_AUTHORITY.toString());
    private static ButtonObject TRADE_LAW_BUTTON = new ButtonObject(
            Lawset.LawType.TRADE_LAW.toString());
    private static ButtonObject FEAST_LAW_BUTTON = new ButtonObject(
            Lawset.LawType.FEAST_LAW.toString());
    private static Object APPOINT_MARSHAL_BUTTON = new Object();
    private static Object AWARD_FIEF_BUTTON = new Object();
    private static Object DECLARE_WAR_BUTTON = new Object();
    private static Object SUE_FOR_PEACE_BUTTON = new Object();
    private static Object REVOKE_FIEF_BUTTON = new Object();
    private static Object TRANSFER_FIEF_BUTTON = new Object();
    private static Object CHANGE_TITLE_BUTTON = new Object();
    private static Object EXILE_LORD_BUTTON = new Object();
    private static Object START_CAMPAIGN_BUTTON = new Object();
    private static Object END_CAMPAIGN_BUTTON = new Object();

    // these are only used for convenience and are not expected to hold useful/accurate data in the general case
    private Lawset laws;
    private FactionAPI faction;

    private static LawsIntelPlugin instance;

    private LawsIntelPlugin(){
        setNew(false);
    }

    @Override
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
        float pad = 3;
        float opad = 10;
        float headerHeight = 25;
        float logoHeight = 160;
        TooltipMakerAPI header = panel.createUIElement(width, headerHeight, false);
        Color uiColor = Global.getSettings().getBasePlayerColor();
        faction = Misc.getCommissionFaction();
        if (faction == null || (!FactionTemplateController.getTemplate(faction).isCanPreformPolicy() && !FactionTemplateController.getTemplate(faction).isCanPreformDiplomacy())) {
            if (PoliticsController.playerFactionHasLaws()) {
                faction = Global.getSector().getPlayerFaction();
            } else {
                header.addPara(words("laws.please_join_or_start_an_organized_faction_to_participate_in_poli"), pad);
                panel.addUIElement(header).inTL(0, 0);
                return;
            }
        }
        //header.addImage(faction.getCrest(), headerHeight, headerHeight, pad);
        header.addSectionHeading(words("laws.laws_of_the_realm"),
                faction.getBrightUIColor(), faction.getDarkUIColor(), Alignment.LMID, opad);
        header.addTooltipToPrevious(new ToolTip(250, words("laws.policies_tooltip")),
                TooltipMakerAPI.TooltipLocation.BELOW);
        panel.addUIElement(header).inTL(0, 0);

        TooltipMakerAPI logo = panel.createUIElement(logoHeight, logoHeight, false);
        logo.addImage(faction.getCrest(), logoHeight, logoHeight, pad);
        panel.addUIElement(logo).belowRight(header, 0);
        laws = PoliticsController.getLaws(faction);
        // Display all laws
        // Display crown/noble/feast/tax laws as a grid
        TooltipMakerAPI prev;
        // proposal powers grow with the player's standing, see PlayerProgression
        prev = addLawRow(panel, header, Lawset.LawType.CROWN_AUTHORITY, faction, CROWN_AUTHORITY_BUTTON, 1);
        prev = addLawRow(panel, prev, Lawset.LawType.NOBLE_AUTHORITY, faction, NOBLE_AUTHORITY_BUTTON, 1);
        prev = addLawRow(panel, prev, Lawset.LawType.TRADE_LAW, faction, TRADE_LAW_BUTTON, 0);
        prev = addLawRow(panel, prev, Lawset.LawType.FEAST_LAW, faction, FEAST_LAW_BUTTON, 0);

        TooltipMakerAPI internalAffairsHeader = panel.createUIElement(width, headerHeight, false);
        internalAffairsHeader.addSectionHeading(words("laws.internal_affairs"),
                faction.getBrightUIColor(), faction.getDarkUIColor(), Alignment.LMID, opad);
        panel.addUIElement(internalAffairsHeader).belowLeft(prev, 0);
        // Change Marshal

        TooltipMakerAPI marshalPanel = panel.createUIElement(width / 2, 80, false);
        Lord marshal = LordController.getLordOrPlayerById(laws.getMarshal());
        marshalPanel.addPara(words("laws.current_marshal"), uiColor, pad);
        if (marshal != null) {
            TooltipMakerAPI marshalImagePanel = marshalPanel.beginImageWithText(
                    marshal.getLordAPI().getPortraitSprite(), 48);
            marshalImagePanel.addPara(marshal.getLordAPI().getNameString(), faction.getBaseUIColor(), pad);
            ButtonAPI changeMarshalButton = marshalImagePanel.addButton(words("laws.change_marshal"), APPOINT_MARSHAL_BUTTON, 150, 20, pad);
            requireStanding(changeMarshalButton, marshalImagePanel, 1);
            if (marshal.isPlayer()) {
                if (EventController.getCurrentCampaign(marshal.getFaction()) != null) {
                    marshalImagePanel.addButton(words("laws.end_campaign"), END_CAMPAIGN_BUTTON, 150, 20, pad);
                } else {
                    ButtonAPI campaignButton = marshalImagePanel.addButton(words("laws.start_campaign"), START_CAMPAIGN_BUTTON, 150, 20, pad);
                    // cannot start campaign if on cooldown
                    int daysPassed = (int) Utils.getDaysSince(
                            EventController.getLastCampaignTime(marshal.getFaction()));
                    if (daysPassed < LordAI.CAMPAIGN_COOLDOWN) {
                        campaignButton.setEnabled(false);
                        marshalImagePanel.addTooltipToPrevious(new ToolTip(250,
                                words("laws.campaign_on_cooldown_for") + (LordAI.CAMPAIGN_COOLDOWN - daysPassed) + words("laws.days")), TooltipMakerAPI.TooltipLocation.BELOW);
                    }
                }
            }
            marshalPanel.addImageWithText(pad);
        } else {
            marshalPanel.addPara(words("laws.none"), faction.getBaseUIColor(), pad);
            ButtonAPI appointMarshalButton = marshalPanel.addButton(words("laws.appoint_marshal"), APPOINT_MARSHAL_BUTTON, 150, 20, pad);
            requireStanding(appointMarshalButton, marshalPanel, 1);
        }
        panel.addUIElement(marshalPanel).belowLeft(internalAffairsHeader, 0);


        // Award Fief
        TooltipMakerAPI fiefPanel = panel.createUIElement(width / 2, 80, false);
        fiefPanel.addPara(words("laws.unclaimed_fief"), uiColor, pad);
        MarketAPI fief = laws.getFiefAward();
        if (fief != null) {
            fiefPanel.addPara(fief.getName(), faction.getBaseUIColor(), 3 * pad);
        } else {
            fiefPanel.addPara(words("laws.none"), faction.getBaseUIColor(), 3 * pad);
        }
        ButtonAPI fiefButton = fiefPanel.addButton(words("laws.award_fief"), AWARD_FIEF_BUTTON, 150, 20, pad);
        if (fief == null) {
            fiefButton.setEnabled(false);
        }
        requireStanding(fiefButton, fiefPanel, 0);
        panel.addUIElement(fiefPanel).belowMid(internalAffairsHeader, 0);


        // War and Peace
        TooltipMakerAPI diplomacyPanel = panel.createUIElement(width, headerHeight, false);
        diplomacyPanel.addSectionHeading(words("laws.foreign_relations"),
                faction.getBrightUIColor(), faction.getDarkUIColor(), Alignment.LMID, opad);
        ButtonAPI button1 = diplomacyPanel.addButton(words("laws.declare_war"), DECLARE_WAR_BUTTON, 150, 20, opad);
        requireStanding(button1, diplomacyPanel, 1);
        ButtonAPI button2 = diplomacyPanel.addButton(words("laws.sue_for_peace"), SUE_FOR_PEACE_BUTTON, 150, 20, opad);
        requireStanding(button2, diplomacyPanel, 1);
        button2.getPosition().rightOfTop(button1, opad);
        panel.addUIElement(diplomacyPanel).belowLeft(marshalPanel, opad);


        // King-only laws
        TooltipMakerAPI rulerPanel = null;
        if (faction.equals(Global.getSector().getPlayerFaction())) {
            // promote lord, demote lord, exile lord, revoke fief
            rulerPanel = panel.createUIElement(width, headerHeight, false);
            rulerPanel.addSectionHeading(words("laws.ruler_actions"),
                    faction.getBrightUIColor(), faction.getDarkUIColor(), Alignment.LMID, opad);
            ButtonAPI button3 = rulerPanel.addButton(words("laws.grant_revoke_title"), CHANGE_TITLE_BUTTON, faction.getBrightUIColor(),
                    faction.getDarkUIColor(), 150, 20, opad);
            ButtonAPI button4 = rulerPanel.addButton(words("laws.revoke_fief"), REVOKE_FIEF_BUTTON, faction.getBrightUIColor(),
                    faction.getDarkUIColor(), 150, 20, opad);
            ButtonAPI button5 = rulerPanel.addButton(words("laws.exile_lord"), EXILE_LORD_BUTTON, faction.getBrightUIColor(),
                    faction.getDarkUIColor(), 150, 20, opad);
            button4.getPosition().rightOfTop(button3, opad);
            button5.getPosition().rightOfTop(button4, opad);
            panel.addUIElement(rulerPanel).belowLeft(diplomacyPanel, opad);
        }

        // Personal Decisions
        TooltipMakerAPI personalPanel = panel.createUIElement(width, headerHeight, false);
        personalPanel.addSectionHeading(words("laws.personal_decisions"),
                faction.getBrightUIColor(), faction.getDarkUIColor(), Alignment.LMID, opad);
        ButtonAPI button6 = personalPanel.addButton(words("laws.transfer_fief"), TRANSFER_FIEF_BUTTON, faction.getBrightUIColor(),
                faction.getDarkUIColor(), 150, 20, opad);

        if(LordController.getPlayerLord().getFiefs().isEmpty())
            button6.setEnabled(false);
        if (PlayerProgression.getFaction() != null) {
            String standing = Presentation.format("progression.standing", "title", PlayerProgression.getStandingTitle());
            String suspension = PlayerProgression.getSuspensionText();
            if (!suspension.isEmpty()) standing += " " + suspension;
            List<String> missing = PlayerProgression.getMissingRequirements();
            if (!missing.isEmpty()) {
                standing += " " + Presentation.format("progression.next_step",
                        "requirements", PlayerProgression.joinRequirements(missing));
            }
            personalPanel.addPara(standing, opad);
        }
        if(rulerPanel == null)
            panel.addUIElement(personalPanel).belowLeft(diplomacyPanel, opad);
        else
            panel.addUIElement(personalPanel).belowLeft(rulerPanel, opad);

    }

    // this screen shows one faction's affairs, so its wording follows that faction's profile
    private String words(String key) {
        return Presentation.textFor(faction, key);
    }

    // disables a proposal button the player's standing doesn't allow yet; call right after adding the button
    private void requireStanding(ButtonAPI button, TooltipMakerAPI panel, int requiredStanding) {
        if (PlayerProgression.getEffectiveStanding() >= requiredStanding) return;
        button.setEnabled(false);
        panel.addTooltipToPrevious(new ToolTip(250, PlayerProgression.getRequirementText(requiredStanding)),
                TooltipMakerAPI.TooltipLocation.BELOW);
    }

    private TooltipMakerAPI addLawRow(CustomPanelAPI panel, UIComponentAPI prev,
                                      Lawset.LawType type, FactionAPI faction, ButtonObject buttonObject,
                                      int requiredStanding) {
        boolean allowed = PlayerProgression.getEffectiveStanding() >= requiredStanding;
        float gridHeight = 50;
        TooltipMakerAPI label = panel.createUIElement(100, gridHeight, false);
        LabelAPI title = label.addPara(Presentation.lawName(faction, type), 0);
        title.setAlignment(Alignment.MID);
        label.addTooltipToPrevious(new ToolTip(200, Presentation.policyDescription(faction, type)),
                TooltipMakerAPI.TooltipLocation.BELOW);
        panel.addUIElement(label).belowLeft(prev, 0);
        LawLevel level = PoliticsController.getLaws(faction).getLawLevel(type);
        prev = label;
        for (int i = 0; i < 5; i++) {
            TooltipMakerAPI buttonPanel = panel.createUIElement(50, gridHeight, false);
            ButtonAPI button;
            if (level.ordinal() == i) {
                Color bgColor = faction.isPlayerFaction() ? DARK_GOLD : faction.getDarkUIColor();
                button = buttonPanel.addButton(Presentation.levelName(faction, LawLevel.values()[i]), buttonObject.clone(i), faction.getBrightUIColor(),
                        bgColor, 50, gridHeight, 0);
                button.setClickable(false);
                button.setMouseOverSound(null);
            } else if (Math.abs(level.ordinal() - i) == 1) {
                button = buttonPanel.addButton(Presentation.levelName(faction, LawLevel.values()[i]), buttonObject.clone(i), 50, gridHeight, 0);
                if (!allowed) button.setEnabled(false);
            } else {
                button = buttonPanel.addButton(Presentation.levelName(faction, LawLevel.values()[i]), buttonObject.clone(i), 50, gridHeight, 0);
                button.setEnabled(false);
                button.setClickable(false);
                button.setMouseOverSound(null);
            }

            String message = Presentation.levelEffects(faction, type, i);
            List<String> messages = new ArrayList<>(Arrays.asList(message.split("\\|")));
            if (!allowed && Math.abs(level.ordinal() - i) == 1) {
                messages.add(PlayerProgression.getRequirementText(requiredStanding));
            }
            buttonPanel.addTooltipToPrevious(new ToolTip(200, messages),
                    TooltipMakerAPI.TooltipLocation.BELOW);

            panel.addUIElement(buttonPanel).rightOfTop(prev, 0);
            prev = buttonPanel;
        }
        return label;
    }

    @Override
    public void createConfirmationPrompt(Object buttonId, TooltipMakerAPI prompt) {
        if (buttonId == START_CAMPAIGN_BUTTON) {
            prompt.addPara(words("laws.this_will_call_the_lords_of_the_realm_to_arms_continue"), 3);
        } else if (buttonId == END_CAMPAIGN_BUTTON) {
            prompt.addPara(words("laws.this_will_end_the_current_campaign_continue"), 3);
        } else if (buttonId instanceof ButtonObject) {
            prompt.addPara(words("laws.you_are_submitting_a_new_legislative_proposal_continue"), 3);
        }
    }

    @Override
    public boolean doesButtonHaveConfirmDialog(Object buttonId) {
        if (buttonId instanceof ButtonObject || buttonId == START_CAMPAIGN_BUTTON
                || buttonId == END_CAMPAIGN_BUTTON) return true;
        return super.doesButtonHaveConfirmDialog(buttonId);
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, final IntelUIAPI ui) {
        if (buttonId == START_CAMPAIGN_BUTTON) {
            // TODO
            LordEvent campaign = new LordEvent(LordEvent.CAMPAIGN, LordController.getPlayerLord());
            EventController.addCampaign(campaign);
            ui.updateUIForItem(this);
            return;
        } else if (buttonId == END_CAMPAIGN_BUTTON) {
            LordEvent campaign = EventController.getCurrentCampaign(faction);
            EventController.endCampaign(campaign);
            ui.updateUIForItem(this);
            return;
        }

        // cant propose a new law if player's current proposal is being debated
        LawProposal currDebate = PoliticsController.getCurrProposal(Utils.getRecruitmentFaction());
        if (currDebate != null && currDebate.getOriginator().equals(
                LordController.getPlayerLord().getLordAPI().getId())) return;

        if (buttonId instanceof ButtonObject) {
            ButtonObject buttonObject = (ButtonObject) buttonId;
            Lawset.LawType law = Lawset.LawType.valueOf(buttonObject.type);
            LawProposal proposal = new LawProposal(law, LordController.getPlayerLord().getLordAPI().getId(),
                    null, null, null, buttonObject.level);
            proposal.setPlayerSupports(true);
            PoliticsController.addProposal(LordController.getPlayerLord(), proposal);
            PoliticsController.updateProposal(proposal);
            ui.updateIntelList();
        } else if (buttonId == APPOINT_MARSHAL_BUTTON || buttonId == AWARD_FIEF_BUTTON || buttonId == EXILE_LORD_BUTTON) {
            String message;
            final String targetFief;
            final Lawset.LawType law;
            final Lord playerLord = LordController.getPlayerLord();
            if (buttonId == APPOINT_MARSHAL_BUTTON) {
                law = Lawset.LawType.APPOINT_MARSHAL;
                targetFief = null;
                message = words("laws.you_are_proposing_new_legislation_select_who_you_nominate_to_be_");
            } else if (buttonId == AWARD_FIEF_BUTTON) {
                law = Lawset.LawType.AWARD_FIEF;
                targetFief = laws.getFiefAward().getId();
                message = words("laws.you_are_proposing_new_legislation_select_who_you_nominate_to_rec") + laws.getFiefAward().getName() + ".";
            } else {
                law = Lawset.LawType.EXILE_LORD;
                targetFief = null;
                message = words("laws.you_are_proposing_new_legislation_select_who_you_nominate_to_be__316");
            }
            ArrayList<Lord> lords = new ArrayList<>();
            ArrayList<String> options = new ArrayList<>();
            ArrayList<String> retVals = new ArrayList<>();
            // cant exile player from their own faction
            // nominating yourself as Fleet Commander needs the top tier
            if (buttonId != EXILE_LORD_BUTTON && (
                    !playerLord.isMarshal() || buttonId == AWARD_FIEF_BUTTON)
                    && (buttonId != APPOINT_MARSHAL_BUTTON || PlayerProgression.getEffectiveStanding() >= 2)) {
                lords.add(LordController.getPlayerLord());
            }
            for (Lord lord : LordController.getLordsList()) {
                if (lord.isMarshal() && buttonId == APPOINT_MARSHAL_BUTTON) continue;
                if (lord.getFaction().equals(faction)) {
                    lords.add(lord);
                }
            }
            Utils.canonicalLordSort(lords);
            for (Lord lord : lords) {
                    options.add(lord.getTitle() + " " + lord.getLordAPI().getNameString());
                    retVals.add(lord.getLordAPI().getId());
            }
            final SelectItemDialogPlugin plugin = new SelectItemDialogPlugin(message, options, retVals);
            plugin.setPostScript(new Script() {
                @Override
                public void run() {
                    String selection = plugin.getRetVal();
                    if (selection != null) {
                        LawProposal proposal = new LawProposal(law, playerLord.getLordAPI().getId(),
                                selection, targetFief, null, 0);
                        proposal.setPlayerSupports(true);
                        PoliticsController.addProposal(LordController.getPlayerLord(), proposal);
                        PoliticsController.updateProposal(proposal);
                        ui.updateIntelList();
                    }
                }
            });
            ui.showDialog(null, plugin);

        } else if (buttonId == DECLARE_WAR_BUTTON || buttonId == SUE_FOR_PEACE_BUTTON) {
            String message;
            final Lawset.LawType law;
            if (buttonId == DECLARE_WAR_BUTTON) {
                law = Lawset.LawType.DECLARE_WAR;
                message = words("laws.you_are_proposing_new_legislation_select_the_faction_you_propose");

            } else {
                law = Lawset.LawType.SUE_FOR_PEACE;
                message = words("laws.you_are_proposing_new_legislation_select_the_faction_you_propose_363");
            }

            ArrayList<String> options = new ArrayList<>();
            ArrayList<String> retVals = new ArrayList<>();
            // only can do diplo on factions with markets
            for (FactionAPI faction : LordController.getFactionsWithLords()) {
                if (faction.equals(this.faction)) continue;
                if (faction.equals(Global.getSector().getPlayerFaction())) continue;
                //if (!Utils.canHaveRelations(faction)) continue;
                if (!FactionTemplateController.getTemplate(faction).isCanPreformDiplomacy()) continue;
                if (!FactionTemplateController.getTemplate(this.faction).isCanPreformDiplomacy()) continue;
                if (faction.isHostileTo(this.faction) != (buttonId == DECLARE_WAR_BUTTON)) {
                    options.add(faction.getDisplayName());
                    retVals.add(faction.getId());
                }
            }
            final SelectItemDialogPlugin plugin = new SelectItemDialogPlugin(message, options, retVals);
            plugin.setPostScript(new Script() {
                @Override
                public void run() {
                    String selection = plugin.getRetVal();
                    if (selection != null) {
                        LawProposal proposal = new LawProposal(law, LordController.getPlayerLord().getLordAPI().getId(),
                                null, null, selection, 0);
                        proposal.setPlayerSupports(true);
                        PoliticsController.addProposal(LordController.getPlayerLord(), proposal);
                        PoliticsController.updateProposal(proposal);
                        ui.updateIntelList();
                    }
                }
            });
            ui.showDialog(null, plugin);

        } else if (buttonId == REVOKE_FIEF_BUTTON) {
            String message = words("laws.you_are_proposing_new_legislation_select_the_fief_to_revoke");
            ArrayList<String> options = new ArrayList<>();
            ArrayList<String> retVals = new ArrayList<>();
            FactionAPI faction = Utils.getRecruitmentFaction();
            for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
                Lord owner = FiefController.getOwner(market);
                if (faction.equals(market.getFaction()) && owner != null) {
                    options.add(market.getName() + words("laws.owned_by") + owner.getLordAPI().getNameString());
                    retVals.add(market.getId());
                }
            }
            final SelectItemDialogPlugin plugin = new SelectItemDialogPlugin(message, options, retVals);
            plugin.setPostScript(new Script() {
                @Override
                public void run() {
                    String selection = plugin.getRetVal();
                    if (selection != null) {
                        MarketAPI market = Global.getSector().getEconomy().getMarket(selection);
                        LawProposal proposal = new LawProposal(Lawset.LawType.REVOKE_FIEF,
                                LordController.getPlayerLord().getLordAPI().getId(),
                                FiefController.getOwner(market).getLordAPI().getId(),
                                selection, null, 0);
                        proposal.setPlayerSupports(true);
                        PoliticsController.addProposal(LordController.getPlayerLord(), proposal);
                        PoliticsController.updateProposal(proposal);
                        ui.updateIntelList();
                    }
                }
            });
            ui.showDialog(null, plugin);
        } else if (buttonId == CHANGE_TITLE_BUTTON) {
            String message = words("laws.you_are_proposing_new_legislation_select_the_lord_to_be_promoted");
            ArrayList<Lord> lords = new ArrayList<>();
            ArrayList<String> options = new ArrayList<>();
            ArrayList<String> retVals = new ArrayList<>();
            FactionAPI faction = Utils.getRecruitmentFaction();
            for (Lord lord : LordController.getLordsList()) {
                if (lord.getFaction().equals(faction)) {
                    lords.add(lord);
                }
            }
            Utils.canonicalLordSort(lords);
            for (Lord lord : lords) {
                options.add(lord.getTitle() + " " + lord.getLordAPI().getNameString());
                retVals.add(lord.getLordAPI().getId());
            }

            final SelectItemDialogPlugin plugin = new SelectItemDialogPlugin(message, options, retVals);
            plugin.setPostScript(new Script() {
                @Override
                public void run() {
                    final String selection = plugin.getRetVal();
                    if (selection != null) {
                        // nested dialogs, yay
                        Lord lord = LordController.getLordOrPlayerById(selection);
                        String message2 = words("laws.select_new_rank_for") + lord.getLordAPI().getNameString();
                        int rank = lord.getRanking();
                        ArrayList<String> options2 = new ArrayList<>();
                        ArrayList<String> retVals2 = new ArrayList<>();
                        for (int i = 0; i < 3; i++) {
                            if (rank != i) {
                                options2.add(Presentation.format("standing.option", "title", Utils.getTitle(lord.getFaction(), i), "tier", Integer.toString(i + 1)));
                                retVals2.add(Integer.toString(i));
                            }
                        }
                        final SelectItemDialogPlugin plugin2 = new SelectItemDialogPlugin(message2, options2, retVals2);
                        plugin2.setPostScript(new Script() {
                            @Override
                            public void run() {
                                String selection2 = plugin2.getRetVal();
                                if (selection2 != null) {
                                    int newRank = Integer.valueOf(selection2);
                                    LawProposal proposal = new LawProposal(Lawset.LawType.CHANGE_RANK,
                                            LordController.getPlayerLord().getLordAPI().getId(),
                                            selection, null, null, newRank);
                                    proposal.setPlayerSupports(true);
                                    PoliticsController.addProposal(LordController.getPlayerLord(), proposal);
                                    PoliticsController.updateProposal(proposal);
                                    ui.updateIntelList();
                                }
                            }
                        });
                        ui.showDialog(null, plugin2);
                    }
                }
            });
            ui.showDialog(null, plugin);

        }
        else if (buttonId == TRANSFER_FIEF_BUTTON) {
            String message = words("laws.you_are_going_to_transfer_one_of_your_fiefs_to_another_lord_sele");
            ArrayList<Lord> lords = new ArrayList<>();
            ArrayList<String> optionsLords = new ArrayList<>();
            ArrayList<String> retValsLords = new ArrayList<>();
            for (Lord lord : LordController.getLordsList()) {
                if (lord.getFaction().equals(faction) && !lord.isPlayer()) {
                    lords.add(lord);
                }
            }
            Utils.canonicalLordSort(lords);
            for (Lord lord : lords) {
                optionsLords.add(lord.getTitle() + " " + lord.getLordAPI().getNameString());
                retValsLords.add(lord.getLordAPI().getId());
            }
            final SelectItemDialogPlugin plugin = new SelectItemDialogPlugin(message, optionsLords, retValsLords);
            plugin.setPostScript(new Script() {
                @Override
                public void run() {
                    final String selection = plugin.getRetVal();
                    if (selection != null) {
                        // nested dialogs, yay
                        Lord lord = LordController.getLordById(selection);
                        String messageFiefs = words("laws.select_whom_to_transfer_fief_to") + lord.getLordAPI().getNameString();
                        ArrayList<String> optionsFiefs = new ArrayList<>();
                        ArrayList<String> retValsFiefs = new ArrayList<>();
                        FactionAPI faction = Utils.getRecruitmentFaction();
                        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
                            Lord owner = FiefController.getOwner(market);
                            if (faction.equals(market.getFaction()) && owner == LordController.getPlayerLord()) {
                                optionsFiefs.add(market.getName() + words("laws.owned_by") + owner.getLordAPI().getNameString());
                                retValsFiefs.add(market.getId());
                            }
                        }
                        final SelectItemDialogPlugin plugin2 = new SelectItemDialogPlugin(messageFiefs, optionsFiefs, retValsFiefs);
                        plugin2.setPostScript(new Script() {
                            @Override
                            public void run() {
                                String selection2 = plugin2.getRetVal();
                                if (selection2 != null) {

                                    MarketAPI market = FiefController.getMarketByID(selection2);
                                    Lord targetLord = LordController.getLordById(selection);

                                    FiefController.playerTransferFief(targetLord,market);


                                }
                            }
                        });
                        ui.showDialog(null, plugin2);
                    }
                }
            });
            ui.showDialog(null, plugin);
        }
    }

    @Override
    public boolean hasSmallDescription() {
        return false;
    }

    @Override
    public boolean hasLargeDescription() {
        return true;
    }

    @Override
    public String getName() {
        return Utils.getRecruitmentFaction().getDisplayName() + words("laws.realm_laws");
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return Utils.getRecruitmentFaction();
    }

    @Override
    public String getIcon() {
        return getFactionForUIColors().getCrest();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(StringUtil.getString(CATEGORY_UI, "politics_category"));
        return tags;
    }

    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_1;
    }

    public static LawsIntelPlugin getInstance(boolean forceReset) {
        if (instance == null || forceReset) {
            List<IntelInfoPlugin> intel = Global.getSector().getIntelManager().getIntel(LawsIntelPlugin.class);
            if (intel.isEmpty()) {
                instance = new LawsIntelPlugin();
                Global.getSector().getIntelManager().addIntel(instance, true);
            } else {
                if (intel.size() > 1) {
                    throw new IllegalStateException("Should only be one LawsIntelPlugin intel registered");
                }
                instance = (LawsIntelPlugin) intel.get(0);
            }
        }
        return instance;
    }

    public static LawsIntelPlugin getInstance() {
        return getInstance(false);
    }
}
