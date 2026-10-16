# Train Pop

AXIO's portrait Android train/robot sorting puzzle. Kotlin, Android Canvas, original shaded artwork and a generated station background.

## Build and run

Open this repository in Android Studio. Android SDK 36 and JDK 17+ are required.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

## Version 0.13

Speed choices launch a rewarded ad directly. Daily spin gifts persist separately from shuffle boosters and daily ad limits; legacy day-two claims receive one compensation spin. 39 tests pass. See [validation](docs/VALIDATION-0.13.md). Merchant account setup is deferred by the user; live purchases remain blocked.

## Previous version 0.12

Soft sage/cream gameplay scene, matching robot/train sprites and a boarding bridge. Difficulty grows by one train every three levels; new colors arrive at levels 9, 23 and 41. Rewarded 2×/3× crowd flow and price-first purchase buttons are included. 37 tests pass, including 200 solvable levels. See [validation](docs/VALIDATION-0.12.md).

## Previous version 0.10

- Brand package/namespace: `com.axioapps.trainpop`.
- Automatic empty-platform dispatch and atomic four-wide waiting-row admission.
- Rendered original railway platform modules, raised depot floor, dimensional conveyor edges, cached static scene and sampled path geometry.
- Occasional preloaded interstitials after level 10, with a two-minute cooldown and no-ads suppression; live ads use UMP consent gating.
- Play-supplied one-time offer tokens/prices and pending-payment feedback. Train Pop draft created in Axio Apps; merchant payments profile still blocks product creation. Approved prices in [catalog](docs/PRODUCT-CATALOG.json).
- 35 passing tests, including 200 solvable levels. See [validation](docs/VALIDATION-0.10.md).

## Previous version 0.9

- Connected queue inlets curve into the crowd loop; a legible boarding station replaces the rail controller.
- Early levels increase from 8 trains / 3 colors / 1 blocked exit to 24 trains / 6 colors / 13 blocked exits at level 9. Candidate selection preserves a known unassisted solution.
- Functional soft-currency booster shop with atomic balance/inventory updates, plus a separate one-time free welcome gift.
- Paid Google Play offers explain setup status and link to the usable coin shop; they do not simulate receipts or prices.
- Existing sessions retain their original demand across generator changes. See [validation notes](docs/VALIDATION-0.9.md).

## Previous version 0.8

- Single axio / LET’S PLAY intro; the clipped duplicate Android launch logo is removed.
- Six premium steam locomotive/carriage texture pairs and clear roof arrows; coupled trains keep their carriage on arrival and departure.
- Dense four-lane circulation starts with up to 176 real passengers. Incoming queues end above the compact loop; all passengers use one ground-depth draw order, with eight fixed-camera directions.
- A station controller replaces numbered parking slots with selectable Coast, Valley, City and Night routes; dispatched trains reserve the chosen dock.
- No white backing rims under buttons. Six rendered menu icons, explicit package contents and unavailable-purchase states.
- Articulated conductor nods and waves independent arms; tapping it shows a welcome greeting.
- Illustrated overhead garden, base twenty-second laps, reserved entry gaps and smooth closing of gaps after the feed ends.
- Legacy saves migrate to the larger passenger demand while retaining level progress and economy.
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

`station.webp`, `conductor.webp` and `locomotive.webp` in `app/src/main/res/drawable-nodpi` are original assets generated with the built-in imagegen tool. Prompts are preserved in `docs/ART.md`. Gameplay trains use individually rendered palette sprites; long trains couple a separate matching carriage without stretching the engine image. New prompts are in `docs/ART-0.4.md`. Crowd robots have 48 original rendered sprites, and the conductor has six articulated parts. The overhead garden and robot prompts are documented in `docs/ART-0.7.md`; new trains/menu prompts are in `docs/ART-0.8.md`. UI primitives are drawn in `Art.kt`; no reference game's art is copied. WAV sounds are locally synthesized and original.

## Remaining polish

Gameplay objects currently use shaded 2D drawings and 3D-looking raster sprites, not real 3D models. The conveyor holds up to 176 robots, with two four-wide incoming queues; unmatched robots keep circling. Only matching robots crossing the gate board. Production 3D assets, more complex level mechanics, device performance profiling, accessible native UI semantics and Play release setup remain ahead. Purchases and live rewarded ads are not ready for public release.

