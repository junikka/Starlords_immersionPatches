# Integrating a faction mod into Starlords

**Status: guide as of Phase 3 (2026-10-20).** It will change in Phases 4 and 5, which bring faction dialogue, registered commander packs, stable IDs and adding commanders to a running campaign. The worked example (Hazard Mining Incorporated) **was applied on 2026-10-24 as the pilot**, exactly as shown below, so the current build can be tested with and without HMI.

## Is it safe to ship support for a mod the player hasn't installed?

Yes. Everything below is safe to commit, and players without the faction mod lose nothing and crash nothing:

| File | Without the faction mod | With it |
|---|---|---|
| `faction.json` | Skipped at game load, with a log line "Skipping faction settings for …: faction is not installed" (before 2026-10-24 this crashed game load) | Active after a restart |
| `titles.json`, `profiles.json`, `greetings.json`, `raves.json`, faction rules in `dialog.json` | Ignored: looked up by faction ID and never asked for | Active after a restart, even in an existing save |
| `data/world/factions/<file>.faction` (comm-panel ranks) | Never loaded: Starsector only reads faction files that a mod's `factions.csv` lists. HMI and Nexerelin both ship partial `.faction` files for factions they don't own. | Merged into the faction's own file |
| `lords.json` commanders | Skipped at new game, with a log line "Skipping commander …: faction … is not installed" | Created at the start of a new campaign only |

One real risk remains: a **broken JSON file** (a typo, a missing comma) fails for every player, whether or not they have the faction. Always run the tests, and start the game once with the faction mod disabled.

## Step by step

Do the steps in order; each one says which file and what it controls. Steps 1 to 3 are the minimum. Everything else is optional, but each step makes the faction feel more at home.

### 0. Read the faction mod first

From the faction mod's folder, write down:

1. **Faction ID**, exactly as written, including case: the `"id"` in its `.faction` file (HMI: `HMI`, uppercase).
2. **Faction file path**, from its `data/world/factions/factions.csv` (HMI: `data/world/factions/hmi.faction`, lowercase). This can differ from the ID, and step 3 needs it.
3. **Display names**: `displayName`, `displayNameLong`.
4. **Pirate or not**: whether the game treats it as a pirate faction (`Misc.isPirateFaction`). Pirate factions get no diplomacy, policies or invasions by default.
5. **Markets**: from its `data/campaign/econ/*.json` (or its Java): which worlds it owns, its capital (look for `headquarters`), and which have military bases. Get the exact **market IDs** in game with Console Commands: `list markets`.
6. **Ships**: its own hulls (`data/hulls/ship_data.csv`, the `tech/manufacturer` column) and **variant IDs** (`data/variants/*.variant`, the `"variantId"` field).
7. **Portraits**: the `portraits` lists in its `.faction` file.
8. **Lore and named characters**: its `descriptions.csv` faction text, plus any characters it creates (`setFirst`/`setLast` in its sources). New commanders must not duplicate them. See the deferred lore study.
9. **Leader**: an important person with a `FACTION_LEADER` rank, if any. Without one, the faction runs as a council with no crown, like the Church.

### 1. Gameplay settings: `data/lords/faction.json`

One entry keyed by faction ID. Every field is optional; the defaults are shown.

| Field | Default | Meaning |
|---|---|---|
| `leaderID` | none | Important-person ID of the faction leader; their stance weighs on council votes. |
| `startingPolicies` | all `MEDIUM` | `crown_authority`, `noble_authority`, `trade_law`, `feast_law`: `VERY_LOW` … `VERY_HIGH`. For `feast_law`, `VERY_LOW` is Anything Goes and `VERY_HIGH` is Banned. |
| `progression` | see PROGRESSION_AND_POLICIES.md | Days and reputation for the player's seat and promotions. |
| `canStarlordsJoin` | `true` | Whether commanders can serve this faction. |
| `canPreformDiplomacy`, `canPreformPolicy` | not pirate | War/peace proposals; policy votes. |
| `canHoldFeasts` | `true` | Raves. |
| `canTrade`, `canBeTradedWith` | `true` | Commander trade ventures. |
| `canGivesFiefs` (sic) | `true` | Holdings can be granted. |
| `canLordsTakeFiefsWithDefection` | can invade | Holdings follow a defector. |
| `maxNumberOfFiefs` | unlimited | Per commander. |
| `fiefIncomeMulti`, `combatIncomeMulti`, `tradeIncomeMulti`, `commissionIncomeMulti`, `lordFleetUpkeepCostMulti` | `1` | Economy multipliers. |
| `lordRepGainPerWin` | `0` | Reputation commanders gain per battle. |
| `canAttack`, `canBeAttacked`, `canInvade`, `canRaid`, `canBeRaided`, `canSatBomb`, `canBeSatBomb`, `canTacticalBombed`, `canBeTacticalBomb`, `canHaveCampaigns` | not pirate / true | Military behaviour. Careful: in the current code the keys `canAttack` and `canBeAttacked` set each other's value, an upstream quirk. |
| `rank_0` … `rank_2` | none | Legacy rank names. Use `titles.json` instead. |

