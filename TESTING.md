# Quick in-game test (wording pass)

Expect faction ranks (Hegemony **Commodore / Admiral / Fleet Admiral**, Tri-Tachyon **Vice President** ladder, Church **Deacon-Captain / Bishop / Archbishop**, pirates **Captain / Warlord / Kingpin**), **Holdings**, **Fleet Commander**, **Expeditions**, **Raves** and **Tournaments**. Policies read **Central Authority**, **Privilege of Rank**, **Revenue Policy** and **Rave Licensing**.

1. Close Starsector. Rebuild (`python tools/build.py build`) so the JAR and ZIP are current, then replace the installed Starlords folder with this folder or extract `build/Starlords_immersionPatches.zip` into `Starsector/mods`. Keep one installed copy. Enable Star Lords, Nexerelin and Console Commands, then restart. A new campaign gives the cleanest test.
2. Advance a few days. Run `PrintLordsDetails` and `PrintLordsAlignments`. Expect populated tables in `starsector-core/starsector.log` with no presentation exceptions.
3. Run `WarpLord Victor`, then `AddLordRelation Victor 50`. Hail Victor Wallis and inspect his intel: he should be **Admiral** in both places, with **Holdings** and **Rank** labels. Read his biography (no "Operation commander" or garbled letters). There should be no `(error)`, `_FIRST`, unresolved `{tokens}` or raw `%LORD_...` markers.
4. Open Politics with a commission or your own faction. Hover the policy rows: level tooltips start with names such as *Free Trade* or *Anything Goes*. With a fresh commission, proposals are locked until you earn a seat (see below), so run `promoteplayer 2` first. Submit a proposal and check its council text.
5. At a rave, try every "spend time together" scene, a gift, the tournament and a recruitment attempt (the justification lines should vary by personality, with the verdict highlighted). Save and reload; everything should persist.
6. Ask someone about the state of the Sector. If the game stalls, `starsector.log` should contain a `Slow dialogue option` line naming the slow step.

Console command IDs remain unchanged: use `WarpLord`, not `WarpCommander`. If Victor has no fleet, advance time or choose another exact first name/ID from the log. For a quick request-text check, `AddLordRequest <character ID> fief_for_defection` creates a market-for-defection request. Use the ID from `PrintLordsDetails`; this command does not accept a first name. Only create a `prison_break` request for someone already imprisoned by another commander.

## Council progression and starting policies

1. Start a new campaign. Open Politics for a few factions you can view (or take a commission with each): the Church should start at *Strict* Rave Licensing, the Path at *Banned*, Tri-Tachyon at *Free Trade*, pirates at *Very Low* Central Authority. Independent and your own faction stay in the middle.
2. Take a commission with the Hegemony. Politics should show you as **Commissioned officer**, with every proposal button greyed out and a tooltip saying you need a seat. `promoteplayer status` lists what's missing (60 days of service and Welcoming).
3. Run `promoteplayer`. You should become a Commodore, with your Hegemony reputation raised to at least Welcoming: holdings, Revenue Policy and Rave Licensing unlock, while war, peace, Central Authority, Privilege of Rank and Fleet Commander stay locked with "You need the rank of Admiral".
4. Run `AddLordRelation <name> 50` on a Hegemony commander, then `promoteplayer vote`. A message should say who put you forward for Admiral, and the proposal should appear in the council list. Wait for the council to take it up (`promoteplayer status` reports if it's still waiting). If the vote fails, relations with the commanders who opposed it shouldn't drop.
5. Hail a Hegemony commander you get on with and open *Ask a question*. "Ask them to put your name forward for promotion" should be there. Asking either starts a vote or explains what's missing. Try it with a commander at relation 0 or below for the refusal line.
6. Run `promoteplayer 2`. Nominating yourself as Fleet Commander should now be possible.
7. Hail an ordinary Hegemony patrol with your transponder on. The captain should greet you as **Fleet Admiral** with a Hegemony line instead of the vanilla greeting, and the usual fleet options should follow. Try `promoteplayer 0` and a Diktat or pirate commission too: each faction and rank has its own line.
8. Lower your reputation below Cooperative (for example with the console's `adjustrelation` or `setrelation` command). Politics should say the council treats you as a lower rank, self-nomination should lock again, and fleets should greet you at that lower rank.

## Optional: prove faction-specific ranks

In the **installed** `data/lords/presentation/titles.json`, temporarily set:

```json
"hegemony": ["Lieutenant", "Captain", "Admiral"],
"pirates": ["Captain", "Warlord", "Fleet leader"]
```

These are test labels, not final lore decisions. Restart Starsector after editing JSON; recompilation is unnecessary. Compare Hegemony and pirate characters. While commissioned by the Hegemony, a pirate's recruitment offers should use **Captain / Admiral**, even if that pirate's current title is **Warlord**. After an actual defection, the character's current title should follow the new faction. An unconfigured mod faction uses Captain / Commodore / Admiral unless its existing `faction.json` explicitly supplies legacy ranks.

For a visual check of first-name substitution, give the player a distinct first and last name and revisit dialogue that uses the first name. For literal-name handling, a test name containing `$` should display normally. Detailed automated tests also cover backslashes and token-shaped text inside names.

If something fails, record the screen/action, character, faction/commission and exception from `starsector-core/starsector.log`. Phase 1's campaign test passed; Phase 2's compilation and controlled API tests do not replace this in-game review. Full institutional/faction prose inheritance arrives in Phase 3.
