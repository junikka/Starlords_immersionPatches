# Immersion overhaul progress

The [accepted design](MODULAR_PRESENTATION_PROPOSAL.md) is the implementation target. Phase numbers follow its "A manageable implementation order" section.

| Phase | Status | Acceptance |
| --- | --- | --- |
| 1. Build foundation and runtime checks | Complete; campaign test passed | Full source build; pinned inputs; identical clean-build outputs; runtime JAR replacement; installable package; optional historical comparisons |
| 2. Neutral presentation and common resolver | Implemented; automated checks passed; in-game review pending | Neutral UI/dialogue; common title resolver; current/destination/host/recipient context; safe tokens; existing content formats preserved |
| 3. Profile inheritance and examples | Not started | Neutral → institutional → faction profiles; per-key fallback; faction-aware offices, laws, events, and UI |
| 4. Faction/personality dialogue | Not started | Authored complete messages with existing branches, conditions, and effects preserved |
| 5. Modular commander packs and stable identity | Not started | Explicit manifests, validation, stable IDs, declared new-campaign/add-on-load policy, duplicate/death persistence |

## Phase 1 evidence — 2026-10-19

- Built all **356 Java source files** into **408 classes**, with no edits to Java source or gameplay data.
- Compiled with **JDK 17.0.3.1**, targeting **Java 16 / class-file major 60**, against **Starsector 0.98a-RC8** and the [recorded integration dependencies](../../build-support/DEPENDENCIES.md).
- `python tools/build.py reproduce`: **passed**. Two independent clean compilations produced identical SHA-256 hashes.
- Rebuilt JAR SHA-256: `d122fe0e81fbd9fe24dbad4ee8515be6ce70a56c9a36e2e7494b5cc9270d5be1`.
- The initial historical comparison passed for original classes, saved fields, public/protected API contracts, enum ordering, and recognized persistence identifiers. Per the user's revised policy, this is now optional and does not gate builds.
- `python -m unittest discover -s tools/tests -v`: **14 tests passed**, including runtime checks, permitted save-schema changes, installable packaging, and successful/failed publication workflows; no skips.
- `jars/StarLords.jar` is now the rebuilt runtime, updated on every successful `build` or `reproduce`. The original JAR remains in Git history, with SHA-256 `83ef185f96b81b1acc13d97ecfaff596544e2b004bdc123fc1ae016d3d02dcda`.
- The build emits **22 existing-source Lombok warnings** (an already-present setter and ineffective `@SneakyThrows` annotations), plus unchecked-operation notes. These do not prevent compilation; the log is retained in `build/javac.log`.

Copy the repository mod folder into `Starsector/mods`, or extract the clean `build/Starlords_immersionPatches.zip` there. The detailed generated report is `build/build-report.json`. [BUILDING.md](../../BUILDING.md) describes configuration, commands, output handling, and baseline policy.

## Phase 1 campaign result — confirmed by user

Characters were present, changes persisted after saving/reloading, and log output was nominal. The user accepted the Phase 1 test and authorized Phase 2.

## Phase 2 evidence — 2026-10-19

- Added `Presentation`, `TitleResolver`, `TextRenderer` and `DialogueTokens`. All internal current-title/promotion/recruitment display paths use the common title resolver; recruitment offers now use the player's destination/commission faction instead of the speaker's faction.
- Added **352 neutral message entries** and explicit vanilla/player title bindings. Unconfigured factions use Commander; unconverted factions can retain explicit legacy ranks. Numbered political standing distinguishes the three tiers independently of title wording.
- Neutralized UI, policy/proposal/request/event wording, dialogue, settings labels and console help. Rewrote the generic fealty/recruitment and social passages that noun replacement alone could not repair. Removed `(error)` prose from usable fallback dialogue and corrected recruitment highlights.
- Preserved all dialogue branch IDs, conditions, effects, option routes, credit/reputation values, roster gameplay fields, settings values and console command IDs. Existing `%...` markers, custom inserts, `dialogOverride`, roster format and internal feature names remain supported.
- Rendering is single-pass and literal. Fixed first-name prefix collisions, regex-sensitive names, speaker/player proposal confusion, missing optional people/ships/markets, and market objects leaking into proposal text. Hints/highlights share custom inserts with their line/option. Missing named context is logged and gets neutral fallback text.
- A ship-generation emergency still supplies its backup ship and now preserves the authored identity/biography rather than replacing them with meta prose.
- `python -m unittest discover -s tools/tests -v`: **26 tests passed, no skips**. Includes real source compilation against the installed APIs, controlled game-context integration checks, dialogue effect ordering, data invariants, highlighting and packaging.
- `python tools/build.py reproduce`: **passed**. **360 source files / 412 classes**, Java 16 bytecode; two clean builds produced identical JAR SHA-256 `92a8bb25e224064e97912682ad858c102ff9cebd2891fa6ccd57f4ec9f0aa544`.
- Updated repository `jars/StarLords.jar` and `build/Starlords_immersionPatches.zip`. The package includes the current data and [testing](../../TESTING.md) / [authoring](../../PRESENTATION.md) instructions. No changes were installed into the game directory.

Phase 2 still needs the user's in-game review of dialogue, policy UI, gatherings and saving/reloading. Automated checks use controlled API context; they do not run a campaign or assess visual layout. Existing Lombok warnings remain unchanged.

## Next phase

Phase 3 implements neutral → institutional → faction profile inheritance and examples, including faction-specific offices, laws, events and messages. The example profile JSON remains a design illustration and is not loaded at runtime. Direct title bindings already work through `data/lords/presentation/titles.json`.

Later phases add richer faction/personality voices and modular commander packs. Authored historical biographies remain historical; changing allegiance does not rewrite their backgrounds. New commander content currently uses the existing `lords.json` format and a new campaign. Old-save compatibility remains optional.

## Future extensions

- [Weaving Starlords into the Sector's lore](LORE_INTEGRATION_STUDY.md): faction leaders as the court, crown worlds, commanders as the military command of their holdings. Deferred by Julie on 2026-10-19; not part of Phases 3–5.
