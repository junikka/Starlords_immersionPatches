# Where Star Lords clashes with vanilla, and a way out

**Status: audit and proposal for Julie (2026-10-25), against build 1.2.** Nothing is implemented. Every clash below was checked in this repository's code and data, or in the game's and Nexerelin's sources on the dev PC.

## Short answer

Yes. Star Lords is a second government bolted next to the vanilla one, not an extension of it. It brings its own cast (48 invented commanders), its own idea of who owns a world, its own rank ladder, its own council and its own wars. Vanilla already has most of those things, with real names: a leader at the capital, an administrator on every world, a base commander at every military base, lore admirals and generals, System Defense Fleets with named admirals, and Nexerelin's diplomacy and invasions. The two run side by side and mostly ignore each other. That's why it feels like a side gig.

## The clashes, point by point

### Who rules
1. **An invented cast in lore jobs.** The 48 commanders in `data/lords/lords.json` (Hegemony 12, Persean League 10, Tri-Tachyon 8, Church 6, Diktat 4, pirates 4, Path 4) are original characters, and their biographies claim lore posts. Nevsky is written as head of the Lion's Guard, which is Horacio Caden in vanilla (`People.CADEN`, `GUARD_HIGH_DEPUTY_EXECUTOR`, *The Usurpers*). Meanwhile vanilla's real elite (Orcus Rao, Laverna Tell, the Kato and Yaribay houses, Archcurate Jaspis, Oxana Hyder…) have no part in Star Lords at all.
2. **The ruler is absent.** The faction leader appears only as a vote multiplier (`leaderID` in `faction.json`, `PoliticsController` "influence of ruler"). Andrada never shows up in Star Lords; his realm is run by people the game otherwise never mentions.
3. **A parallel council.** The council that votes on policy, war and peace, holdings and promotions consists only of the invented commanders, plus the player.

### Who owns the worlds
4. **Holdings over vanilla administrators.** Commanders "hold" worlds that vanilla says someone else runs: Nevsky holds Sindria (Andrada's capital), Sartorius holds Chicomoztoc (Daud's), Askia holds Eochu Bres (Sun's), Kazimierz holds Kazeron (Hannan's). Luan Jee Bak holds Ragnar Complex (Orcus Rao's), Kaliman holds Cruor (Laverna Tell's), and so on. A holding is only an income claim in `FiefController`: the administrator, the colony and the comm board never hear about it.
5. **Defection flips markets the raw way.** A defecting commander can take low-stability holdings with them through `market.setFactionId()` (`DefectionUtils`, line 275). That skips Nexerelin's market transfer: admins, comm-board people, stations. Nexerelin handles this carefully when it captures a market (`SectorManager`, `ColonyManager`).

### Ranks and offices
6. **Ranks replace vanilla's.** Commanders get Star Lords' own rank IDs (`starlords_tier_0/1/2`, `Lord.syncVanillaRank`) instead of vanilla's (`SPACE_ADMIRAL`, `DEPUTY_STAR_MARSHAL`…). Their titles come from a three-step ladder per faction. Several ladders were invented for the mod: Doux / Megas Doux / Archon, the Vice President ladder, Deacon-Captain / Bishop / Archbishop. That's why they read strangely: vanilla separates **rank** (what you are) from **post** (what you do: administrator, base commander, fleet commander, faction leader), and Star Lords merges both into one tier.
7. **An invented Fleet Commander office.** The council's Fleet Commander overlaps vanilla's own fleet commanders: Hyder (Diktat), and the admirals of the four System Defense Fleets, which colony crises depend on (`SDFHegemony` and the others).

### War
8. **A second war machine.**
   - **Star Lords expeditions** run in parallel with Nexerelin's invasions and vanilla's own fleets: patrols, SDFs, and Hyder's and Caden's personal fleets.
   - **Council war and peace votes** are a second source of diplomacy. They drive Nexerelin's `DiplomacyManager.adjustRelations`, or vanilla relations directly without Nexerelin, on top of Nexerelin's own diplomacy AI.

### The player
9. **A ladder on top of the commission.** The vanilla commission (granted by a base commander, paid monthly) now leads into Star Lords' own seat-and-tier ladder. Vanilla has no such thing, so it reads as another system rather than the commission growing up.
10. **Two kinds of important person.** Vanilla's elite live on comm boards; Star Lords' commanders exist only as fleets. They never meet.

### Economy and society
11. **A private economy:** commander wealth, upkeep, trade ventures and holding revenue. None of it touches vanilla's markets, so policies like *Revenue Policy* change nothing the player can see in the colony screens.
12. **Raves, tournaments, courtship:** an invented social layer. The recent work made it fit much better (elite, faction-specific), but it still runs on the invented cast.

## Why it happened

Star Lords ports Mount & Blade, where the lords **are** the state. Starsector already has a state. Each system was copied from M&B rather than mapped onto what Starsector already has.

