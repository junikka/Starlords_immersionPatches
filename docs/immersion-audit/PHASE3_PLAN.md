# Phase 3 plan: faction profiles

**Status: approved by Julie and implemented (2026-10-19).** Decisions: raves and tournaments stay universal; the profile tables below are approved as drafted; the Diktat's council-appointed office becomes *Marshal of the Fleet*; a LunaLib setting picks the player faction's profile; work started while the in-game test of Phase 2 runs. Evidence is in [IMPLEMENTATION_PROGRESS.md](IMPLEMENTATION_PROGRESS.md). Two changes from the plan while implementing: the expedition notifications and fleet status lines also moved out of `strings.json`, because they name the office and the expedition (84 overridable messages instead of 57), and the player-faction setting changes words only, not rank titles, so the comm panel's rank line stays consistent.

## What the accepted design asks for

From [MODULAR_PRESENTATION_PROPOSAL.md](MODULAR_PRESENTATION_PROPOSAL.md), implementation step 3:

> Profile inheritance and a few distinct examples. Implement neutral, naval, corporate, and raider profiles; use actual faction bindings only after wording review. Move law names, offices, events, hints, and tooltips through the resolver too.

Acceptance in [IMPLEMENTATION_PROGRESS.md](IMPLEMENTATION_PROGRESS.md): neutral → institutional → faction profiles, per-key fallback, and faction-aware offices, laws, events and UI.

Supporting rules from the design that Phase 3 must keep:
- Single inheritance (neutral → institutional profile → faction profile), resolved per key.
- Wording never changes mechanics.
- The text's context decides which faction's words apply: the deciding faction for laws and proposals, the host's faction for events, the current faction for a character's title, neutral wording for mixed-faction lists.
- Validation: report cycles, missing parents and bad values instead of failing silently. An unknown faction falls back to neutral.

## What already exists

| Phase 3 item | State today |
|---|---|
| Message catalogue | Done in Phase 2: 376 keys in `neutral.json`, safe tokens, missing-context fallback. |
| Per-faction titles | Done since Phase 2: direct bindings in `titles.json`, plus comm-panel rank names through `sync_ranks.py` and `Lord.syncVanillaRank`. |
| Per-faction greetings | Done: `greetings.json` with a `default` set. |
| Per-faction gameplay data | Done: `startingPolicies` and `progression` in `faction.json`. |
| A faction seam for law names | `Presentation.lawName(faction, law)` already takes the deciding faction and ignores it. That is the hook left for Phase 3. |
| Faction-specific dialogue | Already possible through `currentFaction_whitelist` rules in `dialog.json`. That is Phase 4 material. |
| Profiles and inheritance | **Not started.** `faction_profiles.example.json` is an illustration only, and its wording predates your wording pass. |

So most of the "faction" half of Phase 3 is done for titles. What's missing is the **institutional vocabulary layer** (what a holding, the council, the Fleet Commander, an expedition and the policies are called in each faction) and the machinery to resolve it per faction.

## Remarks before planning

1. **The neutral layer is now the house voice.** The proposal's neutral base was "Commander / assigned market / gathering / operation commander". Your approved wording pass replaced it with Holdings, Rave, Expedition, Fleet Commander, Tournament, Revenue Policy and the rest. The profiles should therefore be **thin overrides on top of the house voice**, not new rewrites. I'd keep `neutral.json` as the root profile (`default`) and not ship the proposal's bland neutral at all.

2. **Keep Rave and Tournament universal in Phase 3.** "Rave" appears 236 times in `dialog.json` and 13 more times in the UI and strings files. If the Church's UI said "Vigil" while its commanders kept talking about the rave, it would read worse than today. Varying event names belongs with the dialogue rewrite in Phase 4, if at all. Phase 3 then varies only what the UI names: **holding, council, Fleet Commander office, expedition, the four policy names, and their level names**.

3. **Whole messages, not term tokens.** Tokens like "Grant {market} to {commander} as a {term.holding}" break on articles, plurals and capitalisation, which is the same lesson as the Phase 2 find-and-replace bugs. A profile overrides **complete message keys**. The overridable set is limited to the roughly 60 keys that mention one of those institutions, and the loader rejects overrides of other keys. That keeps the scope bounded and catches typos.

4. **Titles stay in `titles.json`.** It already works, the comm-panel rank sync reads it, and PRESENTATION.md documents it for mod authors. Profiles add one thing: a title ladder for factions that have no `titles.json` entry. A modded corporate faction bound to `corporate` then gets the VP ladder without writing its own. The resolution order becomes: `titles.json` faction entry → legacy `faction.json` ranks → profile chain → neutral. `sync_ranks.py` must follow the same chain, and a test will check that Java and Python agree.

5. **Two code traps that make per-faction words impossible today:**
   - `LawLevel.displayName` is computed once, when the enum class loads. Every faction shares one set of level names, and reloading wording doesn't update them. It must become a lookup by faction and policy (`Presentation.levelName(faction, law, level)`).
   - `Lawset.LawType.lawName` still loads the old policy names from `strings.json` into the enum. Nothing displays it any more, but it duplicates `policy.name.*` in `neutral.json`. I'd remove it so there's one source.

6. **Test Phase 2 in game first.** Phase 2, the wording pass, council progression and greetings have never run in a campaign. Phase 3 touches the same screens (Politics, Council, events), so a quick pass through [TESTING.md](../../TESTING.md) now would keep bugs from the two phases apart.

