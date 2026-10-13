# Train Pop 0.9 — 14 Ekim 2026

- Ray kumanda görünümü kaldırıldı. Açık renkli biniş istasyonu, ayrı hat başlıkları, doluluk göstergeleri ve net boş/dolu durumları kullanılıyor. Dolu veya ayrılan trenin peronuna dokunmak seçili boş peronu bozmuyor.
- Sol ve sağ giriş yolları kıvrılarak mevcut çembere bağlanıyor; yol kenarları tek çizim geçişinde birleşiyor. Yirmi saniyelik dolaşım hızı korunuyor.
- İlk bölümden sonra tren sayısı, renk sayısı ve başlangıçtaki kapalı çıkışlar artıyor. Aynı seviyede farklı zorluk yerine her seviyeye deterministik bir tahta üretiliyor. 64 aday arasından hedef zorluğa en yakın, ters yerleştirme sırası ile çözülebilen tahta seçiliyor.

| Bölüm | Tren | Renk | Kapalı çıkış |
|---|---:|---:|---:|
| 1 | 8 | 3 | 1 |
| 2 | 10 | 4 | 2 |
| 3 | 12 | 4 | 3 |
| 4 | 14 | 5 | 4 |
| 5 | 16 | 5 | 6 |
| 6 | 18 | 5 | 7 |
| 7 | 20 | 6 | 9 |
| 8 | 22 | 6 | 11 |
| 9 | 24 | 6 | 13 |

Tren sayısı 24, renk sayısı 6 ile sınırlı. Kapalı çıkış hedefi 13. bölüme kadar artıyor, sonrasında yeni düzenler aynı üst zorluk bandında üretiliyor. Eski kayıtlar kendi tren ve yolcu toplamlarını koruyor; yeni zorluk sonraki yeni bölümde uygulanıyor. Yeniden başlatmak mevcut seviyeyi yeni üreticiyle oluşturur.

## Mağaza

- VIP 150, Düzenle 120, Karıştır 100 oyun altını ile alınabilir. Altın düşümü ve seçilen güçlendirmenin artışı tek kalıcı işlemde kaydedilir. Yetersiz bakiye hiçbir şeyi değiştirmez.
- Ana sayfadaki Başlangıç, artık ayrı bir ücretsiz hoş geldin hediyesi: 150 altın ve her güçlendirmeden bir adet. Bir kez alınır. Ücretli başlangıç ürününe sahiplik vermez.
- Gerçek parayla satılan altın/paket/reklamsız ürünler için Google Play ürün kaydı, imza anahtarı ve test dağıtımı gerekli. Bunlar kullanıcı tarafından henüz oluşturulmadı. Fiyat veya ödeme uydurulmaz; ödeme yerine mevcut altın mağazasına geçiş sunulur. Bu yapı canlı para tahsilatı yaptığını iddia etmez.
- Bu sürümde zorunlu reklam gösterilmiyor. İsteğe bağlı ödül reklamları debug sürümde Google test reklamlarıdır.

## Doğrulama

31 birim testi: Game 18, Economy 9, ConveyorGeometry 4. 200 bölüm harcama veya güçlendirme olmadan tamamlandı; çakışma, yolcu sayısı, kayıt göçü, peron rezervasyonu, günlük ödül ve ödeme makbuzu tekrarları kontrol edildi. Android assembleDebug ve lintDebug başarılı.

Pixel 9 emülatöründe birleşik girişler, istasyon, mağaza ve ödeme açıklaması görsel olarak incelendi. Altın mağazasında iki VIP işlemi toplam 300 altın düşürdü ve VIP envanterini 2 artırdı (720→420, 2→4). Google Play canlı ödeme akışı ürünler olmadığı için test edilmedi.

Yeni görsel üretimi yok; önceki özgün varlıklar kullanılıyor. Varlık kaynakları docs/ART-0.7.md ve docs/ART-0.8.md dosyalarında.
