# Train Pop 0.7 doğrulama — 14 Ekim 2026

- Sürüm: 0.7.0 / versionCode 7. APK: 12.413.160 bayt.
- Başlangıçta çemberde en fazla 160 gerçek yolcu, dört fiziksel şerit ve iki taraftan dörder giriş sırası. İlk bölümlerde yolun dolu olması için tren kapasitesi uzunluğa göre 16 / 32 yolcuya çıktı; biniş ve ödül mantığı bu talebi korur.
- Renk grupları 0,025 tur aralıklarla baştan düzenli yerleşir. Normal hız 0,05 tur/saniye. Giriş kuyruğu varken boşluklar yeni yolculara ayrılır; kuyruk bitince kalan grup ileri yürüyerek yakınlaşır.
- 6 renk × 8 yön = 48 tepeden robot sprite’ı. Çift görüntü yapan alfa karışımı kaldırıldı. Viraj yönü yolun teğetinden hesaplanır.
- Üst alan için özgün kiraz çiçekli istasyon bahçesi eklendi. Büyük ortadaki sayaç paneli kaldırıldı.
- Karşılama robotunun başı, gövdesi, iki kolu ve iki ayağı ayrı sprite. Kafa sallama, omuzdan el sallama ve hafif gövde/ayak hareketi var. Dokununca Hoş geldiniz balonu dört saniye görünür. Animasyonlar ayarı kapalıysa ek hareket durur; zorunlu oyun akışı sürer. Sesli konuşma eklenmedi.
- Eski düşük yoğunluklu v1/v2 kayıtlar yeni yolcu talebine dönüştürülür; bölüm, hamleler, gönderilmiş trenler, altın ve güçlendirmeler korunur. Yeni kayıtlar v3 biçiminde.
- 25 birim testi başarılı: 15 oyun, 7 ekonomi, 3 geometri. 200 bölüm güçlendirmesiz tamamlandı. İlk kare yoğunluğu ve eski kayıt göçü için ek testler var.
- Son assembleDebug/lintDebug başarılı; uygulamaya ait AndroidRuntime çökme kaydı görülmedi.
- Emülatörde son APK kuruldu, sahne ve eklemli robot incelendi. Bir tren gönderildikten sonra kayıtta 16 yolcu / 1 hamle / başarısızlık yok doğrulandı.
- Karşılama videosu 7 saniye, oynanış videosu 12 saniye; ikisi gerçek emülatör kaydı. Fiziksel Android cihazında FPS ölçümü yapılmadı. Kullanıcının görsel onayı bekleniyor.

APK SHA-256: E9A897F99D353AB09CCBAB8B321C04BEE5C25EC286411B17F67D99AE872E1293

Üretim yöntemi, tüm kaynaklar ve tam prompt’lar: trainpop-v0.7-art.md.
