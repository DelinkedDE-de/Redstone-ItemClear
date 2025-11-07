# 🎉 Redstone ItemClear v1.0 - Initial Release

**Intelligentes TPS-Management für SkyBlock Server**

> ⚠️ **Wichtig:** Dieses Plugin bereinigt **NUR betroffene Chunks** - nicht den gesamten Server!

---

## 🚀 Was ist Redstone ItemClear?

Ein hochmodernes TPS-Management-System für Minecraft 1.21.4 (Spigot/Paper), das Performance-Probleme **intelligent erkennt** und **nur in Problemzonen** eingreift.

### ✨ Hauptfeatures

🎯 **Intelligente Problemerkennung**
- Identifiziert Chunks mit zu vielen Items, Mobs oder Redstone-Aktivität
- Sortiert nach Schweregrad
- Bereinigt **NUR** betroffene Bereiche

⚡ **4 Stufen automatische Maßnahmen**
- **Warning** (TPS < 18) → Nur Benachrichtigung
- **Moderate** (TPS < 15) → Items über Limit entfernen
- **Severe** (TPS < 12) → Items, Mobs, Redstone einschränken
- **Emergency** (TPS < 10) → Aggressive Bereinigung

🚫 **Chunk-Einschränkungen** (nur in Problemzonen!)
- **Redstone-Blockierung** - Stoppt Hopper, Piston, Observer, etc.
- **Pflanzenwachstum** - Verhindert Lag durch Auto-Farmen
- **Mob-Spawning** - Blockiert natürliches Spawning

📡 **Discord-Integration**
- Webhook-Benachrichtigungen mit detaillierten Embeds
- Koordinaten und Weltinformationen
- Statistiken über Maßnahmen

🛡️ **Geschützte Regionen**
- Whitelist für komplett geschützte Bereiche (Spawn, etc.)
- Custom Regions mit individuellen Grenzwerten (Farm-Zonen, Mob-Grinder)

🔄 **Auto-Recovery**
- Reaktiviert automatisch alle Einschränkungen bei TPS-Erholung
- Konfigurierbare Schwellenwerte

🧪 **Lag-Test System**
- Integrierte Test-Tools für Development
- Simuliert CPU-Lag, Items, Mobs

---

## 📥 Installation

1. Download `Redstone-ItemClear-1.0-SNAPSHOT.jar`
2. In `plugins` Ordner kopieren
3. Server starten
4. Config anpassen: `plugins/Redstone-ItemClear/config.yml`
5. Discord Webhook eintragen (optional)
6. `/ric reload`

---

## 📋 Quick Start

```bash
# Status anzeigen
/ric status

# Problemzonen analysieren
/ric analyze

# Config neu laden
/ric reload
```

### Lag-Test (Development)
```bash
# TPS auf ~15 senken (Severe-Level testen)
/riclag start 3

# 200 Items spawnen
/riclag items 200

# Stoppen
/riclag stop
```

---

## ⚙️ Konfiguration

**Vollständig konfigurierbar:**
- TPS-Schwellenwerte
- Grenzwerte pro Chunk
- Aktionsstufen
- Discord Webhook
- Geschützte Regionen
- Chunk-Einschränkungen

📖 **Ausführliche Dokumentation:** [CONFIG-EXAMPLES.md](https://github.com/DelinkedDE-de/Redstone-ItemClear/blob/main/CONFIG-EXAMPLES.md)

---

## 🎯 Für wen ist das?

✅ **Perfekt für:**
- SkyBlock Server
- Skyblock/Survival Server mit Auto-Farmen
- Server mit vielen Redstone-Contraptions
- Server die TPS-Probleme durch Lag-Bereiche haben

❌ **Nicht geeignet für:**
- Vanilla-Server ohne Performance-Probleme
- Server ohne Auto-Farmen/Redstone

---

## 📊 Technische Details

- **Minecraft:** 1.21.4 (Spigot/Paper)
- **Java:** 21
- **API:** Paper API (mit Spigot Fallback)
- **Features:** Async-Analyse, Auto-Recovery, Discord-Integration

---

## ⚠️ Wichtige Hinweise

### Items vs. Itemstacks
Config-Werte zählen **Item-Entities**, nicht Itemstacks!
- `items-per-chunk: 100` = 100 Item-Objekte auf dem Boden
- Ein Stack mit 64 Wheat = 1 Item-Entity (wenn gemerged)

### Bereichsspezifisch
**Das Plugin bereinigt NUR Problemzonen!**
- Farm-Bereiche mit vielen Items → werden bereinigt
- Spawn ohne Probleme → bleibt unberührt
- Custom Regions können eigene Limits haben

---

## 📜 Lizenz

**Proprietäre Lizenz - Alle Rechte vorbehalten**

✅ Erlaubt: Nutzung auf eigenen/privaten Servern, Config-Anpassung
❌ Nicht erlaubt: Weitergabe, Verbreitung, Dekompilierung, kommerzielle Nutzung ohne Genehmigung

Vollständige Bedingungen: [README.md](https://github.com/DelinkedDE-de/Redstone-ItemClear#lizenz)

---

## 💬 Support & Feedback

- **GitHub Issues:** [Hier melden](https://github.com/DelinkedDE-de/Redstone-ItemClear/issues)
- **Discord:** DelinkedDE
- **Website:** [delinked.de](https://delinked.de)

---

## 🙏 Credits

Entwickelt von **delinked.de** mit Claude.

> 🎵 **Weiteres Projekt:** Interessiert an einem **Discord Moderation & Musik Bot**?
> Besuche [delinked.de](https://delinked.de)!

---

## 📦 Download

**JAR-Datei:** `Redstone-ItemClear-1.0-SNAPSHOT.jar` (siehe Assets)

**Checksums:**
```
MD5: [wird beim Upload generiert]
SHA256: [wird beim Upload generiert]
```

---

**Viel Erfolg mit Redstone ItemClear! 🚀**

Bei Fragen oder Problemen einfach ein Issue erstellen!
