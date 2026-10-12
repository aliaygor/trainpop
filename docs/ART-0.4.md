# Train Pop 0.4 train art

Generated with the built-in imagegen tool, then split into six transparent sprites for Android.

## Generation prompt

Use case: stylized-concept. Asset type: production transparent sprite atlas for a premium portrait mobile color-sorting train puzzle. Create SIX matching toy electric train locomotives in a STRICT 2-column by 3-row grid, one isolated vehicle per equal cell, generous empty transparent margins. All vehicles point vertically UP with the nose at the top. Exact orthographic overhead camera, no perspective convergence, consistent proportions and scale, soft 3D clay/plastic bevels, bright matte materials, gentle highlight from upper left, subtle tight contact shadows, polished casual mobile game art. The silhouette should be a chunky rounded rectangle roughly twice as tall as wide, broad colored roof, small dark navy front windshield at the top, two little cream headlights, short light gray front bumper, dark compact side wheels, a few simple roof panels. Clearly read as cute modern trains, not buses: tapered locomotive nose and tiny coupler, no bus windows along sides. Colors in reading order: cyan blue upper left, sunny yellow upper right, coral pink middle left, lavender purple middle right, bright lime green lower left, tangerine orange lower right. Maintain the SAME model, same camera, same roof panel layout, SAME size in every cell. Dominant flat bright body color must be visible for sorting. No chrome, no gold trim, no metallic textures, no realistic machinery, no busy detail, no chimney, no smoke. No track, no arrows, no lettering, no logo, no UI, no watermark. Genuine transparent background.

## Cleanup prompt

Preserve all six trains, their colors, orientation, sizes and grid. Remove all broad colored halos and bloom. Keep true alpha-zero space around compact vehicle silhouettes and clean antialiased edges.

Generated source: `exec-5959a8a0-4197-411e-a865-86f2f8e516bb.png`.

Project assets: `app/src/main/res/drawable-nodpi/train_blue.webp`, `train_yellow.webp`, `train_pink.webp`, `train_purple.webp`, `train_green.webp`, `train_orange.webp`.

## Matching carriage prompt

Six matching short passenger carriages in a 2 column by 3 row transparent atlas. Exact top-down camera, same softly rounded matte plastic 3D style, cyan/yellow/pink/purple/green/orange reading order, small raised roof panel, dark wheels and gray coupling tabs on both ends. No windshield, lights, engine, chimney, track, lettering, arrow, halo or broad shadow.

Source: `exec-61a6fecb-4e2e-471b-af50-a4c8892abaa1.png`. Project assets: matching `wagon_blue.webp`, `wagon_yellow.webp`, `wagon_pink.webp`, `wagon_purple.webp`, `wagon_green.webp`, `wagon_orange.webp` in `app/src/main/res/drawable-nodpi`.

Final project directory: C:/Users/alica/AndroidStudioProjects/trainpop/app/src/main/res/drawable-nodpi/
The twelve named train_*.webp and wagon_*.webp assets are saved in that directory. Generation and cleanup used the built-in imagegen tool; cell extraction retained alpha and cropped each vehicle's bounds.

