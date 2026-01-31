# AttributeCore

**Version**: 1.6.3.0  
**Minecraft**: Paper 1.20+  
**TabooLib**: 6.2.4  
**Architecture**: SX-Attribute 3.x + AttributePlus JS System

---

## Project Overview

AttributeCore is a lightweight Minecraft attribute system plugin built with **Kotlin + TabooLib 6.x**, featuring a **JavaScript custom attribute system** inspired by AttributePlus.

### Core Features

- **JavaScript Custom Attributes** - Users can create custom attributes via `.js` files
- **Elemental Damage & Reaction System** - Genshin Impact-inspired elemental combat system
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

### Elemental Damage Attributes (JavaScript)

Located in `plugins/AttributeCore/attributes/`:

| File | Attribute | Element | Lore Format |
|------|-----------|---------|-------------|
| `fire_damage.js` | 火元素伤害 (Fire Damage) | FIRE | `§c火元素伤害 §f50` |
| `water_damage.js` | 水元素伤害 (Water Damage) | WATER | `§9水元素伤害 §f50` |
| `ice_damage.js` | 冰元素伤害 (Ice Damage) | ICE | `§b冰元素伤害 §f50` |
| `electro_damage.js` | 雷元素伤害 (Electro Damage) | ELECTRO | `§e雷元素伤害 §f50` |
| `wind_damage.js` | 风元素伤害 (Wind Damage) | WIND | `§a风元素伤害 §f50` |

### Elemental Resistance Attributes (JavaScript)

Located in `plugins/AttributeCore/attributes/`:

| File | Attribute | Element | Lore Format | Reduction Formula |
|------|-----------|---------|-------------|-------------------|
| `fire_resistance.js` | 火元素抗性 (Fire Resistance) | N/A | `§c火元素抗性 §f50` | `damage × (1 - resist / (resist + 100))` |
| `water_resistance.js` | 水元素抗性 (Water Resistance) | N/A | `§9水元素抗性 §f50` | `damage × (1 - resist / (resist + 100))` |
| `ice_resistance.js` | 冰元素抗性 (Ice Resistance) | N/A | `§b冰元素抗性 §f50` | `damage × (1 - resist / (resist + 100))` |
| `electro_resistance.js` | 雷元素抗性 (Electro Resistance) | N/A | `§e雷元素抗性 §f50` | `damage × (1 - resist / (resist + 100))` |
| `wind_resistance.js` | 风元素抗性 (Wind Resistance) | N/A | `§a风元素抗性 §f50` | `damage × (1 - resist / (resist + 100))` |
| `physical_resistance.js` | 物理抗性 (Physical Resistance) | N/A | `§f物理抗性 §f50` | `damage × (1 - resist / (resist + 100))` |

**Resistance Example**: 50 fire resistance → `50 / (50 + 100) = 33.3%` damage reduction

---

## Elemental Reaction System

Inspired by Genshin Impact's elemental combat mechanics.

### How It Works

1. **Elemental Aura Application**: When you hit an enemy with elemental damage, an aura is applied to them for 5 seconds.
2. **Elemental Reactions**: When you hit an enemy with a **different** element, a reaction triggers!
3. **Reaction Effects**: Each reaction has unique damage multipliers and visual effects.

### Available Reactions

Located in `plugins/AttributeCore/scripts/`:

| Reaction | Trigger | Effect | Visual |
|----------|---------|--------|--------|
| **蒸发 (Vaporize)** | Fire → Water Aura | 2.0x damage | Steam clouds + fire particles |
| **融化 (Melt)** | Fire → Ice Aura | 2.0x damage | Water droplets + fire particles |
| **超载 (Overloaded)** | Electro → Fire Aura | 1.5x damage + AoE explosion (3 blocks) | Explosion + lightning particles |
| **冻结 (Frozen)** | Ice → Water Aura | 1.2x damage + Slowness IV (3s) | Ice crystals + blue particles |
| **扩散 (Swirl)** | Wind → Any Aura | 1.3x damage + Spread aura to nearby (4 blocks) | Wind spiral + element particles |

### Elemental Combat Example