`data/config/starlords_factionSettings.json` can also force a faction to count as minor (`isMinorFactionOverride`), have no relations (`haveRelationsOverride`), or be immune to organised warfare (`canBeAttackedOverride`).

### 2. Rank titles: `data/lords/presentation/titles.json`

Three titles, tier 0 to 2, under `factions.<ID>`. Without an entry, the faction's wording profile ladder applies, then Captain / Commodore / Admiral.

### 3. Comm-panel rank names: `data/world/factions/<faction file>.faction`

Generated by `python tools/sync_ranks.py` from step 2. The file must have exactly the faction mod's own path, which may differ from the faction ID (paths are case-sensitive for players on Linux and macOS). When it does, map the ID to the mod's file name in `titles.json`: `"factionFiles": {"HMI": "hmi"}` makes the tool write `hmi.faction`.

### 4. Wording profile: `data/lords/presentation/profiles.json`

Under `factions`, bind the ID either to an existing profile (`naval`, `corporate`, `aristocratic`, `religious`, `pirate`) or to a new profile that `extends` one and overrides only what differs. Only messages listed in `overridable_keys.json` may be overridden, as whole sentences using only the tokens their default messages have. `python tools/preview_profiles.py` shows the result. Rules are in PRESENTATION.md, "Faction profiles".

### 5. Fleet greetings: `data/lords/presentation/greetings.json`

Three lines under `factions.<ID>`: what the faction's ordinary fleets say to the player at seat, tier 1 and tier 2. Tokens: `{title}`, `{lastName}`, `{fullName}`, `{sirOrMadam}`.

### 6. Raves: `data/lords/presentation/raves.json`

Under `factions.<ID>`:
- `image`: an `illustrations` key. Use a vanilla bar, or the mod's own art registered in its `settings.json`.
- `arrival`: one text, or one per Rave Licensing level.
- `crowd`: extra crowd lines.

Write it per [the writing guide](immersion-audit/WRITING_GUIDE.md).

### 7. Commanders: `data/lords/lords.json`

