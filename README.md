# Redstone ItemClear

> 🎵 **Weiteres Projekt:** Interessiert an einem **Discord Moderation & Musik Bot**?
> Besuche [delinkedde.de](https://delinkedde.de) für mehr Informationen!

Ein intelligentes TPS-Management-System für Minecraft Spigot 1.21.4 Server, speziell entwickelt für SkyBlock-Server.

## Features

### Intelligente TPS-Überwachung
- Automatische Überwachung der Server-TPS
- Konfigurierbare Schwellenwerte für verschiedene Warnstufen
- Echtzeit-Performance-Monitoring

### Bereichsspezifische Reinigung
**WICHTIG:** Das Plugin bereinigt **NUR die spezifischen Chunks**, die als Problemzonen identifiziert wurden - **NICHT den gesamten Server!**
- Intelligente Identifikation von Problemzonen
- Nur betroffene Bereiche werden eingeschränkt
- Minimale Auswirkungen auf den Rest des Servers

### Ursachenanalyse
- Automatische Erkennung von Performance-Problemen:
  - Zu viele Entities in Chunks
  - Übermäßige Mob-Konzentration
  - Items auf dem Boden (z.B. von Autofarmen)
  - Excessive Redstone-Aktivität
- Chunk-basierte Analyse mit konfigurierbarem Scan-Radius
- Identifikation der problematischsten Bereiche

### Stufenbasierte Maßnahmen
Das Plugin reagiert automatisch mit verschiedenen Maßnahmen basierend auf den TPS:

1. **Warnung** (TPS < 18.0)
   - Discord-Benachrichtigung
   - Keine automatischen Aktionen

2. **Mäßig** (TPS < 15.0)
   - Discord-Benachrichtigung
   - Entfernung überschüssiger Items (über Limit)
   - **NUR in Problemzonen!**

3. **Schwer** (TPS < 12.0)
   - Discord-Benachrichtigung
   - Aggressive Item-Entfernung
   - Entfernung überschüssiger Mobs
   - **Temporäre Deaktivierung von Redstone in Problemzonen**
   - **Stoppen des Pflanzenwachstums in Problemzonen**
   - **NUR in Problemzonen!**

4. **Notfall** (TPS < 10.0)
   - Discord-Benachrichtigung
   - Entfernung aller Items
   - Entfernung passiver Mobs
   - **Deaktivierung aller Redstone-Komponenten**
   - **Stoppen des Pflanzenwachstums**
   - **Blockieren von Mob-Spawning**
   - **NUR in Problemzonen!**

### Discord-Integration
- Webhook-basierte Benachrichtigungen
- Detaillierte Informationen über:
  - Aktuelle TPS und Warnstufe
  - Durchgeführte Maßnahmen
  - Betroffene Bereiche mit Koordinaten
  - Weltinformationen
  - Statistiken über entfernte Items/Mobs
- Farbcodierte Embeds je nach Schweregrad

### Bereichsspezifische Einstellungen & Einschränkungen
- Whitelist für geschützte Regionen (z.B. Spawn)
- Custom-Regionen mit individuellen Grenzwerten
- Weltspezifisches Monitoring
- Flexible Konfiguration für verschiedene Bereiche

**Chunk-Einschränkungen (nur in Problemzonen):**
- **Redstone-Blockierung**: Stoppt Redstone-Aktivität in problematischen Chunks
  - Konfigurierbare Redstone-Komponenten
  - Temporäre Deaktivierung mit Auto-Recovery
- **Pflanzenwachstum**: Stoppt Crop-Growth in Problemzonen
  - Verhindert Performance-Probleme durch Farmen
  - Konfigurierbare Pflanzentypen
- **Mob-Spawning**: Blockiert natürliches Mob-Spawning
  - Getrennt für passive/hostile Mobs
  - Schützt Spawner und gezielte Spawns

### Auto-Recovery
- Automatische Wiederherstellung nach TPS-Verbesserung
- Konfigurierbare Erholungs-Schwellenwerte
- Sanfte Reaktivierung von Redstone-Komponenten

## Installation

1. Download der `.jar`-Datei aus `Redstone-ItemClear-1.0-SNAPSHOT.jar`
2. In den `plugins` Ordner des Servers kopieren
3. Server neu starten
4. Config anpassen (siehe Konfiguration)

## Konfiguration

Die Konfigurationsdatei wird beim ersten Start automatisch erstellt: `plugins/Redstone-ItemClear/config.yml`

**📖 Ausführliche Config-Dokumentation:** Siehe [CONFIG-EXAMPLES.md](CONFIG-EXAMPLES.md) für:
- Erklärung Items vs. Itemstacks
- Alle verfügbaren Pflanzentypen
- Alle verfügbaren Redstone-Komponenten
- Schritt-für-Schritt Anleitung für Regionen
- Empfohlene Einstellungen für verschiedene Server-Typen

### Wichtige Einstellungen

```yaml
# TPS-Schwellenwerte anpassen
tps-monitor:
  thresholds:
    warning: 18.0
    critical: 15.0
    emergency: 12.0

# Discord Webhook konfigurieren
discord:
  enabled: true
  webhook-url: "https://discord.com/api/webhooks/..."

# Überwachte Welten
analysis:
  monitored-worlds:
    - world
    - world_nether
    - world_the_end

# Einschränkungen (nur in Problemzonen!)
chunk-restrictions:
  plant-growth:
    enabled: true
    affected-types:
      - WHEAT
      - CARROTS
      - POTATOES
      # etc...

  redstone:
    enabled: true
    block-types:
      - REDSTONE_WIRE
      - PISTON
      - HOPPER
      # etc...

  mob-spawning:
    enabled: true
    block-passive: true
    block-hostile: false

# Aktionsstufen mit Einschränkungen
action-levels:
  severe:
    restrictions:
      disable-plant-growth: true
      disable-redstone: true
```

## Commands

### Haupt-Commands

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/ric status` | Zeigt den aktuellen TPS-Status | `redstoneitemclear.status` |
| `/ric analyze` | Analysiert und zeigt Problemzonen | `redstoneitemclear.analyze` |
| `/ric run <type>` | Manuelle Bereinigung von Problemzonen | `redstoneitemclear.run` |
| `/ric enable <type> [all]` | Aktiviert Einschränkungen wieder | `redstoneitemclear.enable` |
| `/ric reload` | Lädt die Konfiguration neu | `redstoneitemclear.reload` |
| `/ric info` | Plugin-Informationen | - |
| `/ric help` | Zeigt die Hilfe | - |

**Aliases:** `/redstoneitemclear`, `/riclear`

### `/ric run` - Manuelle Bereinigung

Führt manuelle Bereinigung in Problemzonen **UND** geladenen Chunks um Spieler durch:

| Syntax | Beschreibung |
|--------|--------------|
| `/ric run items` | Entfernt Items aus allen Chunks |
| `/ric run mobs` | Entfernt Mobs aus allen Chunks |
| `/ric run redstone` | Deaktiviert Redstone in allen Chunks |
| `/ric run plant` | Stoppt Pflanzenwachstum in allen Chunks |
| `/ric run all` | Führt alle Bereinigungen durch |
| `/ric run mobs,items` | Mehrere Optionen kombiniert (komma-getrennt) |

**Beispiele:**
```
/ric run items              # Nur Items entfernen
/ric run mobs,items         # Mobs und Items entfernen
/ric run all                # Komplette Bereinigung
```

**Was wird gescannt:**
1. **Problemzonen** vom automatischen Analyzer (Chunks die Thresholds überschreiten)
2. **Geladene Chunks** im 8-Chunk Radius um ALLE Online-Spieler
3. Beide Listen werden kombiniert (keine Duplikate)

**Ausgabe:**
```
Scanne 150 Chunks (25 Problemzonen + 125 Spieler-Chunks)...
Entferne Mobs aus 87 Chunks...
Mobs entfernt: 2459
Chunks gescannt: 150
Aktuelle TPS: 19.45
```

**Wichtig:** Entity-Removal läuft auf Main-Thread für maximale Stabilität!

### `/ric enable` - Einschränkungen reaktivieren

Aktiviert temporär deaktivierte Chunk-Einschränkungen (Redstone, Pflanzenwachstum, Mob-Spawning) wieder.

**Syntax:**
```
/ric enable <type> [all]
```

**Typen:**

| Typ | Beschreibung |
|-----|--------------|
| `all` | Aktiviert alle Einschränkungen wieder |
| `redstone` | Aktiviert Redstone wieder |
| `plant` | Aktiviert Pflanzenwachstum wieder |
| `mobs` | Aktiviert Mob-Spawning wieder |

**Bereiche:**

| Bereich | Beschreibung |
|---------|--------------|
| Ohne `all` | **Nur Chunks im 8-Chunk Radius** um den Spieler |
| Mit `all` | **ALLE Chunks** auf dem gesamten Server |

**Beispiele:**
```
/ric enable redstone            # Redstone in Spieler-Nähe aktivieren
/ric enable redstone all        # Redstone server-weit aktivieren
/ric enable plant              # Pflanzenwachstum in Spieler-Nähe aktivieren
/ric enable all                # Alle Einschränkungen in Spieler-Nähe aufheben
/ric enable all all            # Alle Einschränkungen server-weit aufheben
```

**Ausgabe:**
```
Redstone in 12 Chunks wurde reaktiviert
Redstone in 0 Chunks war bereits aktiv
Gesamt: 12 Chunks untersucht
```

**Wichtig:**
- Einschränkungen sind nur im RAM gespeichert
- Nach Server-Restart sind automatisch ALLE Einschränkungen aufgehoben
- Tab-Completion verfügbar für alle Parameter

### Lag-Test Commands (Development/Testing)

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/riclag start <1-5>` | Startet CPU-Lag-Simulation (1=mild, 5=extrem) | `redstoneitemclear.lagtest` |
| `/riclag stop` | Stoppt alle Lag-Simulationen | `redstoneitemclear.lagtest` |
| `/riclag items <anzahl>` | Spawnt Items um Spieler (max 1000) | `redstoneitemclear.lagtest` |
| `/riclag mobs <anzahl>` | Spawnt Mobs um Spieler (max 500) | `redstoneitemclear.lagtest` |
| `/riclag status` | Zeigt Lag-Test Status und TPS | `redstoneitemclear.lagtest` |

**Alias:** `/lagtest`

**⚠️ Hinweis:** Lag-Test Commands sind NUR für Testing/Development gedacht!

## Permissions

| Permission | Beschreibung | Standard |
|------------|--------------|----------|
| `redstoneitemclear.*` | Alle Permissions | OP |
| `redstoneitemclear.status` | Status-Command | OP |
| `redstoneitemclear.analyze` | Analyze-Command | OP |
| `redstoneitemclear.run` | Run-Command (Manuelle Bereinigung) | OP |
| `redstoneitemclear.enable` | Enable-Command (Einschränkungen aufheben) | OP |
| `redstoneitemclear.reload` | Reload-Command | OP |
| `redstoneitemclear.notifications` | In-Game-Benachrichtigungen | OP |
| `redstoneitemclear.lagtest` | Lag-Test Commands (Development) | OP |
| `redstoneitemclear.webpanel` | Zugriff auf WebPanel Dashboard | OP |

## Funktionsweise

### Monitoring-Zyklus
1. Plugin prüft alle X Sekunden die TPS (konfigurierbar)
2. Bei niedrigen TPS wird eine **Chunk-Analyse** durchgeführt
3. **Problemzonen werden identifiziert** und nach Schweregrad sortiert
4. Entsprechende Maßnahmen werden **NUR in den Problemzonen** durchgeführt
5. Benachrichtigungen werden an Discord und In-Game gesendet

### Chunk-Einschränkungen (nur in Problemzonen!)

**Redstone-Verwaltung:**
- Redstone-Events werden getrackt pro Chunk
- Bei Überschreitung des Limits wird Redstone **nur in diesem Chunk** temporär deaktiviert
- Automatische Reaktivierung nach konfigurierter Dauer oder TPS-Erholung

**Pflanzenwachstum:**
- BlockGrowEvent und BlockSpreadEvent werden überwacht
- Wachstum wird **nur in Problemzonen** gestoppt
- Konfigurierbare Liste betroffener Pflanzentypen
- Automatische Reaktivierung bei TPS-Erholung

**Mob-Spawning:**
- Natürliches Mob-Spawning wird **nur in Problemzonen** blockiert
- Spawner, Breeding und andere spezielle Spawn-Gründe bleiben unberührt
- Getrennte Konfiguration für passive und hostile Mobs

### Mob-Management (Whitelist/Blacklist)

Du kannst spezifische Mob-Typen konfigurieren:

**Whitelist** - Diese Mobs werden NIEMALS entfernt:
```yaml
mob-management:
  whitelist:
    - "VILLAGER"
    - "ARMOR_STAND"
    - "ITEM_FRAME"
    - "HORSE"
```

**Blacklist** - Diese Mobs werden IMMER sofort entfernt:
```yaml
mob-management:
  blacklist:
    - "CREEPER"
    - "TNT"
    - "PHANTOM"
```

**Brennende Mobs** - Automatisch entfernen:
```yaml
mob-management:
  remove-burning-mobs: true
```

#### Verfügbare Mob-Typen

**Hostile Mobs:**
- `ZOMBIE`, `SKELETON`, `CREEPER`, `SPIDER`, `CAVE_SPIDER`
- `ENDERMAN`, `WITCH`, `BLAZE`, `GHAST`, `SLIME`
- `MAGMA_CUBE`, `SILVERFISH`, `ENDERMITE`, `GUARDIAN`
- `ELDER_GUARDIAN`, `SHULKER`, `PHANTOM`, `DROWNED`
- `HUSK`, `STRAY`, `VEX`, `VINDICATOR`, `EVOKER`
- `PILLAGER`, `RAVAGER`, `HOGLIN`, `ZOGLIN`, `PIGLIN_BRUTE`
- `WITHER_SKELETON`, `ZOMBIE_VILLAGER`, `WARDEN`

**Passive/Neutrale Mobs:**
- `COW`, `PIG`, `SHEEP`, `CHICKEN`, `RABBIT`
- `HORSE`, `DONKEY`, `MULE`, `LLAMA`, `CAT`, `DOG`
- `VILLAGER`, `IRON_GOLEM`, `SNOW_GOLEM`
- `BAT`, `SQUID`, `DOLPHIN`, `TURTLE`, `FOX`
- `PANDA`, `POLAR_BEAR`, `BEE`, `AXOLOTL`, `GOAT`
- `FROG`, `TADPOLE`, `ALLAY`, `SNIFFER`, `CAMEL`

**Spezielle Entities:**
- `ARMOR_STAND`, `ITEM_FRAME`, `GLOW_ITEM_FRAME`
- `PAINTING`, `MINECART`, `BOAT`
- `TNT`, `ENDER_CRYSTAL`, `ENDER_DRAGON`
- `WITHER`, `EXPERIENCE_ORB`

**Vollständige Liste:** [Spigot EntityType JavaDocs](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html)

### Protected Regions
Definiere geschützte Bereiche, die nie bereinigt werden:

```yaml
protected-regions:
  whitelist:
    - type: "world"
      world: "world"
      center-x: 0
      center-z: 0
      radius: 100
      reason: "Spawn-Bereich"
```

## Entwicklung

### Build
```bash
./gradlew build
```

### Projekt-Struktur
```
src/main/java/de/delinkedde/redstoneItemClear/
├── action/          # Ausführung von Maßnahmen
├── analyzer/        # Problem-Analyse
├── command/         # Commands
├── config/          # Konfigurations-Management
├── discord/         # Discord-Integration
├── listener/        # Event-Listener
├── manager/         # Performance-Manager
├── model/           # Datenmodelle
└── monitor/         # TPS-Monitoring
```

## Technische Details

- **Spigot Version:** 1.21.4
- **Java Version:** 21
- **API-Version:** 1.21
- **Build-Tool:** Gradle 8.8

## Web-Dashboard Integration

### ✅ Implementierte Features
- **Live TPS-Monitoring** mit Grafiken (Stunde/Tag/Woche)
- **Multi-Server Dashboard** - Ein Login für alle Server!
- **Problem Zones Visualisierung** mit Koordinaten
- **Sichere Authentifizierung** mit In-Game Verifizierung
- **WebSocket Live-Updates** für Echtzeit-Daten
- **BungeeCord Support** - Zentrale Verifizierung für alle Backend-Server

### 🔐 WebPanel Authentifizierung

**Für Spieler mit `redstoneitemclear.webpanel` Permission:**

1. **Login-Seite öffnen:** `https://delinkedde.de/minecraft-login.html`
2. **Minecraft-Username eingeben** (z.B. `DelinkedDE`)
3. **Verifizierungs-Code erhalten** (z.B. `AB12CD`)
4. **In-Game verifizieren:**
   ```
   /ricpanel verify AB12CD
   ```
   *Hinweis: Bei BungeeCord-Setup den Command `/ricbungee verify AB12CD` verwenden!*
5. **Automatisch zum Dashboard weitergeleitet** → `https://delinkedde.de/minecraft-dashboard.html`

**WebPanel Commands:**

| Command | Beschreibung |
|---------|--------------|
| `/ricpanel verify <code>` | Code vom WebPanel verifizieren |
| `/ricpanel info` | Zeigt Account-Info & Anleitung |
| `/ricpanel help` | Zeigt Hilfe |

### 🌐 Multi-Server Setup (BungeeCord)

**Für BungeeCord Networks:**
1. **BungeeCord Plugin** installieren: `Redstone-ItemClear-Bungee`
2. **Backend-Server** konfigurieren mit `bungee-name`:
   ```yaml
   webpanel:
     bungee-name: "lobby"  # Name aus BungeeCord config.yml
     display-name: ""      # Optional, wird auto-generiert aus bungee-name
   ```
3. **Einmalige Verifizierung** mit `/ricbungee verify <code>` auf BELIEBIGEM Server
4. **Zugriff auf ALLE Server** im Dashboard wo Permission vorhanden ist

**Vorteile:**
- ✅ Ein Login für alle Backend-Server
- ✅ Automatische Server-Erkennung
- ✅ Zentrale Permission-Verwaltung
- ✅ Server-Namen werden automatisch aus BungeeCord übernommen

### 🔜 Geplante Features
- WorldGuard-Integration
- PlotSquared-Integration
- Erweiterte Statistik-Logs
- Export-Funktionen für Reports

## Support

Bei Fragen oder Problemen:
- GitHub Issues: https://github.com/DelinkedDE-de/Redstone-ItemClear
- Discord: DelinkedDE
- Website: https://delinkedde.de

## Lizenz

**Proprietäre Lizenz - Alle Rechte vorbehalten**

Copyright © 2025 delinkedde.de - Alle Rechte vorbehalten.

### Nutzungsbedingungen

Dieses Plugin ist **nicht Open Source** und unterliegt den folgenden Bedingungen:

✅ **Erlaubt:**
- Nutzung auf eigenen/privaten Servern
- Anpassung der Konfiguration für eigene Zwecke

❌ **NICHT erlaubt:**
- Weitergabe, Verbreitung oder Verkauf des Plugins
- Veröffentlichung auf Plugin-Plattformen (SpigotMC, Bukkit, etc.)
- Dekompilierung oder Reverse Engineering
- Verwendung des Codes in eigenen Projekten
- Kommerzielle Nutzung ohne ausdrückliche Genehmigung

**Haftungsausschluss:**
Dieses Plugin wird "wie besehen" bereitgestellt, ohne jegliche Garantie.
Der Autor übernimmt keine Haftung für Schäden die durch die Nutzung entstehen.

Für kommerzielle Lizenzen oder Sondergenehmigungen kontaktieren Sie: https://delinkedde.de

---

**Entwickelt mit Fokus auf Performance und Zuverlässigkeit für SkyBlock-Server**
