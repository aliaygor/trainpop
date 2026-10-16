# Train Pop 0.13 — 17 Ekim 2026

- Hız seçenekleri artık doğrudan ödüllü reklamı başlatır. İkinci bir onay/reklam düğmesi kaldırıldı. Ödül callback'i gelmeden hız verilmez; reklam başarısızsa hız değişmez. Etkin hız tekrar seçilemez. Reklamın başladığı bölüm nesnesi korunur; başka bölüme yanlış ödül uygulanmaz.
- Günlük ikinci gün hediyesi ayrı bir çevirme hakkıdır; Karıştır envanteriyle karışmaz. Bonus haklar gün değişse ve uygulama yeniden açılsa da kalır. Bonus önce tüketilir; günlük ücretsiz hak ve reklam limiti ayrı tutulur.
- Eski ikinci gün hediyesini almış hesaplara bir defalık +1 çevirme telafisi; eski güçlendirme korunur.
- Kullanıcı satıcı hesabı açılışını sonraya bıraktı. Play Console ürün sayfası hâlâ Google Payments satıcı hesabı gerektiriyor. Canlı ürünler ve ödeme testi tamamlanmadı. Ödeme kurulumu tamamlandı iddiası yok.

Doğrulama: testDebugUnitTest, assembleDebug, lintDebug başarılı. 39 test; bonusun yeniden açılış/gün değişimi/harcama/reklam hakkıyla etkileşimi ve bir defalık telafi dahil. Pixel 9 emülatöründe 2× seçimi Google resmi ödüllü test reklamını doğrudan açtı. Üretim AdMob kimlikleri olmadan canlı reklam yayını açılmaz.

Emülatör testinde reklam Reward granted durumuna ulaştı; kapatıldıktan sonra oyun 2× Hız gösterdi ve session kaydı |2 ile sonlandı. Eski günlük kayıt (daily.index=2) için wheel.bonus=1 yazıldığı da doğrulandı.
