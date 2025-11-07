# Config-Beispiele & Erklärungen

## Wichtige Klarstellungen

### Items vs. Itemstacks

**WICHTIG:** In der Config werden **Item-Entities** gezählt, NICHT Itemstacks!

```yaml
items-per-chunk: 100
```

**Was bedeutet das?**
- **Item-Entity** = Ein einzelnes Item-Objekt auf dem Boden
- **Itemstack** = Die Anzahl der Items in einem Stack (z.B. 64 Wheat)

**Beispiele:**
- 64 Wheat die als EIN Stack auf dem Boden liegen = **1 Item-Entity**
- 64 einzelne Wheat verteilt = **64 Item-Entities**
- Auto-Farm mit vielen einzelnen Items = schnell **100+ Item-Entities**

**Warum ist das wichtig?**
- Minecraft merged Items automatisch wenn sie nah beieinander sind
- Auto-Farmen erzeugen oft viele einzelne Item-Entities
- 100 Item-Entities können 100-6400 tatsächliche Items sein!

---

## Pflanzen-Typen (Plant Growth)

### Alle verfügbaren Material-Namen für `affected-types`:

#### Farm-Crops (Standard)
```yaml
affected-types:
  - WHEAT
  - CARROTS
  - POTATOES
  - BEETROOTS
```

#### Kürbis & Melone
```yaml
affected-types:
  - MELON_STEM
  - PUMPKIN_STEM
  - ATTACHED_MELON_STEM
  - ATTACHED_PUMPKIN_STEM
```

#### Schnellwachsende Pflanzen (Performance-intensiv!)
```yaml
affected-types:
  - SUGAR_CANE      # Sehr schnelles Wachstum
  - CACTUS          # Sehr schnelles Wachstum
  - BAMBOO          # EXTREM schnelles Wachstum!
  - BAMBOO_SAPLING
  - KELP            # Schnelles Wachstum im Wasser
  - KELP_PLANT
```

#### Beeren & Ranken
```yaml
affected-types:
  - SWEET_BERRY_BUSH
  - CAVE_VINES
  - CAVE_VINES_PLANT
  - TWISTING_VINES
  - TWISTING_VINES_PLANT
  - WEEPING_VINES
  - WEEPING_VINES_PLANT
```

#### Nether-Pflanzen
```yaml
affected-types:
  - NETHER_WART
  - WARPED_FUNGUS
  - CRIMSON_FUNGUS
  - WARPED_ROOTS
  - CRIMSON_ROOTS
```

#### Bäume (Setzlinge)
```yaml
affected-types:
  - OAK_SAPLING
  - BIRCH_SAPLING
  - SPRUCE_SAPLING
  - JUNGLE_SAPLING
  - ACACIA_SAPLING
  - DARK_OAK_SAPLING
  - CHERRY_SAPLING
  - MANGROVE_PROPAGULE
```

#### 1.20+ Neue Pflanzen
```yaml
affected-types:
  - PITCHER_CROP
  - TORCHFLOWER_CROP
```

#### Sonstiges
```yaml
affected-types:
  - COCOA              # Kakao an Bäumen
  - CHORUS_FLOWER      # End-Pflanzen
  - CHORUS_PLANT
  - GLOW_LICHEN        # Leuchtendes Moos
  - MOSS_BLOCK         # Moos-Ausbreitung
  - BROWN_MUSHROOM     # Pilze
  - RED_MUSHROOM
```

---

## Redstone-Komponenten

### Alle verfügbaren Material-Namen für `block-types`:

#### Grund-Komponenten
```yaml
block-types:
  - REDSTONE_WIRE          # Redstone Staub
  - REDSTONE_TORCH
  - REDSTONE_WALL_TORCH
  - REDSTONE_BLOCK
```

#### Logik-Komponenten
```yaml
block-types:
  - REPEATER              # Verstärker
  - COMPARATOR            # Komparator
  - REDSTONE_LAMP         # Lampe
  - DAYLIGHT_DETECTOR     # Tageslicht-Sensor
```

#### Bewegung (Performance-intensiv!)
```yaml
block-types:
  - PISTON                # Standard-Kolben
  - STICKY_PISTON         # Klebriger Kolben
  - SLIME_BLOCK           # Für Slime-Maschinen
  - HONEY_BLOCK           # Für Honey-Maschinen
```

#### Container/Transport (SEHR Performance-intensiv!)
```yaml
block-types:
  - HOPPER                # ⚠️ GRÖSSTES Performance-Problem!
  - DROPPER
  - DISPENSER
  - CHEST                 # Für Redstone-Signale
  - TRAPPED_CHEST
  - BARREL
```

