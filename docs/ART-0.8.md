# Train Pop 0.8 görsel kaynakları

Üretim yöntemi: yerleşik imagegen. Lokomotif/vagonlarda ikinci atlas seçildi; ilk çalışma yalnızca stil referansı olarak kullanıldı. PNG alfa kanalı korunarak teknik hücre kırpma, komşu sprite parçalarından ayrıştırma ve kayıpsız WebP dönüşümü uygulandı.

Projeye kaydedilen dosyalar: C:/Users/alica/AndroidStudioProjects/trainpop/app/src/main/res/drawable-nodpi/

- engine_{blue,yellow,pink,purple,green,orange}_v8.webp
- carriage_{blue,yellow,pink,purple,green,orange}_v8.webp
- menu_{starter,ads,shop,supply,wheel,daily}_v8.webp

Trenler oyun hücresinin tanımlı genişlik/yüksekliğine çizilir; uzun trenlerde lokomotif ve vagon ayrı parçadır. Bunlar 3D render görünümlü raster sprite’lardır, gerçek zamanlı 3D mesh değildir. Önceki robot ve bahçe varlıkları korunmuştur.

## Tren ilk stil çalışması

Kaynak: C:/Users/alica/.codex/generated_images/01a131bb-0229-71d3-ba83-0528f4c80fcc/exec-44b247c3-a982-4570-85f7-40b6a1e85d43.png

Tam prompt:

```text
Use case: stylized-concept. Asset type: production train and carriage game sprite atlas. One transparent RGBA sheet 1024x1536 with strict 2 columns by 6 rows, each cell 512x256, exactly 12 separate sprites. Each row is one color: cyan blue, golden yellow, coral pink, violet purple, lime green, orange. Left column: ONE premium chunky short locomotive, right column ONE matching passenger carriage. Nose points to top of image, orthographic overhead camera at 70 degrees, consistent camera/scale/lighting. Locomotive has substantial rounded metal body, wide cabin roof, dark windshield toward nose, sculpted cowcatcher/buffer, small brass hardware, side wheels, crisp seams and ventilation, soft highlights, believable toy 3D volume. Carriage matching color has rounded roof, two small roof vents, visible dark side windows, chunky wheels and couplers at both ends. Rich polished casual mobile game 3D art, not icons or flat drawings. Broad robust silhouette width about 65 percent of height, full length fits inside each cell, small transparent padding; no part touches neighbor cells. Consistent upper-left light, no cast shadow beyond sprite, no white outline/white matte/white base/halo, no UI text/arrows/numbers/logos. Truly transparent background. Purpose: visually substantial trains in a portrait train yard puzzle, engines and wagons connect naturally.
```

## Seçilen lokomotif ve vagon atlası

Kaynak: C:/Users/alica/.codex/generated_images/01a131bb-0229-71d3-ba83-0528f4c80fcc/exec-9a27a045-f96e-4765-8d56-4ab3f3151122.png

Tam prompt:

```text
Use case: precise-object-edit. Reference image: keep the colored metal/brass toy train material style, but fix the camera, layout and proportions. Produce a NEW truly transparent sprite atlas 1536x1024 arranged SIX COLUMNS by TWO ROWS, each cell 256x512. Columns cyan blue, yellow, pink, purple, green, orange. Row1 six short CHUNKY LOCOMOTIVES, row2 six matching passenger carriages. Every vehicle viewed from ALMOST DIRECTLY ABOVE, overhead camera 80 degrees. Vehicle long axis VERTICAL, nose pointing to TOP OF IMAGE, exactly upright. Show roof and chimney from above, only tiny strips of sidewalls/wheels, never a side view or portrait illustration. Broad compact silhouette: width 65 to 75 percent of length, like a premium short toy train, not a long thin train. Engine has round short nose, cabin roof, small top chimney, brass bumper, visible wheel nubs, teal-colored metal and matched six color variations. Carriage shorter than locomotive with chunky roof and tiny window slivers. Uniform scale, matching couplers. Full vehicle fits well inside each cell with ample padding. High quality soft 3D mobile-game render. No arrows, no letters, no white halo or matte, no cast shadows, no ground, no bases, no layout lines. All silhouettes isolated on transparent RGBA.
```

## Menü görselleri

Kaynak: C:/Users/alica/.codex/generated_images/01a131bb-0229-71d3-ba83-0528f4c80fcc/exec-fb80d6f8-4ce2-4c8b-ae01-93c7a28332e5.png

Tam prompt:

```text
Use case: stylized-concept. Asset type: six original premium 3D menu icons for a train puzzle mobile game. Transparent RGBA atlas 1024x1536, strict 2 columns by 3 rows. Exactly one centered isolated icon per cell with generous padding. Reading order: 1 open teal suitcase overflowing with shiny gold train-token coins and tiny gift ribbon (starter pack); 2 glossy red prohibition symbol around a small navy video screen (remove ads), no letters; 3 miniature teal train-station shop kiosk with striped gold/teal awning, coin bag and brass train emblem (store); 4 blue maintenance supply chest with gold tokens and a small wrench (daily supply); 5 polished eight-segment lucky wheel purple/teal/gold with brass hub and pointer (spin); 6 chunky navy and cream calendar card with a large green check mark and small gold gift (daily check-in). Cohesive polished casual mobile game toy 3D style, real volume, soft highlights and studio lighting, navy/teal/brass palette. No white circle frames, no white base plates, no cast shadow outside silhouette, no white matte, no text, no typography, no label strips, no borders. All isolated on genuinely transparent background, nothing touching neighboring cells.
```

