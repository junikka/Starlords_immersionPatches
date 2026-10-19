# Wording review of the Phase 2 rewrite

> **Status (2026-10-19):** Julie approved sections 1–5 and chose *Rave*. All of it is applied to the data files, together with the hand-written dialogue pass. Not yet rebuilt or tested in game.

This is a review of the wording GPT chose in Phase 2, checked against the live files (`titles.json`, `neutral.json`, `strings.json`, `dialog.json`, `lords.json`). Nothing has been edited yet. Each section ends with a recommendation for you to accept, change or reject.

**The overall verdict:** GPT got rid of the feudal vocabulary, but it replaced it with the voice of an HR department. Vanilla Starsector is terse, dry, a bit cynical, and full of naval and frontier words: *commission, expedition, muster, holdings, brig, transponder, the Collapse, the Domain*. The fix is not to go back to the medieval words. It is to pick **space-naval words with some weight**, and to give a little personality back to the dialogue.

---

## 1. Real bugs to fix whatever we decide

These were caused by a blind find-and-replace, so they are errors and not matters of taste.

| Where | Now reads | Should read |
|---|---|---|
| `lords.json` Nevsky, Kaliman, Serban bios (5×) | "Star **Operation commander**", "Deputy Star Operation commander" | Star Marshal / Deputy Star Marshal (vanilla Diktat rank) |
| `lords.json` Holk bio | "the **Commander Protector** of Olinadu" | Lord Protector (a real title that suits a police state; it's fine) |
| `lords.json` Neiss bio | "protégée of **Commander Wallis**" | Admiral Wallis |
| `lords.json` Neiss and Hamilton bios | "prot**Ă©**g**Ă©**e", "attach**Ă©**" (garbled characters) | protégée, attaché |
| `lords.json` pirate bios (4×) | "Pirate **Commander**" | Warlord, or "pirate warlord" |
| `titles.json` | **every faction set to Commander ×3**, which overrides the ranks in `faction.json` | see section 2 |

The last item is the main reason the game feels duller now than it did before Phase 2: Hegemony admirals, Tri-Tachyon executives and Pather brothers all display as "Commander".

## 2. Titles per faction (tier 0 / 1 / 2)

The original `faction.json` had its own problems ("First Star Lord", Persean tiers 0 and 2 both "Megas Doux", Tri-Tachyon's ladder upside down with SVP below Executive, Path and pirates flat). Proposed replacements:

| Faction | Original | GPT | **Proposed** |
|---|---|---|---|
| Neutral fallback | Commander ×3 | Commander ×3 | **Captain / Commodore / Admiral** |
| Hegemony | Lord Admiral / Lord High Admiral / First Star Lord | Commander ×3 | **Commodore / Admiral / Fleet Admiral** |
| Sindrian Diktat | Deputy Star Marshal / Star Marshal / Generalissimo | Commander ×3 | **Deputy Star Marshal / Star Marshal / Grand Marshal** (or keep Generalissimo, which suits Andrada's ego) |
| Persean League | Megas Doux / Doux / Megas Doux | Commander ×3 | **Doux / Megas Doux / Archon** (the League really is aristocratic in vanilla, so noble titles fit there) |
| Tri-Tachyon | SVP / Executive / COO | Commander ×3 | **Vice President / Senior Vice President / Executive Vice President** |
| Luddic Church | Bishop / Archbishop / Cardinal | Commander ×3 | **Deacon-Captain / Bishop / Archbishop**, which matches the vanilla rank you saw on Anastasia |
| Luddic Path | Brother ×3 | Commander ×3 | **Brother-Captain / Elder / Herald of Ludd** |
| Pirates | Pirate Lord ×3 | Commander ×3 | **Captain / Warlord / Kingpin** |
| Independent | (none) | Commander ×3 | **Captain / Commodore / Fleet Commander** |
| Player faction | (none) | Commander ×3 | **Captain / Commodore / Admiral** (renameable later) |

Once each tier has a distinct title, the "Political standing 2 of 3" crutch can be dropped from the UI (see section 4).

## 3. Core vocabulary

| Concept | Original | GPT | Problem | **Proposed** |
|---|---|---|---|---|
| Lord (collective) | Lords | Commanders | Fine, but it clashes with the vanilla officer rank "Commander" | **Commanders** in the UI; "fleet lords" never |
| Fief | fief / demesne | assigned market / holding (mixed) | Reads like a spreadsheet, and it is inconsistent | **Holdings**: "grant a holding", "revoke a holding". Factions can rename it in Phase 3 (garrison command, concession, turf) |
| Marshal (office) | Marshal | operation commander | Clunky, and it broke the bios | **Fleet Commander**, the office title Sartorius's bio already uses |
| Campaign | campaign | operation | "Operation" is fine but bland. **Expedition** is a vanilla word (Hegemony and League expeditions) | **Expedition**: "Start expedition", "taking part in an expedition" |
| Feast | feast | gathering | Dull, and you said so | ✅ **Rave** (Julie's call: fun over strict believability). Status line "Raving at X", heading "Ongoing rave" |
| Feast policy | Feast Law | Party restrictions | Accurate but dry | **Rave Licensing**, with levels *Anything Goes (30 d) / Permissive (90 d) / Licensed (180 d) / Strict (365 d) / Banned* |
| Tournament | tournament | piloting competition | "Ask to enter the gathering piloting competition" is the clunkiest line in the mod. "Tournament" isn't medieval; e-sports use it | **Tournament**, or **Sim Tournament** where context is needed |
| Trade law | Tariff Level | Taxation | Better, but the real mechanic is *holding revenue vs. trade income* | **Revenue Policy**, levels *Free Trade / Light Levies / Balanced / Heavy Levies / Extortionate* |
| Crown authority | Crown Authority | Executive authority | OK | **Central Authority** |
| Noble authority | Noble Authority | Seniority influence | Vague | **Privilege of Rank** |
| Rank tier | rank | political standing N of 3 | Too gamey | **Seniority: Junior / Senior / High Command**, or drop it once titles differ |
| Liege | liege | leadership / %PLAYER_TITLE | Fine | keep |
| Oubliette | oubliette | cell | Fine | **the brig**, which is naval |

## 4. UI and event lines

| Key | GPT now | **Proposed** |
|---|---|---|
| `event.campaign.description` | Operation commander {commander} has mobilized the faction fleets. Participating commanders should report to the assembly point. | Fleet Commander {commander} has called an expedition. All available commanders are to muster at the rally point. |
| `event.feast.description` | {host} invites you to a gathering. These events offer time to relax, meet other commanders, and build relationships. | {host} is throwing a rave. Half the faction's officers will be there, which makes it the best place in the Sector to drink, dance, trade gossip and make friends in useful places. |
| `campaign_end` | The operation has ended. Participating commanders are returning to their other duties. | The expedition is over. Its fleets are standing down. |
| `lord_spawned` | A new commander by the name of X has appeared and joined the Y | X has accepted a commission with Y. |
| `fleet_collect_taxes_desc` | Collecting local revenue at X | Collecting revenue at X |
| `generation.fallback.biography` | A fleet commander operating in the Sector. | Little about this officer is on public record. |
| `standing.option` | {title} — standing {tier} of 3 | {title} |
| Promotion option | "%RANK_TITLE1, with political standing 2 of 3" | Promotion to %RANK_TITLE1 |
| `mod_info` description | Persistent fleet commanders, faction politics and personal relationships across the Sector. | The Sector's admirals, executives and warlords now have names, grudges and ambitions. Fight beside them, court them, or turn them against their masters. |

## 5. Dialogue: where it went flat

GPT's rewrites are grammatical but generic. Character lines lost their character, and the reception vignettes were cut to about half their length with the sensory detail stripped out. A few examples, with replacements in a Starsector voice:

| Context | Original | GPT | **Proposed** |
|---|---|---|---|
| Honourable greeting | Peace to you, and always remember to temper your valor with mercy, your courage with honor. | Good to meet you, %PLAYER_NAME. I hope we can count on each other out here. | Fly straight, %PLAYER_NAME. The Sector has cutthroats enough. |
| Feast greeting | …tonight we will feast and make merry. | Leave the operational reports for tomorrow. Tonight we have time to catch up. | Welcome, %PLAYER_NAME. The war will keep until morning. The rave won't. |
| Recruitment accepted | …Hail %PLAYER_LIEGE_NAME, rightful ruler of the sector! | You have convinced me… My fleet and any holdings under my control will come with me. | You've made your case, %PLAYER_NAME. I'll have our transponders recoded. From today my fleet flies for %PLAYER_FACTION_NAME, and my holdings come with it. |
| Justification ×12 | "your claim is seen as fully justified" | "gives me a strong reason to join you" (repeated twelve times) | Vary them: "Your offer is generous. Generous enough." / "Your colonies are stable and growing. Out here, that's rare." / "I've seen what your fleet can do. That's persuasive." / "Your record speaks for itself." |
| Loyal vassal | My sword is at the disposal of my rightful liege… | My fleet serves the faction. In return, I expect its leadership to stand by us… | My guns answer to %LORD_LIEGE_NAME, as long as %LORD_LIEGE_NAME remembers who keeps them loaded. |
| Marriage accepted | …join our houses together. | We have been planning separate futures for too long… | I accept. Two fleets, one course. It's about time. |
| Butter joke | exotic butters / butter sampler platter | food from different worlds / imported delicacies | Keep the joke: "genuine dairy butter, not the vat-grown stuff". It's charming, and vanilla has plenty of dry humour. |
| Battle cry | For honor and glory! | Ready for launch. | Good hunting. |
| Tournament dedication | my tournament victory goes out to you | my piloting competition victory goes out to you | %LORD_NAME, that last win was for you. |
| State of affairs | …in the realm? | …in the faction? | What's your read on how things stand for us? |
| Balcony vignette | stars as "a reminder of the once-glorious reach of humanity and its Icarian downfall" | "watching the traffic pass…" | Bring back the Collapse imagery: dead gates and the dark beyond the core worlds. That is pure Starsector, and GPT cut it. |

**Recommendation for the dialogue:** do a hand-written pass over about 60 high-visibility lines (greetings, recruitment, the reception vignettes, the justifications) rather than another scripted replacement. The scripted approach is what produced section 1.

---

### Decisions needed

1. Section 2 titles: accept, or adjust per faction?
2. Section 3 vocabulary: Holdings / Fleet Commander / Expedition / Reception / Social Calendar / Tournament / Revenue Policy?
3. Section 5: should I do the hand-written dialogue pass in this voice?
