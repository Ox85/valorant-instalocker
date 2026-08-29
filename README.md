<div align="center">

# 🎯 Instalocker

**Valorant için otomatik ajan kilitleyici** — maç başında ajan-seçme (pregame) ekranı açılır açılmaz, seçtiğin ajanı milisaniyeler içinde otomatik kilitler.

![Java](https://img.shields.io/badge/Java-15%2B-orange)
![UI](https://img.shields.io/badge/UI-Swing%20%2B%20Darklaf-1f2937)
![Region](https://img.shields.io/badge/Region-EU-ff4655)
![Amaç](https://img.shields.io/badge/Amaç-Eğitim-blueviolet)

<img src="preview.png" width="520" alt="Instalocker arayüzü">

</div>

---

## ✨ Özellikler

- ⚡ **Otomatik kilitleme** — pregame açılır açılmaz ajanını anında kilitler
- 🎭 **Tüm ajanlar**, rol bazında filtreli (Duelist · Controller · Initiator · Sentinel)
- 🔓 **Envanter algılama** — sahip olduğun ajanlar otomatik aktif olur
- 🎨 **Premium arayüz** — HD ikonlar, Valorant temalı koyu/kırmızı tasarım
- 🔁 **Dayanıklı** — kilitleme başarısız olursa `select` + `lock` tekrar dener, token süresi dolarsa yeniler

## 📦 Gereksinimler

- **Valorant** (şu an **EU** sunucusu için ayarlı)
- **JDK 15+** (proje Amazon Corretto 15 ile derlenmiştir)
- **IntelliJ IDEA** (veya Maven)

## 🚀 Kurulum & Kullanım

```bash
git clone https://github.com/Ox85/instalocker.git
```

1. Projeyi **IntelliJ IDEA**'da aç → Maven bağımlılıkları insin (`gson`, `darklaf`, `commons-io`).
2. **Valorant açıkken** `start.java` dosyasını çalıştır.
3. Arayüzde: **rol seç → ajan seç → `LOCK IN`** (buton "STOP" olur, artık bekliyor).
4. Bir maç kuyruğuna gir. **Ajan-seçme ekranı açılınca ajanın otomatik kilitlenir.** 🎯

> ℹ️ Program açılışta `data.json` (ajan listesi) ve `assets.zip` (ikonlar) dosyalarını bir GitHub deposundan indirip geçici klasöre açar. Bu depo `start.java` içindeki **`REPO_OWNER` / `REPO_NAME` / `REPO_BRANCH`** sabitlerinden belirlenir — kendi veri deponu kullanmak istersen sadece bunları değiştir. (Yönetici olarak çalıştırmaya gerek yoktur.)

## 🔒 Güvenli mi?

**Zararlı yazılım açısından — şeffaftır:**

- ✅ **Tamamen açık kaynak.** Her satırı kendin okuyabilirsin.
- ✅ **Şifreni/hesabını çalmaz, hiçbir yere göndermez.** Yalnızca Valorant'ın **kendi bilgisayarındaki yerel** istemci API'si (`lockfile`) ve Riot'un **resmi** sunucularıyla konuşur.
- ✅ Hiçbir 3. parti sunucuya kişisel veri gitmez (GitHub'dan yalnızca ikon/ajan listesi indirilir).

**⚠️ AMA ban riski açısından — garanti YOKTUR:**

- Instalocker'lar Valorant **Hizmet Şartları'na aykırıdır** (oyunla etkileşen 3. parti yazılım).
- **Riot Vanguard** bu tür araçları tespit edebilir → **hesap banı riski vardır.**
- Bu proje **eğitim ve öğrenme amaçlıdır.** Kullanım **tamamen kendi sorumluluğundadır.**

## 🛠️ Nasıl çalışır?

1. Yerel `lockfile`'dan port + şifreyi okur, entitlements token'ını alır.
2. `X-Riot-ClientVersion`'ı `ShooterGame.log`'daki **gerçek istemci sürümünden** okur (yanlış sürüm Riot tarafından reddedilir).
3. `pregame/v1/players/{puuid}` uç noktasını yoklar; 200 + MatchID gelince maçı yakalar.
4. `pregame/v1/matches/{matchID}/lock/{agentID}` uç noktasına POST atarak ajanı kilitler.

## ⚖️ Sorumluluk Reddi

Bu proje Riot Games ile **bağlantılı, onaylı veya sponsorlu değildir.** "VALORANT" ve ilgili tüm varlıklar Riot Games'e aittir. Yazılım "olduğu gibi" (as-is) sunulur; kullanımından doğacak hiçbir sonuçtan (hesap yasağı dahil) geliştirici sorumlu tutulamaz.
