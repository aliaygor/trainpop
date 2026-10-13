# Train Pop 0.6

- Bilet panosu kaldırıldı; sürekli dönen robot kalabalığı geri geldi.
- Altı renkli özgün 3D render sprite'ı eklendi. Gerçek zamanlı 3D model kullanılmıyor.
- Şerit aralığı 12'den 16 sanal piksele genişledi. Yeni robotların gövdeleri daha az örtüşür.
- Çemberde en fazla 48 robot dolaşır; yan kuyruklar boşalan yerlere yumuşak geçişle katılır. Var olan kayıtlar korunur.
- Bir tur 20 saniye; virajlarda yaklaşık sabit yer hızı. Başlangıç grupları şeritlere göre küçük faz farkıyla biner.
- 23 test geçti. 200 bölüm bilinen çözümleriyle tamamlandı. Son assembleDebug ve lintDebug başarılı.
- Emülatörde yeni sprite'lar incelendi, temiz bölüm yeniden başlatıldı, tren gönderildi. Kalıcı durumda 1 hamle / 4 binen robot doğrulandı.
- 10 saniyelik emülatör kaydı hazır. Fiziksel telefon testi yapılmadı; kullanıcı görsel onayı bekleniyor.
- Görsel üretim yöntemi, kaynak dosya ve tam prompt: ART-0.6.md.
