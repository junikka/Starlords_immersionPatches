# Making raves less dry

**Status: approved by Julie (2026-10-20); step 1 implemented.** Decisions: the picture is an establishing shot in the text; the Church keeps vanilla's Luddic teahouse; the Luddic Path gets its own scene, "even more ridiculous than the Church and a bit violent"; the order below stands. Step 1 (arrival scenes, establishing shot, crowd lines, intel picture) lives in `data/lords/presentation/raves.json`; the texts there are the current ones. Step 2 is done too: a **Join the rave** menu at the top level of every rave conversation, holding the dance floor and the rave's courtship options, which used to hide under *Ask another question* where Julie never found them.

**Tone.** A rave is a real party, loud and a bit mad, not a picnic. The Sector around it is grim, and the parties feel like it: people dance hard *because* of what's waiting outside. Humour comes from faction character (Andrada's ego, Tri-Tachyon's excess, the Church's slide into a sanctified frenzy), not from slapstick. Intoxicants are implied, never named, except among pirates. Moments between people should feel human and relatable, and on the dance floor these commanders are good at it. The crowd is people genuinely having fun, forgetting the war for a night.

## What a rave has today

- A rave greeting per personality, plus a host greeting.
- **Spend time together**: 36 scene variants, but every one leads *away* from the rave (a quiet table, the hangar, the observation deck, the gardens, a card game). Nothing happens on the dance floor.
- Gifts, admiration, courtship, marriage and the wedding, the tournament, recruitment.
- The rave track while you talk.
- **Missing:** an arrival scene, any picture of the place, crowd life, and anything that involves the whole room.

## The picture

Vanilla's bar illustrations, chosen per faction the same way vanilla's `rules.csv` (`bar_desc*`) does:

| Faction | Illustration | What it shows |
|---|---|---|
| Tri-Tachyon | `tritachyon_bar` | Neon lounge with a glowing ring bar |
| Pirates | `pirate_bar` | Packed, dark, red light |
| Hegemony | `hegemony_bar` | Uniformed officers celebrating, glasses up |
| Sindrian Diktat | `bar_diktat` | Bar under Andrada's portrait and propaganda screens |
| Luddic Church and Path | `teahouse` | Vanilla's Luddic bar (see question 3) |
| Persean League, independents, anything else | `space_bar` | Spacer bar with neon and smoke |

The left panel shows a picture *or* a portrait, so the recommended way is an **establishing shot in the text**: on the first conversation at a rave, the image goes into the text panel (`TextPanelAPI.addImage`) above the arrival scene, and the commander's portrait stays on the left. An optional **Look around** option could swap the left panel to the picture full-size. Your own art can replace any of these later via a merged `illustrations` entry, like the music. The faction → picture table goes in a data file, so other mods can add venues.

## 1. Arrival scene (S)

One paragraph the first time you talk to someone at a given rave, under the picture. The text is chosen by the host's faction and, where a faction has variants, by its current **Rave Licensing** level. The Church needs that. Any faction may also have only one text.

**Luddic Church** gets more ironic the further the player loosens Rave Licensing:

- *Banned* (only reachable through `startrave`, or a host defying the Synod): "There should be no rave. There is one anyway, in a stripped-out chapel bay with the icons turned to face the wall. Between tracks nobody speaks above a whisper. Everyone here knows the names of the Knights who would come if they heard."
- *Strict*: "The Synod permits one gathering a year, and permits it the way a surgeon permits pain. A curate reads the Litany of Restraint over the opening bars; censer smoke hangs where the lights would be. The music was approved only after it was declared a hymn. Every dancer keeps one eye on the honour guard at the door, and the guard keeps both eyes on them."
- *Licensed*: "The speakers are blessed before the first track, though the blessing has grown shorter every year. Bass is permitted now, provided it is devout bass. The faithful sway in hooded rows while a deacon writes down the names of those who sway too freely."
- *Permissive*: "The censers are full of something that is not incense. A bishop in full vestments is preaching to the speaker stacks, and the congregation answers on the beat. Somewhere in the crowd a Knight has taken off his helm, and he is not putting it back on."
- *Anything Goes*: "The Synod has declared the rave a sacrament, and the faithful have taken the sacrament very seriously. The icons wear glowsticks. The choir has gone feral. An archbishop rides a reliquary over the crowd while every preacher in the hall swears Ludd always meant it this way. Nobody can remember who said so first, and nobody is sober enough to look it up."

