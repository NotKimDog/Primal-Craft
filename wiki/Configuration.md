# Configuration Guide

Learn how to customize KimDog SMP for your playstyle.

## 🎛️ Configuration Options

### Current Configuration

KimDog SMP is designed to work out-of-the-box with sensible defaults. Currently, configuration is limited, but customization can be done through:

1. **HUD Customization**
2. **Server Properties**
3. **Datapack Modifications**
4. **Gamerules**

---

## 🖥️ HUD Configuration

### Toggling HUD Elements

**Press `H` to access HUD menu:**

1. **Stamina Bar**
   - Toggle visibility
   - Adjust position
   - Resize (0.5x - 2.0x)
   - Change opacity

2. **Thirst Bar**
   - Same options as stamina
   - Independent positioning
   - Custom colors (future)

3. **Temperature Display**
   - Show/hide temperature
   - Position adjustment
   - Scale customization

4. **Status Effects**
   - Show/hide active effects
   - Reposition indicators
   - Size adjustment

### HUD Positioning

**Reposition:**
- Click and drag HUD element
- Snap to grid enabled
- Save custom layout automatically

**Scaling:**
- Scroll to resize
- Individual element sizing
- Range: 0.5x to 2.0x

**Opacity:**
- 0% = Invisible
- 100% = Full opacity
- Useful for screenshots

---

## 🎮 Server Configuration

### server.properties

**KimDog SMP Recommended Settings:**

```properties
# Difficulty (KimDog benefits from Hard)
difficulty=3

# PvP (for combat features)
pvp=true

# World Generation
generate-structures=true
allow-nether=true
allow-the-end=true

# Performance
simulation-distance=10
view-distance=16
```

### Gamerules

**Recommended Gamerules:**

```
/gamerule doMobLoot true        # Enable mob drops
/gamerule doMobSpawning true    # Enable mobs
/gamerule doDaylight true       # Day/night cycle
/gamerule commandFeedback true  # See command results
/gamerule showDeathMessages true # Death announcements
```

### Difficulty Modifiers

**Peaceful:** No stamina/thirst drain (easy)
**Easy:** Reduced stamina/thirst drain
**Normal:** Standard gameplay
**Hard:** Increased stamina/thirst drain (recommended)

---

## 📦 Datapack Configuration

### Using Datapacks

Datapacks modify:
- Recipes
- Loot tables
- World generation
- Advancement

**Installing Datapacks:**
1. Create `world/datapacks/` folder
2. Add datapack ZIP
3. Run `/reload`
4. Datapack active!

### Custom Recipes

**Modify Recipes:**
1. Create `data/tutorialmod/recipe/`
2. Add JSON recipe files
3. Run `/reload`
4. Test recipes

**Recipe Format:**
```json
{
  "type": "crafting_shapeless",
  "ingredients": [
    {"item": "minecraft:cobblestone"},
    {"item": "tutorialmod:pink_garnet"}
  ],
  "result": {
    "item": "tutorialmod:pink_garnet_block",
    "count": 1
  }
}
```

### Custom Loot Tables

**Modify Loot:**
1. Create `data/minecraft/loot_table/`
2. Add JSON loot files
3. Run `/reload`
4. Apply to chest/mob

---

## ⚙️ Performance Tuning

### Reduce Lag

**1. Lower Render Distance**
- Default: 16
- Reduce to: 10-12
- Improves FPS significantly

**2. Lower Simulation Distance**
- Default: 10
- Reduce to: 6-8
- Reduces entity updates

**3. Disable Particles**
- Wind particles
- Weather effects
- Fall damage effects

**4. Reduce Tick Speed** (server)
```
/gamerule randomTickSpeed 1  # Less plant growth
/gamerule mobGriefing false  # Prevent destruction
```

### Allocate More RAM

**Launcher Settings:**
- Find JVM Arguments
- Change `-Xmx1G` to `-Xmx2G` or more
- Requires 64-bit Java

**Server:**
```bash
java -Xmx4G -Xms4G -jar server.jar
```

---

## 🎨 Visual Customization

### HUD Colors

**Currently:**
- Green for stamina
- Blue for thirst
- Red for temperature

