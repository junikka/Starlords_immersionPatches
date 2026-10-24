# Starlords: Immersion Patches

A fork of **[Star Lords](https://github.com/Alaricdragon/Starlords_Temp)** for [Starsector](https://fractalsoftworks.com/) that makes the Sector's commanders feel like they belong in it: faction-specific words and ranks, a council you climb, elite raves, and support for modded factions.

**Start with the original README:** [Star Lords README](https://github.com/Alaricdragon/Starlords_Temp#readme) (a copy as of version 0.3.78 is in [README_ORIGINAL.md](README_ORIGINAL.md)). It explains what Star Lords is: commanders with fleets and personalities, holdings, the council and its politics, campaigns, defection, prisoners. Everything in it still applies. This README only covers **what this fork changes or adds**.

## Install

1. Download the latest ZIP from [Releases](https://github.com/junikka/Starlords_immersionPatches/releases).
2. Remove any older Star Lords folder from `Starsector/mods`, then extract the ZIP there. Keep one copy.
3. Recommended: [Nexerelin](https://github.com/Histidine91/Nexerelin), [LunaLib](https://github.com/Lukas22041/LunaLib) (settings) and [Console Commands](https://github.com/LazyWizard/console-commands) (testing). Star Lords itself has no dependencies.
4. Start a new campaign for the cleanest result. The fork works in existing saves, but some things, such as commanders and starting policies, are only set up when a campaign begins.

Updates are announced through Version Checker.

## What's different

### Words and ranks that fit the Sector
- **Space-opera vocabulary** instead of medieval terms: *holdings*, *expeditions*, *raves*, *tournaments*, the *Fleet Commander*, and the policies *Central Authority*, *Privilege of Rank*, *Revenue Policy* and *Rave Licensing*.
- **Faction wording profiles:** each faction names its own institutions.

  | Faction | Holding | Council | Office | Expedition | Policies |
  |---|---|---|---|---|---|
  | Hegemony, independents | Command | Admiralty Board | Fleet Commander | Expedition | Chain of Command, Seniority, Levy Policy |
  | Sindrian Diktat | Prefecture | Executive Council | Marshal of the Fleet | Expedition | as Hegemony |
  | Tri-Tachyon | Concession | Board | Director of Operations | Operation | Executive Control, Executive Privilege, Margin Policy |
  | Persean League | Seat | League Council | Strategos | Expedition | Crown Prerogative, Privilege of Rank, Tribute Policy |
  | Luddic Church | Parish | Synod | Knight-Commander | Crusade | Doctrine of Obedience, Clerical Privilege, Tithes |
  | Luddic Path | Cell | Conclave | Knight-Commander | Purge | as Church |
  | Pirates | Turf | Captains' Table | Warmaster | Armada | Boss's Word, Captains' Share, Cut of the Take |

  Your own faction uses the house words; LunaLib → *Other* → *Your faction's wording* switches it to any profile.
- **Rank ladders per faction:** Commodore / Admiral / Fleet Admiral for the Hegemony, Vice President up to Executive Vice President for Tri-Tachyon, Deacon-Captain / Bishop / Archbishop for the Church, Captain / Warlord / Kingpin for pirates, and so on. The comm panel's *Rank* line shows the same title.
- **A hand-written dialogue pass** and safe dialogue tokens, so names containing odd characters display correctly.

### Earning a place on the council
- A commission no longer hands you a council seat. You start as a **Commissioned officer** and climb **seat → tier 1 → tier 2**. The seat comes with time and reputation (Welcoming). Each promotion after that needs time in rank, reputation (Friendly, then Cooperative) and a track record, and is decided by a real council vote that a friendly commander sponsors. You can also ask a commander in person to sponsor you.
- If your reputation slips, the council treats you as a lower rank until it recovers. Locked proposal buttons say what they need.
- **Ordinary fleets of your faction greet you by rank** once you hold a seat.
- **Per-faction starting policies:** the Church starts strict about raves, the Path bans them, Tri-Tachyon favours free trade, and so on.

### Raves: elite parties
- Raves are gatherings of the faction's elite: admirals, ministers, House heads, executives, kingpins and high clergy. **A seat on the host faction's council gets you in.** Without one, a bodyguard blocks the door. You can try to slip in behind a minister's entourage, but you may be caught and thrown out for the rest of the night. Everything rave-related, including the original mod's options, sits behind **Join the rave.**
- Inside: **the rave's own music**, a **venue picture** and a **gonzo arrival scene** written for each faction. The Church's scene gets wilder as its Rave Licensing loosens, and the Luddic Path has its own scene, which involves wrenches. You also get **crowd lines**, **Hit the dance floor** (a scene per personality and a relationship boost), the **tournament** and courtship.
- The *Ongoing rave* intel shows the venue.

### Weddings
- Accepting a proposal makes you **engaged**. Your fiancé names a venue: their own holding, otherwise their faction's largest world. **Dock there and choose "Attend your wedding"** from the market menu. Weddings are no longer held at raves.

### Support for modded factions
- Every Star Lords data file can carry entries for faction mods **without making them a dependency**. Players who don't have a faction mod lose nothing and crash nothing: its settings and commanders are skipped with a log line, and everything else is simply never used.
- **Hazard Mining Incorporated** is supported as the first example: claims instead of holdings, Foreman / Director / Board Director, Company greetings, an ore-hold rave, and one commander.

### Fixes to the original
- Values the mod stores for a limited time (text and yes/no flags) no longer expire the moment they're set.
- New campaigns no longer crash when Star Lords data mentions a faction that isn't installed.

### New console commands (testing)
- `promoteplayer [status|vote|commissioned|0|1|2]`: inspect or set your council standing.
- `startrave [first name|ID]`: the nearest commander, or the one you name, throws a rave right away.
- `engagelord <first name|ID>`: skip the courtship and get engaged.

### New settings (LunaLib → Other)
- *Your faction's wording*: the wording profile for your own faction.
- *Rave music*: on or off.

## Add a faction mod, step by step

Short version below. The full guide, with every field, its defaults and a complete worked example (Hazard Mining), is [docs/FACTION_INTEGRATION.md](docs/FACTION_INTEGRATION.md).

1. **Read the faction mod.** Note its exact faction ID (case matters), the path of its `.faction` file (from its `data/world/factions/factions.csv`), its markets (market ID = the first entity of each market in its `data/campaign/econ/*.json`, or check with Console Commands `list markets`), its ships and variant IDs, portraits, and its own named characters (don't duplicate them).
2. **Gameplay:** in `data/lords/faction.json`, add an entry keyed by the faction ID. Every field is optional: starting policies, council progression, leader, and what the faction may do (diplomacy, raves, holdings…).
3. **Ranks:** in `data/lords/presentation/titles.json`, add three titles under `factions`. If the mod's faction file name differs from its ID, map it under `factionFiles`. Then run `python tools/sync_ranks.py` to generate the comm-panel rank file.
4. **Words:** in `data/lords/presentation/profiles.json`, bind the faction to a profile (`naval`, `corporate`, `aristocratic`, `religious`, `pirate`), or add a profile that `extends` one and overrides only what differs. Check the result with `python tools/preview_profiles.py`.
5. **Fleet greetings:** in `data/lords/presentation/greetings.json`, add three lines, one per council rank.
6. **Rave:** in `data/lords/presentation/raves.json`, add a venue picture, an arrival scene and optional crowd lines. Follow [the writing guide](docs/immersion-audit/WRITING_GUIDE.md): what you see, gonzo-style, elite guests.
7. **Commanders:** see the next section.
8. **Check:** run `python -m unittest discover -s tools/tests -v`. Start the game once **without** the faction mod (it must load cleanly, and the log shows the skip lines) and once **with** it.

Another mod can also ship these entries in its own copies of the same files. Starsector merges them.

## Add a commander, step by step

1. Open `data/lords/lords.json` and add an entry keyed by the commander's display name.
2. Fill in the required fields:
   - `faction` (exact ID), `fleetName`, `isMale`
   - `personality`: `Upstanding`, `Martial`, `Calculating` or `Quarrelsome`
   - `battle_personality`: `Timid`, `Cautious`, `Steady`, `Aggressive` or `Reckless`
   - `level` and `ranking` (0 to 2)
   - `fief`: a market ID, or `null`
   - `flagship` (a variant ID) and `shipPref` (variant ID → weight)
   - `portrait`: a path, or a name under `graphics/portraits/`
   - `lore`: the biography
3. Optional fields: `preferredItem` (favourite gift), Nexerelin `alignments`, `customSkills`, S-mod settings, executive officers, `dialogOverride`. Each is described in the [original README](README_ORIGINAL.md#adding-your-own-lords).
4. Don't duplicate a faction's own story characters, and don't give a commander a capital that one of them runs.
5. Commanders appear when a **new campaign** starts. A commander whose faction isn't installed is skipped.
6. Test with `warplord <first name>`, then hail them.

A full example is in [docs/FACTION_INTEGRATION.md](docs/FACTION_INTEGRATION.md#datalordslordsjson-add-one-sample-commander).

## For writers and developers

- [docs/immersion-audit/WRITING_GUIDE.md](docs/immersion-audit/WRITING_GUIDE.md): tone and style for new text.
- [PRESENTATION.md](PRESENTATION.md): the wording, title, profile, greeting, rave and music files.
- [BUILDING.md](BUILDING.md): reproducible build. [TESTING.md](TESTING.md): in-game checks.
- [docs/immersion-audit/](docs/immersion-audit/): the overhaul's design, plans and progress.

## Credits

Everyone credited in the [original README](README_ORIGINAL.md#credits): Deluth, Alaricdragon and the Star Lords contributors, the Starsector and Nexerelin teams, and Interestio for the portraits. Immersion patches by Julie ([junikka](https://github.com/junikka)).
