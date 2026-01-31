# AttributeCore

**Version**: 1.4.0.0  
**Minecraft**: Paper 1.20+  
**TabooLib**: 6.2.4  
**Architecture**: SX-Attribute 3.x + AttributePlus JS System

---

## Project Overview

AttributeCore is a lightweight Minecraft attribute system plugin built with **Kotlin + TabooLib 6.x**, featuring a **JavaScript custom attribute system** inspired by AttributePlus.

### Core Features

- **JavaScript Custom Attributes** - Users can create custom attributes via `.js` files
- **PlaceholderAPI Integration** - Auto-register all attribute placeholders via TabooLib
- **SX-Attribute Compatible Architecture** - Based on SX-Attribute 3.x design
- **Lore Attribute Reading** - Supports color code formats (e.g., `§c攻击力 §f100`)
- **NBT Attribute Reading** - Uses TabooLib ItemTag API
- **Extensible Attribute System** - Easy to extend via `SubAttribute` base class or JavaScript
- **Combat Power Calculation** - Automatic entity combat power calculation
- **Priority Management** - Attribute priority sorting support
- **High Performance** - ConcurrentHashMap caching, async event handling
- **Command System** - `/attrcore reload`, `/attrcore info`, `/attrcore list`

---

## Attribute System

### Core Attributes (Kotlin - Built-in)

| Attribute | Type | Description | Lore Format |
|-----------|------|-------------|-------------|
| **攻击力** (Attack Damage) | Attack | Increases physical damage | `§c攻击力 §f100` |
| **防御力** (Defense) | Defence | Reduces damage taken | `§9防御力 §f50` |
| **暴击率** (Crit Chance) | Attack | Critical hit probability (%) | `§6暴击率 §f25%` |
| **暴击伤害** (Crit Damage) | Other | Critical damage multiplier (%) | `§6暴击伤害 §f150%` |

### JavaScript Attributes (User-defined)

Located in `plugins/AttributeCore/attributes/`:

| File | Attribute | Type | Description |
|------|-----------|------|-------------|
| `lifesteal.js` | 吸血 (Life Steal) | Attack | Heal on damage dealt |
| `dodge.js` | 闪避 (Dodge) | Defence | Chance to avoid damage |
| `thorns.js` | 荆棘 (Thorns) | Defence | Reflect damage to attacker |
| `execute.js` | 处决 (Execute) | Attack | Extra damage on low HP targets |

---

## Quick Start

### Installation

1. Download `AttributeCore-1.4.0.0.jar`
2. Place in server `plugins/` directory
3. Restart server
4. Config files generate in `plugins/AttributeCore/`

### Configuration

```yaml
# config.yml
damage:
  enabled: true
  min-damage: 0.1
  defense-formula:
    base-value: 100

combat-power-weights:
  attack_damage: 1.5
  defense: 1.2
  crit_chance: 0.8
  crit_damage: 0.5
```

---

## JavaScript Custom Attributes

### Creating a Custom Attribute

Create a `.js` file in `plugins/AttributeCore/attributes/`:

```javascript
// priority: Lower = higher priority
var priority = 10;

// Combat power weight
var combatPower = 0.8;

// Unique attribute identifier
var attributeName = "my_attribute";

// Attribute type: Attack, Defence, Update, Runtime, Killer, Custom, Other
var attributeType = "Attack";

// Lore pattern matching
var pattern = "我的属性";
var patternSuffix = "%";

// Called when entity attacks (Attack type)
function runAttack(attr, attacker, entity, handle) {
    var value = attr.getRandomValue(attacker, handle);
    if (value <= 0) return true;
    
    // Your logic here
    var damage = handle.getDamage();
    handle.setDamage(damage * 1.5);
    
    return true;
}

// Only implement the functions you need!
// Placeholders are automatically registered via PlaceholderAPI integration
```

### JavaScript API Reference

