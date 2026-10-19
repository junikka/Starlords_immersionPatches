# Player progression and starting policies

> **Status (2026-10-19):** implemented as approved by Julie, with these decisions: the Church starts at *Strict* Rave Licensing and the Path at *Banned*; a failed promotion vote costs nothing and only starts a short cooldown (30 days); the vote itself is the existing, deterministic council vote. Needs a JAR rebuild and an in-game test.

Every number below lives in `data/lords/faction.json`, so a modded faction can tune it or leave it out and inherit the defaults.

## 1. Earning your place in a faction

Accepting a commission no longer hands you a full council seat. You climb four steps, and each one unlocks more of the council.

| Step | Shown as | You can propose | To reach the next step (defaults) |
|---|---|---|---|
| Commissioned | "Commissioned officer" | Nothing. You can still talk, sway votes, take quests, join expeditions and receive a holding someone else proposes. Your vote doesn't count yet. | Serve 60 days and reach **Welcoming** with the faction. The seat is granted automatically. |
| Tier 0 seat | e.g. Commodore / Deputy Star Marshal | Holdings, Revenue Policy, Rave Licensing | Serve 90 days at this rank, reach **Friendly**, and hold a holding *or* win 3 battles alongside faction fleets |
| Tier 1 | e.g. Admiral / Star Marshal | Also war and peace, Central Authority, Privilege of Rank, and nominating others as Fleet Commander | Serve 120 days at this rank, reach **Cooperative**, and have 3 commanders at relation 25 or more |
| Tier 2 | e.g. Fleet Admiral / Grand Marshal | Also nominating yourself as Fleet Commander | none |

**Keeping your standing:** each rank also needs its reputation to stay in force: Welcoming for a seat, Friendly for tier 1 and Cooperative for tier 2. If your reputation drops below that, you keep the rank and title, but the council treats you as the highest rank your reputation still supports. You lose the proposals above that rank, and below Welcoming you lose your vote. Everything comes back when the reputation recovers. The Politics screen and `promoteplayer status` say when this is happening.

**Fleet greetings:** once you hold a seat in good standing, ordinary fleets of that faction greet you by rank instead of with the generic vanilla line. Each faction has three greetings, one per rank, in `data/lords/presentation/greetings.json`. Factions not listed there use the default set. Starlords commanders keep their own conversations, and hostile fleets or fleets with a special story greeting are unaffected. Your transponder has to be on for them to know who you are.

**How promotion happens:** once a month, if you meet the next step's requirements, the commander who likes you most (ties broken by ID, so it's deterministic) puts forward a rank-change proposal for you. The council votes on it like any other proposal. That commander keeps the proposal on the table until the council takes it up. If the vote fails, nobody's opinion of you drops, and a new sponsor can try again after the cooldown.

**Asking in person:** under *Ask a question*, a commander of your faction offers "Ask them to put your name forward for promotion" once you hold a seat and aren't at the top. If you meet the requirements and they like you (relation above 0), that commander sponsors you on the spot, without waiting for the monthly check. Otherwise they say why not: you're not ready yet (the missing requirements are listed), a promotion is already pending, the last vote was too recent, they don't like you enough, or their own proposal is being debated. The yes and no answers have a line per personality.

Locked buttons in the Politics screen are greyed out, and their tooltip says what rank they need. The Personal decisions section shows your standing and what the next step still needs.

**Your own faction** is unchanged: you rule it from the start. Changing your commission starts the climb again at Commissioned. A save that already had a commission also starts at Commissioned, with the 60 days counted from the first day after the update.

The defaults were raised on Julie's request so that ranks read as the faction's elite: the original proposal had Favorable / Welcoming / Friendly and no time in rank after the seat.

**Per-faction tuning:** add a `progression` block. Every key is optional.

```json
"luddic_church": {
  "progression": {
    "seatAfterDays": 90,
    "seatRep": "WELCOMING",
    "tier1AfterDays": 120,
    "tier1Rep": "FRIENDLY",
    "tier1Battles": 3,
    "tier2AfterDays": 180,
    "tier2Rep": "COOPERATIVE",
    "tier2Friends": 3,
    "tier2FriendRelation": 25,
    "promotionCooldownDays": 30
  }
}
```

Reputation levels use the vanilla names: `VENGEFUL`, `HOSTILE`, `INHOSPITABLE`, `SUSPICIOUS`, `NEUTRAL`, `FAVORABLE`, `WELCOMING`, `FRIENDLY`, `COOPERATIVE`.

**Testing:** the `promoteplayer` console command.

| Command | Effect |
|---|---|
| `promoteplayer` | One step up, skipping requirements and the vote. Your reputation is raised to what the new step needs. |
| `promoteplayer commissioned` / `0` / `1` / `2` | Jump to that step, also raising reputation as needed |
| `promoteplayer status` | Show your step and what the next one still needs |
| `promoteplayer vote` | Start a real promotion vote now, skipping the requirements and the monthly wait |

## 2. Starting policies per faction

Each faction's `startingPolicies` block sets its sliders when a new campaign starts. The levels are `VERY_LOW`, `LOW`, `MEDIUM`, `HIGH` and `VERY_HIGH`, and any policy left out starts at `MEDIUM`.

| Faction | Central Authority | Privilege of Rank | Revenue Policy | Rave Licensing |
|---|---|---|---|---|
| Hegemony | High | High | Balanced | Licensed |
| Sindrian Diktat | Very High | Medium | Heavy Levies | Licensed |
| Persean League | Low | Very High | Light Levies | Permissive |
| Tri-Tachyon | Medium | Low | Free Trade | Anything Goes |
| Luddic Church | High | Medium | Heavy Levies | **Strict** (about one rave a year) |
| Luddic Path | Very High | Very Low | Extortionate | **Banned** |
| Pirates | Very Low | Low | Extortionate | Anything Goes |
| Independent, player | Medium ×4 | | | |

```json
"luddic_church": {
  "startingPolicies": {
    "crown_authority": "HIGH",
    "noble_authority": "MEDIUM",
    "trade_law": "HIGH",
    "feast_law": "HIGH"
  }
}
```

The internal IDs keep their old names: `crown_authority` is Central Authority, `noble_authority` is Privilege of Rank, `trade_law` is Revenue Policy and `feast_law` is Rave Licensing.

**Where it applies:** new campaigns only, so existing saves keep their current sliders. The council can still vote the levels up or down afterwards. *Banned* means Path commanders never hold raves; if that cuts too much of the social game, change `feast_law` to `HIGH`.
