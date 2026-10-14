# Train Pop satın alma bağlantısı

14 Ekim 2026: Train Pop taslağı Axio Apps hesabında oluşturuldu. Google Payments satıcı/ödeme profili bulunmadığı için ürün sayfası kapalı; gerçek satış etkin değildir. Onaylı ürün içerikleri ve fiyatları PRODUCT-CATALOG.json dosyasında. Uygulama imzalı satın alma makbuzu olmadan ücretli hak vermez.

Paket kimliği: com.axioapps.trainpop

Kodda hazır ürün kimlikleri:

| Kimlik | İçerik | Kullanım |
|---|---|---|
| starter_pack | 1000 altın ve her güçlendirmeden 2 adet | Tek seferlik paket |
| remove_ads | Zorunlu reklamları kaldırma | Kalıcı |
| ad_free_bundle | Reklamsız +1500 altın ve her güçlendirmeden 1 adet | Tek seferlik paket |
| coins_500 | 500 altın | Tekrar satın alınabilir |
| coins_3000 | 3000 altın | Tekrar satın alınabilir |

Sonraki bağlantı işi: Play Console kaydı, bu ürünlerin aynı kimliklerle oluşturulması, kullanıcının fiyatları belirlemesi, imzalı uygulamanın dahili test kanalına yüklenmesi ve lisans test hesaplarıyla satın alma/iptal/geri yüklemenin doğrulanması. TRAINPOP_BILLING_PUBLIC_KEY yerel Gradle ayarına girilir; anahtar ve hesap bilgileri depoya yazılmaz.

Mevcut kod Google Play ürün sorgusu/fiyatı, Billing ödeme penceresi, imzalı satın alma makbuzu kontrolü, ürün verme, tüketme/onaylama ve geri yükleme yollarını içerir. Sunucuda makbuz kontrolü, iade/iptal uzlaştırması ve cihazlar arası haklar yayın öncesi kalan işlerdir.

Debug ödül reklamları resmi test birimini kullanır. Canlı reklam hesabı, kullanıcı onayı ve yayın ayarları henüz etkin değildir.


## 0.10 / 14 Ekim 2026 güncellemesi

Train Pop taslağı Axio Apps hesabında oluşturuldu; paket com.axioapps.trainpop. Şu an engel uygulama kaydı değil, Google Payments satıcı/ödeme profili eksikliği. Kullanıcı ödeme profilini tamamlayınca tek seferlik ürünler sayfasında docs/PRODUCT-CATALOG.json içindeki onaylı kimlik, içerik ve Türkiye fiyatları kullanılacak. Bu JSON bir Publishing API yükleme gövdesi değil, proje ürün kataloğudur.

Satıcı hesabı: https://play.google.com/console/u/0/developers/5990238439296916848/paymentssettings

Canlı Gradle ayarları: TRAINPOP_ADMOB_APP_ID, TRAINPOP_INTERSTITIAL_AD_ID, TRAINPOP_REWARDED_AD_ID, TRAINPOP_BILLING_PUBLIC_KEY. UMP gizlilik mesajı AdMob'da yapılandırılmalıdır. İmzalı AAB ve Play lisans test hesaplarıyla satın alma, iptal, pending, tüketim ve geri yükleme doğrulanmadan canlı ödeme tamamlandı sayılmaz.
