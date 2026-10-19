package starlords.ui;

import com.fs.starfarer.api.Global;
import starlords.presentation.Presentation;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import starlords.person.Lord;
import starlords.person.LordEvent;
import starlords.util.Utils;

import java.awt.*;
import java.util.Set;

// Small intel entry to remind player of ongoing feasts and campaigns
public class EventIntelPlugin extends BaseIntelPlugin {

    private final LordEvent event;
    private static final float pad = 3;
    private static final float opad = 10;
    private static final int MAX_PORTRAITS_PER_ROW = 6;

    public EventIntelPlugin(LordEvent event) {
        setImportant(true);
        this.event = event;
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color uiColor = Global.getSettings().getBasePlayerColor();
        Lord originator = event.getOriginator();
        FactionAPI faction = originator.getFaction();
        info.addImages(256, 128, pad, 0, faction.getCrest(),
                originator.getLordAPI().getPortraitSprite());
        switch (event.getType()) {
            case LordEvent.CAMPAIGN:
                info.addPara(Presentation.format("event.campaign.description", "commander", originator.getLordAPI().getNameString()), opad);
                info.addPara(Presentation.format("event.location", "location", Utils.getNearbyDescription(originator.getFleet())), uiColor, opad);
                String orderStr = Presentation.text("event.assembling");
                if (event.getTarget() != null) {
                    MarketAPI target = event.getTarget().getMarket();
                    if (target.getFaction().isHostileTo(faction)) {
                        orderStr = Presentation.format("event.attacking", "market", target.getName());
                    } else {
                        orderStr = Presentation.format("event.defending", "market", target.getName());
                    }
                }
                info.addPara(Presentation.format("event.orders", "orders", orderStr), uiColor, pad);
                break;
            case LordEvent.FEAST:
                info.addPara(Presentation.format("event.feast.description", "host", originator.getLordAPI().getNameString()), opad);
                info.addPara(Presentation.format("event.location", "location", Utils.getNearbyDescription(originator.getFleet())), uiColor, opad);
                break;
        }
        LabelAPI supportText = info.addPara(Presentation.text("event.attendees"), uiColor, pad);
        int numPortraits = 0;
        int totalPortraits = 1 + event.getParticipants().size();
        String[] portraits = new String[Math.min(MAX_PORTRAITS_PER_ROW, totalPortraits - numPortraits)];
        portraits[0] = event.getOriginator().getLordAPI().getPortraitSprite();
        for (int i = 1; i < portraits.length; i++) {
            portraits[i] = event.getParticipants().get(numPortraits + i - 1).getLordAPI().getPortraitSprite();
        }
        info.addImages(48 * portraits.length, 48, pad, 0, portraits);
        numPortraits += portraits.length;
        while (numPortraits < totalPortraits) {
            portraits = new String[Math.min(MAX_PORTRAITS_PER_ROW, totalPortraits - numPortraits)];
            for (int i = 0; i < portraits.length; i++) {
                portraits[i] = event.getParticipants().get(numPortraits + i - 1).getLordAPI().getPortraitSprite();
            }
            info.addImages(48 * portraits.length, 48, pad, 0, portraits);
            numPortraits += portraits.length;
        }
    }

    @Override
    public boolean hasSmallDescription() {
        return true;
    }

    @Override
    public boolean hasLargeDescription() {
        return false;
    }

    @Override
    public boolean isHidden() {
        return !Utils.getRecruitmentFaction().equals(event.getOriginator().getFaction());
    }

    @Override
    public boolean isDone() {
        return !event.isAlive();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        switch (event.getType()) {
            case LordEvent.FEAST:
                return event.getTarget();
            default:
                return event.getOriginator().getFleet();
        }
    }

    @Override
    public boolean shouldRemoveIntel() {
        return !event.isAlive();
    }

    @Override
    public String getName() {
        return Presentation.text(LordEvent.FEAST.equals(event.getType()) ? "event.feast.heading" : "event.campaign.heading");
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return event.getOriginator().getFaction();
    }

    @Override
    public String getIcon() {
        return event.getOriginator().getLordAPI().getPortraitSprite();
    }

    public Color getTitleColor(ListInfoMode mode) {
        return event.getOriginator().getFaction().getBaseUIColor();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add("Missions");
        return tags;
    }
}
