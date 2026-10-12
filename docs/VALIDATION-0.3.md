# Train Pop 0.3 doğrulama

- `testDebugUnitTest`, `assembleDebug`, `lintDebug`: başarılı.
- 13 oyun + 7 ekonomi testi: 20 başarılı, 0 başarısız.
- 200 deterministik bölüm, güçlendirme veya ek peron olmadan çözüldü.
- Eşleşmeyen robotların dönüşü, birleşme mesafesi, güncelleme sıklığına göre hareket, v1 kayıt geçişi ve v2 konum saklama test edildi.
- Android emülatöründe tek axio / LET’S PLAY açılışı ve oyun ekranı görsel olarak incelendi.
- Bir saniye arayla alınan karelerde robot konumları değişti; sarı robotlar sarı trenlere binip trenleri gönderdi.
- Yeniden başlatma çalıştı. VIP düğmesi işlemi doğrudan uyguladı, kalıcı adet 2’den 1’e düştü.
- Son çalıştırmada AndroidRuntime hata kaydı görülmedi.
- Robotlar duraklatma, ayarlar ve ödül reklamı sırasında bilinçli olarak durur. Oyun devam ettiğinde konumlarını korur.
- Sahne robot sprite’larını önbellekten çizer. Fiziksel Android telefonlarda FPS ölçümü henüz yapılmadı; emülatör yazılımsal GPU kullanıyor.
- Gerçek para ödemeleri ve canlı reklamlar Play Console/AdMob yapılandırması bekliyor. APK ödüllü Google test reklamlarını kullanır; ödüller yalnızca tamamlanan reklam callback’inde verilir.

APK: trainpop-v0.3-debug.apk
SHA-256: 768C84E6CE1514724F8BE392CB2169ABB80C5BDD4A1C4D7F34F6C0187ECF35DC
