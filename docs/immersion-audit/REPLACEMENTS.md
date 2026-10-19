# Replacement decisions

Fill the “Decision” column with your chosen wording, or “keep”. These decisions are intentionally blank. Context-specific exceptions are useful; a single global replacement is not assumed.

## Shared concepts

| Concept | Current wording | Decision | Exceptions / intended meaning |
| --- | --- | --- | --- |
| Global roster category | Lords | | Mixed factions; plural category, not a personal rank |
| Generic individual | lord / starlord / Star Lord | | Individual fleet leader when faction-specific wording is unavailable |
| Generic plural | lords / starlords | | Explicit plural, not a mechanically appended “s” |
| Default rank 0 | Lord | | Lowest political/economic tier |
| Default rank 1 | High Lord | | Middle tier |
| Default rank 2 | First Lord | | Highest tier |
| Direct address | lord / lady / my lord; Sir / Ma'am | | Decide separately from rank and pronouns |
| Campaign leadership office | Marshal | | Appointed office, independent of rank |
| Faction territory/polity | realm | | Government/faction, not necessarily a geographical region |
| Faction head | liege / ruler | | May be a person, institution, or absent |
| Members under player authority | subjects / loyal lords | | Employment, allegiance, or command relationship |
| Controlled market | fief / fiefdom / demesne | | Brings revenue and may follow a defector |
| Assign a market | award / grant / gift a fief | | Distinguish ownership from administrative or revenue rights |
| Remove a market assignment | revoke / strip a fief | | Same rights model as the assignment |
| Transfer a market assignment | transfer a fief | | May be a personal decision rather than a vote |
| Political menu | Laws of the Realm / Realm Laws | | Faction law/policy menu |
| Political assembly | council | | Consider corporate, military, religious, and pirate variants |
| Executive power law | Dictatorial Power / Ruler Political Weight | | Keep gameplay effects visible |
| Senior member power law | Oligarchical Power / Lord Base Income | | Keep rank, vote, and income distinctions |
| Social gathering | feast / feasting | | Existing event includes extended socializing, romance, and competition |
| Social gathering regulation | Feast Regulations | | Cooldown and ban remain mechanically accurate |
| Gathering venue | estate / hall | | Can vary by faction/location |
| Gather for war | raise the realm's navy / rally to banners | | Fleet mobilization |
| Recruitment argument | rightful rule / claim to unite the sector | | Rewrite complete arguments for each social personality |
| Loyalty | swear allegiance / serve one's liege | | May fit selected authoritarian or religious factions |
| Competitive event | tournament | | Piloting competition; not intrinsically medieval |
| Victory gesture | dedicate tournament victory | | Decide whether to retain the romance ritual |
| Relationship framing | courtship / suitor / admiration | | Romance can remain with contemporary dialogue |
| Marriage framing | join our houses / household / ceremony | | Separate personal marriage from dynastic alliance |
| Prison dismissal | oubliette | | Source: prisoner dialogue option |
| Tone cleanup | (error), DLC, generator Easter eggs | | Rewrite fallback text while preserving diagnostic logging |
| Mod branding | Star Lords / Mount and Blade in Space | | Optional; branding need not match an in-world title |

## Faction profiles

Use the **exact faction ID**. Add rows for every modded faction you want to support. Current code supports the three rank columns; the other profile fields would need presentation support.

| Faction ID | Rank 0 | Rank 1 | Rank 2 | Collective singular / plural | Campaign office | Address / voice notes |
| --- | --- | --- | --- | --- | --- | --- |
| default fallback | | | | | | |
| player | | | | | | |
| hegemony | | | | | | |
| persean | | | | | | |
| tritachyon | | | | | | |
| sindrian_diktat | | | | | | |
| luddic_church | | | | | | |
| luddic_path | | | | | | |
| pirates | | | | | | |
| independent | | | | | | |

## Individual or passage exceptions

Use inventory IDs to identify exact occurrences. A single original value can appear in multiple personality branches with different meanings. For Java entries, rewrite the complete expression using the context in string_inventory.json.

| Inventory ID / character | Replacement / decision | Reason / conditions |
| --- | --- | --- |
| | | |

Preserve internal IDs, JSON keys, dialogue line IDs, `%...` placeholders, `%c0`-style arguments, `$string` count/order, and `|`-separated tooltip formatting until an intentional code change supports a new contract. If visible highlighted words change, update their highlight values too. Do not change internal `Lord` classes, `fief` keys, `feast` event IDs, or command names as part of a prose pass.
