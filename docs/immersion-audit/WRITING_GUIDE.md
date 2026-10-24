# Writing guide

How new text for this mod should read: scenes, dialogue, arrival texts, crowd lines and notifications. Julie set this direction on 2026-10-20 after the first rave drafts came out too sombre.

## One mood

Loud, joyful, a bit mad. The Sector is grim, and everybody knows it, which is exactly why a rave is the one night they get to forget it. Don't import the grimness into the party: no trembling hands, no confessions, no quiet tragedy at the edge of the dance floor. The grim universe stays outside the door, or shows up only as what people are leaving behind.

## Raves are for the elite

The people at a rave are the faction's elite: commanders, admirals, ministers, House heads, executives, kingpins, high clergy, and the guests they choose. Think of the high-society party scenes in *Andor*: opulent, exclusive, hedonistic, with politics happening on the dance floor. No dockhands, no ordinary crews; the crowd is made of very important people letting go.

## Gonzo: tell what happens

Write like a participant reporting from the middle of it, not like a narrator profiling a character.

- **Events, not descriptions.** Don't say what someone *is* ("they're a menace on the dance floor"). Say what they *did*, in order, and let the reader draw the conclusion ("by the third song they're up on a cargo crate, leading a hundred strangers through a routine none of them knew five minutes ago").
- **You are there.** Second person, the player in the scene, seeing it unfold: "you lose them in the crowd, then find them again…".
- **Escalate.** Each sentence goes one step further than the last. Let a paragraph pile up, using "and… and… and" when the moment calls for it.
- **Contrast is the joke.** The best beat is the buttoned-up officer who turns into a beast on the dance floor, so good you'd never have guessed. Set up the starch, then pull it out from under the reader.
- **Land on a deadpan snap-back.** End with the person returning to form as if nothing happened: they straighten their uniform, pick up their drink exactly where they left it, deny the whole thing. "Adequate," they say.
- **Concrete spacer detail.** Chits, glowsticks, cargo crates, chronometers, transponders, marines from somebody else's fleet, a bell behind the bar. Specific things, not adjectives.
- **Present tense**, as vanilla's bar and encounter descriptions use, with short past-tense asides where natural.

## Show the lore, don't state it

Scene text (the rave screen above all) is what the player sees, hears and is handed, in the order it happens. Lore goes into those details, never into a narrator's summary:

- Not: "Officially this is a morale exercise, and officially it never took place."
- But: "A marine at the hangar door takes your personal recorder, seals it in a bag and writes your name on the bag."

If a sentence explains what something means, turn it into something the player notices that implies it.

## Humour

- It comes from situations and contrast, and from faction character: Andrada's ego, Tri-Tachyon's excess, the Hegemony's secrecy, the Church sliding into a sanctified frenzy as its licensing loosens, the Path smashing speakers as penance.
- No puns, no memes the characters couldn't know, no winking at the player, no slapstick for its own sake.
- Commanders are competent and a little larger than life. A decade of space combat makes anyone a professional raver.

## Practical rules

- One paragraph, about three to six sentences.
- Intoxicants are implied, never named, except among pirates.
- Use the dialogue tokens (`%LORD_NAME`, `%LORD_HOST_NAME`, `%PLAYER_NAME` and so on; see PRESENTATION.md). Scene text in `raves.json` takes no tokens.
- Raves are "raves" in every faction. Faction-specific words for holdings, councils, offices and policies come from the profiles.
- No profile wording uses the word "raid".

## Before and after

Too descriptive, too sombre:

> %LORD_NAME dances stiffly at first, the way soldiers do when there's no drill to follow. Afterwards, leaning on the rail, they tell you it's the first time in months their hands haven't been shaking.

Gonzo, one mood:

> %LORD_NAME walks onto the floor like it's a parade ground: back straight, jaw set, not a flicker of expression. Then the beat kicks in, and there is no warning at all. One second you're standing next to a decorated officer, the next you're standing next to something built for this, all footwork and fury, hitting every drop like a broadside. A pilot tries to keep up and gives up halfway through the track. The crowd starts chanting a name, and it takes you a full minute to realise it's theirs. Afterwards %LORD_NAME is barely winded. "Adequate," they say.
