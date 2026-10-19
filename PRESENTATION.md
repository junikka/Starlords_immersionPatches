# Editing Phase 2 wording and titles

Phase 2 supplies a neutral presentation layer. Gameplay numbers, faction capabilities, dialogue conditions/effects, and roster identities keep their existing formats. Restart Starsector after editing runtime JSON. A Java rebuild is unnecessary for wording edits; rebuild when you want the distribution ZIP refreshed.

## Titles

Edit `data/lords/presentation/titles.json`:

```json
{
  "schemaVersion": 1,
  "neutralRanks": ["Captain", "Commodore", "Admiral"],
  "factions": {
    "my_faction_id": ["Captain", "Commodore", "Admiral"]
  }
}
```

The example is a shape reference: **add a faction entry to the supplied file**, retaining the existing vanilla/player bindings. Use exact faction IDs. Each list needs three nonempty strings, corresponding to internal political tiers 0–2 (displayed as standings 1–3). Repeating a title is valid. Changing a title does not change political influence or income.

Resolution order is explicit `titles.json` faction binding → explicit legacy `data/lords/faction.json` `rank_0/1/2` → neutral title for a missing tier. The supplied vanilla/player bindings deliberately override the old Lord titles. Unknown factions require no binding and fall back to Captain / Commodore / Admiral. A title binding never creates a faction or spawns a character; an absent optional faction does not prevent loading.

Characters use their **current** faction's title. Recruitment offers use the **destination** faction, including the player's commission. Promotion proposals use the proposing faction's offered title. Speaker, recipient, host and player titles are resolved independently. The Fleet Commander is an office, separate from the person's title and political standing.

### The comm panel's "Rank:" line

The portrait panel in conversations shows Starsector's own rank, not the Starlords title. Lords carry the rank ids `starlords_tier_0/1/2`, and `data/world/factions/default_ranks.json` plus one partial `<faction>.faction` per faction give those ids the same names as `titles.json`. These files are generated: after editing `titles.json`, run `python tools/sync_ranks.py`. A modded faction pack ships its own partial `.faction` file:

```json
{ "ranks": { "ranks": {
  "starlords_tier_0": {"name": "Lieutenant"},
  "starlords_tier_1": {"name": "Captain"},
  "starlords_tier_2": {"name": "Grand Admiral"}
} } }
```

### Ranks for modded factions

`titles.json` is read with Starsector's merged-JSON loader, so a faction pack does not need to touch this file. Ship `data/lords/presentation/titles.json` inside your own mod with only your factions:

```json
{
  "schemaVersion": 1,
  "factions": {
    "my_faction_id": ["Lieutenant", "Captain", "Grand Admiral"]
  }
}
```

Starsector merges it with the Starlords copy at load time.

## Starting policies and council progression

`data/lords/faction.json` also holds two gameplay blocks per faction: `startingPolicies` (where the four policy sliders start in a new campaign) and `progression` (what the player needs to climb that faction's council). Both are optional. The format, defaults and vanilla values are in `docs/immersion-audit/PROGRESSION_AND_POLICIES.md`. Their player-facing texts are the `progression.*` and `promote_player.*` messages in `neutral.json`.

## Fleet greetings by rank

`data/lords/presentation/greetings.json` holds what ordinary fleets of your council faction say when you hail them: three lines per faction, one per rank, plus a `default` set. Tokens: `{title}`, `{lastName}`, `{fullName}`, `{sirOrMadam}`. Another mod can add its faction under `factions` in its own copy of the file; Starsector merges them. The rule that triggers them is `starlords_rankGreeting` in `data/campaign/rules.csv`.

## Where wording lives

| File | Purpose |
| --- | --- |
| `data/lords/presentation/neutral.json` | Shared UI labels, complete proposal/event/request messages, console feedback and context fallbacks |
| `data/strings/strings.json` | Existing localized notifications, policy descriptions and modifiers |
| `data/lords/dialog/dialog.json` | Existing dialogue branches, with neutral text, hints and highlights |
| `data/lords/lords.json` | Existing authored identities, biographies, fleet names and gameplay templates |

Message IDs are stable identifiers, not visible prose. Some IDs still contain `lord`, `fief`, `feast` or other original implementation names. Edit the values. Some older UI messages are still composed from fragments with significant leading/trailing spaces; complete proposal/event/request messages use named tokens.

`Presentation.format("holding.assign", "market", marketName, "commander", formalName)` renders `Grant {market} to {commander} as a holding`. A named token must be supplied by that call's context. Missing context logs the message ID and token and displays a neutral unavailable message. Invalid schemas, non-text/empty messages, or malformed explicit rank arrays fail with a path-specific diagnostic. No expressions, regex replacements or runtime text generation are executed.

## Dialogue authoring

The existing `%PLAYER_NAME`, `%PLAYER_NAME_FIRST`, `%LORD_TITLE`, `%SECOND_LORD_NAME`, host/spouse markers, `%c0` custom inserts, and `dialogOverride` format remain supported. Tokens match exactly and are substituted once. Inserted names/values are literal: `$`, backslashes and token-shaped text inside a name are not interpreted again.

New bounded aliases available to dialogue include:

- `{player.fullName}`, `{speaker.fullName}`, `{recipient.fullName}`, `{host.fullName}`; the same actors provide `.formalName`, `.title`, `.factionName`.
- Partner actors `playerPartner`, `speakerPartner`, `recipientPartner`, `hostPartner`, `weddingPartner`, `weddingPartnerSpouse`, with the same four fields.
- `{market.name}`, `{destination.name}`, `{destination.rank0}`, `{destination.rank1}`, `{destination.rank2}`.

Missing optional actors/ships/markets use neutral context descriptions. The existing gameplay rules should still select an appropriate branch. Custom inserts are evaluated once for a displayed line/option; text, highlights and hints share those values. Caller-supplied inserts override custom inserts, which override built-in context. Inserted values cannot themselves expand into more tokens.

Change text, `hint`, and highlight text together. Leave branch keys, rules, options, addons, commodity IDs, credits, reputation values and custom-insert definitions intact unless deliberately changing gameplay. Literal highlights must occur in their associated sentence.

## Scope of the next phases

Phase 3 adds neutral → institutional → faction inheritance and faction-specific laws, offices, events and messages. This phase has direct faction title bindings and a shared neutral message catalog; the design's example profile JSON is not a runtime configuration yet.

Phase 4 expands authored faction/personality voice and exceptional character address. Existing character-specific overrides remain usable. Biographies retain authored historical context rather than changing faction history when a character defects. A ship-generation emergency now preserves an authored character's identity instead of replacing it with a meta joke.

Phase 5 adds registered commander packs and stable IDs. For now, add commanders through the existing `lords.json` format and start a new campaign. Changing display names is not yet a stable-ID roster migration mechanism.