```
Player attacks zombie with Fire Damage (50)
→ Zombie gets FIRE aura (5 seconds)

Player attacks same zombie with Water Damage (50)
→ Vaporize reaction triggers!
→ Water damage × 2.0 = 100 total damage
→ "§c§l[蒸发] §e触发! 造成 §c2倍 §e伤害!" message appears
→ Steam particle effects play
→ Fire aura is consumed
```

### Creating Custom Reactions

Create a `.js` file in `plugins/AttributeCore/scripts/`:

```javascript
function canTrigger(context) {
    return context.triggerElement === "FIRE" && context.auraElement === "ICE";
}

function execute(context) {
    if (!canTrigger(context)) return;
    
    context.damageMultiplier = 2.0;
    
    if (context.attacker && context.attacker.getType() === "PLAYER") {
        context.attacker.sendMessage("Custom reaction triggered!");
    }
    
    var location = context.victim.getLocation();
    var world = location.getWorld();
    world.spawnParticle("FLAME", location.getX(), location.getY() + 1, location.getZ(), 10);
}
```

**Available Context Properties:**
- `context.attacker` - Attacking LivingEntity
- `context.victim` - Victim LivingEntity
- `context.attackerData` - Attacker's AttributeData
- `context.victimData` - Victim's AttributeData
- `context.damageBucket` - DamageBucket (damage breakdown)
- `context.triggerElement` - Attacking element (FIRE, WATER, ICE, ELECTRO, WIND)
- `context.auraElement` - Existing aura on victim
- `context.damageMultiplier` - Modify this to change damage (default: 1.0)
- `context.cancelled` - Set to true to cancel reaction

---

## Quick Start

### Installation

1. Download `AttributeCore-1.6.0.0.jar`
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

// PlaceholderAPI suffix (optional, defaults to attributeName)
var placeholder = "my_attr";

// Lore pattern matching
var pattern = "我的属性";
var patternSuffix = "%";

// Element type (optional): FIRE, WATER, ICE, ELECTRO, WIND, PHYSICAL (default)
var element = "FIRE";

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
| `attr.getDamage(entity, handle)` | Get total damage |
| `attr.getDamage(entity, element, handle)` | Get damage for specific element |
| `attr.setDamage(entity, value, handle)` | Set damage for this attribute's element |
| `attr.setDamage(entity, element, value, handle)` | Set damage for specific element |
| `attr.addDamage(entity, value, handle)` | Add to this attribute's element bucket |
| `attr.addDamage(entity, element, value, handle)` | Add to specific element bucket |
| `attr.takeDamage(entity, value, handle)` | Subtract from element bucket |
| `attr.takeDamage(entity, element, value, handle)` | Subtract from specific element |
| `attr.addFinalDamage(entity, value, handle)` | Add to final total damage (after all calcs) |
| `attr.takeFinalDamage(entity, value, handle)` | Subtract from final total |
| `attr.setFinalDamage(entity, value, handle)` | Set final total damage |
| `attr.getElement()` | Get this attribute's element name |
| `attr.getElementDisplayName()` | Get element display name (Chinese) |
| `attr.chance(percent)` | Random chance check (0-100) |
| `attr.setCancelled(boolean, handle)` | Cancel the event |
| `attr.heal(entity, amount, handle)` | Heal entity |

