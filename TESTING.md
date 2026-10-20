# Quick in-game test (wording pass)

Expect faction ranks (Hegemony **Commodore / Admiral / Fleet Admiral**, Tri-Tachyon **Vice President** ladder, Church **Deacon-Captain / Bishop / Archbishop**, pirates **Captain / Warlord / Kingpin**), **Raves** and **Tournaments** everywhere, and each faction's own words for holdings, the council, the Fleet Commander, expeditions and policies (see "Faction wording" below). Your own faction uses the house words by default: **Holdings**, **Council**, **Fleet Commander**, **Expeditions**, **Central Authority**, **Privilege of Rank**, **Revenue Policy** and **Rave Licensing**.

1. Close Starsector. Rebuild (`python tools/build.py build`) so the JAR and ZIP are current, then replace the installed Starlords folder with this folder or extract `build/Starlords_immersionPatches.zip` into `Starsector/mods`. Keep one installed copy. Enable Star Lords, Nexerelin and Console Commands, then restart. A new campaign gives the cleanest test.
2. Advance a few days. Run `PrintLordsDetails` and `PrintLordsAlignments`. Expect populated tables in `starsector-core/starsector.log` with no presentation exceptions.
3. Run `WarpLord Victor`, then `AddLordRelation Victor 50`. Hail Victor Wallis and inspect his intel: he should be **Admiral** in both places, with **Commands** (the Hegemony's word for holdings) and **Rank** labels. Read his biography (no "Operation commander" or garbled letters). There should be no `(error)`, `_FIRST`, unresolved `{tokens}` or raw `%LORD_...` markers.
4. Open Politics with a commission or your own faction. Hover the policy rows: level tooltips start with names such as *Free Trade* or *Anything Goes*. With a fresh commission, proposals are locked until you earn a seat (see below), so run `promoteplayer 2` first. Submit a proposal and check its council text.
5. At a rave, try every "spend time together" scene, a gift, the tournament and a recruitment attempt (the justification lines should vary by personality, with the verdict highlighted). Save and reload; everything should persist.
6. Ask someone about the state of the Sector. If the game stalls, `starsector.log` should contain a `Slow dialogue option` line naming the slow step.

Console command IDs remain unchanged: use `WarpLord`, not `WarpCommander`. If Victor has no fleet, advance time or choose another exact first name/ID from the log. For a quick request-text check, `AddLordRequest <character ID> fief_for_defection` creates a market-for-defection request. Use the ID from `PrintLordsDetails`; this command does not accept a first name. Only create a `prison_break` request for someone already imprisoned by another commander.

## Council progression and starting policies

1. Start a new campaign. Open Politics for a few factions you can view (or take a commission with each): the Church should start at *Strict* Rave Licensing, the Path at *Banned*, Tri-Tachyon at *Free Trade*, pirates at *Very Low* Boss's Word (their name for Central Authority). Independent and your own faction stay in the middle.
2. Take a commission with the Hegemony. Politics should show you as **Commissioned officer**, with every proposal button greyed out and a tooltip saying you need a seat. `promoteplayer status` lists what's missing (60 days of service and Welcoming).
3. Run `promoteplayer`. You should become a Commodore, with your Hegemony reputation raised to at least Welcoming: commands, Levy Policy and Rave Licensing unlock, while war, peace, Chain of Command, Seniority and Fleet Commander stay locked with "You need the rank of Admiral".
4. Run `AddLordRelation <name> 50` on a Hegemony commander, then `promoteplayer vote`. A message should say who put you forward for Admiral, and the proposal should appear in the council list. Wait for the council to take it up (`promoteplayer status` reports if it's still waiting). If the vote fails, relations with the commanders who opposed it shouldn't drop.
5. Hail a Hegemony commander you get on with and open *Ask a question*. "Ask them to put your name forward for promotion" should be there. Asking either starts a vote or explains what's missing. Try it with a commander at relation 0 or below for the refusal line.
6. Run `promoteplayer 2`. Nominating yourself as Fleet Commander should now be possible.
7. Hail an ordinary Hegemony patrol with your transponder on. The captain should greet you as **Fleet Admiral** with a Hegemony line instead of the vanilla greeting, and the usual fleet options should follow. Try `promoteplayer 0` and a Diktat or pirate commission too: each faction and rank has its own line.
8. Lower your reputation below Cooperative (for example with the console's `adjustrelation` or `setrelation` command). Politics should say the council treats you as a lower rank, self-nomination should lock again, and fleets should greet you at that lower rank.

## Faction wording (Phase 3)

Each faction names its own institutions. Raves and tournaments are the same everywhere.

| Faction | Holding | Council | Fleet Commander | Expedition | Policies (Central Authority / Privilege of Rank / Revenue Policy) |
|---|---|---|---|---|---|
| Hegemony, independents | Command | Admiralty Board | Fleet Commander | Expedition | Chain of Command / Seniority / Levy Policy |
| Sindrian Diktat | Prefecture | Executive Council | Marshal of the Fleet | Expedition | as Hegemony |
| Tri-Tachyon | Concession | Board | Director of Operations | Operation | Executive Control / Executive Privilege / Margin Policy |
| Persean League | Seat | League Council (High Council for other aristocratic factions) | Strategos | Expedition | Crown Prerogative / Privilege of Rank / Tribute Policy |
| Luddic Church | Parish | Synod | Knight-Commander | Crusade | Doctrine of Obedience / Clerical Privilege / Tithes |
| Luddic Path | Cell | Conclave | Knight-Commander | Purge | as Church |
| Pirates | Turf | Captains' Table | Warmaster | Armada | Boss's Word / Captains' Share / Cut of the Take |

1. Open the Politics screen with a Tri-Tachyon commission, then a Hegemony one (`promoteplayer 2` unlocks everything). Each screen should use its own faction's words: the policy names and tooltips, the Fleet Commander line, the holding buttons, and the "Call ..." button if you're the Fleet Commander. Switch back and forth: nothing from one faction should leak into the other.
2. Submit a proposal in each. The proposal title, the council message ("The Board is now voting...") and the vote reasons ("Recipient has few concessions") should match that faction.
3. Open a Hegemony commander's intel: the holdings line reads **Commands**. A Tri-Tachyon commander's reads **Concessions**.
4. When a faction starts an expedition, its event intel should read "Ongoing operation" for Tri-Tachyon, "Ongoing crusade" for the Church, and so on, naming the faction's office.
5. In LunaLib's settings, under **Other**, set **Your faction's wording** to *corporate*. With your own faction, its Politics screen should now say Concession, Board and Director of Operations. Rank titles stay Captain / Commodore / Admiral; edit `titles.json` to change those.
6. `starsector.log` should have no "Missing presentation" warnings and no `profiles` errors.

To read every message as each faction sees it without launching the game, run `python tools/preview_profiles.py` and open `docs/immersion-audit/generated/profile_preview.md`.

## Rave scenes and music

The scenes:

1. Run `startrave` near a Hegemony commander (or any faction), wait for the fleet to arrive, and hail any commander at the rave. Above the greeting you should see the venue picture (the Hegemony's officers' bar) and the arrival scene. Below it, one grey crowd line.
2. Hail another commander at the same rave: no picture or arrival scene this time, but a new crowd line.
3. Open the *Ongoing rave* intel: the venue picture sits under the location line.
4. Hail a commander at the rave: the main menu has **Join the rave.** (only at raves). It opens the rave menu: **Hit the dance floor together** (a scene per personality, small relation gain, once per commander per rave), **Profess your most ardent admiration** (once per rave), and, once a commander accepts your admiration, **Spend some time together at the rave** and the marriage proposal. **Back** returns to the main menu. *Ask another question* no longer lists admiration or the date.
5. The Church's scene follows its current Rave Licensing. It starts at *Strict* (the Litany of Restraint). Take a Church commission, use `promoteplayer 2`, and propose looser licensing; once the Synod passes it, `startrave` a Church commander and the scene should match the new level, up to the *Anything Goes* sacrament. Under *Banned*, only `startrave` can make a rave, and it gets the whispered illicit scene. The Path always gets its own scene.

The music:

1. Go to a rave: the intel screen lists it as **Ongoing rave**, with the host and location. To skip the wait, run `startrave` (or `startrave <first name>`): the nearest commander throws one at the nearest friendly world, ignoring Rave Licensing, the cooldown and the cost. Hail a commander who is at it: within a second the music should switch to the rave track.
2. Leave the conversation: the normal music should come back.
3. Enter the tournament from a rave conversation: the rave track keeps playing, the fight has normal combat music, and the rave track returns after each round. Leaving the tournament restores the normal music.
4. Turn off LunaLib's **Other → Rave music** and hail a raving commander again: the music shouldn't change.

## Optional: prove faction-specific ranks

In the **installed** `data/lords/presentation/titles.json`, temporarily set:

```json
"hegemony": ["Lieutenant", "Captain", "Admiral"],
"pirates": ["Captain", "Warlord", "Fleet leader"]
```

These are test labels, not final lore decisions. Restart Starsector after editing JSON; recompilation is unnecessary. Compare Hegemony and pirate characters. While commissioned by the Hegemony, a pirate's recruitment offers should use **Captain / Admiral**, even if that pirate's current title is **Warlord**. After an actual defection, the character's current title should follow the new faction. An unconfigured mod faction uses Captain / Commodore / Admiral unless its existing `faction.json` explicitly supplies legacy ranks.

For a visual check of first-name substitution, give the player a distinct first and last name and revisit dialogue that uses the first name. For literal-name handling, a test name containing `$` should display normally. Detailed automated tests also cover backslashes and token-shaped text inside names.

If something fails, record the screen/action, character, faction/commission and exception from `starsector-core/starsector.log`. Phase 1's campaign test passed; Phase 2's compilation and controlled API tests do not replace this in-game review. Full institutional/faction prose inheritance arrives in Phase 3.
