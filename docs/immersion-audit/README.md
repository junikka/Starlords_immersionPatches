# Starlords immersion audit

Audit date: 2026-10-18. Local mod metadata: **0.3.78**, targeting Starsector **0.98a-RC5**.

**Faction-specific titles are already supported. A consistent change of tone needs more than changing the word “Lord”.** The existing character, fleet, and political systems can support a space-opera interpretation, but the shared writing repeatedly presents every faction as a feudal realm.

This audit adds documentation and an extraction script only. No gameplay data, Java source, or packaged JAR was changed. Findings describe the supplied source; they have not been tested in a running campaign, and source/JAR parity has not been established.

## Review files

Start with the [replacement decisions worksheet](E:/Modding/Starsector/Starlords_immersionPatches/docs/immersion-audit/REPLACEMENTS.md). It separates the vocabulary choices you need to make before editing individual passages.

The follow-up [modular presentation proposal](E:/Modding/Starsector/Starlords_immersionPatches/docs/immersion-audit/MODULAR_PRESENTATION_PROPOSAL.md) describes faction codebooks, inherited profiles, character exceptions, and an incremental implementation plan. Its [example JSON](E:/Modding/Starsector/Starlords_immersionPatches/docs/immersion-audit/faction_profiles.example.json) is a proposed format, not current runtime configuration.

The [distinct wording list](E:/Modding/Starsector/Starlords_immersionPatches/docs/immersion-audit/generated/00_distinct_wording.md) groups **445 exact text values** for replacement review. The detailed files contain **887 active-source candidate occurrences across 34 files**, including contextual terms that may be kept. These are occurrences, not 887 distinct phrases or confirmed defects. Repeated personality variants and highlighting fragments have separate locations.

| Review file | Entries | Contents |
| --- | ---: | --- |
| [Dialogue](E:/Modding/Starsector/Starlords_immersionPatches/docs/immersion-audit/generated/01_dialogue.md) | 555 | Four personalities, shared options, romance, recruitment, fallback dialogue, highlights |
| [Titles, UI, and events](E:/Modding/Starsector/Starlords_immersionPatches/docs/immersion-audit/generated/02_titles_ui_and_events.md) | 131 | Faction ranks, localized strings, hard-coded Java text, derived event heading |
| [Biographies and fleets](E:/Modding/Starsector/Starlords_immersionPatches/docs/immersion-audit/generated/03_biographies_and_fleets.md) | 49 | All 48 active biographies for contextual review, plus a flagged fleet name |
| [Settings and console](E:/Modding/Starsector/Starlords_immersionPatches/docs/immersion-audit/generated/04_settings_and_console.md) | 152 | Luna settings, console help/feedback, mod metadata |
| [Reference copies](E:/Modding/Starsector/Starlords_immersionPatches/docs/immersion-audit/generated/05_inactive_reference_copies.md) | 157 | Unloaded faction rosters, opt-in dialogue template, hidden portrait faction, unused honorific helper |

Every entry has an ID, exact original value or Java literal fragment, file, line, JSON path where applicable, and blank replacement/notes fields. The [full JSON inventory](E:/Modding/Starsector/Starlords_immersionPatches/docs/immersion-audit/generated/string_inventory.json) also retains unflagged strings, Java expression context, and excluded diagnostics/identifiers: **4,255 extracted records** in total. An `active` classification means part of the active source/configuration, not proof that every branch is reachable in a particular save.

`rewrite_candidate` flags conspicuous feudal wording or meta text. `context_review` includes military ranks, religious vocabulary, tournaments, romance, and biographies that can fit the setting. Nothing in the inventory is an instruction to replace every matching word automatically.

## What the mod actually adds

The active roster has 48 authored characters. They have distinct personalities, biographies, fleet preferences, relationships, ranks, income, and persistent fleets. They collect income from assigned markets, trade, replenish their forces, patrol, raid, and join coordinated military operations. The player can build relationships, influence decisions, recruit defectors, capture/ransom prisoners, and respond to requests. Politics includes voting, appointments, promotion, market allocation, and faction laws.

