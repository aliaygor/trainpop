# Train Pop 0.2 validation

Checked on a Pixel 9 Android emulator with software GPU rendering.

- Debug APK builds successfully.
- 14 JVM tests pass: eight gameplay/session tests and six economy/claim tests.
- Tests solve 200 generated levels without boosters and check full-length blocking, arrival gating, true deadlock detection, rescue, passenger conservation and checkpoint restoration.
- Reward tests cover daily cycling, supply order, daily ad-spin limits, recovery of interrupted spins, no charges for unusable boosters, and idempotent wins/purchases.
- Android lint completes with zero errors. Remaining warnings are dependency update notices, portrait orientation on large screens and optional KTX style suggestions.
- Home and seven-day daily screens inspected; claiming daily reward increased coins from 280 to 330 and removed its badge.
- Animated wheel inspected and spun; the selected 500-coin reward persisted, and the pending reward marker was cleared.
- A matching train was selected in level 2; four robots boarded and progress was saved when backgrounded, then restored after process restart.
- Google's rewarded TEST ad loaded and completed; `Reward granted` was displayed and coins increased from 830 to 880. No real ads revenue or live purchase was tested.
- Google Play products are unconfigured, so shop prices cannot yet be obtained; purchase entitlement is gated on signed receipts and configured verification key.

This is a substantial playable iteration, not a Play Store release candidate. Final reference-parity boarding behavior, production 3D models, billing backend/refund reconciliation, consent/live ads and physical-device profiling remain release work.