One entry per commander, keyed by display name. Shipping commanders is safe for players without the faction mod (they're skipped). But commanders only appear in a **new campaign**, and renaming a key creates a different commander.

| Field | Required | Notes |
|---|---|---|
| `faction` | yes | Exact faction ID. |
| `fleetName` | yes | |
| `isMale` | yes | |
| `personality` | yes | `Upstanding`, `Martial`, `Calculating`, `Quarrelsome`: chooses dialogue and politics. |
| `battle_personality` | yes | `Timid`, `Cautious`, `Steady`, `Aggressive`, `Reckless`. |
| `level` | yes | Officer level. |
| `ranking` | yes | Tier 0 to 2. |
| `fief` | yes | Market ID, or `null`. A missing market makes the commander get a random holding. Don't give a commander the faction's capital if a lore character runs it. |
| `flagship` | yes | Variant ID. |
| `shipPref` | yes | Variant ID → weight, for the rest of the fleet. |
| `portrait` | yes | A path (`graphics/…png`) or a name under `graphics/portraits/`. Don't use a lore character's portrait. |
| `lore` | yes | Biography. It stays historical even after a defection. |
| `preferredItem` | no | Favourite gift: `alpha_core`, `hand_weapons`, `food`, `luxury_goods`, `lobster`, `drugs`, `domestic_goods`. |
| `alignments` | no | Nexerelin alignments; generated if absent. |
| `customSkills`, `executiveOfficers`, `customFleetSMods`, `customLordSMods`, `flagshipForceCustomSMods`, `dialogOverride` | no | Advanced; see theManyReadmes. |

Alternative without writing commanders: LunaLib's *Generate additional commanders on new game start* gives any faction without commanders generated ones.

### 8. Optional: faction dialogue in `dialog.json` (Phase 4 territory)

A top-level dialogue set with `"rules": {"currentFaction": {"<ID>": true}}` and a priority above the personalities (2) overrides any line for that faction's commanders. For example, a rave greeting via a `feast` sub-set with `"isAtFeast": true`. Keep branch keys, options and addons identical; change only text.

### 9. Check, then commit

1. `python -m unittest discover -s tools/tests -v`: all green. It catches bad profile keys, tokens, rave levels, missing pictures and broken JSON.
2. `python tools/preview_profiles.py`, then read the faction's column.
3. `python tools/build.py build`.
4. In game, **without** the faction mod enabled: the game starts and a new campaign starts; `starsector.log` shows the skip line for its commanders and no errors.
5. In game, **with** it enabled, start a new campaign and check:
   - the commanders appear;
   - their titles agree in dialogue, intel and the comm panel;
   - Politics uses the profile's words;
   - fleets greet you at each rank;
   - a `startrave` rave shows the venue and arrival.
6. Commit. Starlords' `mod_info.json` gets **no dependency** on the faction mod: the support is optional.

---

## Worked example: Hazard Mining Incorporated (applied as the pilot)

What I read from the installed HMI 0.4.0e:
- **ID and files:** faction ID `HMI`, file `data/world/factions/hmi.faction`, display "H.M.I." / "Hazard Mining Incorporated", not a pirate faction, playable in Nexerelin.
- **Markets:**
  - Fuyutsuki (size 6, headquarters, High Command)
  - Nevermore Station (3)
  - Hurricane Station (3)
  - Samiel (4, military base)
  - Galena (4)
  - Mess Station 2 (5, military base)
- **Leader:** none. Named characters: Bob Kurita, Brother Francis, the Maxwells. Founder Haseo "Hazard" Fuyutsuki appears in the lore only.
- **Ships:** its own techmined line (Marlin, Brasher, Category, Ascendante, Suleiman, Scarecrow, Weaver, Herakles) and junkers.

### `data/lords/faction.json`: add

```json
"HMI": {
  "startingPolicies": {
    "crown_authority": "MEDIUM",
    "noble_authority": "HIGH",
    "trade_law": "HIGH",
    "feast_law": "VERY_LOW"
  }
}
```

No `leaderID` (HMI has no leader character). Seniority and heavy claim levies suit a cut-throat company that pays its people in shares. Its raves are famous, hence Anything Goes.

### `data/lords/presentation/titles.json`: add under `factions`

```json
"HMI": ["Foreman", "Director", "Board Director"]
```

### `data/world/factions/hmi.faction`: new, generated by `sync_ranks.py` (with `"factionFiles": {"HMI": "hmi"}` in `titles.json`)

```json
# generated by tools/sync_ranks.py; edit titles.json instead
{
  "ranks": {
    "ranks": {
      "starlords_tier_0": {"name": "Foreman"},
      "starlords_tier_1": {"name": "Director"},
      "starlords_tier_2": {"name": "Board Director"}
    }
  }
}
```

### `data/lords/presentation/profiles.json`: add

A holding is a mining **claim**. Everything else (Board, Director of Operations, operations, Executive Control, Margin Policy) inherits from `corporate`. Generated from the same templates as the shipped profiles, then given a hand pass ("Unclaimed claim:" becomes "Unassigned claim:"):

```json
"profiles": {
  "hmi": {
    "extends": "corporate",
    "messages": {
      "fief.to_your_fiefs": " to your claims.",
      "holding.assign": "Grant {market} to {commander} as a claim",
      "holding.revoke": "Revoke {commander}'s claim of {market}",
      "laws.award_fief": "Grant claim",
      "laws.revoke_fief": "Revoke claim",
      "laws.select_whom_to_transfer_fief_to": "Select a commander to receive the claim: ",
      "laws.transfer_fief": "Transfer claim",
      "laws.unclaimed_fief": "Unassigned claim:",
      "laws.you_are_going_to_transfer_one_of_your_fiefs_to_another_lord_sele": "Select one of your claims to transfer to another commander.",
      "laws.you_are_proposing_new_legislation_select_the_fief_to_revoke": "Select the claim you propose to revoke.",
      "lord_request.fief_for_defection": "Claim for defection",
      "lords.fiefs": "Claims: ",
      "lords.fiefs_none": "Claims: None",
      "policy.description.trade_law": "How commanders fund their fleets: squeezing their claims, or running trade ventures. Higher levels raise claim revenue and cut trade income. Market tariffs and your colony income are unaffected.",
      "policy.level.trade_law.0": "Free Trade|-50% claim revenue |+150% trade income",
      "policy.level.trade_law.1": "Light Levies|-25% claim revenue |+75% trade income",
      "policy.level.trade_law.3": "Heavy Levies|+50% claim revenue |-25% trade income",
      "policy.level.trade_law.4": "Extortionate|+100% claim revenue |-50% trade income",
      "politics.no_fiefs_to_tax": " No claims to tax",
      "politics.recipient_has_few_fiefs": " Recipient has few claims",
      "politics.recipient_has_many_fiefs": " Recipient has many claims",
      "politics.recipient_has_no_fiefs": " Recipient has no claims",
      "politics.recipient_has_too_many_fiefs": " Recipient has too many claims",
      "progression.need_holding_or_battles": "hold a claim, or win {battles} battles alongside faction fleets (so far {current})",
      "request.excellent_which_shall_be_my_new_demesne": "Agreed. Which claim will be mine?",
      "request.holding.offer": "{player}, my name is {commander}. I've given {faction} years of service, and all I have to show for it is a dead-end posting. Grant me a claim of my own and my fleet will fly under your colours."
    }
  }
},
"factions": {
  "HMI": "hmi"
}
```

### `data/lords/presentation/greetings.json`: add under `factions`

```json
"HMI": [
  "The captain looks up from a manifest. \"{title} {lastName}. Company business or pleasure? Either way, the meter isn't running for you.\"",
  "\"{title} {lastName}, {sirOrMadam}.\" The captain kills the hold music. \"The Board says you're good for it. What does the Company need?\"",
  "The bridge crew stops pretending to work. \"{title} {lastName}. Executive priority, {sirOrMadam}. Name the claim and we'll jump it.\""
]
```

### `data/lords/presentation/raves.json`: add under `factions`

HMI has no bar art of its own, so it borrows vanilla's pirate bar, which suits a company of freebooters.

```json
"HMI": {
  "image": "pirate_bar",
  "arrival": "The Company doesn't do venues. It requisitions an ore hold on Fuyutsuki, hoses the slag off the deck and calls it a party. Mining drones hang from the ceiling with their cutting lasers dialled down to strobe. A quarterly bonus is being paid out in cash from a cargo crate by the door, and the queue for it is dancing. In the middle of the floor a Director in a pressed suit is doing a frankly alarming impression of a mass driver, and not one person in this hold will ever mention it to the shareholders.",
  "crowd": [
    "A shift crew still in their hardsuits stomps out a rhythm on the deck plates. The DJ considers it for a moment, then builds the next track around it.",
    "Someone has parked a mining tug in the middle of the dance floor and painted the Company logo on it. People are dancing on the roof."
  ]
}
```

### `data/lords/lords.json`: add one sample commander

An original character (no HMI lore character is duplicated). Her holding is Fuyutsuki, which no lore administrator runs. Her fleet uses HMI's techmined line, and her portrait is one of the mercenary portraits from HMI's own faction list.

```json
"Tamsin Okoro-Vance": {
  "faction": "HMI",
  "fleetName": "Okoro-Vance's Claim Jumpers",
  "isMale": false,
  "personality": "Calculating",
  "battle_personality": "Steady",
  "preferredItem": "luxury_goods",
  "level": 9,
  "ranking": 1,
  "fief": "fuyutsuki",
  "portrait": "graphics/portraits/portrait_mercenary05.png",
  "flagship": "scarecrow_std",
  "shipPref": {
    "marlin_std": 10,
    "brasher_attack": 10,
    "category_std": 10,
    "ascendate_std": 10,
    "sulemain_std": 15,
    "scarecrow_std": 15,
    "weaver_std": 10,
    "hmi_herakles_brawl": 5
  },
  "alignments": {
    "CORPORATE": 0.5,
    "TECHNOCRATIC": 0.5,
    "MILITARIST": 0.0,
    "HIERARCHICAL": -0.5,
    "DIPLOMATIC": -0.5,
    "IDEOLOGICAL": -0.5
  },
  "lore": "Tamsin Okoro-Vance started on Fuyutsuki's ore docks as a claims assessor, the person who decided which wrecks and asteroids the Company would 'discover' first. She was very good at it. Twelve cycles later she sits on the Board as Director of Outer Claims, commands a fleet of techmined cruisers bought cheap and refitted expensively, and has never once filed a claim she couldn't defend with guns. Rival directors call her the Auditor. Nobody who has been audited calls her anything at all."
}
```

The market ID is `fuyutsuki`: markets defined in a mod's `econ/*.json` take the ID of their first entity, as vanilla's Sindria is `sindria`. All variant IDs exist in HMI 0.4.0e.

### Tooling changes made for it

- `tools/sync_ranks.py` reads the optional `factionFiles` map from `titles.json`, so the partial file is written as `hmi.faction`; the generated-rank-files test uses the same mapping.
- The test guarding `lords.json` now allows new commanders while keeping the original roster unchanged.
- Nothing in Java: everything above is data.

### What a player would then see

- **Without HMI:** no change, no errors, one log line saying Tamsin Okoro-Vance was skipped.
- **With HMI, existing save:** after a restart, HMI's Politics screen talks about claims, ranks read Foreman / Director / Board Director, its fleets greet you in Company style, and its raves happen in an ore hold on Fuyutsuki.
- **With HMI, new campaign:** all of the above, plus Director Okoro-Vance and her Claim Jumpers in the Sector.