The source also implements social gatherings, piloting tournaments, romance/marriage, and spouses serving in the player's fleet. These are partly described as future work in the older README, so the README alone understates the current implementation. Optional generators add random characters at campaign creation and through the life/death system. Nexerelin integration supplies additional faction, diplomacy, and invasion behavior.

This is explicitly Mount & Blade-inspired in the README and mod description. The clash is mainly the interpretation of these systems as landed nobility, not the existence of persistent political fleet leaders.

## Where immersion breaks

| Area | Actual wording/examples | Editorial assessment |
| --- | --- | --- |
| Universal social class | “Lords”, “Lord Actions”, “Lord Requests”, “Exile Lord” | Uses an aristocratic identity for everyone, including corporate and military characters. A neutral global category and faction-specific collective terms should be separate decisions. |
| Political geography | “Laws of the Realm”, “Realm Laws”, “loyal lord of the realm” | Assumes a realm and personal sovereignty across every government. |
| Territorial control | “Award Fief”, “Revoke Fief”, “Transfer Fief”, “my demesne” | Makes market assignments hereditary-style personal possessions. The replacement should still convey that control produces income and can change hands on defection. |
| Military mobilization | “All loyal subjects should rally to the banners!” | The underlying action is fleet mobilization; the prose presents a feudal levy. “Marshal” itself can fit a space opera, but its universal use and surrounding prose need review. |
| Recruitment | “sworn”, “liege”, “claim”, “rightful”, “allegiance” | Often frames a change of employer/command as accepting a sovereign's legitimacy. Whole arguments need rewriting, not just nouns. |
| Social events | “feast”, “estate”, “revelry”, “morrow”, weeks away from duties | Gatherings can become receptions, conferences, shore leave, or faction-specific celebrations. Choose an event concept that fits the existing duration and activities. |
| Romance | “join our houses together”, courtship and tournament dedication | Ordinary romance can fit. Dynastic language and automatic aristocratic assumptions are the issue. Preserve the distinction between a spouse and a military subordinate. |
| Character writing | “Lord Admiral”, “First Star Lord”, “Doux”, “Megas Doux”, noble backgrounds | Biographies contain titles independently of the dynamic rank system. Some use historical ranks or actual aristocratic backgrounds intentionally; review the character, not just the word. |
| Meta/fallback text | Literal “(error)” prose, “Tri-pad DLC”, “Final Boss”, generator jokes | These break tone even after medieval terms are removed. Fallback dialogue is active as a fallback; it is not merely a developer comment. |

The Church's religious titles and the Diktat's “Star Marshal”/“Generalissimo” need not be removed merely because they are old words. Similarly, a pirate “Warlord” can fit the intended tone. The objective is institutional variety, rather than making every faction speak like the same navy. This is an editorial assessment, not an assertion that every flagged phrase contradicts established lore.

Mechanics set limits on a wording-only patch. An “allocation” that a commander can take along on defection remains personal territorial power; a “briefing” with days of recreation and marriage ceremonies remains a social gathering. Changing those behaviors would be a separate design task.

## Faction titles: what works now

[Faction configuration](E:/Modding/Starsector/Starlords_immersionPatches/data/lords/faction.json) supports `rank_0`, `rank_1`, and `rank_2` for arbitrary faction IDs. The source path is `Lord.getTitle()` → `Utils.getFactionTitle()` → `FactionTemplateController` → `FactionTemplate`. The rank is a numeric gameplay tier; the displayed title is text.

| Faction | Tier 0 | Tier 1 | Tier 2 |
| --- | --- | --- | --- |
| Hegemony | Lord Admiral | Lord High Admiral | First Star Lord |
| Luddic Church | Bishop | Archbishop | Cardinal |
| Persean League | Megas Doux | Doux | Megas Doux |
| Sindrian Diktat | Deputy Star Marshal | Star Marshal | Generalissimo |
| Tri-Tachyon | SVP | Executive | COO |
| Luddic Path | Brother | Brother | Brother |
| Pirates | Pirate Lord | Pirate Lord | Pirate Lord |
| Player / unspecified faction ranks | Lord | High Lord | First Lord |