**Handle Object (`handle`)**:
| Method | Description |
|--------|-------------|
| `handle.getAttacker()` | Get attacking entity |
| `handle.getEntity()` / `handle.getVictim()` | Get victim entity |
| `handle.getDamage()` | Get total damage (bucket + finalModifier) |
| `handle.getDamage(element)` | Get damage for specific element |
| `handle.setDamage(value)` | Set physical damage |
| `handle.setDamage(element, value)` | Set damage for specific element |
| `handle.addDamage(value)` | Add to physical damage |
| `handle.addDamage(element, value)` | Add to specific element |
| `handle.takeDamage(value)` | Subtract from physical damage |
| `handle.takeDamage(element, value)` | Subtract from specific element |
| `handle.addFinalDamage(value)` | Add modifier applied after all calcs |
| `handle.takeFinalDamage(value)` | Subtract from final modifier |
| `handle.setFinalDamage(value)` | Set total final damage |
| `handle.getDamageBucket()` | Get the DamageBucket object |
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
  - "§c火元素伤害 §f50"
  - "§9水元素伤害 §f50"
  - "§b冰元素伤害 §f50"
  - "§e雷元素伤害 §f50"
  - "§a风元素伤害 §f50"
  - "§c火元素抗性 §f50"
  - "§9水元素抗性 §f50"
  - "§b冰元素抗性 §f50"
  - "§e雷元素抗性 §f50"
  - "§a风元素抗性 §f50"
  - "§f物理抗性 §f50"
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
│   │   ├── SubAttribute.kt           # Attribute base class
│   │   ├── DamageBucket.kt           # Damage breakdown tracker
│   │   ├── Element.kt                # Element enum (FIRE, WATER, ICE, ELECTRO, WIND)
│   │   └── ElementalAura.kt          # Elemental aura manager
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
    ├── attributes/                   # Default JS attributes
    │   ├── lifesteal.js
    │   ├── dodge.js
    │   ├── thorns.js
    │   ├── execute.js
    │   ├── fire_damage.js            # Elemental damage attributes
    │   ├── water_damage.js
    │   ├── ice_damage.js
    │   ├── electro_damage.js
    │   ├── wind_damage.js
    │   ├── fire_resistance.js        # Elemental resistance attributes
    │   ├── water_resistance.js
    │   ├── ice_resistance.js
    │   ├── electro_resistance.js
    │   ├── wind_resistance.js
    │   └── physical_resistance.js
    └── scripts/                      # Elemental reaction scripts
        ├── vaporize.js               # Fire + Water
        ├── melt.js                   # Fire + Ice
        ├── overloaded.js             # Electro + Fire
        ├── frozen.js                 # Ice + Water
        └── swirl.js                  # Wind + Any
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

### v1.6.3.0 (2026-01-31)
- **Complete API System** - Added 4 comprehensive API modules for external plugin integration
- **AttributeCoreAPI** - Main API for entity attributes, item parsing, damage calculation, combat power
  - Entity attribute queries: `getAttribute()`, `getAttributeFinal()`, `getAttributesBatch()`, `getAllNonZeroAttributes()`
  - Plugin attribute management: `setPluginAttribute()`, `addPluginAttribute()`, `removePluginAttribute()`, `clearPluginAllData()`
  - Item parsing: `loadItemData()`, `parseAttributesFromLore()`
  - Damage system: `buildDamageBucket()`, `getResistances()`, `calculateFinalDamage()`
  - Combat power: `getCombatPower()`
- **DamageAPI** - Damage bucket operations for elemental damage system
  - Bucket creation: `createPhysicalBucket()`, `createElementalBucket()`, `createMixedBucket()`
  - Damage operations: `addDamage()`, `setDamage()`, `getDamage()`, `multiplyDamage()`
  - Resistance calculation: `applyResistances()`, `calculateDamageReduction()`, `calculateEffectiveDamage()`
  - Bucket utilities: `mergeBuckets()`, `cloneBucket()`, `hasElement()`, `getElements()`
- **AttributeAPI** - Attribute registry queries and metadata access
  - Attribute listing: `getAll()`, `getAllNames()`, `getByName()`, `getByType()`
  - JS attributes: `getJsAttributes()`, `getJsAttribute()`, `getJsAttributesByElement()`
  - Metadata: `getNbtName()`, `getPlaceholder()`, `getPriority()`, `getCombatPowerWeight()`, `getElement()`
  - Mapping: `getNbtNameMapping()`, `getAttributeNameFromNbt()`, `getAllPlaceholders()`
- **ElementAPI** - Elemental aura management and reaction triggers
  - Aura management: `applyAura()`, `getAura()`, `hasAura()`, `consumeAura()`, `clearAura()`
  - Element queries: `getElement()`, `getElements()`, `getReactiveElements()`
  - Reaction: `triggerReaction()`
- All API methods have `@JvmStatic` annotation for Java interoperability
- Added comprehensive API documentation: `docs/API_USAGE.md`, `docs/DEVELOPER_API.md`

