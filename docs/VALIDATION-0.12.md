# Train Pop 0.12 — 17 Ekim 2026

Oyun ekranı adaçayı yeşili ve krem paletine geçirildi. Bahçe arka planı sakinleştirildi; konveyör, depo zemini, başlık ve güçlendirme kartları aynı yumuşak tema içinde düzenlendi. Robot, tren ve peronlar mevcut render edilmiş oyuncak stiliyle birlikte kullanılıyor; gerçek zamanlı 3D motor değildir.

Zorluk artık 8 trenle başlar, üç bölümde bir bir tren artar ve 24 trende sınırlanır. Dördüncü renk 9, beşinci 23, altıncı 41. bölümde açılır. Renk açılması tren sayısı artışıyla aynı bölüme denk gelmez. Her yedinci bölümde hedef kapalı çıkış sayısı bir azaltılır. Üretici çözümü bilinen yerleşimler arasından seçim yapar. Eski kayıtların mevcut yerleşimleri korunur.

Önceki çalışma da bu sürüme dahil: eşleşen 12 tren ve 48 yönlü robot görseli, peronlara uzanan biniş köprüsü, reklam ödülüyle bölüm boyunca 2× veya 3× robot akışı seçimi. Hız yalnızca ödül callback'iyle etkinleşir, checkpoint'e yazılır; tren animasyonları hızlanmaz. Eski kayıtlarda varsayılan 1× korunur.

Satın alma düğmelerinde fiyat ve Satın al görünür. Play fiyatı gelmeden onaylı planlanan Türkiye fiyatı gösterilir ve satışın henüz açılmadığı açıklanır. Merchant profili ve gerçek Play ürünleri eksik olduğundan gerçek ödeme henüz test edilmemiştir; sahte satın alma hakkı verilmez.

Doğrulama: testDebugUnitTest, assembleDebug ve lintDebug başarılı. 37 test; 200 bölümün güçlendirmesiz çözülebilmesi, aşamalı zorluk profili, dört kişilik giriş, hız ve kayıt uyumluluğu kontrolleri dahil. Pixel 9 emülatöründe ekran görüntüsü ve akış kaydı alındı. Gerçek cihaz FPS ölçümü ve üretim reklam/ödeme testi yapılmadı.
