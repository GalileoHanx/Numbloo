# Numbloo 🟢 Math snacks with Pip

A playful Android math app for kids aged 4–10. Pip, a hungry, goofy blob, explains each idea in a few silly cards, then the child practises through mini-games.

* **Fully offline.** The app asks for **no permissions** at all: no internet, no camera, no microphone.
* **Read-aloud** uses the phone's built-in text-to-speech, which works without internet.
* **Sound effects** are generated in code, so the app ships no audio files.
* **Progress stays on the phone.**

## What's inside (v0.2)

| Area | Details |
|---|---|
| Players | Several profiles. Each has its own name, age, Pip colour, stars, stickers and per-topic records. |
| Levels | Beginner (playable). Intermediate and Pro are shown as "coming soon". |
| Topics | Addition, Subtraction, Multiplication, Division |
| Each topic | A 6-card lesson ("Learn with Pip"), then 3 guided tries, then 5 games and a quiz |
| Games (+) | Feed Pip, Balloon Pop, Domino Dots, Snail Race, Mystery Box |
| Games (−) | Frog Hop, Sneaky Squirrel, Bubble Pop, Tower Compare, Magic Hat |
| Games (×) | Cookie Plates, Kangaroo Jumps, Flower Garden, Ladybug Spots, Times Machine |
| Games (÷) | Pizza Party, Egg Packers, Monster Munch, Cupcake Trays, Divide-o-Matic |
| Every topic | Snack Quiz |
| Confidence features | Small numbers first. Difficulty adjusts automatically (Easy / Medium / Tricky). Wrong answers get a funny "hic!" instead of a penalty. A visual hint appears after one miss, and the right answer wiggles after two. Every game gives at least 1 star. |
| Rewards | Stars, plus a sticker book with 24 stickers per level (Beginner, Intermediate, Pro). Hats unlock with stars (party hat, crown, wizard hat, propeller cap). |
| Pip's Closet | Opens after the first 3-star game. More 3-star wins unlock colours (Mint, Ruby, Gold, Midnight), glasses (shades, specs, star glasses) and neckwear (bow tie, scarf). Hats can be picked here too. |
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
  ui/games/                          the 21 mini-games
```