### v1.6.2.0 (2026-01-31)
- **NBT Attribute Format Refactoring** - Changed NBT key format to use Chinese display names
- NBT keys now use `pattern` (e.g., `雷元素伤害`) instead of `attributeName` (e.g., `electro_damage`)
- Percentage attributes detected by `%` suffix in the **value** (e.g., `攻击力: "20%"`)
- Added `nbtName` property to `SubAttribute` base class for NBT key customization
- Updated `ItemAttributeReader` to support new NBT format with automatic name mapping
- **NBT Format Examples**:
  - Flat value: `AttributeCore.雷元素伤害: 50`
  - Percent value: `AttributeCore.攻击力: "20%"` (string with % suffix)
  - Before: `AttributeCore.electro_damage: 50` or `AttributeCore.Percent.attack_damage: 20`

### v1.6.1.0 (2026-01-31)
- **Elemental Resistance Attributes** - Added 6 resistance attributes for damage reduction
- Added resistance attributes: `fire_resistance.js`, `water_resistance.js`, `ice_resistance.js`, `electro_resistance.js`, `wind_resistance.js`, `physical_resistance.js`
- Resistance formula: `damage × (1 - resistance / (resistance + 100))`
- Resistances auto-applied via `DamageBucket.applyResistances()` - no need for `runDefense()` functions
- Updated README with resistance attribute documentation and lore examples

### v1.6.0.0 (2026-01-31)
- **Element-Aware Damage API** - Major API refactoring for elemental damage system
- **New JS Attribute Configuration**:
  - Added `var element = "FIRE"` - Explicit element declaration (FIRE, WATER, ICE, ELECTRO, WIND, PHYSICAL)
  - Added `var placeholder = "name"` - Custom PlaceholderAPI suffix
- **Enhanced Damage Methods** (AttributePlus-style API):
  - `addDamage(entity, element, value, handle)` - Add damage to specific element bucket
  - `takeDamage(entity, element, value, handle)` - Subtract from element bucket
  - `setDamage(entity, element, value, handle)` - Set element bucket value
  - `addFinalDamage(entity, value, handle)` - Add to final total (after all calculations)
  - `takeFinalDamage(entity, value, handle)` - Subtract from final total
  - `setFinalDamage(entity, value, handle)` - Set final total damage
  - `attr.getElement()` / `attr.getElementDisplayName()` - Get element info
- **AttributeHandle Refactoring**:
  - Now stores `DamageBucket` internally instead of single damage value
  - `handle.getDamageBucket()` returns the internal damage bucket
  - `handle.getDamage(element)` gets damage for specific element
  - `handle.setDamageBucket(bucket)` sets bucket from external source
- **DamageBucket Enhancement**:
  - `fromAttributeData()` now uses `JsAttribute.element` configuration first, then fallback to name detection
- Updated all elemental damage JS files with `element` and `placeholder` configuration

### v1.6.0.1 (2026-01-31)
- Removed `Element.fromAttributeName()` and related prefix/Chinese name maps
- Element detection now exclusively uses `JsAttribute.element` configuration
- Removed fallback logic in `DamageBucket.fromAttributeData()` - defaults to PHYSICAL if element not configured

### v1.5.0.0 (2026-01-31)
- **Elemental Damage System** - Added 5 elemental damage attributes (Fire, Water, Ice, Electro, Wind)
- **Elemental Reaction System** - Genshin Impact-inspired reactions with visual effects
- Added 5 elemental damage attributes: `fire_damage.js`, `water_damage.js`, `ice_damage.js`, `electro_damage.js`, `wind_damage.js`
- Added 5 reaction scripts: `vaporize.js` (Fire+Water), `melt.js` (Fire+Ice), `overloaded.js` (Electro+Fire), `frozen.js` (Ice+Water), `swirl.js` (Wind+Any)
- Added `DamageBucket.kt` - Tracks damage breakdown by element
- Added `Element.kt` - Element enum (FIRE, WATER, ICE, ELECTRO, WIND)
- Added `ElementalAura.kt` - Manages elemental auras on entities (5 second duration)
- Elemental reactions trigger when attacking with different element than target's aura
- Each reaction has unique effects: damage multipliers, AoE explosions, status effects, aura spreading
- Updated ScriptManager to auto-release elemental reaction scripts

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
