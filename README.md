# Train Pop

AXIO's Android train and robot color puzzle. Kotlin, Android Canvas, portrait layout.

## Run
Open this folder in Android Studio (JDK 17+, Android SDK 36). Run `gradlew.bat :app:assembleDebug :app:testDebugUnitTest`.

## First playable slice
- AXIO intro and illustrated loading transition
- Animated station menu with six feature entries
- Rotating robot queue, four platforms, directional train blockers and capacity-based boarding
- Level completion, local coins/progress, daily reward and supply, demo wheel
- Pause, restart confirmation, hints and coin-funded rescue
- Original procedural vector artwork; no reference game assets included

## Current limits
This is a 2D gameplay prototype, not the final 3D art build. Loading is a brief intro transition because the procedural scene needs no large assets. Levels share a solvable layout and cycle through 3–5 colors; a handcrafted level collection and special mechanics are next. Wheel rewards are instant demo results. Music, haptics, rewarded ads, billing, seven-day reward progression, additional platforms and final animation/sound work are not implemented. Shop screens explicitly disclose their demo status.

## Next milestones
1. Confirm exact moving-queue boarding rules from a gameplay recording.
2. Add mixed train directions, variable capacities, level data and automated solution validation.
3. Replace procedural prototype art with polished original train/robot assets and boarding/departure animations.
4. Integrate tested reward persistence, billing, ads and store-ready release configuration.
