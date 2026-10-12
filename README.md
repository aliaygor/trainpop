# Train Pop

AXIO's portrait Android train/robot sorting puzzle. Kotlin, Android Canvas, original shaded artwork and a generated station background.

## Build and run

Open this repository in Android Studio. Android SDK 36 and JDK 17+ are required.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

## Version 0.4

- Single axio / LET’S PLAY intro; the clipped duplicate Android launch logo is removed.
- Matched six-color locomotive and carriage sprites with natural proportions and clear roof arrows.
- Twenty-second robot laps, arc-length movement through bends, and a smooth incoming merge transition.
- Stable passenger positions and cached robot sprites avoid reindexing jumps during boarding.
- Integrated booster cards apply actions immediately; empty inventory can be refilled through rewarded test ads.
- Reward-ad loading waits visibly and opens automatically, with a timeout and failure feedback.
- Illustrated station, icon menu buttons, conductor greetings, two-second wheel tease, actionable notification badges.
- Directional one/two-cell trains in a six-by-six depot; four reserved platforms and three unlockable ones.
- Continuous departure/arrival routes, continuous robot circulation with gate-based color boarding and capacity counters, platform deadlocks and recovery.
- Deterministic level generation with known solutions; tests solve 200 levels without spending or boosters.
- VIP bypass, queue arrangement and color shuffle with conserved passenger demand.
- Seven-day reward cycle, five-step supply, animated six-prize wheel with one free daily spin and nine optional ad spins.
- Persistent coins, inventory, claims, purchase receipts and resumable in-progress levels.
- Original sound effects, looping music, separate music/sound/haptic/motion settings, pause and navigation confirmations.
- Real Google rewarded TEST ads in debug; Play Billing product queries, prices, signed receipt handling, acknowledgment/consumption and restore.

## Commerce setup

Shop buttons are wired to Google Play Billing. They never simulate a payment or grant an item simply because the purchase button was tapped. The repository has no configured live products, signing key or live ads account yet.

Create these one-time products in the Play Console for `com.aliaygor.trainpop`:

| Product ID | Contents | Type |
|---|---|---|
| `starter_pack` | 1,000 coins and 2 of each booster | Non-consumable |
| `remove_ads` | Remove forced ads | Non-consumable |
| `ad_free_bundle` | Remove forced ads, 1,500 coins, 1 of each booster | Non-consumable |
| `coins_500` | 500 coins | Consumable |
| `coins_3000` | 3,000 coins | Consumable |

Configure `TRAINPOP_BILLING_PUBLIC_KEY` in the local Gradle properties outside version control, then test through a Play internal track with license testers. Prices are sourced from Play; no fictional prices or discounts are shown. Receipt signature and local idempotency are implemented; server verification, refund/revocation reconciliation and cross-device entitlements are remaining release work. Forced/interstitial ads are not introduced in this version.

Debug ads always use Google's official rewarded test unit. Rewards only apply in `onUserEarnedReward`, never merely on dismissal. The first supply reward is free; the later rewards use real test ads. Release rewarded loading remains disabled until live account IDs and a consent flow are configured. `TRAINPOP_ADMOB_APP_ID` and `TRAINPOP_REWARDED_AD_ID` are the build properties for the later live integration. Production monetization has not been verified or enabled.

## Art and audio

`station.webp`, `conductor.webp` and `locomotive.webp` in `app/src/main/res/drawable-nodpi` are original assets generated with the built-in imagegen tool. Prompts are preserved in `docs/ART.md`. Gameplay trains use individually rendered palette sprites; long trains couple a separate matching carriage without stretching the engine image. New prompts are in `docs/ART-0.4.md`. Robot and UI primitives are drawn in `Art.kt`; no reference game's art is copied. WAV sounds are locally synthesized and original.

## Remaining polish

Gameplay objects currently use shaded 2D drawings and 3D-looking raster sprites, not real 3D models. The conveyor holds up to 64 robots, with incoming overflow lanes; unmatched robots keep circling. Only matching robots crossing the gate board. Production 3D assets, more complex level mechanics, device performance profiling, accessible native UI semantics and Play release setup remain ahead. Purchases and live rewarded ads are not ready for public release.

