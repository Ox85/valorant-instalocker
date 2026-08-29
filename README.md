# valorant-instalocker — data reposu

Bu repo, **Valorant Instalocker** programının çalışmak için uzaktan indirdiği iki dosyayı barındırır:

| Dosya | Ne işe yarar |
|-------|--------------|
| `data.json`  | Ajan listesi (kategori + UUID) ve `updateAvailable` (bakım modu) bayrağı. Program açılışta buradan okur. |
| `assets.zip` | Tüm görseller (ajan ikonları, rol ikonları, logo, question_mark). Program açılışta indirip TEMP klasörüne (`%TEMP%\spicy`) açar — `C:\` köküne yazmak admin gerektirdiği için oraya değil. |

Program bu dosyaları **raw GitHub** linklerinden çeker; bu yüzden ikisi de reponun **kökünde** ve `main` dalında olmalıdır.

---

## ⚠️ ÖNEMLİ — Yeni repo açtıysan linkleri güncelle

Program içindeki linkler eski repoya (`l0x85l/valorant-instalocker`) işaret ediyor. Yeni bir repo açtığın için **`start.java` dosyasındaki 2 linki** kendi reponla değiştirmelisin.

`src/main/java/spicy/valorant/start.java` içinde (yeniden düzenlenmiş sürümde tek yerde toplandı):

```java
// BUNLARI kendi kullanıcı adın/repo adınla değiştir:
public static final String REPO_OWNER = "SENIN_KULLANICI_ADIN";
public static final String REPO_NAME  = "valorant-instalocker";
```

Repoyu **aynı adla** (`valorant-instalocker`) ve `main` dalıyla açarsan sadece `REPO_OWNER`'ı değiştirmen yeterli. Repoyu public yapmayı unutma (raw linkler public olmalı).

> İpucu: Yüklemeden sonra linkin çalıştığını tarayıcıda şu adresle test et:
> `https://raw.githubusercontent.com/<OWNER>/<REPO>/refs/heads/main/data.json`

---

## Repo yapısı

```
instalocker-repo/
├─ data.json            # program verisi (ajanlar + UUID + bakım bayrağı)
├─ assets.zip           # programın indirdiği görsel paketi (35 dosya)
├─ build-assets.ps1     # assets/ klasöründen assets.zip'i yeniden üretir
├─ README.md
└─ assets/              # zip'in AÇILMIŞ kaynağı (görselleri buradan düzenle)
   ├─ logo.png              # program penceresi ikonu
   ├─ question_mark.png     # takım bilgisinde ajanı seçilmemiş oyuncu
   ├─ duelist.png           # sol menü rol ikonları (TEKİL isim!)
   ├─ controller.png
   ├─ initiator.png
   ├─ sentinel.png
   ├─ duelist/     iso jett neon phoenix raze reyna waylay yoru
   ├─ controller/  astra brimstone clove harbor miks omen viper
   ├─ initiator/   breach fade gekko kayo skye sova tejo
   └─ sentinel/    chamber cypher deadlock killjoy sage veto vyse
```

**Kritik isimlendirme kuralı:** Program ikon dosyasını `agentName.toLowerCase(Locale.ENGLISH) + ".png"` ile arar.
Bu yüzden dosya adları **saf ASCII küçük harf** olmalı (örn. `iso.png` — Türkçe noktasız `ıso.png` DEĞİL) ve
`data.json`'daki ajan anahtarının küçük hâliyle birebir eşleşmeli. `KAY/O` ajanının anahtarı `KAYO`, dosyası `kayo.png`'dir (eğik çizgi yok).

---

## data.json formatı

```jsonc
{
  "updateAvailable": false,          // true = program "bakım modu" der ve açılmaz (uzaktan kapatma anahtarı)
  "categories": [
    { "duelists":    { "Jett": "add6443a-...", ... } },   // anahtar = ajan adı, değer = Riot karakter UUID'si
    { "controllers": { ... } },
    { "initiators":  { ... } },
    { "sentinels":   { ... } }
  ]
}
```

- Kategori anahtarları **çoğul** olmalı: `duelists`, `controllers`, `initiators`, `sentinels` (program bunlarla birebir kıyaslıyor).
- Asset klasör adları ise **tekil**: `duelist`, `controller`, `initiator`, `sentinel`.
- UUID'ler `valorant-api.com/v1/agents` kaynağından alınan resmî Riot karakter UUID'leridir (29 ajan, Ağustos 2026).

### Yeni ajan eklemek
1. `data.json`'da doğru kategoriye `"AjanAdı": "uuid"` satırı ekle.
2. `assets/<kategori>/ajanadı.png` ikonunu ekle (küçük harf, ASCII).
3. `build-assets.ps1`'i çalıştırıp `assets.zip`'i yenile.
4. İkisini de repoya push'la.

### Programı uzaktan kapatmak (bakım modu)
`data.json`'da `"updateAvailable": true` yapıp push'la — açılan tüm istemciler "bakım modu" mesajı verip kapanır.

---

## assets.zip'i yeniden üretmek

Görselleri `assets/` içinde değiştirdikten sonra:

```powershell
powershell -ExecutionPolicy Bypass -File .\build-assets.ps1
```

Script, zip girdilerini **forward-slash** ('/') ile ve `assets/` içeriğini **kökte** olacak şekilde üretir (Java `ZipInputStream` için doğru biçim).