The repeated Persean tier 0/2 values are exactly what this configuration contains; their ordering merits review. Missing rank values fall back individually to `starlords_title.title_default_0/1/2` in [strings.json](E:/Modding/Starsector/Starlords_immersionPatches/data/strings/strings.json:78).

You can therefore give one faction naval titles, another “Warlord”, and another corporate titles already. `%LORD_TITLE` and the equivalent person markers insert the character's resolved title. The main lookup uses **current faction and current rank**, so promotion and defection affect it. Player titles use the recruitment faction, including a commission when applicable.

However, these fields do **not** control generic plurals (“other lords”), direct address (“my lord”), “Marshal”, government names, territory labels, or static biography text. Nor is there currently a per-character title override. Gendered title variants are not part of the faction rank configuration.

## Source issues to resolve before a consistent terminology patch

1. **Two title lookup systems.** [Utils.getTitle](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/util/Utils.java:360) still requests `title_<faction>_<rank>` from strings.json rather than the faction template. [Promotion choices](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/ui/LawsIntelPlugin.java:459) and [promotion descriptions](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/faction/LawProposal.java:176) call it. This can produce default/missing/null text depending on missing-string behavior. Both paths should use one resolver.
2. **Recruitment rank placeholders use the wrong faction.** In [DialogSet](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/util/dialogControler/DialogSet.java:248), `%PLAYER_FACTION_RANK_TITLE0/1/2` reads the conversational NPC's faction. Offers to join the player's faction can advertise the NPC's old ranks. Use the player's recruitment faction.
3. **Token substitution is not boundary-safe.** [DialogSet.insertData](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/util/dialogControler/DialogSet.java:145) uses regex replacement, and `%PLAYER_NAME`/`%LORD_NAME` are substituted before their `_FIRST`/`_LAST` variants. The source therefore permits a full name followed by a literal suffix such as `_FIRST`. Resolve whole tokens and treat inserted text literally; this also matters for custom names containing `$` or backslashes. `StringUtil` similarly uses regex replacement for `$string` values.
4. **Some headings are derived from internal IDs.** [EventIntelPlugin.getName](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/ui/EventIntelPlugin.java:117) capitalizes `feast` to produce “Ongoing Feast”. Add a display-name mapping; do not rename the event ID.
5. **Biography text bypasses dialogue expansion.** [LordsIntelPlugin](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/ui/LordsIntelPlugin.java:137) prints `template.lore` directly. Putting `%LORD_TITLE` into biographies will not currently resolve it. Keep biographies historical/title-neutral or deliberately implement biography formatting.
6. **Faction leaders can be absent.** `leaderID` is optional; missing people become “noone” in dialogue. New faction writing needs a no-leader/institutional-leadership variant, rather than assuming every faction has a named sovereign.
7. **Java text needs a rebuilt runtime.** Editing the supplied Java files alone does not change [the loaded JAR declaration](E:/Modding/Starsector/Starlords_immersionPatches/mod_info.json:13). This checkout has no checked-in build script. A build/dependency setup and campaign verification will be needed when implementing Java changes.

These are source findings, not a claim that they have all been observed in-game. No fixes were applied in this audit.

## Approach for many authored faction commanders

Use one faction terminology profile with a neutral fallback. Keep the existing numeric rank tiers and existing internal keys. Add presentation fields for singular/plural collective, forms of address, the campaign leader's office, territorial assignments, political institutions, and social events. Prefer full sentence templates when grammar or ideology differs by faction.

Keep three concepts separate:

- **Rank:** the individual's standing, such as a naval rank or corporate grade.
- **Office:** the appointed leader of a campaign; currently “Marshal”, independently tracked by the political system.
- **Collective:** what the UI calls all these characters, including a mixed-faction roster.

An eventual title resolver should use an explicit per-person override when appropriate, then the current faction's rank/profile, then the neutral default. A personal override needs a policy for defection: a permanent epithet may survive, while a commissioned rank should normally change. Those override fields would be new code, not existing JSON support.

