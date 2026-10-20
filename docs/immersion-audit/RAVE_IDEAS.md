# Making raves less dry

**Status: approved by Julie (2026-10-20); step 1 implemented.** Decisions: the picture is an establishing shot in the text; the Church keeps vanilla's Luddic teahouse; the Luddic Path gets its own scene, "even more ridiculous than the Church and a bit violent"; the order below stands. Step 1 (arrival scenes, establishing shot, crowd lines, intel picture) lives in `data/lords/presentation/raves.json`; the texts there are the current ones. Step 2 is done too: a **Join the rave** menu at the top level of every rave conversation, holding the dance floor and the rave's courtship options, which used to hide under *Ask another question* where Julie never found them.

**Tone.** One mood, everywhere: a real party, loud, joyful and a bit mad, in a grim Sector that this one night gets to forget. Write it gonzo-style: what you see happen, in order, as it escalates; not descriptions of who people are. See the [writing guide](WRITING_GUIDE.md). Intoxicants are implied, never named, except among pirates.

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

In the **Join the rave** menu. +3 relation, once per commander per rave. The picture Julie had in mind: a serious, buttoned-up officer who turns into a beast on the dance floor, so good you'd never have guessed. Told as events you witness, ending on the snap back to formality:

- *Default*: "One moment %LORD_NAME is standing at the edge of the floor with their hands clasped behind their back, looking like they're about to inspect your fleet. Then the bass drops and something in them comes loose. You lose them in the crowd, then find them again at the centre of a ring of screaming strangers, spinning on one heel, conducting the room with a glowstick somebody handed them. When the track ends they straighten their uniform, nod to you once, and ask if you'd like a drink, as if none of it happened."
- *Martial*: "%LORD_NAME walks onto the floor like it's a parade ground: back straight, jaw set, not a flicker of expression. Then the beat kicks in, and there is no warning at all. One second you're standing next to a decorated officer, the next you're standing next to something built for this, all footwork and fury, hitting every drop like a broadside. A pilot tries to keep up and gives up halfway through the track. The crowd starts chanting a name, and it takes you a full minute to realise it's theirs. Afterwards %LORD_NAME is barely winded. "Adequate," they say."
- *Upstanding*: "%LORD_NAME spends the first song politely apologising to everyone they bump into. Somewhere in the second song the apologising stops. By the third they're up on a cargo crate, leading a hundred strangers through a routine none of them knew five minutes ago, and the strangers are loving it, and so are you, and somewhere a commanding officer is going to hear about this. When you finally talk them down, they're grinning like a cadet on first leave. "Academy shore leave," they say. "Don't ask.""
- *Calculating*: "%LORD_NAME watches the floor for a full track without moving, the way they'd watch a battle readout. You assume they've decided against it. Then the DJ shifts tempo and they're gone, sliding through the crowd with terrifying precision, every move landing exactly where the beat is about to be. People stop dancing to watch. Mid-spin, you catch them glancing at their chronometer. When it's over they step back to their spot at the bar, adjust their cuffs and pick up their drink exactly where they left it. "Timing," they say, and nothing else."
- *Quarrelsome*: "%LORD_NAME announces, loudly and to anyone in earshot, that they do not dance. Ten minutes later you find them in the middle of the floor, sleeves rolled up, in a full-blown dance-off with three marines from somebody else's fleet, and winning. Every time a challenger falls back the crowd roars, and %LORD_NAME points at the next one. When the music finally breaks they find you, still breathing hard, and deny the whole thing. "I don't dance," they say again. Somebody behind you was recording."

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

Once per rave, small relation gains through the existing addons. Same mood as the dance floor:

- *To absent friends* (relation with the commander you're talking to): "You raise your glass and shout a toast to absent friends. It turns out to be either a mistake or the best idea you've had all year, because the whole room picks it up. Somebody starts naming ships. Somebody else names theirs louder. Within a minute three hundred spacers are bellowing the names of every hull they ever lost and loved, glasses in the air, until the DJ gives up, kills the track, and drops the chant back in as a sample under the next beat."
- *To the host* (relation with the host): "'To %LORD_HOST_NAME!' you yell, and it comes out a lot louder than you meant. The host hears it from across the hall, climbs onto the bar and toasts you back. Then everyone toasts everyone. Glasses are emptied, refilled and emptied again, and by the end nobody can remember who started it, except the host, who will remember it for a very long time."
- *To the faction*: flavour, with a reply per personality.

## 5. Request a track (S–M)

You ask the DJ for something. Pure flavour now. With several files in `sounds.json` it can move the playlist on (`RaveMusic.next()`).

## 6. Buy a round for the room (L)

Costs credits that scale with the guest count, +1 or +2 relation with every commander at the rave:

"You slap your chit on the bar and tell the keeper the next round is yours. The keeper looks at you, looks at the crowd, and rings a bell you hadn't noticed. The noise that follows is something you feel in your ribs. Drinks start moving hand over hand toward the back of the hall, and strangers start moving hand over hand toward you. By the time it's over you've been hugged by a quartermaster, hoisted by three marines and proposed to at least once. Your chit is noticeably lighter. Nobody here will forget your name tonight."

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