#### Sensoren/Observer (Erzeugen viele Events!)
```yaml
block-types:
  - OBSERVER              # ⚠️ Kann SEHR viele Events erzeugen!
  - LECTERN
  - TARGET
  - SCULK_SENSOR
  - CALIBRATED_SCULK_SENSOR
```

#### Türen & Tore
```yaml
block-types:
  - IRON_DOOR
  - IRON_TRAPDOOR
  # Alle Holz-Varianten:
  - OAK_DOOR
  - SPRUCE_DOOR
  - BIRCH_DOOR
  - JUNGLE_DOOR
  - ACACIA_DOOR
  - DARK_OAK_DOOR
  - CHERRY_DOOR
  - MANGROVE_DOOR
  # Trapdoors:
  - OAK_TRAPDOOR
  # ... alle anderen _TRAPDOOR
  # Fence Gates:
  - OAK_FENCE_GATE
  # ... alle anderen _FENCE_GATE
```

#### Schienen
```yaml
block-types:
  - POWERED_RAIL          # Beschleunigungs-Schiene
  - ACTIVATOR_RAIL        # Aktivierungs-Schiene
  - DETECTOR_RAIL         # Detektor-Schiene
```

#### Sonstiges
```yaml
block-types:
  - NOTE_BLOCK
  - LEVER
  - STONE_BUTTON
  # Alle Button-Typen (_BUTTON)
  - STONE_PRESSURE_PLATE
  # Alle Pressure-Plate-Typen (_PRESSURE_PLATE)
  - TRIPWIRE
  - TRIPWIRE_HOOK
  - TNT
```

---

## Regionen hinzufügen

### Whitelist (Geschützte Regionen)

**Format:**
```yaml
protected-regions:
  whitelist:
    - type: "world"
      world: "weltname"
      center-x: 0
      center-z: 0
      radius: 100
      reason: "Beschreibung"
```

**Mehrere Regionen hinzufügen:**
```yaml
protected-regions:
  whitelist:
    # Region 1: Spawn
    - type: "world"
      world: "world"
      center-x: 0
      center-z: 0
      radius: 100
      reason: "Spawn-Bereich"

    # Region 2: Spieler-Stadt bei X=1000, Z=2000
    - type: "world"
      world: "world"
      center-x: 1000
      center-z: 2000
      radius: 150
      reason: "Hauptstadt der Spieler"

    # Region 3: Event-Arena
    - type: "world"
      world: "world"
      center-x: -500
      center-z: -500
      radius: 75
      reason: "PvP-Arena"

    # Region 4: Nether-Hub
    - type: "world"
      world: "world_nether"
      center-x: 0
      center-z: 0
      radius: 64
      reason: "Nether-Hub"
```

**Wichtig:**
- `world` = Exakter Weltname (wie in server.properties)
- `center-x` / `center-z` = Mittelpunkt der Region
- `radius` = Radius in Blöcken (Kreis!)
- `reason` = Optional, nur für Logs

---

### Custom Regions (Spezielle Grenzwerte)

**Format:**
```yaml
protected-regions:
  custom-regions:
    - name: "Eindeutiger Name"
      world: "weltname"
      center-x: 1000
      center-z: 1000
      radius: 50
      custom-thresholds:
        entities-per-chunk: 100
        mobs-per-chunk: 50
        items-per-chunk: 200
        redstone-activity: 40
```

**Beispiele:**

```yaml
protected-regions:
  custom-regions:
    # Farm-Zone mit höheren Limits
    - name: "Farm-Zone Nord"
      world: "world"
      center-x: 1000
      center-z: 1000
      radius: 50
      custom-thresholds:
        items-per-chunk: 200      # Doppelt so viele Items
        mobs-per-chunk: 50
        redstone-activity: 40

    # Mob-Grinder
    - name: "Mob-Farm Süd"
      world: "world"
      center-x: -2000
      center-z: 3000
      radius: 30
      custom-thresholds:
        mobs-per-chunk: 100       # Viele Mobs erlaubt
        items-per-chunk: 300      # Viele Drops erlaubt

    # Redstone-Testgebiet
    - name: "Redstone-Labor"
      world: "world"
      center-x: 500
      center-z: -500
      radius: 40
      custom-thresholds:
        redstone-activity: 100    # Sehr viel Redstone
        entities-per-chunk: 150
```

**Was kann man konfigurieren?**
- `entities-per-chunk` = Alle Entities (Mobs, Items, Minecarts, etc.)
- `mobs-per-chunk` = Nur lebende Kreaturen
- `items-per-chunk` = Nur Items auf dem Boden
- `redstone-activity` = Redstone-Events pro Sekunde

