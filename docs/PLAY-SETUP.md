# Train Pop satın alma bağlantısı

14 Ekim 2026: Kullanıcı Play Console uygulama kaydını ve ürünlerini henüz oluşturmadığını belirtti. Bu nedenle gerçek satış etkin değildir. Uygulama fiyat uydurmaz, satın alma düğmesine basılınca para/ürün verilmez.

Paket kimliği: com.aliaygor.trainpop

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
