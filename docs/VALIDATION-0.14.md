# Train Pop 0.14 — 17 Ekim 2026

Ana sayfa robotu trenin önünde ortalanmış bir ayak basamağına yerleştirildi. Ayakların yanal/yürüme salınımı kaldırıldı; kafa, gövde ve kol hareketleri sürüyor. Bölüm levhası ve konuşma balonu yeni konuma göre taşındı. Alınan Başlangıç Hediyesi ana sayfadan kaybolmaz; Alındı durumuyla açılabilir, tekrar ödül vermez.

assembleDebug ve lintDebug başarılı. Değişiklik yalnızca ana sayfa çizimi/yerleşimi; önceki 39 mantık testi bu sürümde yeniden çalıştırılmadı. Pixel 9 emülatöründe ana sayfa görsel kontrolü yapıldı.

Reklamsız hakkı mevcut kodda consume edilmeyen Play ürünü olarak acknowledge edilir. Başlangıçta ve resume sırasında queryPurchasesAsync ile sahip olunan ürünler okunur; doğrulanmış PURCHASED remove_ads/ad_free_bundle noAds hakkını açar. Ayarlar'da manuel geri yükleme de vardır. Aynı Play hesabı ve ilk geri yüklemede bağlantı gerekir. Mevcut yerel hak sonraki çevrimdışı açılışlarda korunur. İsteğe bağlı ödüllü reklamlar ayrıdır. Canlı ürünler/satıcı hesabı henüz yok; yeniden kurulumla gerçek lisans testi yapılmadı. Üretim öncesi sunucu doğrulama, iade/iptal uzlaştırması ve paket bonuslarının cihazlar arası tekrar verilmesini önleyen sunucu kaydı tamamlanmalıdır.

Kaynak: https://developer.android.com/google/play/billing/integrate
