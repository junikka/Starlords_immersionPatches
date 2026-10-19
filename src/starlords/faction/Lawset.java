package starlords.faction;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import starlords.controllers.LordController;
import lombok.Getter;
import lombok.Setter;
import starlords.person.Lord;
import starlords.util.factionUtils.FactionTemplate;
import starlords.util.factionUtils.FactionTemplateController;



@Getter
@Setter
public class Lawset {
    public enum LawType {
        CROWN_AUTHORITY(3, 2, 0, 2, 4),
        NOBLE_AUTHORITY(3, 2, 2, 2, 1),
        TRADE_LAW(2, 4, 0, 2, 2),
        FEAST_LAW(3, 3, 1, 0, 2),
        APPOINT_MARSHAL,
        AWARD_FIEF,
        DECLARE_WAR,
        SUE_FOR_PEACE,
        REVOKE_FIEF,
        CHANGE_RANK,
        EXILE_LORD;

        // display names depend on the deciding faction, see Presentation.lawName
        public int[] pref; // preferred level for law for upstanding, martial, calculating, quarrelsome, and ruler

        LawType() {}

        LawType(int u, int m, int c, int q, int r) {
            pref = new int[]{u, m, c, q, r};
        }
    }

    private String factionId;
    private String marshal;
    private LawLevel crownAuthority;
    private LawLevel nobleAuthority; // power of high-rank nobles
    private LawLevel tradeFavor;
    private LawLevel feastLaw;
    private MarketAPI fiefAward;

    public Lawset(FactionAPI faction) {
        factionId = faction.getId();
        // startingPolicies in data/lords/faction.json; only read when the faction's laws are first created
        FactionTemplate template = FactionTemplateController.getTemplate(faction);
        crownAuthority = template.getStartingPolicy(LawType.CROWN_AUTHORITY);
        nobleAuthority = template.getStartingPolicy(LawType.NOBLE_AUTHORITY);
        tradeFavor = template.getStartingPolicy(LawType.TRADE_LAW);
        feastLaw = template.getStartingPolicy(LawType.FEAST_LAW);
        marshal = null;
        for (Lord lord : LordController.getLordsList()) {
            if (faction.equals(lord.getLordAPI().getFaction()) && lord.getRanking() == 2) {
                marshal = lord.getLordAPI().getId();
            }
        }
    }

    public LawLevel getLawLevel(LawType type) {
        switch(type) {
            case CROWN_AUTHORITY:
                return crownAuthority;
            case NOBLE_AUTHORITY:
                return nobleAuthority;
            case TRADE_LAW:
                return tradeFavor;
            case FEAST_LAW:
                return feastLaw;
        }
        return null;
    }
}