**Wichtig:**
- Custom-Regionen überschreiben NUR die angegebenen Werte
- Nicht angegebene Werte nutzen die globalen Einstellungen
- Custom-Regionen werden trotzdem bereinigt wenn TPS zu niedrig sind
- Für komplett geschützte Bereiche → Whitelist nutzen!

---

## WebPanel Konfiguration

### Standalone Server (Ohne BungeeCord)

```yaml
webpanel:
  api-url: "https://delinkedde.de/api/minecraft"
  hostname: "play.delinkedde.de"          # Server-IP/Hostname
  display-name: "Mein SkyBlock Server"    # Name im Dashboard
  bungee-name: ""                         # Leer bei Standalone
  api-key: ""                             # Auto-generiert
  server-id: ""                           # Auto-generiert
```

---

### BungeeCord Multi-Server Setup

**Wichtig:** Der `bungee-name` MUSS mit dem Server-Namen in der BungeeCord `config.yml` übereinstimmen!

**Lobby Server:**
```yaml
webpanel:
  api-url: "https://delinkedde.de/api/minecraft"
  hostname: "play.delinkedde.de"
  display-name: ""           # Leer = "Lobby" (auto-generiert aus bungee-name)
  bungee-name: "lobby"       # ← Name aus BungeeCord config.yml
  api-key: ""                # Auto-generiert
  server-id: ""              # Auto-generiert
```

**Survival Server:**
```yaml
webpanel:
  api-url: "https://delinkedde.de/api/minecraft"
  hostname: "play.delinkedde.de"
  display-name: ""           # Leer = "Survival" (auto-generiert)
  bungee-name: "survival"    # ← Name aus BungeeCord config.yml
  api-key: ""                # Auto-generiert
  server-id: ""              # Auto-generiert
```

**Creative Server:**
```yaml
webpanel:
  api-url: "https://delinkedde.de/api/minecraft"
  hostname: "play.delinkedde.de"
  display-name: "Kreativ-Welt"  # Optional: Custom Name
  bungee-name: "creative"        # ← Name aus BungeeCord config.yml
  api-key: ""                    # Auto-generiert
  server-id: ""                  # Auto-generiert
```

**Beispiel BungeeCord config.yml:**
```yaml
servers:
  lobby:
    address: localhost:25566

  survival:
    address: localhost:25567

  creative:
    address: localhost:25568
```

---

### Display Name Generation

**Automatische Generation:** Wenn `display-name` leer ist, wird der Name automatisch aus `bungee-name` generiert:

| bungee-name | display-name (auto) |
|-------------|---------------------|
| `lobby` | `Lobby` |
| `survival` | `Survival` |
| `creative` | `Creative` |
| `skyblock` | `Skyblock` |
| `minigames` | `Minigames` |

**Custom Display Name:** Du kannst auch einen eigenen Namen setzen:

```yaml
webpanel:
  bungee-name: "skyblock"           # BungeeCord Server-Name
  display-name: "SkyBlock Deluxe"   # Custom Name im Dashboard
```

---

### Permissions für WebPanel

**Permission vergeben:**
```
/lp user DelinkedDE permission set redstoneitemclear.webpanel true
```

**Gruppen-Permission (für alle Admins):**
```
/lp group admin permission set redstoneitemclear.webpanel true
```

**Permission prüfen:**
```
/lp user DelinkedDE permission check redstoneitemclear.webpanel
```

---

### WebPanel Commands

| Command | Beschreibung |
|---------|--------------|
| `/ricpanel verify <code>` | Code vom WebPanel verifizieren (Standalone) |
| `/ricbungee verify <code>` | Code vom WebPanel verifizieren (BungeeCord) |
| `/ricpanel info` | Zeigt Account-Info & Anleitung |
| `/ricpanel help` | Zeigt Hilfe |

---

### WebPanel Workflow

**Standalone Server:**
1. Gehe zu: `https://delinkedde.de/minecraft-login.html`
2. Username eingeben (z.B. `DelinkedDE`)
3. Code erhalten (z.B. `AB12CD`)
4. In-Game: `/ricpanel verify AB12CD`
5. Dashboard öffnet sich automatisch

**BungeeCord Network:**
1. Gehe zu: `https://delinkedde.de/minecraft-login.html`
2. Username eingeben (z.B. `DelinkedDE`)
3. Code erhalten (z.B. `AB12CD`)
4. Auf BELIEBIGEM Server: `/ricbungee verify AB12CD`
5. Dashboard zeigt ALLE Server wo Permission vorhanden ist

