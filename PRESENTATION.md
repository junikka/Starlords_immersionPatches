# Editing wording, titles and faction profiles

Phase 2 supplied the presentation layer and its house wording; Phase 3 added faction profiles on top of it. Gameplay numbers, faction capabilities, dialogue conditions/effects, and roster identities keep their existing formats. Restart Starsector after editing runtime JSON. A Java rebuild is unnecessary for wording edits; rebuild when you want the distribution ZIP refreshed.

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

Resolution order is explicit `titles.json` faction binding → explicit legacy `data/lords/faction.json` `rank_0/1/2` → the faction's profile ladder (see [Faction profiles](#faction-profiles)) → neutral title for a missing tier. The supplied vanilla/player bindings deliberately override the old Lord titles. Unknown factions require no binding and fall back to Captain / Commodore / Admiral. A title binding never creates a faction or spawns a character; an absent optional faction does not prevent loading.

Characters use their **current** faction's title. Recruitment offers use the **destination** faction, including the player's commission. Promotion proposals use the proposing faction's offered title. Speaker, recipient, host and player titles are resolved independently. The Fleet Commander is an office, separate from the person's title and political standing.

### The comm panel's "Rank:" line

The portrait panel in conversations shows Starsector's own rank, not the Starlords title. Lords carry the rank ids `starlords_tier_0/1/2`, and `data/world/factions/default_ranks.json` plus one partial `<faction>.faction` per faction give those ids the same names as `titles.json`. These files are generated: after editing `titles.json` or `profiles.json`, run `python tools/sync_ranks.py`. It resolves titles in the same order as the game, and a test checks that both agree. A modded faction pack ships its own partial `.faction` file:

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

## Faction profiles

`data/lords/presentation/profiles.json` lets a faction call its institutions by its own names: what a holding is, what the council is called, the Fleet Commander office, an expedition, and the four policies with their tooltips. Raves and tournaments are deliberately the same in every faction.

```json
{
  "schemaVersion": 1,
  "profiles": {
    "corporate": {
      "titles": ["Vice President", "Senior Vice President", "Executive Vice President"],
      "messages": {
        "laws.award_fief": "Grant concession",
        "holding.assign": "Grant {market} to {commander} as a concession"
      }
    },
    "my_company": { "extends": "corporate", "messages": { "council.council": " Directorate" } }
  },
  "factions": { "tritachyon": "corporate", "my_faction_id": "my_company" }
}
```

- **Inheritance is per message.** A faction's profile is checked first, then each profile it `extends`, then the house wording in `neutral.json` (the implicit `default` profile, which can't be redefined). A profile only lists what it changes.
- **Only institutional messages can be overridden.** The allowed keys are listed in `overridable_keys.json`, the 84 messages that name a holding, the council, the office, an expedition or a policy. Overriding any other key is a load error, which also catches typos.
- **Write whole messages, not single words.** Each override is a complete sentence with its own grammar ("a command", "some turf"). It may use only the `{tokens}` its default message receives, and must keep that message's leading/trailing spaces and its `|` tooltip separators.
- **Which faction's words apply** depends on what the text is about: policy and proposal texts use the deciding faction; council screens, the council's faction; expedition events, the faction calling them; a commander's intel, that commander's faction; requests to you, your commission's faction. Lists mixing factions use the default wording.
- **`titles`** is an optional three-rank ladder. It only applies to factions without a `titles.json` entry, so a modded faction bound to `corporate` gets the Vice President ladder for free.
- **Your own faction** uses the `default` profile unless you pick another under LunaLib's **Other** settings ("Your faction's wording"), or `playerFactionProfile` in `data/config/starlord_settings.json` without LunaLib. This changes words only: your rank titles stay as `titles.json` sets them.
- **Another mod** adds a profile or binds its faction by shipping its own `data/lords/presentation/profiles.json` with just its entries; Starsector merges it with this one. Unknown factions use `default`.

Every error names the file and key: an unknown parent, an inheritance cycle, a binding to a missing profile, a non-overridable key, an empty message or a token the context doesn't supply. `python tools/preview_profiles.py` writes `docs/immersion-audit/generated/profile_preview.md`, a table of every overridable message as each faction sees it, for reviewing wording without launching the game.

## Starting policies and council progression

`data/lords/faction.json` also holds two gameplay blocks per faction: `startingPolicies` (where the four policy sliders start in a new campaign) and `progression` (what the player needs to climb that faction's council). Both are optional. The format, defaults and vanilla values are in `docs/immersion-audit/PROGRESSION_AND_POLICIES.md`. Their player-facing texts are the `progression.*` and `promote_player.*` messages in `neutral.json`.

## Fleet greetings by rank

`data/lords/presentation/greetings.json` holds what ordinary fleets of your council faction say when you hail them: three lines per faction, one per rank, plus a `default` set. Tokens: `{title}`, `{lastName}`, `{fullName}`, `{sirOrMadam}`. Another mod can add its faction under `factions` in its own copy of the file; Starsector merges them. The rule that triggers them is `starlords_rankGreeting` in `data/campaign/rules.csv`.

## Rave music

While you talk to a commander at a rave, and during the tournament, the game plays the music set `starlords_rave` instead of the normal campaign or market music. Leaving hands the music back, the way vanilla's Luddic shrines do it. If a rave dialog closes some other way (Esc, say), a failsafe restores the normal music after about a second and a half. The tournament fight itself keeps the normal combat music, and the rave track comes back afterwards. LunaLib's **Other → Rave music** turns it off (`raveMusic` in `starlord_settings.json` without LunaLib).

The set is defined in `data/config/sounds.json`, which Starsector merges into its own. It plays `sounds/music/starlords_rave.ogg` from this mod. To change or add tracks:

1. Save them as OGG Vorbis in `sounds/music/` inside this mod. Keep them to a few MB each; about 160–192 kbps is plenty for game music.
2. List them in the `files` array of the `starlords_rave` entry in `data/config/sounds.json` to make a playlist.
3. Restart Starsector. No Java rebuild is needed; a rebuild just puts the tracks into the distribution ZIP.

Without a file of its own the mod can point at a track in the game's archive instead; the comment in `sounds.json` shows the original placeholder, Tri-Tachyon's friendly market theme.

Only ship music you're allowed to redistribute, and credit it in the README if its licence asks for that.

## Where wording lives

| File | Purpose |
| --- | --- |
| `data/lords/presentation/neutral.json` | Shared UI labels, complete proposal/event/request messages, policy names, levels and tooltips, console feedback and context fallbacks. This is the `default` profile |
| `data/lords/presentation/profiles.json` | Faction profiles: institutional wording per faction (see above) |
| `data/lords/presentation/overridable_keys.json` | Which `neutral.json` messages a profile may override |
| `data/strings/strings.json` | Existing localized notifications and modifiers |
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

Phase 3 (done) added the faction profiles described above. Notifications still read from `strings.json` (captures, defections, revenue collection) and the dialogue in `dialog.json` use the house wording in every faction.

Phase 4 expands authored faction/personality voice and exceptional character address, including dialogue that names holdings or the council the way each faction does. Existing character-specific overrides remain usable. Biographies retain authored historical context rather than changing faction history when a character defects. A ship-generation emergency now preserves an authored character's identity instead of replacing it with a meta joke.

Phase 5 adds registered commander packs and stable IDs. For now, add commanders through the existing `lords.json` format and start a new campaign. Changing display names is not yet a stable-ID roster migration mechanism.
