# Train Pop 0.10 — 14 Ekim 2026

## Oynanış ve tasarım

- Yeni Android paket kimliği: com.axioapps.trainpop. Görünen isim Train Pop, geliştirici Axio Apps. Kullanıcının kişisel kullanıcı adı paket/namespace'den kaldırıldı.
- Depodaki trene dokununca boş peron otomatik atanır; önceden peron seçmek gerekmez. Kapalı çıkış ve dolu istasyon gerçek engel olarak kalır.
- Bekleyen ilk dört robot aynı anda çembere girer; dört şeridin tümünde boşluk bulunana kadar sıranın tamamı bekler. Renk sırası atlanmaz.
- Yeni özgün render edilmiş peron varlığı, dört dikey raylı peron, ayrı doluluk levhaları, derinlikli yol kenarları ve yükseltilmiş depo zemini. Gerçek zamanlı 3D motor değil, 3D görünümünde raster varlıklar ve Canvas ışık/gölge çizimleri kullanılır.
- Statik sahne 800×1700 bitmap olarak bir kez çizilip saklanır. Yolun sinüs/kosinüs noktaları önceden hesaplanır. Oyun döngüsü şerit listelerini tekrar kullanır; yolcu başına IdentityHashMap hız tahsisi ve kare başına O(n²) canlı nesne taraması kaldırılır. Bölüm üretimi hedefe ulaşınca durur; hücre çakışmaları BooleanArray üzerinden kontrol edilir. Belirli bir FPS veya yüzde hız artışı iddia edilmez.
- Eski v4 kayıtlarının yolcu sayısı korunur, dört kişilik gruplara yeniden yerleştirilir; yeni v5 kayıtları konumlarını aynen korur.

## Reklam

10. bölümden sonra 13, 16, 19... bölüm bitişlerinde, son gösterimden en az 120 saniye geçmişse Devam geçişinde önceden yüklenmiş reklam gösterilir. Reklam hazır değilse oyun bekletilmez. Reklamsız satın alanlara bu reklam gösterilmez. Debug resmi Google test reklamları kullanır. Release reklamı gerçek AdMob kimlikleri ve UMP canRequestAds kontrolü ile açılır; gizlilik tercihi girişi Ayarlar'da gerektiğinde görünür.

## Ödeme ve Play Console

Kullanıcının yasal beyan onayıyla Axio Apps hesabında Train Pop taslağı oluşturuldu; paket com.axioapps.trainpop müsaitliği doğrulandı. İngilizce varsayılan dil, ücretsiz oyun.

Kontrol paneli: https://play.google.com/console/u/0/developers/5990238439296916848/app/4974282223281190344/app-dashboard

Tek seferlik ürünler ekranı Google Payments satıcı hesabının bulunmadığını bildiriyor ve ürün oluşturmayı engelliyor. Ödeme profili kurulumu mali hesap bilgileri ve sözleşmeler içerdiğinden kullanıcıya bırakıldı. Kullanıcı tarafından onaylanan fiyatlar docs/PRODUCT-CATALOG.json dosyasında; Play Console'da ürünler henüz oluşturulmadı.

Google Play'den gelen fiyat ve teklif token'ı aynı ürün için kullanılır; bekleyen ödeme açıklanır. İmzalı PURCHASED makbuzu olmadan altın veya reklamsız hakkı verilmez. Mevcut ürün verme/tüketme/onaylama/geri yükleme ve tekrar makbuzu koruması sürer. Gerçek para tahsilatı test edilmedi. Yayın öncesi imzalama anahtarı, dahili test sürümü, lisans test hesapları, AdMob canlı kimlikleri, sunucu/iade uzlaştırması ve mağaza içeriklerinin tamamlanması gerekir.

Yeni paket kimliği nedeniyle APK önceki com.aliaygor.trainpop kurulumunun üzerine güncellenmez; ayrı kurulur. Önceki uygulama ve kayıtları silinmedi. AAB şu aşamada imzasız ve canlı yapılandırma içermeyen hazırlık çıktısıdır; doğrudan Play'e yayınlanacak final paket değildir.

## Doğrulama

35 test başarılı: Game 20, Economy 9, ConveyorGeometry 4, BreakAdPolicy 2. İlk 200 bölüm güçlendirmesiz çözüldü. Dörtlü girişin atomik olması, sıra koruması, otomatik peron rezervasyonu, kayıt göçü, reklam eşik/aralık/reklamsız kuralları test edildi. assembleDebug, lintDebug ve bundleRelease başarılı.

Yeni debug paketinde geçici 13. bölüm tamamlanma kaydıyla reklam geçişi sınandı. SDK onAdShowedFullScreenContent callback'i break.lastLevel=13 ve break.lastShown değerlerini yazdı; sonraki bölüm açıldı. Bu test gerçek para/reklam geliri üretmez. Geçici QA kaydı başlangıç yedeğine geri alınır. Eski kişisel paket kurulumuna müdahale edilmedi.

Görsel kaynak ve tam istem docs/ART-0.10.md dosyasında. Yeni tasarım emülatör ekranında incelendi.

Resmi uygulama kaynakları:
- https://developers.google.com/admob/android/interstitial
- https://developers.google.com/admob/android/privacy
- https://developer.android.com/google/play/billing/integrate
- https://support.google.com/googleplay/android-developer/answer/16430488