**Coming Soon:**
- Custom color schemes
- Theme options
- Dark/light modes

### Shader Compatibility

**Compatible Shaders:**
- Complementary
- SEUS Renewed
- Sonic Ether's
- Most major shaders

**Known Issues:**
- Some shaders may conflict with custom rendering
- Report issues for support

---

## 🌍 World Customization

### World Generation Settings

**Structure Generation:**
```
/gamerule doStructures true/false
```

**Biome Distribution:**
- Vanilla biomes used
- Enhanced temperature values
- Custom ore distribution

**Seed Reproduction:**
```properties
level-seed=kimdogsmp
```
Use same seed on different server for identical world.

### Custom Biomes

**Coming Soon:**
- Entirely custom biomes
- Unique terrain features
- Custom mobs/structures

---

## 🔧 Advanced Configuration

### Modifying System Values

**Stamina Settings** (future):
```json
{
  "stamina": {
    "max": 100,
    "drain_rate": 2,
    "recovery_rate": 1,
    "sprint_cooldown": 3
  }
}
```

**Thirst Settings** (future):
```json
{
  "thirst": {
    "max": 100,
    "drain_rate": 1,
    "hot_drain_multiplier": 2,
    "cold_drain_multiplier": 0.5
  }
}
```

**Temperature Settings** (future):
```json
{
  "temperature": {
    "freezing_damage": true,
    "heat_damage": true,
    "armor_bonus": 5
  }
}
```

---

## 📝 Config File Structure

**Coming Soon - Full Config System:**

```
KimDog-SMP Config Structure:
world/kimdogsmp/
├── recipes/          # Custom recipes
├── loot_tables/      # Custom loot
├── structures/       # Custom structures
└── config.json       # Main configuration
```

---

## 🔄 Resetting Configuration

### Reset to Defaults

**HUD:**
1. Press `H`
2. Select "Reset to Default"
3. Confirm

**Server:**
1. Delete `server.properties`
2. Restart server
3. New default config generated

**World:**
1. Backup world
2. Delete world
3. Create new world
4. Settings reset to default

---

## 🎛️ Future Configuration

### Planned Features

- **Toggle Individual Systems**
  - Enable/disable stamina
  - Enable/disable thirst
  - Enable/disable temperature

- **Difficulty Modifiers**
  - Stamina drain rate
  - Thirst increase rate
  - Temperature sensitivity
  - Damage multipliers

- **Performance Options**
  - Particle effect quality
  - HUD update frequency
  - Network sync rate

- **Gameplay Options**
  - Hardcore mode
  - Permadeath mode
  - Speedrun mode
  - Creative-friendly mode

### Configuration Priority List

1. ✅ System toggles
2. 🔄 Difficulty modifiers
3. 📊 Balance tweaking
4. 🎨 Visual options
5. ⚡ Performance settings

---

## 📊 Performance Impact

### Configuration Changes Impact

| Change | FPS Impact | Performance |
|--------|-----------|-------------|
| Render Distance ↓ 8 | +20-30% | Better |
| Particles Disabled | +5-10% | Better |
| Simulation Distance ↓ 6 | +10-15% | Better |
| More Players | -5% per | Worse |
| Mods Added | -5-10% | Varies |

---

## 🆘 Configuration Issues

### Config Not Applied

**Cause:** Server not reloaded

**Solution:**
```
/reload
```

### Settings Reset After Restart

**Cause:** Config file overwritten

**Solution:**
1. Make backup of config
2. Check file permissions
3. Use read-only flag if desired

### Conflicts Between Settings

**Example:** Peaceful mode + Stamina system

**Solution:**
- Peaceful disables stamina drain
- Survival/Hard mode enables it
- Change difficulty to apply

---

## 📚 Configuration Resources

- **Gamerule Reference:** [Minecraft Wiki](https://minecraft.wiki)
- **JSON Formatting:** [JSON.org](https://www.json.org)
- **Datapack Guide:** See Minecraft Wiki

---

**Ready to customize?** Start with [HUD Configuration](#hud-configuration) section.

See [FAQ](FAQ) for common configuration questions.
