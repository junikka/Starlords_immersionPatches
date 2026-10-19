# Proposal: shared mechanics, faction-specific institutions and writing

**Status: accepted implementation target.** Progress is tracked in [IMPLEMENTATION_PROGRESS.md](IMPLEMENTATION_PROGRESS.md), using the five phases below. The example JSON beside this document is not yet loaded by the mod. Its names and ranks illustrate an approach; they are not claims about canonical faction ranks or final replacement decisions.

**User decisions, 2026-10-19:** compatibility with older saves is optional and must not constrain a better design. A phase may require a new campaign. Every successful build replaces the repository runtime JAR and produces a mod ready to copy into Starsector's `mods` directory, with brief testing instructions. These decisions supersede the original save-migration requirements.

The proposed premise is: **influential fleet commanders control resources, maintain personal networks, and compete for authority inside their factions.** That supports professional navies, corporations, religious orders, pirate coalitions, and more. It also explains why a commander has enough independent influence to defect with ships and sometimes markets.

The target is a coherent neutral experience for any unconfigured faction, with richer authored profiles for factions we support. Perfect bespoke writing for every possible mod cannot be automatic. A good fallback makes missing custom content unobtrusive.

## Preserve features by describing their function

| Existing feature | Neutral description | Military interpretation | Corporate interpretation | Raider interpretation |
| --- | --- | --- | --- | --- |
| Lord | Commander | Officer | Executive | Captain / warlord |
| Fief | Assigned market | Territorial command | Concession | Holding / territory |
| Fief income | Local revenue | Command allocation | Operating revenue | Tribute / proceeds |
| Marshal | Operation commander | Task force commander | Operations director | Raid leader |
| Council | Leadership council | Command council | Executive board | Captains' council |
| Crown authority | Executive authority | Command authority | Executive control | Leader's authority |
| Noble authority | Seniority influence | Senior officer influence | Executive influence | Captains' influence |
| Feast | Gathering | Shore-leave gathering | Corporate retreat | Victory celebration |
| Tournament | Piloting competition | Fleet championship | Sponsored competition | Piloting contest |
| Recruitment/defection | Change allegiance | Transfer / defection | Change employer / break with employer | Join another coalition |
| Courtship/marriage | Romance / marriage | Personal relationship | Personal relationship | Personal relationship |

These labels must remain truthful to the mechanics. A multi-day event involving socializing, competition, and romance should not become a short operational briefing. A vote that can reject a proposal should not be described as an order already issued. A market that can follow a defector needs more than a purely ceremonial appointment. A marriage should not silently become a military alliance with different implied obligations.

Keep mechanics and presentation independently configurable. A corporate wording profile must not silently change tax income, vote weight, defection odds, or event availability. Later behavior presets can be explicit optional additions.

## Four authoring layers

1. **Neutral base:** complete UI text and complete usable dialogue for every feature and personality. Avoid assumptions of monarchy, nobility, human-only institutions, or a named faction leader where possible.
2. **Reusable institutional profiles:** naval, corporate, raider, religious, and optionally dynastic. Each overrides only the terms and sentences it needs. Begin with a few profiles, not a taxonomy of every possible government.
3. **Faction profile:** selects one base profile and overrides its distinctive ranks, institutional names, events, and dialogue. Most factions should require a small file.
4. **Character profile:** optional personal titles, address preferences, background-specific voice, and exceptional lines. A character's personality remains separate from the institution they serve.

Use single inheritance for profiles initially: neutral → institutional profile → faction profile. Avoid multiple inheritance and independent style modules until there is a demonstrated need; contradictory inherited phrases are difficult to debug.

Inheritance applies **per key**. For prose that varies by personality, select the character-specific variant first, then the most specific faction/profile variant, then the neutral personality line. A generic faction line should not accidentally replace every personality's voice: personality-specific variants are preferred across the inheritance chain before a generic variant. A character author may explicitly force a personal line regardless of personality.

Existing dialogue rules still decide whether an action succeeds, which branch is chosen, and which effects run. The new presentation layer supplies the words for that selected branch. Character-specific scripted dialogue can continue using the existing `dialogOverride` mechanism.

## The codebook needs vocabulary and complete messages

Vocabulary handles repeated labels: singular/plural nouns, rank titles, forms of address, institutions, event names, and action labels. Full messages handle grammar and meaning. Replacing “liege” with “CEO” cannot repair a speech about a rightful claimant and sworn fealty.

Prefer semantic message keys such as `holding.assigned`, `campaign.started`, and `recruitment.accept`. These describe what happened, independently of the eventual words. The existing `fief`, `feast`, enum values, classes, and save keys can remain behind an adapter.

Examples of the same event:

- Neutral: “Mara Venn has been assigned control of Kestrel.”
- Naval: “Admiral Venn has assumed command of the Kestrel district.”
- Corporate: “Director Venn has been awarded the Kestrel concession.”
- Raider: “Venn's captains now control Kestrel.”

