# Making raves less dry

**Status: approved by Julie (2026-10-20); step 1 implemented.** Decisions: the picture is an establishing shot in the text; the Church keeps vanilla's Luddic teahouse; the Luddic Path gets its own scene, "even more ridiculous than the Church and a bit violent"; the order below stands. Step 1 (arrival scenes, establishing shot, crowd lines, intel picture) lives in `data/lords/presentation/raves.json`; the texts there are the current ones. Step 2 is done too: a **Join the rave** menu at the top level of every rave conversation, holding the dance floor and the rave's courtship options, which used to hide under *Ask another question* where Julie never found them.

**Tone.** A rave is an elite gathering, not a public party: the guests are the faction's commanders, admirals, ministers, House heads, executives, kingpins and their favoured guests, in the spirit of the high-society party scenes in *Andor*: opulent, exclusive, hedonistic, with deals made on the dance floor. One mood: loud, glamorous and a bit mad. Write it gonzo-style, as what you see unfold, with the faction's lore smuggled into the details. See the [writing guide](WRITING_GUIDE.md). Intoxicants are implied, never named, except among pirates.

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

## 1. Arrival scene (S), done

One paragraph the first time you talk to someone at a given rave, under the picture. Chosen by the host's faction and, for the Church, by its current Rave Licensing level (Banned, Strict, Licensed, Permissive, Anything Goes, getting wilder as licensing loosens). The Luddic Path has its own scene.

**Perspective (Julie, 2026-10-24):** the scene is what *you* see as it unfolds, gonzo-style, with the faction's lore smuggled into the details you notice, never stated by a narrator. The Hegemony's secrecy shows as a marine bagging your recorder and commodores shooting down a camera drone, not as "officially this never happened". The current texts live in `data/lords/presentation/raves.json`.

## 2. Hit the dance floor (M), done

In the **Join the rave** menu. +3 relation, once per commander per rave. A serious, buttoned-up officer turns into a beast on the dance floor in front of the faction's elite, told as events you witness and ending on the snap back to formality. Current texts: `dance_floor` in `data/lords/dialog/dialog.json` (default and each personality).

## 3. Crowd lines (S), done

One line of background life in every rave conversation, from the shared pool plus the host faction's own: the elite letting their hair down (a governor finishing a song barefoot on the bar, rivals laughing too hard at the same joke). Current texts: `crowd` in `data/lords/presentation/raves.json`.

## 4. Raise a toast (M)

Once per rave, small relation gains through the existing addons. Drafts in the elite register:

- *To absent friends* (relation with the commander you're talking to): "You raise your glass and call a toast to absent friends. It carries further than you meant. An admiral picks it up and names a ship. A minister names a world. Within a minute the whole hall is on its feet, glasses raised, calling out the names of every hull and colony the faction ever lost, until the DJ gives up, kills the track and drops the chant back in as a sample under the next beat."
- *To the host* (relation with the host): "'To %LORD_HOST_NAME!' you call, louder than you meant. The host hears it across the hall, climbs onto the banquet table and toasts you back. Then every admiral toasts every minister, and every minister toasts somebody they're plotting against. Glasses are emptied and refilled and emptied again, and by the end nobody can remember who started it, except the host, who will remember it for a very long time."
- *To the faction*: flavour, with a reply per personality.

## 5. Request a track (S–M)

You ask the DJ for something. Pure flavour now. With several files in `sounds.json` it can move the playlist on (`RaveMusic.next()`).

## 6. Buy a round for the room (L)

Costs a lot of credits, scaling with the guest count, and gives +1 or +2 relation with every commander at the rave. At an elite party, a round is a statement everyone remembers:

"You catch the steward's eye and tell him the next case is on you, the good one. He looks at you, looks at the room, and rings a little silver bell you hadn't noticed. The noise that follows starts at the bar and rolls across the hall. Bottles older than the Collapse start moving from hand to hand, and so do the guests, toward you. By the time it's over you've been embraced by a minister, toasted by three admirals and proposed to by an heir to one of the great Houses at least once. Your credit balance is noticeably lighter. Nobody in this room will forget your name tonight."

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
