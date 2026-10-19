# Future extension: weaving Starlords into the Sector's lore

**Status: deferred future extension plan (Julie, 2026-10-19).** Not part of Phases 3–5 and nothing here is implemented. Revisit after Phase 5. The "Suggested Phase 3 order" below means the steps of this extension, not Phase 3 of the [accepted design](MODULAR_PRESENTATION_PROPOSAL.md).

## The problem

Mount & Blade has one political system: kings, lords and their fiefs. Starsector already has one too. Vanilla places faction leaders and other lore characters at specific markets, administrators run colonies, and base commanders hand out commissions. Starlords brings in the Warband version alongside it. As a result, generated commanders with no lore presence own worlds that vanilla says Andrada, Daud or Sun run. We have also tied our rank ladder to the vanilla commission. The goal is to make the two systems one story without despawning anyone, breaking quests or breaking other mods.

## What the code and the game do today

These facts were checked against this repository and against the game's own API source (`starfarer.api.zip`, `People.java`, `CoreLifecyclePluginImpl.java`, `rulecmd/missions/Commission.java`).

- **Lords are generated people.** `Lord(LordTemplate)` creates them with `createRandomPerson` and then applies the name and portrait from `data/lords/lords_*.json`. None of them is a vanilla person, so no lore character literally exists twice.
- **Lords can only be reached through their fleets.** The single rules.csv hook is `LordsOpenComms` on `$entity.lords_isLord`, which matches a fleet. Lords never appear in a comm directory. If you don't see the fleet, you can't talk to them.
- **A holding is an income claim, not government.** `FiefController` keeps an abstract tax and trade pool per market and pays the owner from it. The market's administrator, industries and comm directory are untouched.
- **Defection can move markets the raw way.** `DefectionUtils` calls `market.setFactionId(...)` on low-stability holdings. That skips Nexerelin's market-transfer handling (admins, comm directories, other mods' listeners). This is an existing risk, independent of this study.
- **Starlords already has a sovereign.** `faction.json` has a `leaderID` per faction (`daud`, `reynard_hannan`, `andrada`, `sun`, `cotton`). The vanilla leader's stance multiplies council votes, adds an "influence of ruler" term to each lord's approval, and is shown in the council intel. The sovereign never appears in space, never hosts anything and has no Starlords dialogue.
- **Vanilla commissions come from posts, not people.** `Commission.personCanGiveCommission()` accepts base commanders, station commanders, outpost commanders and administrators. `createInitialPeople` generates a base commander for every market with a military base or high command, and a station commander for every market with a station.
- **Raves are hosted by a lord at their nearest base** (`LordAI`: `new LordEvent(FEAST, lord, lord.getClosestBase())`). The host must be a `Lord`.

## Where the two casts collide

Vanilla people come from `People.java`. Holders are the current `fief` entries in `data/lords`.

| Market | Vanilla figure (post) | Starlords holder (rank) |
|---|---|---|
| Sindria | Philip Andrada (faction leader, admin) | Leonidas Nevsky (1) |
| Chicomoztoc | Baikal Daud (faction leader, admin) | Horatio Sartorius (2) |
| Eochu Bres | Artemisia Sun (faction leader, admin); Glamor-Rotanev; Arroyo | Minto Askia (1) |
| Kazeron | Reynard Hannan (League leader); Horus Yaribay and Fortuna Kato (house leaders) | Samson Kazimierz (2) |
| Ragnar Complex | Orcus Rao (space admiral, military administrator) | Luan Jee Bak (0) |
| Cruor | Laverna Tell (ground general, military administrator) | Simeon Kaliman (0) |
| Mazalot | Dardan Kato (administrator) | Blanche Ryu (1) |
| Olinadu | Menes Yaribay (administrator) | Sigurd Holk (0) |
| Gilead | Archcurate Sophronia Jaspis | Musa Bodhi (2) |
| Eventide | the Rao family | Titus Raeder (0) |
| Hesperus | Gideon Oak (Knights of Ludd) | Anastasia Davoud (0) |
| Volturn | Curate Moyra Standfast | Malamir Litvoy (2) |

