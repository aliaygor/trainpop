# Train Pop 0.4 doğrulama

- APK sürümü 0.4.0 / versionCode 4.
- Oyun ve ekonomi testleri: 23 geçti, 0 başarısız. 200 bölüm bilinen çözümleriyle güçlendirmesiz tamamlandı.
- Robotların bir turu 20 saniye sürüyor; önceki 5,56 saniyelik turdan 3,6 kat yavaş.
- Çember konumları yay uzunluğu tablosundan hesaplanıyor; virajlarda yaklaşık sabit yer hızı test edildi.
- Yan kuyruktan çembere katılan robotlara 0,7 saniyelik konum geçişi eklendi. Bekleyen sıra saniyede 35 sanal piksele kadar ilerliyor.
- Altı renkli lokomotif ve altı eşleşen vagon sprite’ı kullanılıyor. Uzun trenler iki ayrı parçadan çiziliyor; lokomotif resmi uzatılmıyor.
- Son APK emülatörde görsel olarak incelendi. Gerçek oynanıştan 12 saniyelik MP4 kaydı alındı.
- Gönderilen pembe tren dört robot aldı; kayıt sonunda kalıcı oyun durumu boarded=4 / moves=1 olarak doğrulandı.
- Son assembleDebug/lintDebug başarılı, lint hata sayısı sıfır. AndroidRuntime hata kaydı görülmedi.
- Fiziksel Android telefonunda FPS ölçümü yapılmadı. Video yazılımsal GPU kullanan emülatörden alınmıştır.

APK SHA-256: 62E3D10C9037868DE9241811D91E6FAB2EA90B903FD4DD6DB0646626781AAFE3
