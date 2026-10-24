# Research: real lore characters as Starlords commanders

**Status: approved as the backup take for rulers (Julie, 2026-10-24); not implemented.** Decisions:
- **Who:** start with Andrada, Daud, Sun and Hannan.
- **Capture:** of free rulers, in a later phase.
- **Titles:** *Supreme Executor* (Andrada), *High Hegemon* (Daud), *CEO* (Sun), *High Archon* (Hannan, one step above the League's Archon rank).
- **Capitals:** the four commanders who hold them move to other worlds.

This is now one building block of the wider [Heroes vision](HEROES_VISION.md): rulers become the first hero archetype. This answers "what would it take for real lore characters to travel the Sector, with consequences, without breaking Nexerelin or quests". It refines [SOVEREIGNS_PLAN.md](SOVEREIGNS_PLAN.md) and supersedes the deferred [lore study](LORE_INTEGRATION_STUDY.md)'s rejection of leader fleets.

Sources checked: the game's API sources (1,947 files) and `rules.csv` on your PC, Nexerelin 0.12.2d's sources (653 files), and Starlords' own code.

## Bottom line

**It's feasible.** The surprise is that vanilla already does a version of it, so the pattern is proven:

- **Vanilla already fields lore characters.** `PersonalFleetScript` gives Oxana Hyder (*Askonia System Defense Armada*) and Horacio Caden (*Lion's Guard Grand Armada*) real roaming fleets commanded by their own person objects. The fleet respawns 10–20 days after defeat and can fire story triggers. Their comm-board entries and quests keep working throughout.
- **Quests reach lore characters through their comm board, not their fleet.** Rules match `$id == andrada` on the comm-board conversation. A character can command a fleet and still be found at their market. Nothing in vanilla or Nexerelin looks up a person's fleet (`person.getFleet()`: zero uses).
- **Nexerelin keeps faction leaders on their capital's comm board even when the capital falls.** It strips their admin role, never moves them or flips their faction, and restores them as admin when the capital is retaken. So the leader must keep their post (`FACTION_LEADER`), faction and market. Our design never touches those.
- **Missions mark the people they need** with `$missionImportant` (vanilla's `Misc.makeImportant`). That gives us one generic, future-proof guard: while it's set, Starlords steps aside for that character.

What doesn't work: taking over characters vanilla already fields (Hyder, Caden), or touching the four System Defense Fleets, which the colony crises depend on (`SDFHegemonyDefeated` and the others).

## Who could take the field

| Character | Role | Where vanilla needs them | Verdict |
|---|---|---|---|
| **Philip Andrada** | Diktat leader, Sindria | One greeting rule (Nex adds an admin skill, unaffected) | **Pilot.** Almost quest-free |
| **Artemisia Sun** | Tri-Tachyon leader, Eochu Bres | One greeting rule | **Pilot** |
| **Baikal Daud** | Hegemony leader, Chicomoztoc | Galatia finale and the Hegemony inspections talk, both at his comm board (6 rules) | Field, fully protected |
| **Jorien Kanta** | Pirate leader, Kanta's Den | Kanta's Wrath crisis, *A Favor for Kanta*, protection deals, all via comm board | Field, fully protected. A roaming warlord suits him |
| **Reynard Hannan** | League leader, Kazeron | League membership and blockade, *Gens Hannan Machinations* (18 rules) | Possible, fully protected; later |
| Orcus Rao | Hegemony, Ragnar Complex | *Sword of Eventide* (12 rules) | Later, protected |
| Laverna Tell | Diktat, Cruor (ground general) | No rules | Possible, but a ground general isn't a fleet admiral |
| **Hyder, Caden** | Diktat | Vanilla personal fleets, *The Usurpers* fleet hails | **Don't take over.** They already roam; a second fleet would duplicate them |
| Livewell Cotton | Path leader, hidden at Epiphany | Galatia finale | No. A hidden terrorist leader doesn't parade |
| Macario, Rao family, Katos, Yaribays, Jaspis, Siyavong, Galatia staff… | Story characters | Many quests | No |
| System Defense Fleet admirals | Generated, not lore | Colony crises | No |

## Consequences, in tiers

M&B kings can be beaten and captured. Here, the consequence depends on how much vanilla needs the character:

1. **Defeat (everyone):** their fleet retreats, and they re-form at their capital some weeks later. Vanilla's personal fleets do exactly this (10–20 days), and so does Starlords (30 days).
2. **Capture (only "free" characters: no quest rules, not `$missionImportant`):** held for ransom like a Starlords commander. While held, their comm-board entry is hidden; on release it returns. For Andrada and Sun the only thing affected is a greeting.
3. **Never:** death, defection, exile, marriage, rank changes, a faction or post change, or leaving their comm board. Fully protected characters (Daud, Kanta, Hannan, Rao) are never captured either; a lost battle is a retreat.
4. **The live guard:** while a character is `$missionImportant`, Starlords' conversation steps aside so the vanilla mission's lines win, and they aren't capturable. This also covers quests from other mods.

## What has to be built

1. **A commander from an existing person:** Starlords already does this for the player; it needs the same for listed lore people. Data goes in `sovereigns.json`, keyed by person ID.
2. **Runtime resolution:** the person and their home market are looked up when the campaign starts, not hard-coded. In Nexerelin random-sector games, leaders can sit elsewhere or be created later. A missing person is skipped, like a missing faction.
3. **Never double a fielded character:** skip anyone vanilla already runs a personal fleet for (checked generically by looking for a `PersonalFleetScript` with that person).
4. **The guards above:** Starlords' rank sync skips them (vanilla keeps `FACTION_LEADER`). They're excluded from capture (except free characters), Life and Death, defection, exile, courtship and rank proposals. The conversation hook gets the `$missionImportant` exception.
5. **Holdings:** the capital is theirs. Starlords commanders who hold capitals now (Nevsky at Sindria, Sartorius at Chicomoztoc, Askia at Eochu Bres, Kazimierz at Kazeron) move to other worlds.
6. **Council:** they keep their existing role as the ruler whose stance weighs on every vote; no second ordinary vote.
7. **Words:** a ruler's title from data (for example *Supreme Executor*), and a handful of gonzo lines where generic commander dialogue would sound wrong.
8. **Overlaps to clean up:** Nevsky's biography makes him head of the Lion's Guard, which is Caden's job in vanilla. The Diktat's Fleet Commander office overlaps Hyder.

## Testing that would prove it

- **Nexerelin:**
  - Invade a capital whose ruler is in the field: the ruler stays on the comm board, comes back as admin when the capital is retaken, and their fleet keeps working.
  - Start a random-sector game: missing or relocated leaders are handled.
- **Quests:**
  - Play *The Usurpers*: Hyder and Caden behave exactly as in vanilla.
  - The Galatia finale (talk to Daud and Kanta) works while their fleets are out.
  - Kanta's protection deal works.
- **Combat:**
  - Beat Andrada: he retreats or is captured (if allowed), his comm entry hides and returns, and he re-forms at Sindria.
  - Beat Daud: he only retreats.
- **Switch-off:** emptying `sovereigns.json` turns all of it off for new campaigns.

## Suggested phases

1. **Pilot: Andrada and Sun**, fully protected (defeat = retreat), in a new campaign. This proves the pattern with the two least entangled rulers.
2. **Daud and Kanta**, fully protected.
3. **Capture with ransom** for free characters (Andrada, Sun), with their comm entry hidden while held.
4. **Hannan and Rao**, and cleaning up the Lion's Guard and Fleet Commander overlaps.

## Decisions for you

1. Start with the Andrada + Sun pilot?
2. Capture of free rulers (Andrada, Sun) as a later phase, or never?
3. Titles: *Supreme Executor* (Andrada), *CEO* (Sun)?
4. OK to move the four commanders off the capitals?