There are also two role collisions without a market. Vanilla's Diktat has **Oxana Hyder (Deputy Star Marshal, Fleet Commander)** and **Horacio Caden (leader of the Lion's Guard)**. Neither is in a comm directory; they appear through quests. Starlords' council-appointed marshal fills Hyder's post. Nevsky's biography makes him head of the Lion's Guard, which is Caden's role.

The biographies themselves already lean the right way: Sartorius, Wallis, Nevsky, Litvoy, Melci and Kazimierz are written as people who serve under Daud, Andrada, Sun or Hannan. The writing assumes a court above the lords; the mechanics just don't show it.

## Three directions

### A. Replace the roster with lore characters: not recommended

Make Andrada, Daud, Rao, Tell, the Katos and the rest into the lords, and drop the generated ones.

- The lore cast is too small and too civilian. Most named vanilla people are agents, scientists, priests or aristocrats, and many have their comm entry hidden until a quest reveals them.
- They are quest-bound. Vanilla and mod quests find them by `People` id and expect them at their market, in their faction, alive. Starlords captures, imprisons, marries, kills and defects lords. Each of those would break a quest somewhere.
- Despawning the generated lords throws away the biographies and the roster that the rest of the mod is built on.

### B. Give the lore leaders fleets as full lords: only in a narrow form

Wrap the vanilla `PersonAPI` (not a copy) in a `Lord`, so Andrada commands a real fleet. Because it is the same person object, nobody is duplicated, and his comm entry at Sindria stays where it is.

As a free-roaming lord this is too risky: every capture, death, defection, marriage and rank-change path would need a guard, and Nexerelin and other mods could see a faction leader flying around the Sector. The safe part of this idea is a **ceremonial court fleet** (part of C, step 4). It stays in orbit of the capital for the length of a rave and can't be engaged, captured or killed.

### C. Court and Fleet: recommended

Two layers, each matching something vanilla already has.

**1. The court (the sovereign).** The vanilla leader stays exactly where vanilla put them. Starlords treats them as the crown, as it already does for votes, and adds:

- **Crown worlds.** A market can't be granted as a holding if its administrator is an important person (HIGH or VERY_HIGH) whom Starlords didn't create. This is detected automatically, so another mod's lore admins are protected without extra data. An optional `crownMarkets` list in `faction.json` handles exceptions. Sindria, Chicomoztoc, Eochu Bres, Kazeron, Ragnar Complex, Cruor, Mazalot and Olinadu then stop being property and become seats of power.
- **Audiences.** We add one option to the leader's existing comm conversation, never replacing their greeting or vanilla options: *Raise a matter before the council*. It opens a Starlords dialogue set written for the sovereign: petitions for promotion, requests for a holding, arguments for war or peace. Promotions above the first tier end with the sovereign's assent: "Andrada confirms you as Star Marshal."
- **The court rave.** The faction's grand rave is held at the capital with the sovereign as host. The lords fly in, so the capital becomes where the player finds the whole court once a season.

**2. The fleet lords (the current roster).** They stay as they are, but a holding becomes something vanilla already understands: the lord is that world's **military command**.

- **They live in comm directories.** A lord who holds a market takes its base commander post (or station commander, for stations) in that market's comm directory. A new rules.csv row on the person (not the fleet) opens the Starlords dialogue. If their fleet is elsewhere, the call goes through as a relay ("Their flag lieutenant patches you through"). This fixes "I can't find anyone" without moving any vanilla character.
- **They grant commissions.** Because vanilla gives commissions to base and station commanders, a lord in that post automatically becomes someone who can commission you. The commander who commissioned you becomes your natural sponsor for the first promotion vote. The vanilla commission and our council ladder then form one road: commission from a lord, seat on the council, rank confirmed by the sovereign.
- **Holdings are reworded, not reworked.** The income is the garrison levy and patrol tariffs; the administrator still runs the colony. Each faction can call a holding by its own term (for example a Hegemony *command*, a Diktat *prefecture*, a Tri-Tachyon *portfolio*, a League *house seat*, a Church *parish*, pirate *turf*) through the presentation files we already have.