**Sindrian Diktat** (Andrada's ego, at full volume): "Sindria throws the best raves in the Sector, and every channel in the system will tell you so. The bass rig was pulled from a Domain-era capital ship and certified the deepest in human space by a commission the Supreme Executor chaired himself. Credits rain from the balconies. Andrada's face fills every screen, smiling down on the floor, and the playlist is, by decree, the greatest ever assembled. Rumour says he picks the opening track personally. Rumour says he picks it every time."

**Tri-Tachyon**: "Tri-Tachyon doesn't rent a venue; it buys one for the night and writes it off. Something gold runs in a channel cut into the bar, drone servers carry trays of things nobody can name, and the light show was designed by a system the company swears is not an AI. Executives dance like they're closing deals and close deals like they're dancing, and by morning none of them will know which was which. Somewhere upstairs, someone is paying for all of it, and someone else is paying for that."

**Hegemony**: "Officially this is a morale exercise, and officially it never took place. The hangar is sealed, transponders logged, personal recorders surrendered at the door. Behind the blast doors the Hegemony's finest dance like the Domain never fell. Tomorrow every one of them will deny it under oath, and the ones who don't will be quietly reassigned."

**Pirates** (the only place it's said outright): "You hear it two decks out. Inside it's hot, dark and heaving; the bass shakes rust out of the ceiling, someone is selling something from a bag that rattles, and half the crowd has pupils like reactor cores. Nobody here expects to live long. Tonight that's the point."

**Luddic Path** (one scene at every level): "The Path does not hold raves. The Path holds a Cleansing of the Instruments, which by pure coincidence requires every speaker in the system to be dragged into one hold and played at full volume while the faithful beat it to death with wrenches. The congregation dances on the wreckage in time with whatever still works. A Brother-Captain has set a synthesizer on fire and is preaching from on top of it. By the third hour nobody can tell the sermon from the bass, two Pathers are headbutting a subwoofer in what they insist is penance, and the Elders have stopped trying to tell the faithful that this is not what Ludd meant."

**Persean League**: "Every House brought its own sound system and its own grudge. The two compete across the hall until the songs collide into something nobody planned, and the dancing turns into a war of attrition fought in fine clothes."

**Default**: "The cargo hold has been stripped to the bulkheads and packed to them again. Strobes, sweat, a bass line that rattles the deck plates, and a crowd that left its ranks at the door along with most of its sense. The air tastes of ozone and something sweeter. Whatever the war is doing out there, in here it isn't invited."

## 2. Hit the dance floor (M), done

In the **Join the rave** menu. +3 relation, once per commander per rave. A decade of space combat turns anyone into a professional raver, so they dance great, each in their own style (rewritten on Julie's note; the earlier drafts were too sombre):

- *Default*: "%LORD_NAME takes the floor like they own it, and within one track they do. Years of high-g manoeuvres have given them balance a gymnast would envy and reflexes that hit every beat before it lands. The crowd opens up around the two of you. When the bass finally drops out, people are cheering, and some of them are cheering for you."
- *Martial*: "%LORD_NAME dances the way they fight: no wasted motion, perfect timing, absolutely no mercy. They read the floor like a battlespace, slip through the crowd on a flanking line, and land every drop like a broadside. By the end there's a ring of people around you both, trying to copy the moves."
- *Upstanding*: "%LORD_NAME turns out to be a menace on the dance floor, all big grins and bigger moves. They pull strangers in, spin you through a gap you didn't know was there, and somehow keep the whole circle in step. "Twenty years of shore leave," they shout over the bass. "You pick things up!""
- *Calculating*: "%LORD_NAME barely seems to move at first: cool, precise, every step exactly on the beat. Then the DJ shifts tempo and they go from understated to unstoppable without missing a count. Afterwards they straighten their cuffs. "Rhythm is just timing," they say. "And timing is my whole job.""
- *Quarrelsome*: "%LORD_NAME treats the dance floor as contested space, and wins it. Every challenger gets stared down, out-stepped and sent back to the bar. When two pilots try a dance-off, %LORD_NAME takes them both at once, then drags you in for the finale like it was the plan all along."

## 3. Crowd lines (S)

One line of background life in rave conversations, from a shared pool plus optional faction pools. People actually having fun and forgetting the war for a while. Warm rather than silly; the war is only there in what they're letting go of:

- "A dockhand and a commodore are trading dance moves by the speakers, and for once nobody is checking anyone's rank."
- "A crew in from the frontier cheers every time the bass drops, like it's the first time they've heard music this loud. It might be."
- "Someone started a chant three songs ago. Half the room still doesn't know the words, and it hasn't slowed anyone down."
- "A pilot hoists her wingmate onto her shoulders so he can see the lights. He spends the whole song pointing at them."
- "Two crews who nearly came to blows over a docking slot last week are sharing a table, arguing happily about whose ship is the worst."
- "The DJ drops an old Domain-era anthem, and a whole corner of the hall sings it back word for word, grinning at each other."
- "A rating from an engine crew, barely old enough to be here, dances like nobody ever told them how. Nobody does."
- "Someone has projected a sky from a world most of the room has never seen across the ceiling. People keep looking up and smiling."

## 4. Raise a toast (M)

Once per rave. A spacer's toast, closer to a ritual than a joke. Small relation gains, using the existing relation addons:

- *To absent friends* (relation with the commander you're talking to): "You raise your glass and say the names of the crews who didn't come back. The table goes quiet. Then, one by one, the others add theirs. By the end half the room is on its feet, the music has dropped away to nothing, and when it comes back it's louder than before."
- *To the host* (relation with the host): "'To %LORD_HOST_NAME, who keeps the lights on.' It isn't much of a speech. The room drinks to it anyway, because out here keeping the lights on is most of what anyone can promise."
- *To the faction* (flavour, with loyalty-coloured replies per personality).

## 5. Request a track (S–M)

You ask the DJ for something. Pure flavour now. With several files in `sounds.json` it can move the playlist on (`RaveMusic.next()`).

## 6. Buy a round for the room (L)

Costs credits that scale with the guest count, and gives +1 or +2 relation with every commander at the rave. The first action that touches the whole room. In the spirit of a Ceres dock bar, where a round is a public statement and everyone remembers who paid:

"You put your chit on the bar and tell the keeper the next round is yours. It goes quiet for a heartbeat while the room works out who you are, then the noise rolls back through the crowd from the bar outward. For the rest of the night, people you've never met nod to you like you came off their ship."

## 7. Gossip (L)

"What's everyone talking about?" One true rumour from live data: who dislikes whom, how a commander leans on the current proposal, who is short of money. Most new code.

## 8. The rave in intel (S)

The *Ongoing rave* intel shows the venue picture beside the host's portrait.

## Suggested order

1. Arrival scenes (with the Church's licensing variants) + establishing shot + crowd lines + intel picture.
2. Hit the dance floor.
3. Raise a toast, Buy a round.
4. Request a track, Gossip.

All text goes in data files, so every line here can be rewritten without touching code. Raves stay "raves" in every faction.

## Questions for you

1. Establishing shot in the text (recommended), a "Look around" swap, or both?
2. The Church's picture: keep vanilla's Luddic teahouse (it's the only Luddic interior), or use the generic bar? The text no longer mentions tea either way.
3. Luddic Path: its own grim text (it starts at Banned, so its raves are always illicit), or share the Church's Banned and Strict variants?
4. Is the order right?