For dialogue, the current machinery already provides `dialogOverride` per character and `currentFaction`/`startingFaction` conditions. Use current faction for institutional allegiance and starting faction only for deliberate background references. Override only the necessary lines; missing lines fall back to other dialogue. Give custom dialogue sets unique IDs, and validate all referenced IDs because the lookup dereferences them without a missing-set guard. Do not copy the entire 76-group template for every character unless each line truly needs to differ.

For roster authoring:

- **The loaded roster is `data/lords/lords.json`.** `lords_hegemony.json`, `lords_tritach.json`, and the other split files are not discovered by the supplied loader. Merely creating `lords_myFaction.json` will not add anyone.
- **Data packs are feasible.** The roster, faction profiles, and dialogue all use `getMergedJSONForMod`. A companion mod can contribute those same paths; actual merge precedence/conflict behavior should be verified in the chosen mod load order. Optional faction packs should declare the corresponding faction mod dependency. Another option is authoring per-faction files and generating one runtime roster with a small packer, which does not exist yet.
- **Names are currently template keys.** The top-level character name is also used when refreshing templates on save load. Use unique names, do not embed ranks in those keys, and avoid casual renaming after a campaign begins. A future separate stable ID would need backward compatibility, not a blind conversion.
- **Required fields:** `faction`, `fleetName`, `isMale`, `personality`, `flagship`, `lore`, `portrait`, `fief`, `battle_personality`, `level`, `ranking`, and `shipPref`. Source: [LordTemplate](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/person/LordTemplate.java:49). For no initial market, the string `"null"` follows the constructor's explicit handling. Keep modded faction IDs and ship variant IDs in their exact case; only recognized vanilla faction names are normalized by the constructor, despite the broader README claim. Market IDs are lowercased by the current code.
- **Personality versus combat behavior:** the social personalities are Upstanding, Martial, Calculating, and Quarrelsome. Combat personality is a separate field. `ranking` is 0–2 and changes political/economic behavior, not just appearance.
- **Existing optional content:** `preferredItem`, `customSkills`, fleet/flagship S-mod preferences, executive officers, alignments, and `dialogOverride` can customize individuals. Authoring validation should check assets and dependencies, not only JSON syntax; malformed templates are silently skipped in the current parser.
- **Existing-save additions need an explicit path.** `loadLords()` restores existing intel characters and refreshes their templates; it does not spawn every newly added roster entry. Use a new campaign for initial roster testing, or implement deliberate mid-save registration using the available `addLordMidGame` APIs. Static template caching also means file edits are not a reliable hot-reload workflow.
- **Check generator policy when mixing authored and random characters.** The new-game generator normally avoids factions already represented unless additional generation is enabled. The life/death system has its own controls. The neutral terminology must cover generated characters too.

## Coverage and verification

The extraction scans all 356 Java source files, active JSON prose/ranks, all authored biographies and fleet names, Luna settings, console help, rule text fields, mod metadata, and the portrait faction's display names. It retains nonempty Java literals even when classified as internal or diagnostic. Dialogue extraction includes basic strings, advanced `line`/`hint` fields, and highlights; `additionalText`, options, rules, and addon targets are references/code and are not replacement prose.

Manually traced the rank, dialogue, roster, event heading, biography, and save-load paths; reviewed the unflagged longer dialogue for missed vocabulary and added “demesne”, “oubliette”, dynastic-house language, and meta/fallback text. Religious terms, marriage, wagers, and ordinary military language are contextual candidates. The full inventory keeps unflagged text available for further editorial review.

Not covered as replacement prose: binary images, compiled-JAR-only content, strings supplied by other installed mods or the base game, code comments, developer documentation, and historical changelog text. README wording can be updated once a final vocabulary is chosen. This is a broad source inventory with heuristic classification, not a proof that every subjective tonal issue has been identified.

To regenerate the snapshot, run `python tools/audit_immersion_strings.py` from the repository. This overwrites files under `generated/`; copy a review file before filling its per-line replacements, or keep decisions in REPLACEMENTS.md, which the script does not touch. It does not apply replacements to the mod.