Each message represents the same underlying transfer of control. Each may use a different sentence structure. We should not force every faction through “Award {holding} to {lord}”.

An authoring note such as `writingNotes: "formal, transactional, status-conscious"` can guide human writers, but it does not generate dialogue. The game renders explicitly authored strings. There is no runtime language-model dependency.

## Titles must distinguish rank, role, office, and address

- **Political tier:** the existing 0–2 value that affects influence and income.
- **Displayed rank/title:** faction-specific wording; mapping it onto political tier is a configurable convention, not a universal truth.
- **Role:** optional per-character profession, such as security executive, commodore, or religious representative. This can explain characters of equal tier with different titles.
- **Office:** an appointment, such as the person coordinating a campaign. It must not be confused with the faction leader or the character's permanent rank.
- **Address:** how someone is spoken to. It need not equal the full formal title.

A neutral profile can call all three tiers “Commander” while showing political standing separately. It need not invent a universal admiralty. A naval profile can explicitly choose a rank ladder; a corporate profile can choose corporate titles without making every tier-2 character the company's CEO.

Use one title resolver for profiles, options, notifications, promotion, and recruitment offers. Current faction normally controls an institutional rank. A personal epithet may survive defection; a former faction's commissioned rank should not be preserved merely because it was stored as a character override. Overrides therefore need a declared scope, such as permanent or restricted to specified faction IDs. A character's name must contain neither their current rank nor their office.

An explicit new profile binding should take precedence over legacy faction `rank_0/1/2` values. For unconverted content, an adapter can still respect explicitly configured legacy ranks, with the neutral fallback for missing values. Ship deliberate bindings for vanilla factions and the player so inherited original “Lord” titles do not leak back into the new presentation.

## Context must be explicit

The renderer receives the relevant people, markets, and authority faction; it must not infer everything from whichever NPC happens to be speaking.

| Text | Which profile supplies institutional wording? |
| --- | --- |
| Character's current rank | Character's current faction |
| Rank offered in recruitment | Destination faction, including player's commission where appropriate |
| Market award or law proposal | Faction making the decision |
| Event invitation and venue | Event host's faction |
| Target's title within another person's dialogue | Target's current faction |
| Mixed-faction roster/category label | Neutral UI vocabulary |
| Biography/background references | Authored historical context; do not rewrite history on defection |
| Romance/personal address | Character preferences/background, with a neutral fallback |

Use a bounded token vocabulary, for example `{recipient.fullName}`, `{recipient.formalName}`, `{market.name}`, and `{host.formalName}`. The code computes these values. Tokens are exact matches; inserted values are literal text, never regex replacements or executable expressions. Preserve old `%LORD_TITLE`-style markers through a compatibility adapter while migrating.

Resolve live profile views from current state. Record event-time names/factions/titles for historical notifications where later defection would otherwise rewrite what the message says happened. Validate tokens against each message's allowed context so an award message cannot request a missing host.

## JSON organization and loading

The [worked profile example](E:/Modding/Starsector/Starlords_immersionPatches/docs/immersion-audit/faction_profiles.example.json) groups profiles in one file to make comparison easy. Production authoring should use one faction file and a separate commander roster, registered through an explicit pack manifest.

Illustrative layout, not existing paths:

```text
data/lords/immersion/packs.json             merged registry keyed by namespaced pack ID
data/my_pack/manifest.json                 schema version, requirements, registered files
data/my_pack/profiles/my_faction.json       inherits a base; overrides wording
data/my_pack/commanders/my_faction.json     authored commanders
data/my_pack/dialogue/special_characters.json
```

The manifest should declare the providing mod ID, required faction mods, exact faction IDs, profile files, roster files, and exceptional dialogue files. Load through Starsector's mod-aware resource access, not arbitrary operating-system directory scanning. An explicit manifest supports packaged mods and gives reproducible registration order.

Recommended merge rules:

- Namespace profile, character, pack, and new dialogue IDs with the author's mod/pack ID.
- Deep-merge ordinary objects; replace arrays rather than silently concatenating them.
- Missing keys inherit. Invalid/null values produce diagnostics rather than silently deleting required words. Intentional suppression must be explicit.
- Detect inheritance cycles, missing parents, and ambiguous equal-priority overrides.
- A pack that intentionally overrides another declares that relationship and precedence. Do not rely on incidental JSON iteration order.
- Do not instantiate bindings for absent factions. Skip optional content with a clear diagnostic; keep unrelated factions functional.
- Unknown available factions use the neutral profile automatically. Never guess culture from a faction's display name.

An unfamiliar faction can still use the shared systems with neutral wording. More unusual societies, such as factions without individual human leadership, may need authored character roles and relationship scenes as well as institutional labels. We should support those exceptions without removing a feature from the mod as a whole; a word substitution alone cannot promise a faithful portrayal of every fictional society.