**Attribute Object (`attr`)**:
| Method | Description |
|--------|-------------|
| `attr.getRandomValue(entity, handle)` | Get attribute value for entity |
| `attr.getAttributeValue(entity, handle)` | Get [min, max] value array |
| `attr.getDamage(entity, handle)` | Get current damage |
| `attr.setDamage(entity, value, handle)` | Set damage value |
| `attr.addDamage(entity, value, handle)` | Add to damage |
| `attr.takeDamage(entity, value, handle)` | Subtract from damage |
| `attr.chance(percent)` | Random chance check (0-100) |
| `attr.setCancelled(boolean, handle)` | Cancel the event |
| `attr.heal(entity, amount, handle)` | Heal entity |

**Handle Object (`handle`)**:
| Method | Description |
|--------|-------------|
| `handle.getAttacker()` | Get attacking entity |
| `handle.getEntity()` / `handle.getVictim()` | Get victim entity |
| `handle.getDamage()` | Get current damage value |
| `handle.setDamage(value)` | Set damage value |
| `handle.addDamage(value)` | Add to damage |
| `handle.takeDamage(value)` | Subtract from damage |
| `handle.isCancelled()` | Check if event cancelled |
| `handle.setCancelled(boolean)` | Cancel the event |
| `handle.isProjectile()` | Is projectile damage |
| `handle.isSkillDamage()` | Is skill damage |
| `handle.sendMessage(entity, message)` | Send message to player |

---

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/attrcore reload` | `attributecore.reload` | Reload JS attributes |
| `/attrcore info` | `attributecore.info` | View your attributes |
| `/attrcore list` | `attributecore.list` | List all registered attributes |
| `/attrcore help` | `attributecore.admin` | Show help message |

**Aliases**: `/attributecore`, `/ac`

---

## PlaceholderAPI Integration

All attributes are automatically registered as PlaceholderAPI placeholders. No configuration needed!

### Placeholder Format

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%attributecore_<name>%` | Get attribute flat value | `%attributecore_attack_damage%` → `100.00` |
| `%attributecore_<name>_percent%` | Get attribute percent bonus | `%attributecore_defense_percent%` → `20.00` |
| `%attributecore_<name>_final%` | Get final value (flat * (1 + %/100)) | `%attributecore_attack_damage_final%` → `120.00` |
| `%attributecore_combat_power%` | Get total combat power | `%attributecore_combat_power%` → `1500.00` |
| `%attributecore_cp%` | Alias for combat_power | `%attributecore_cp%` → `1500.00` |
| `%attributecore_list%` | List all non-zero attributes | `attack_damage: 100, defense: 50` |

### Examples

```
%attributecore_life_steal%      → 10.00
%attributecore_dodge%           → 15.00
%attributecore_thorns%          → 20.00
%attributecore_execute%         → 30.00
%attributecore_crit_chance%     → 25.00
```

---

## Lore Format Examples

```yaml
lore:
  - "§c攻击力 §f100"
  - "§9防御力 §f50"
  - "§6暴击率 §f25%"
  - "§6暴击伤害 §f150%"
  - "§c吸血 §f10%"
  - "§a闪避 §f15%"
  - "§5荆棘 §f20%"
  - "§4处决 §f30%"
```

---

## Project Structure

