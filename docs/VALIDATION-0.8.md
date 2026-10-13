# Train Pop 0.8 doğrulama — 14 Ekim 2026

- Sürüm 0.8.0 / versionCode 8. APK 15,057,978 bayt. SHA-256: DA3B8CB9B8D030553B59AE3C1EA05DDB67F24ED29EBA1FEF406ACBBAA2F18C57
- Kalabalık kapasitesi 160'tan 176'ya çıktı; dört fiziksel şeritte 44 sıra, 1/44 tur aralığı. Başlangıç rengi kapıya 3,2 saniyede gelir; ilk treni seçmeye zaman bırakır.
- Bekleyen kuyrukların ayakları y=118'de durur; çember aşağıda y=239 merkezli, yarıçapları 139/60. Bekleyen gövde ile hareketli gövde için ayrı alan güvenliği test edildi.
- Bekleyenler, giriş geçişleri ve dolaşanlar tek zemin-Y sırasıyla çizilir. Geçiş 1,1 saniye; girişte robot gerçek hareket yönüne bakar. Eski iki ayrı çizim katmanı kaldırıldı.
- Kıyı/Vadi/Şehir/Gece adlı dört seçilebilir ray hattı ve üç ek hat alanı var. Seçilen boş hat gönderilen trene ayrılır; işlemden sonra sıradaki boş hat otomatik seçilir. Hat doluysa tren kaybolmaz.
- Peronlarda gerçek kapasite/biniş göstergesi; uzun trenin vagonu perona gelince veya çıkınca kaybolmaz. Yeni lokomotif/vagon ve menü sprite'ları özgün imagegen görselleridir.
- Beyaz buton tabanları ve ana sayfa düğmelerinin beyaz çerçeve/etiket arkalıkları kaldırıldı. Panelin içerik alanındaki açık renk bilinçli arayüz zemini olarak kaldı.
- Paket kartları içerik ve detay ekranına bağlı. Kurulmamış satın alma Kullanılamıyor olarak gösterilir; fiyat veya ödeme sonucu uydurulmaz. Reklamsız paketin 1500 altını detayda da görünür.
- Kullanıcı Play Console uygulama/ürün kaydını henüz oluşturmadığını bildirdi. Gerçek ödeme etkin değildir. İmzalı Play makbuzu olmadan ürün verilmez; kayıt, ürünler, fiyat ve dahili test kurulumu ileride bağlanacak.
- Debug ödül reklamı yüklenirken bekleyen isteğin unutulması düzeltildi: yükleme, 15 saniye zaman aşımı ve hata geri bildirimi var. Bu sürümde canlı reklam/ödeme doğrulaması yapılmadı.
- 27 birim testi başarılı (16 oyun, 7 ekonomi, 4 geometri); 200 bölüm güçlendirmesiz çözüldü. Seçilen ray rezervasyonu ve giriş kuyruğu/çember ayrımı için regresyon testleri var.
- Son assembleDebug ve lintDebug başarılı. Emülatörde mağaza İncele -> ürün detay ekranı açıldı; kullanılamayan satın alma düğmesine dokununca altın 945'te kaldı ve ürün verilmedi.
- Oyun/ana sayfa/mağaza görüntüleri ve 18 saniyelik gerçek emülatör oynanış kaydı hazırlandı. Fiziksel telefonda FPS ölçülmedi; kullanıcı görsel onayı bekleniyor.

Görsel kaynakları ve tam prompt'lar: trainpop-v0.8-art.md.
Satın alma bağlantı notu: trainpop-play-console-notu.md.

Son kayıt kontrolü: seçilen hatta tren gönderildikten sonra kalıcı oturum v4 / seviye 2 / 16 binen yolcu / 1 hamle / başarısızlık yok olarak doğrulandı.
