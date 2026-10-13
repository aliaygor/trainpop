# Train Pop 0.7 görsel kaynakları

Tüm görseller yerleşik imagegen ile üretildi. Robotlar sekiz yönden 3D render görünümlü 2D sprite olarak çizilir; gerçek zamanlı 3D mesh kullanılmaz. Saydamlık orijinal RGBA atlaslardan korunur. Hücre kırpma, boyutlandırma ve WebP dönüşümü teknik varlık hazırlama adımlarıdır.

Projeye kaydedilen varlıklar: C:/Users/alica/AndroidStudioProjects/trainpop/app/src/main/res/drawable-nodpi/

- crowd_{cyan,yellow,pink,purple,green,orange}_{front,frontright,right,backright,back,backleft,left,frontleft}_v7.webp — 48 yönlü karakter görseli.
- host_{head,body,arm_left,arm_right,leg_left,leg_right}_v7.webp — bağımsız kafa, omuz ve ayak hareketleri için 6 parça.
- garden_v7.webp — oyun alanının üstündeki istasyon bahçesi.

## Ana yönler

Kaynak: C:/Users/alica/.codex/generated_images/01a131bb-0229-71d3-ba83-0528f4c80fcc/exec-c0593b8a-5074-4d76-9adb-2770091b4826.png

Tam prompt:

```text
Use case: stylized-concept. Asset type: compact crowd game sprite atlas. Create one transparent RGBA landscape sheet 1536x1024, strict 6 columns and 4 rows, exactly 24 complete tiny robot figures with ample empty padding in each cell. Each column is ONE color: cyan, yellow, coral pink, purple, lime green, orange. Each row is ONE walking direction under the SAME fixed orthographic overhead camera tilted down 65 degrees: row1 facing bottom of image (front mostly hidden by top of head), row2 facing left, row3 facing top (back visible), row4 facing right. Same compact robot design all cells: small rounded cube head only 35 percent of figure height, chunky single-color capsule body, short stubby arms and feet. Silhouette like a simple toy human passenger with subtle mechanical joints. No antenna, no ears, no giant luminous eyes, no portrait heads. Color dominates entire head/body/limbs. Small dark visor on front only; no glowing details. Soft matte 3D plastic, high quality game render, upper left lighting, overhead view shows crown and shoulders, visually coherent at 20 pixels high. Feet remain at same ground baseline each cell. Actual transparent background, no ground, no halo, no labels, no text, no contact sheets borders. Exactly six color columns and four direction rows, uniformly scaled.
```

## Çapraz yönler

Kaynak: C:/Users/alica/.codex/generated_images/01a131bb-0229-71d3-ba83-0528f4c80fcc/exec-fcf3253c-6515-4467-a384-5a96cf14fd20.png

Tam prompt:

```text
Use case: stylized-concept. Reference image is the already approved compact robot crowd atlas; preserve that exact robot identity, colors, proportions, camera and lighting. Produce a NEW actual transparent RGBA landscape sprite atlas 1536x1024, six columns by four rows. Exactly 24 full body figures with padding. Columns colors in reading order cyan yellow coral pink purple lime orange. Rows now show FOUR DIAGONAL WALK DIRECTIONS under the same fixed orthographic overhead camera tilted down 65 degrees. Row1 facing BOTTOM LEFT (front-left three-quarter). Row2 facing TOP LEFT (rear-left three-quarter). Row3 facing TOP RIGHT (rear-right three-quarter). Row4 facing BOTTOM RIGHT (front-right three-quarter). Rotate the 3D robot about its vertical axis, never rotate the image/camera. Compact matte single-color body, small rounded cube head, short arms and feet, subtle navy mechanical joints, tiny visor only when front visible. No eyes, antenna, ears or extra details. Uniform scale and baseline, no halo, floor, text or borders. Purpose: intermediate turn directions to avoid double-image crossfades in a dense moving crowd.
```

## Karşılama robotu parçaları

Kaynak: C:/Users/alica/.codex/generated_images/01a131bb-0229-71d3-ba83-0528f4c80fcc/exec-363d6f5a-b6b6-46bc-9f60-71ca0132eaba.png

Tam prompt:

```text
Use case: precise-object-edit. Edit target: attached train conductor robot. Create a production animation rig sprite atlas on actual transparent RGBA background, 1024x1536, strict two-column three-row grid with six SEPARATE NONOVERLAPPING detached parts, not full robots. Preserve the character identity: golden yellow friendly robot, dark navy conductor cap with gold train badge, turquoise blue vest with navy tie, navy gloves, blue boots. Reading order: 1 head including cap and entire face WITHOUT torso or arms, front facing straight level and smiling. 2 torso vest with neck socket WITHOUT head, arms or legs. 3 robot's right arm (viewer left) complete yellow upper arm, blue cuff and open navy waving hand, detached straight downward, shoulder at top, palm forward. 4 robot's left arm (viewer right) same detached complete arm downward. 5 robot's right leg (viewer left) complete detached boot and yellow shin. 6 left leg (viewer right) complete detached boot and shin. Each part centered in its own 512x512 cell with padding. Head should be symmetric level for nod animation, body front symmetric. Same lighting, scale and 3D plastic materials as reference. Parts are drawn separately with rounded shoulder/neck joints, ready to overlap in a skeletal 2D animation. No full assembled character anywhere. No outlines, no text, no layout borders, no colored background, no extra props. Preserve transparency.
```

## Bahçe

Kaynak: C:/Users/alica/.codex/generated_images/01a131bb-0229-71d3-ba83-0528f4c80fcc/exec-2b809980-b196-4dd9-9858-8b0867c4c451.png

Tam prompt:

```text
Use case: stylized-concept. Asset type: overhead background art for the TOP third of a portrait casual train puzzle game. High quality soft 3D toy diorama, fixed orthographic camera looking downward at 65 degrees, consistent upper-left sunlight. A charming station garden in spring: creamy pale warm stone paving fills a very large EMPTY middle 85% of the scene; delicate pale pink cherry blossom trees tightly tucked against the extreme left and right edges, a few rounded green bushes and small stones in the bottom corners, a tiny stone lantern at far lower right, a shallow ornamental pond at far lower left. Keep center completely empty for a large U-shaped crowd pathway drawn by the game. No railway, road, crowd, robots, trains, buildings, labels, UI, text, logos or borders. Pastel restrained colors with dimensional shading and soft contact shadows, polished mobile game environment, not flat vector. Landscape 1536x1024. Opaque background.
```

