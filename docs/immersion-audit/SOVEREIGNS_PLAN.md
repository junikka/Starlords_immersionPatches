# Plan: sovereigns take the field

**Status: superseded by [LORE_CHARACTERS_RESEARCH.md](LORE_CHARACTERS_RESEARCH.md) (decisions recorded there) and folded into the [Heroes vision](HEROES_VISION.md).** Nothing is implemented yet. This revisits option B of the deferred [lore study](LORE_INTEGRATION_STUDY.md), which rejected leader fleets as too risky. The risks turn out to be a short, known list that can be guarded one by one.

## Goal

As in Mount & Blade: a faction's ruler is the first commander of the realm. Philip Andrada takes the field with his own fleet, leads the Diktat to war, rests at Sindria between campaigns, and throws the Sector's loudest raves there. It is the **same person** as the Andrada in Sindria's comm directory, never a copy.

## Who

Data-driven, via a new `data/lords/sovereigns.json` keyed by the leader's person ID (`andrada`, `daud`, `sun`, `reynard_hannan`, optionally `kanta` and `cotton`). Each entry has the same fields as a commander, but no `faction` (taken from the person) and no generated identity: fleet name, flagship, ship preferences, personality, battle personality and fief (normally the capital). Modded factions add theirs the same way. A missing person is skipped, like a missing faction.

## How, and the guards that make it safe

1. **The same person.** A new path creates the commander from the existing `PersonAPI` (the mod already does this for the player). The person stays in their market's comm directory, and their vanilla conversations, quests and missions are untouched.
2. **Never captured, never killed.** When their fleet loses, they always escape: the existing respawn path (back to their capital, a new fleet after 30 days). They are excluded from capture in `BattleListener`, from death in Life and Death, and from ransom and prison lists.
3. **Never changes sides.** Excluded from monthly defection, from persuasion in dialogue, and from exile proposals.
4. **Rank and title untouched.** `syncVanillaRank` skips them, so Andrada stays `FACTION_LEADER` for vanilla. Their title in Starlords comes from a `title` field in `sovereigns.json` (for example *Supreme Executor*, *High Hegemon*, *CEO*), not the tier ladder.
5. **Not a council pawn.** They aren't a proposal target (no rank change, exile, holding revocation or Fleet Commander nomination against them), and they keep their existing role as the ruler whose stance weighs on every vote. They don't get a second, ordinary vote.
6. **No courtship.** Admiration, dates and proposals are hidden for sovereigns. (This could be a later feature, with very different stakes.)
7. **Holdings.** The capital becomes the sovereign's holding. Commanders who hold capitals today move to another world of their faction: Nevsky (Sindria), Sartorius (Chicomoztoc), Askia (Eochu Bres) and Kazimierz (Kazeron). Their biographies already say they serve the ruler.
8. **Behaviour.** The usual commander AI with ranking 2: campaigns (a natural pick for Fleet Commander), raves at the capital, defending home, collecting revenue. They rest at the capital between campaigns, since that's their holding.
9. **Dialogue.** Hailing the sovereign's fleet opens the Starlords conversation. Their vanilla conversation stays in the comm directory. A small set of sovereign lines (greeting, rave host) in the gonzo style replaces the generic commander lines where they would read wrong.

## Effort and risk

About a day of work, plus your in-game test. It needs a **new campaign** for the fleets to appear. Tests cover: the same person object and comm entry, no capture or death on defeat, no defection, exile or courtship, rank untouched, and a missing leader skipped.

One real unknown: vanilla code that inspects a person's fleet or location could react to a faction leader commanding a fleet. Nothing obvious does, and the guards keep the person's market, rank and faction untouched, but only playing will tell. If something breaks, `sovereigns.json` can simply be emptied.

## Decisions for you

1. Which rulers: Andrada, Daud, Sun, Hannan? Also Kanta (pirates) and Cotton (Path), or not?
2. Defeat: they always escape to the capital and return 30 days later. OK?
3. Capitals move to the sovereigns, and the four commanders who hold them now get other worlds. OK?
4. Titles in Starlords: *Supreme Executor* (Andrada), *High Hegemon* (Daud), *CEO* (Sun), *Lord Protector*? (Hannan; please pick).
5. Courtship of sovereigns: off for now?