---

## Koordinaten finden

### In-Game Koordinaten ablesen:
1. Drücke **F3** (Debug-Screen)
2. Schaue auf **XYZ** (Block-Position)
3. Nutze **X** und **Z** für die Config (Y wird ignoriert)

### Beispiel:
```
Position: 1234.5 / 64.0 / 5678.9
         └─ X ─┘       └─ Z ─┘

Config:
center-x: 1234
center-z: 5678
```

---

## Manuelle Bereinigung & Einschränkungen

### `/ric run` Command

Das `/ric run` Command führt manuelle Bereinigungen durch und scannt:
1. **Problemzonen** - Chunks die von der automatischen Analyse als problematisch erkannt wurden
2. **Spieler-Chunks** - Alle geladenen Chunks im 8-Chunk Radius um ALLE Online-Spieler

**Wichtig:**
- Bereinigt ALLE gefundenen Chunks, unabhängig von Thresholds
- Entity-Removal läuft auf Main-Thread für maximale Stabilität
- Scannt mehr Chunks als automatische Analyse
- Ideal für manuelle Lag-Behebung

**Beispiele:**
```bash
/ric run items          # Nur Items entfernen
/ric run mobs           # Nur Mobs entfernen
/ric run redstone       # Nur Redstone deaktivieren
/ric run plant          # Nur Pflanzenwachstum stoppen
/ric run mobs,items     # Mehrere Aktionen
/ric run all            # Alles bereinigen
```

**Ausgabe:**
```
Scanne 150 Chunks (25 Problemzonen + 125 Spieler-Chunks)...
Entferne Mobs aus 87 Chunks...
Mobs entfernt: 2459
Chunks gescannt: 150
Aktuelle TPS: 19.45
```

### `/ric enable` Command

Reaktiviert temporär deaktivierte Chunk-Einschränkungen.

**Wichtig:**
- Einschränkungen sind nur im **RAM** gespeichert
- Nach **Server-Restart** sind automatisch ALLE Einschränkungen aufgehoben
- Zwei Bereiche: **Spieler-Nähe** (8-Chunk Radius) oder **Server-weit** (mit `all`)

**Beispiele:**
```bash
/ric enable redstone            # Redstone in Spieler-Nähe aktivieren
/ric enable redstone all        # Redstone server-weit aktivieren
/ric enable plant              # Pflanzenwachstum in Spieler-Nähe aktivieren
/ric enable mobs               # Mob-Spawning in Spieler-Nähe aktivieren
/ric enable all                # Alle Einschränkungen in Spieler-Nähe aufheben
/ric enable all all            # Alle Einschränkungen server-weit aufheben
```

**Ausgabe:**
```
Redstone in 12 Chunks wurde reaktiviert
Redstone in 0 Chunks war bereits aktiv
Gesamt: 12 Chunks untersucht
```

**Workflow:**
1. Server laggt → Automatische Analyse deaktiviert Redstone/Plant Growth in Problemzonen
2. Admin behebt Problem (z.B. entfernt Lag-Farm)
3. Admin aktiviert Einschränkungen wieder: `/ric enable all all`

### Chunk-Einschränkungen Persistenz

| Ereignis | Status |
|----------|--------|
| Plugin Reload (`/ric reload`) | ✅ Einschränkungen bleiben aktiv |
| Server Restart | ❌ Einschränkungen werden aufgehoben |
| TPS-Recovery (Auto) | ✅ Einschränkungen werden automatisch aufgehoben |
| Manuell (`/ric enable`) | ✅ Einschränkungen werden sofort aufgehoben |

**Warum kein permanenter Storage?**
- Einschränkungen sind als **temporäre Notfallmaßnahme** gedacht
- Würde bei jedem Restart falsch-positive Chunks blockieren
- Performance-Probleme sollten gelöst werden, nicht permanent blockiert

---

## Empfohlene Einstellungen

### Für Standard SkyBlock Server:
```yaml
analysis:
  thresholds:
    entities-per-chunk: 50
    mobs-per-chunk: 30
    items-per-chunk: 100
    redstone-activity: 20
```

### Für Server mit vielen Farmen:
```yaml
analysis:
  thresholds:
    entities-per-chunk: 75
    mobs-per-chunk: 40
    items-per-chunk: 150
    redstone-activity: 30
```

### Für kleine/schwache Server:
```yaml
analysis:
  thresholds:
    entities-per-chunk: 30
    mobs-per-chunk: 20
    items-per-chunk: 50
    redstone-activity: 10
```