## The profiles

Draft wording for your review. Only the overridden keys are listed; everything else inherits from the house voice.

| Profile | Inherits | Used by | Holding | Council | Office | Expedition |
|---|---|---|---|---|---|---|
| `default` (house voice) | — | anything unbound, player | Holding | Council | Fleet Commander | Expedition |
| `naval` | default | Hegemony, independents | Command | Admiralty Board | Fleet Commander | Expedition |
| `corporate` | default | Tri-Tachyon | Concession | Board | Director of Operations | Operation |
| `aristocratic` | default | Persean League | Seat | League Council | Strategos | Expedition |
| `religious` | default | Luddic Church, Luddic Path | Parish | Synod | Knight-Commander | Crusade |
| `raider` | default | Pirates | Turf | Captains' Table | Raid Boss | Raid |

Faction-level overrides on top of that (each is a few keys):
- **Sindrian Diktat** (naval): council → *Executive Council*; holding → *Prefecture*; office → *Marshal of the Fleet* (question 3).
- **Luddic Path** (religious): council → *Conclave*; holding → *Cell*; expedition → *Purge*.

Policy names per profile (levels can be renamed too):

| Policy (house name) | naval | corporate | aristocratic | religious | raider |
|---|---|---|---|---|---|
| Central Authority | Command Authority | Executive Control | Archon's Prerogative | Pontifical Authority | Boss's Word |
| Privilege of Rank | Privilege of Rank | Seniority Rights | Ancient Privilege | Clerical Privilege | Captains' Rights |
| Revenue Policy | Revenue Policy | Margin Policy | Levies | Tithes | Cut of the Take |
| Rave Licensing | Rave Licensing | Rave Licensing | Rave Licensing | Rave Licensing | Rave Licensing |

The proposal also had a "dynastic" option; the Persean League's `aristocratic` profile covers it. These are first drafts to argue with, not decisions.

## Implementation steps

1. **Classify the keys.** A script tags each `neutral.json` key that names one of the six institutions (holding, council, office, expedition, policy, policy level) and writes `data/lords/presentation/overridable_keys.json`. I check the list by hand. This becomes the only set a profile may override.
2. **Profiles file and loader.** Add `data/lords/presentation/profiles.json`, loaded through Starsector's merged-JSON loader (as `titles.json` is), so a mod can add a profile or bind its faction in its own copy:
   ```json
   { "schemaVersion": 1,
     "profiles": { "corporate": { "extends": "default", "titles": [...], "messages": { "holding.assign": "..." } } },
     "factions": { "tritachyon": "corporate" } }
   ```
   `ProfileRegistry` resolves the chain once per load into an immutable per-faction view. It rejects: unknown parents, cycles, overrides of keys outside the overridable list, empty text, and an override that uses a token its neutral key's context doesn't supply. Every error names the file path and key.
3. **API.** `Presentation.text(key, faction)` and `format(key, faction, ...)`, plus `lawName` (now real), `levelName` and `title` through the chain. The existing faction-less calls stay and mean `default`, so unconverted call sites keep working.
4. **Convert the call sites that carry institutional words**, following the design's context table: `LawsIntelPlugin`, `LawProposal` and `ProposalIntelPlugin` use the deciding faction; `CouncilIntelPlugin` uses the council's faction; event intel uses the host's faction; requests use the requesting commander's faction; mixed lists (`LordsIntelPlugin`) stay `default`. Replace `LawLevel.displayName`, and remove `LawType.lawName` with its `strings.json` entries.
5. **Write the profiles** from the table above, after you've reviewed the wording. The design says bindings come only after a wording review.
6. **Tools and tests.**
   - `sync_ranks.py` follows the profile chain.
   - New `tools/preview_profiles.py` writes a Markdown table of every overridable key as each vanilla faction sees it, for wording review without launching the game. The design listed this as a later tool; it's cheap and saves campaign restarts.
   - Tests: chain resolution, cycle and parent errors, override-key and token validation, unknown faction → default, two factions rendered back to back with no leakage (the old enum problem), titles agreeing between Java and Python, and every binding naming an existing profile.
7. **Docs, build and checklist.** Update PRESENTATION.md (profiles for mod authors), TESTING.md (what to look at in each faction's Politics screen), and IMPLEMENTATION_PROGRESS.md. Rebuild the JAR and ZIP, and commit in reviewable pieces.

No save changes: profiles only affect wording, so a running campaign picks up new words on restart.

## Questions for you

1. **Rave and Tournament:** keep them universal in Phase 3 (my recommendation), or should event names vary by faction now?
2. **Profile wording:** accept, change or strike entries in the two tables. "Crusade" for the Church and "Raid Boss" for pirates are the most opinionated.
3. **Diktat office:** vanilla already has a Diktat Fleet Commander, Oxana Hyder (see the deferred [lore study](LORE_INTEGRATION_STUDY.md)). Rename our council-appointed office for the Diktat (for example *Marshal of the Fleet*) or leave it?
4. **Player faction:** stay on `default`, or add a LunaLib setting to pick a profile for your own faction? It's small but optional.
5. **In-game test first:** will you run the TESTING.md pass before I start, or should I begin with steps 1–3 (no visible changes) in the meantime?