## The way out: a ruling elite built from vanilla

Your idea, made concrete: **each faction has a pool of its ruling elite (politicians and high military command) drawn from the people vanilla already has, ordered by an abstract seniority.** They vote on policies that change things vanilla can see. Star Lords' engine stays (AI, relations, council votes, dialogue, raves, progression); the cast, the meaning of "holding", the ranks and the war hooks change.

### Who sits in the elite
- **The leader** at the top (Andrada, Daud, Sun, Hannan, Kanta; Cotton stays hidden). They keep their vanilla post and comm board, and can take the field later (the approved ruler plan).
- **Lore figures** with a place in the state: Orcus Rao, Laverna Tell, Horus Yaribay and Fortuna Kato, Archcurate Jaspis, Gideon Oak, Macario, Hyder, Caden… They're **council members**, voting from where they are. Fleets only for those the [lore research](LORE_CHARACTERS_RESEARCH.md) clears.
- **Vanilla's office holders:** the **administrator** of each of the faction's major worlds, the **base commander** of each military base and high command, and the **System Defense Fleet admiral**. These are real persons vanilla already creates (`CoreLifecyclePluginImpl.createInitialPeople`), sitting on comm boards. They become council members, and the world they run is *their* world: no invented holdings needed.
- **A small number of Star Lords' original commanders,** kept as field admirals where a faction needs more fleets, with biographies rewritten so they don't claim lore jobs. Possibly none.

### Seniority instead of invented ranks
- Members keep their **vanilla rank and post**: the comm panel and quests stay right.
- Star Lords adds only an abstract **seniority** inside the faction, for "who is above whom": leader > the lore inner circle > high command and capital administrators > base commanders and other administrators. It's derived from post and the size of their world, plus merit over time. This number weighs votes. The player climbs the same seniority later.
- Displayed titles come from the vanilla rank and post ("Admiral Orcus Rao, Military Administrator of Ragnar Complex"), not a three-step ladder.

### Policies that matter in vanilla
Policies change things the player can see in vanilla screens, through standard market and faction modifiers:
- **Revenue:** tariffs and stability on the faction's worlds.
- **Military budget:** patrol and defence strength (vanilla patrol and fleet-size modifiers).
- **War and peace:** proposed by the council but **carried out through Nexerelin's diplomacy** (or vanilla relations without Nexerelin), as one input to Nexerelin's own decisions, not a parallel switch.
- **Expeditions:** council-approved offensives become **Nexerelin invasions** (with Nexerelin), led by the elite's military members, instead of a separate campaign system.
- **Rave Licensing** stays.

### What happens to today's features

| Feature | Fate |
|---|---|
| 48 invented commanders | Replaced by the elite pool; a few kept as field admirals, rewritten |
| Holdings | Become "the world a member administers or commands", from vanilla posts |
| Rank ladders and comm-panel ranks | Replaced by vanilla rank and post plus seniority |
| Council, votes, proposals | Kept, with vanilla-backed policies |
| Expeditions, war and peace | Routed through Nexerelin |
| Player progression | Kept, re-expressed as seniority (later, as you said) |
| Raves, dance floor, weddings, greetings, faction profiles | Kept; they work with any cast |
| Defection | Only for members who could plausibly change sides (not leaders or lore figures), and through Nexerelin's transfer |
| Heroes vision | Unchanged: elite members with fleets are heroes; non-military heroes come later |

### Effort, honestly

This is a rebuild of the core, not a patch: the roster, the holdings model, ranks, the council's membership, policy effects and war hooks. The engine (AI, relations, votes, dialogue, raves) carries over. Roughly several focused sessions plus testing, and it needs a new campaign.

## Suggested order

1. **Quick fixes now, worth doing anyway:** route defection's market flips through Nexerelin's transfer.
2. **E1, membership:** build each faction's elite from vanilla persons (leader, lore figures, administrators, base commanders, SDF admirals), with seniority. They vote alongside the current commanders at first, so nothing breaks.
3. **E2, holdings:** a member's world is the one they administer or command; invented holdings retire.
4. **E3, policies with vanilla effects**, and war, peace and expeditions through Nexerelin.
5. **E4, the cast:** retire or rewrite the invented commanders; titles from vanilla ranks and posts.
6. **E5, rulers and lore admirals take the field** (the approved ruler plan), then the wider Heroes vision.

## Questions for you

1. Do you agree with the diagnosis, and with building the elite from vanilla's own people (leader, lore figures, administrators, base commanders, SDF admirals)?
2. The invented commanders: retire them completely, or keep a few per faction as rewritten field admirals?
3. Policies: are tariffs and stability, military budget, war and peace, and invasions via Nexerelin the right levers?
4. Should a world's administrator and base commander become council members automatically, or only for major worlds (for example size 5 and up)?
5. Quick fix first (defection through Nexerelin's transfer), before the redesign?
