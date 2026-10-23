# Numbloo 🟢 Math snacks with Pip

A playful Android math app for kids aged 4–10. Pip, a hungry, goofy blob, explains each idea in a few silly cards, then the child practises through mini-games.

* **Fully offline.** The app asks for **no permissions** at all: no internet, no camera, no microphone.
* **Read-aloud** uses the phone's built-in text-to-speech, which works without internet.
* **Sound effects** are generated in code, so the app ships no audio files.
* **Progress stays on the phone.**

## What's inside (v0.5)

| Area | Details |
|---|---|
| Players | Several profiles. Each has its own name, age, Pip colour, stars, stickers and per-topic records. |
| Levels | Beginner, Intermediate and Pro for each operation. A level opens when every game and the quiz in the level before has 3 stars. Grown-ups can switch on "Unlock all levels". Intermediate: numbers to 50 and all times tables. Pro: two-digit numbers, carrying, borrowing, up to 200. |
| Topics | Addition, Subtraction, Multiplication, Division |
| Each operation, each level | 3 short lessons (36 in total), each followed by 3 guided tries, then the games and a quiz |
| Games (+) | Feed Pip, Balloon Pop, Fruit Basket, Star Catcher, Domino Dots, Snail Race, Mystery Box, Sum Snap, Silly Sums |
| Games (−) | Frog Hop, Sneaky Squirrel, Mole Whack, Mail Sorter, Bubble Pop, Tower Compare, Magic Hat, Difference Detective, Silly Take-aways |
| Games (×) | Cookie Plates, Kangaroo Jumps, Tile Painter, Go Fishing, Flower Garden, Ladybug Spots, Times Machine, Times Twins, Silly Times |
| Games (÷) | Pizza Party, Egg Packers, Cookie Share, Lane Racer, Monster Munch, Cupcake Trays, Divide-o-Matic, Share Pairs, Silly Shares |
| Higher levels | Use the games that work with big numbers: Balloon Pop, number lines, Mystery Box, Magic Hat, Kangaroo, the machines, Snap and Silly Pip |
| Every topic | Snack Quiz |
| Action games | Drag and drop (Fruit Basket, Mail Sorter, Cookie Share), finger painting (Tile Painter), catch and steer arcades (Star Catcher, Lane Racer), whack and hook (Mole Whack, Go Fishing). These also appear in Intermediate and Pro. Levels already opened stay open when new games are added. |
| Confidence features | Small numbers first. Difficulty adjusts automatically (Easy / Medium / Tricky). Wrong answers get a funny "hic!" instead of a penalty. A visual hint appears after one miss, and the right answer wiggles after two. Every game gives at least 1 star. |
| Rewards | Stars, plus a sticker book with 24 stickers per level (Beginner, Intermediate, Pro); stickers come from the level being played. |
| Pip's Closet | 137 items on 10 shelves: colours, flags, hair, jobs, eyes, mouths, hats (incl. ghutra, shemagh, hijab), glasses, neckwear and pets. Items unlock with **collected stars** (the best stars in each game, so all 300 stars count once): something new every 3–15 stars, right up to the last star. **20 trophies** win special prizes: each operation at each level (a pet), each whole level (medal + hat or colour), each operation at all levels (a special colour) and every star in the game (Rainbow Pip + super cape). Items already earned in older versions are kept. |
| Grown-ups area | Protected by a parent gate. Turn voice and sounds on or off, see accuracy per topic, change age, reset or delete a player. |

## Get the APK from GitHub (no Android Studio needed)

1. Create a new repository on GitHub. It can be private.
2. Upload **everything in this folder**, including the hidden `.github` folder:
   ```bash
   git init && git add . && git commit -m "Numbloo v0.1"
   git branch -M main
   git remote add origin https://github.com/<you>/numbloo.git
   git push -u origin main
   ```
   If you upload through the GitHub website instead, check that `.github/workflows/build-apk.yml` arrived. Folders that start with a dot are often hidden.
3. Open the **Actions** tab and wait for **Build APK** to finish (about 5–8 minutes).
4. Open the finished run and download **Numbloo-apk** under *Artifacts*. It's a zip that contains `Numbloo.apk`.
5. Copy `Numbloo.apk` to the phone, open it, and allow "install unknown apps" when Android asks.

Every build is signed with the same test key (`app/numbloo-debug.keystore`). New builds therefore install over old ones and keep the kids' progress. Before publishing to the Play Store, create your own private release key.

## Building locally (optional)
Open the folder in Android Studio (Ladybug or newer) and press Run. You can also run `./gradlew assembleDebug`.

Tech: Kotlin 2.0, Jetpack Compose, minSdk 24 (Android 7.0+), targetSdk 35.

## Project map
```
app/src/main/java/com/numbloo/mathfun/
  MainActivity.kt, AppState.kt       app start + shared state
  model/Models.kt                    topics, games, profiles, hats, stickers, Pip's jokes
  logic/QuestionGen.kt               age/difficulty-aware question maker
  data/ProfileStore.kt               on-device saving
  audio/Speaker.kt, SoundFx.kt       offline voice + synthesised sounds
  ui/components/                     Pip (drawn in code), buttons, confetti, visuals
  ui/screens/                        players, levels, topic map, lessons, results, stickers, closet, grown-ups
  ui/games/                          the mini-games (37 game types); PlayKit.kt has the drag-and-drop helpers
```