For a new faction, the author should be able to select a base, set three ranks and a few institutions, add their commanders, and play. Writing custom recruitment speeches, gathering scenes, and biographies should be optional additional work.

## Commander packs

Separate identity and content from wording. New records should eventually have a stable namespaced ID and a distinct display name. Keep ship preferences, faction IDs, portraits, and lore in commander data. Keep most titles and institutional vocabulary in the faction profile. Custom titles or voice IDs belong on individual commanders only when they are exceptions.

Support the existing name-keyed `lords.json` authoring format through an adapter where useful. New records use stable IDs so display-name edits do not duplicate or orphan characters within a supported campaign. Converting old-version saves is optional; stable IDs may be introduced with a new-campaign requirement.

For packs that support adding characters to an ongoing campaign, register each character once, keyed by stable template ID and saved person ID. Distinguish “never spawned” from “spawned and subsequently died”; otherwise loading a pack could resurrect dead characters. Keep a saved registration record/tombstone when the life/death system is active. Packs may instead require a new campaign; document the chosen behavior explicitly.

Keep optional faction rosters in packs that declare their dependencies. Validate faction IDs, ship variants, portraits, markets, rank ranges, personalities, and dialogue references. Report the failing record and field; the current silent template-skip behavior would be frustrating for a large manual roster.

## Concrete Java seams

Proposed components:

- `PresentationRegistry`: loads, validates, resolves inheritance, and caches immutable profiles.
- `FactionPresentation`: resolved terminology, rank/office/address rules, and message overrides. Separate from the existing gameplay-focused `FactionTemplate`.
- `TextRenderer`: safe exact-token formatting with explicit context and neutral fallback.
- `TitleResolver`: one current/destination-faction-aware title lookup used by all UI/dialogue paths.
- `CommanderCatalog`: optional subsequent work for registered roster files, stable IDs, and existing-save additions.

Integration points confirmed in the source:

- [FactionTemplateController](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/util/factionUtils/FactionTemplateController.java) already establishes a faction lookup/fallback pattern.
- [Lord.getTitle](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/person/Lord.java:487), `Utils.getFactionTitle`, and legacy `Utils.getTitle` should delegate to the same resolver.
- [DialogSet](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/util/dialogControler/DialogSet.java:649) applies rules, text, addons, and options. Add presentation resolution at text/hint/highlight rendering, preserving selected behavior and effects. Do not replace whole scripted dialogue objects just to change one sentence.
- [Lawset.LawType](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/faction/Lawset.java:18) stores `lawName` globally on enum instances. Replace display-time uses with `getLawName(faction, lawType)`; mutating the enum for a faction would change other factions too. [LawProposal](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/faction/LawProposal.java:152) and [LawsIntelPlugin](E:/Modding/Starsector/Starlords_immersionPatches/src/starlords/ui/LawsIntelPlugin.java:216) both read it.
- Localize complete event/intel/request messages and event headings. Changing strings.json alone misses Java literals and derived event names.
- Retain existing internal names when convenient, but allow class, enum, memory-key, and saved-data changes when they improve the design. Old saves need not be migrated; saving and reloading within a supported build must still work.

## A manageable implementation order

1. **Build foundation and runtime checks.** Establish a reproducible JAR build against the intended game/dependencies. Keep a historical source/runtime baseline, automatically update the runtime JAR, and produce an installable package. Old-save comparisons are optional diagnostics.
2. **Neutral presentation and common resolver.** Externalize visible text, fix title inconsistencies and token handling, keep existing formats working, and preserve all features. This makes unconfigured factions coherent immediately.
3. **Profile inheritance and a few distinct examples.** Implement neutral, naval, corporate, and raider profiles; use actual faction bindings only after wording review. Move law names, offices, events, hints, and tooltips through the resolver too.
4. **Faction/personality dialogue.** Rewrite recruitment, territorial control, social events, and romance passages where noun substitution cannot work. Keep shared branch conditions/effects.
5. **Modular commander packs and stable identity.** Add manifest registration and stable IDs separately from the wording overhaul. Define pack additions/death persistence for supported campaigns; allow a new-campaign requirement when changing the save format.

Verification should cover an unconfigured faction, an absent optional faction mod, two simultaneous faction UIs, a commissioned player, promotion, cross-faction recruitment, defection, a missing faction leader, a character override, names containing token-like characters, and saving/reloading a campaign created with the same build. Older-version saves are optional. Dialogue checks must verify that credits, reputation, options, and other effects remain the same after a text override. Once roster registration exists, verify duplicate prevention and death persistence as well.

A simple development preview should render the same event for each profile and all four personalities without requiring a campaign for every prose revision. It should also show which profile supplied each inherited value. This can be a later authoring tool; it is not part of the current draft.