With this framing, most collisions in the table stop being collisions. Nevsky commanding Sindria's garrison under Andrada is what his biography already says. The remaining real conflicts are markets whose vanilla administrator is already the military command (Ragnar Complex with Orcus Rao, Cruor with Laverna Tell). There the lord takes the station post or the holding moves elsewhere.

**3. Optional later: lore notables as patrons.** Orcus Rao, Laverna Tell, the Kato and Yaribay houses, Archcurate Jaspis and Gideon Oak could add influence to council votes without becoming lords. They would be a lightweight "patron" record that adds weight to a vote, not a `Lord`. The `Lord` class assumes a fleet throughout (AI, prisoners, relations).

## Compatibility rules

Hold these in every phase:

1. Never despawn, move, rename, change the faction of, or kill a vanilla or modded person. Starlords only adds memory flags and dialogue options.
2. Changes to rules.csv are additive, scored below story rows, and keyed on Starlords' own memory flags (for example `$person.starlords_sovereign`).
3. Sovereigns come from `leaderID`. A faction without one (the Church today, and most mod factions) runs as a council with no crown, as it does now.
4. When Nexerelin is present, market ownership changes go through its transfer path. That means fixing the raw `setFactionId` in `DefectionUtils`.
5. A court fleet, if we build one, is the same `PersonAPI` as the comm entry. It can't be engaged and is removed when the rave ends.

## Your three concerns

- **Players can't find key characters.** Today lords exist only as fleets. Under C they are listed in the comm directories of the worlds they command, and the sovereign is always at the capital. The court rave gathers everyone in one place.
- **Duplicates of lore characters.** There are no literal duplicates now, but there are role duplicates: a second ruler of Sindria, a second Fleet Commander next to Hyder, a second head of the Lion's Guard next to Caden. Crown worlds and the command framing remove the first kind. The other two need data and wording decisions (questions 1 and 2 below).
- **Two dialogue systems.** Vanilla rules.csv and Starlords' dialogue JSON stay separate. They meet at exactly two doors: the lord's comm entry opens Starlords, and the sovereign's comm conversation gains one Starlords option. One thing to verify early: that closing the Starlords dialogue cleanly returns to the vanilla option list.

## Suggested order for this extension

| Step | Content | Size |
|---|---|---|
| 3a | Crown-world rule, move holdings off true conflicts, holding wording, Nexerelin-safe transfers on defection | small |
| 3b | Lords in comm directories as base/station commanders, a rules.csv row on the person, relay calls, commission giver as sponsor | medium |
| 3c | Sovereign audiences and sovereign assent on promotions; raves hosted by a sovereign at the capital (needs a host that isn't a `Lord`) | medium |
| 3d | Optional: ceremonial court fleet, lore notables as patrons | larger |

## Questions for you

1. **Fleet Commander and the Lion's Guard.** For the Diktat, should the council-appointed marshal be renamed so it doesn't collide with Hyder? Should Nevsky's biography move him under Caden instead of heading the Guard?
2. **Base commander.** Should a lord replace the generated base commander at their holding (then lords hand out commissions), or sit beside them?
3. **Sovereigns for the Church and the pirates.** Vanilla has no faction-leader person for the Church; Jaspis is Archcurate of Gilead. Kanta is a faction leader in vanilla, but the pirates have no `leaderID`. Should we seat them, or keep both as crownless councils?
4. **Court fleet.** Is "the sovereign hosts the rave at the capital" enough, or do you want the visible ceremonial fleet, so you can literally see Andrada's flagship over Sindria?