```
AttributeCore/
├── src/main/kotlin/com/attributecore/
│   ├── AttributeCore.kt              # Main plugin class
│   ├── data/
│   │   ├── AttributeData.kt          # Attribute data container
│   │   ├── AttributeType.kt          # Attribute type enum
│   │   └── SubAttribute.kt           # Attribute base class
│   ├── manager/
│   │   ├── AttributeManager.kt       # Attribute manager
│   │   └── ItemAttributeReader.kt    # Item attribute reader
│   ├── command/
│   │   └── AttributeCoreCommand.kt   # Plugin commands
│   ├── hook/
│   │   └── PlaceholderHook.kt        # PlaceholderAPI integration
│   ├── listener/
│   │   ├── DamageListener.kt         # Damage event listener
│   │   └── EquipmentListener.kt      # Equipment change listener
│   ├── attribute/                    # Core Kotlin attributes
│   │   ├── AttackDamage.kt
│   │   ├── Defense.kt
│   │   ├── CritChance.kt
│   │   └── CritDamage.kt
│   └── script/                       # JavaScript attribute system
│       ├── AttributeHandle.kt        # Combat context object
│       ├── JsAttribute.kt            # JS attribute wrapper
│       ├── JsAttributeLoader.kt      # JS attribute loader
│       ├── ScriptManager.kt          # Script manager
│       └── ScriptAPI.kt              # Script API
└── src/main/resources/
    ├── config.yml                    # Main config
    └── attributes/                   # Default JS attributes
        ├── lifesteal.js
        ├── dodge.js
        ├── thorns.js
        └── execute.js
```

---

## Tech Stack

- **Language**: Kotlin 2.2.0
- **Framework**: TabooLib 6.2.4
- **Platform**: Paper 1.20+
- **Build Tool**: Gradle 8.14.3
- **Script Engine**: Nashorn (TabooLib common5)

---

## Changelog

### v1.4.0.0 (2026-01-31)
- **PlaceholderAPI Integration** - Auto-register all attribute placeholders via TabooLib
- Added `hook/PlaceholderHook.kt` implementing TabooLib `PlaceholderExpansion`
- Simplified JS attributes - removed placeholder functions (auto-registered now)
- Placeholder formats: `%attributecore_<attr>%`, `%attributecore_<attr>_percent%`, `%attributecore_<attr>_final%`
- Added `%attributecore_combat_power%` and `%attributecore_list%` placeholders

### v1.3.1.0 (2026-01-31)
- **JS Attribute Integration in DamageListener** - JS attributes now trigger during combat
- Added `/attrcore reload` command to reload JS attributes
- Added `/attrcore info` command to view player attributes
- Added `/attrcore list` command to list all attributes
- Added Entity Death event listener for Killer type JS attributes
- Added Projectile damage support (arrows, tridents, etc.)
- Added `KillerEventData` for death events

### v1.3.0.0 (2026-01-31)
- **JavaScript Custom Attribute System** - Users can create attributes via JS files
- Removed `AttributeRegistry.kt` (auto priority registration)
- Moved LifeSteal, DodgeChance, Thorns, ExecuteThreshold to JavaScript
- Added `script/AttributeHandle.kt` - Combat context for JS
- Added `script/JsAttribute.kt` - JS attribute wrapper
- Added `script/JsAttributeLoader.kt` - JS attribute loader
- Added AttributeType: Runtime, Killer, Custom
- Simplified SubAttribute.register() logic

### v1.2.1.0 (2026-01-30)
- Added example scripts and auto attribute-priority registration
- Added new attributes: LifeSteal, DodgeChance, Thorns, ExecuteThreshold

### v1.2.0.1 (2026-01-30)
- Added elemental reaction system with damage bucket, aura, script API

### v1.1.0.0 (2026-01-30)
- Added configurable debug logging system

### v1.0.1.1 (2026-01-30)
- Fixed regex pattern not matching colon+space format

### v1.0.1.0 (2026-01-30)
- Fixed Lore parsing and equipment loading bugs
- Added auto config file release

### v1.0.0.0 (2026-01-30)
- Initial release based on SX-Attribute 3.x architecture
- Core attributes: Attack Damage, Defense, Crit Chance, Crit Damage
- Lore and NBT attribute reading
- Combat power calculation system

---

## Credits

- **SX-Attribute** - Architecture design reference
- **AttributePlus** - JavaScript attribute system inspiration
- **TabooLib** - Powerful Minecraft plugin framework

---

## License

MIT License

---

**Made with Kotlin + TabooLib**
