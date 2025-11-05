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

1. Download der `.jar`-Datei aus `build/libs/Redstone-ItemClear-1.0-SNAPSHOT.jar`
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
| `/ric reload` | Lädt die Konfiguration neu | `redstoneitemclear.reload` |
| `/ric info` | Plugin-Informationen | - |
| `/ric help` | Zeigt die Hilfe | - |

**Aliases:** `/redstoneitemclear`, `/riclear`

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
| `redstoneitemclear.reload` | Reload-Command | OP |
| `redstoneitemclear.notifications` | In-Game-Benachrichtigungen | OP |
| `redstoneitemclear.lagtest` | Lag-Test Commands (Development) | OP |

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

## Geplante Features

- Web-Dashboard für Statistiken
- WorldGuard-Integration
- PlotSquared-Integration
- Erweiterte Statistik-Logs
- Grafische Visualisierung von Problemzonen

## Support

Bei Fragen oder Problemen:
- GitHub Issues: https://github.com/DelinkedDE-de/Redstone-ItemClear
- Discord: DelinkedDE
- Website: https://delinkedde.de

## Lizenz

**Proprietäre Lizenz - Alle Rechte vorbehalten**

Copyright © 2024 delinkedde.de - Alle Rechte vorbehalten.

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
