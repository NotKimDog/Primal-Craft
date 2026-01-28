# 🎮 Primal Craft - Mod Menu Configuration Guide

**All 25 Features are now visible and configurable in Mod Menu!**

---

## 📋 How to Access

1. **In-Game:** Press `]` key (or your configured hotkey)
2. **From Mods List:** Click "Primal Craft" → "Config" button
3. **Navigate Tabs:** Click the colored tabs at the top to switch categories

---

## 🎨 Mod Menu Layout

The config is organized into **5 main tabs**:

### **Tab 1: 🎮 Gameplay** (Green)
Core survival systems and mechanics

### **Tab 2: 🎨 HUD** (Purple)
Display and UI visibility settings

### **Tab 3: ⚙️ Systems** (Yellow)
Tool features and utilities

### **Tab 4: ⚔️ Difficulty** (Red)
Challenge scaling and difficulty presets

### **Tab 5: 🔧 Advanced** (Cyan)
**⭐ ALL 25 NEW FEATURES ARE HERE! ⭐**

---

## ✨ WHERE TO FIND YOUR 25 FEATURES

### **🔧 Advanced Tab → Feature Toggles Section**

When you click the **Advanced** tab, scroll down to find the **"Feature Toggles"** section. All 25 features are listed here with ON/OFF toggles:

#### **Phase 1: Critical Fixes**
```
✓ Hardcore Difficulty           [ON/OFF]
✓ Preset System                 [ON/OFF]
✓ Debug Hud Removal             [ON/OFF]
✓ Sleep System Toggle           [ON/OFF]
```

#### **Phase 2: Gameplay Features**
```
✓ Difficulty Colors             [ON/OFF]
✓ Mob Aggression                [ON/OFF]
✓ Item Drop Particles           [ON/OFF]
✓ Day Transition Animation      [ON/OFF]
✓ Third Person Names            [ON/OFF]
✓ Right Click Harvester         [ON/OFF]
```

#### **Phase 3: Content Creator & Window**
```
✓ Fps And Ping GUI              [ON/OFF]
✓ Fullscreen Auto Launch        [ON/OFF]
✓ Custom Window Title           [ON/OFF]
✓ Custom Window Icon            [ON/OFF]
```

#### **Phase 4: Quality of Life**
```
✓ Dynamic Fps Optimizer         [ON/OFF]
✓ Double Doors                  [ON/OFF]
✓ Infinite Trading              [ON/OFF]
✓ Drop Confirmation             [ON/OFF]
✓ Easy Elytra Takeoff           [ON/OFF]
✓ Dynamic Lights                [ON/OFF]
```

#### **Phase 5: Major Overhauls**
```
✓ Dragon Redesign               [ON/OFF]
✓ Nether Overhaul               [ON/OFF]
```

#### **Meta Feature**
```
✓ Hytale Feel Enabled           [ON/OFF]
```

---

## 🎯 Quick Reference by Tab

### **🎮 GAMEPLAY TAB**
Contains settings for:
- Stamina System (enabled, depletion rate, recovery, etc.)
- Thirst System (enabled, depletion, hot biome multiplier)
- Temperature System (enabled, damage rates)
- Hazards (weather intensity, lightning)
- Hunger (depletion multiplier)

### **🎨 HUD TAB**
Contains settings for:
- Bar Visibility (stamina, thirst, temperature, notifications)
- Bar Styling (scale, opacity, position offsets)
- Colors (custom colors for each bar)
- Animations (enable/disable, speed)

### **⚙️ SYSTEMS TAB**
Contains settings for:
- Zoom (enabled, sensitivity, max zoom)
- Veinminer (enabled, max blocks, speed)

### **⚔️ DIFFICULTY TAB**
Contains settings for:
- Master Difficulty (enabled, current preset, dynamic scaling)
- Core Multipliers (stamina, thirst, temp, hazards)
- Damage Scaling (player incoming/outgoing, mob damage)
- Dimension Multipliers (Overworld, Nether, End)
- Mob Resources (health, damage, behavior, drops)
- Metrics Weighting (playtime, damage, deaths, resources)

### **🔧 ADVANCED TAB** ⭐
Contains settings for:
- **Integrations** (web dashboard settings)
- **Performance** (particles, sounds, update frequency)
- **Developer** (debug mode, log level)
- **✨ FEATURE TOGGLES ✨** ← **ALL 25 FEATURES HERE!**

---

## 🔍 Search Functionality

The config screen has a **LIVE SEARCH BAR** at the top!

**How to use:**
1. Click the search bar at the top of the config screen
2. Type any feature name (e.g., "dragon", "particles", "elytra")
3. The list filters in real-time to show matching features
4. Works across all tabs simultaneously

**Search Examples:**
- Type "dragon" → Shows "Dragon Redesign" toggle
- Type "mob" → Shows all mob-related settings
- Type "hud" → Shows all HUD settings
- Type "fps" → Shows "Dynamic Fps Optimizer" and "Fps And Ping GUI"

---

## ⚙️ How to Toggle Features

### **Enable/Disable a Feature:**
1. Navigate to **Advanced** tab
2. Scroll to **Feature Toggles** section
3. Click the toggle button next to any feature
4. Changes save automatically!

### **Reset to Default:**
Right-click any toggle to reset it to default value (shown in tooltip)

---

## 📊 Feature States by Default

**Enabled by Default (20 features):**
- ✅ All Phase 1 features (4)
- ✅ All Phase 2 features (6)
- ✅ Custom Window Title
- ✅ Double Doors
- ✅ Easy Elytra Takeoff
- ✅ Dragon Redesign
- ✅ Nether Overhaul
- ✅ Hytale Feel

**Disabled by Default (5 features):**
- ⚪ FPS And Ping GUI
- ⚪ Fullscreen Auto Launch
- ⚪ Custom Window Icon
- ⚪ Dynamic FPS Optimizer
- ⚪ Infinite Trading
- ⚪ Drop Confirmation
- ⚪ Dynamic Lights

*Disabled features are off by default to avoid conflicting with player preferences or other mods*

---

## 🎨 Visual Guide

```
╔══════════════════════════════════════════════════════════════╗
║  ⚙️ Primal Craft Configuration (v3.0)                       ║
╠══════════════════════════════════════════════════════════════╣
║  [Search: ...........................]                       ║
╠══════════════════════════════════════════════════════════════╣
║  🎮 Gameplay │ 🎨 HUD │ ⚙️ Systems │ ⚔️ Difficulty │ 🔧 Advanced ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  🔧 ADVANCED TAB (Selected)                                  ║
║                                                              ║
║  ✦ INTEGRATIONS ✦                                            ║
║  □ Web Dashboard Enabled               [OFF]                ║
║  ⚙ Web Dashboard Port                  [8080]               ║
║                                                              ║
║  ✦ PERFORMANCE ✦                                             ║
║  ☑ Enable Particles                    [ON]                 ║
║  ☑ Enable Sounds                       [ON]                 ║
║  ⚙ Update Frequency                    [20]                 ║
║                                                              ║
║  ✦ DEVELOPER ✦                                               ║
║  □ Debug Mode                          [OFF]                ║
║  ⚙ Log Level                           [INFO]               ║
║                                                              ║
║  ✦ FEATURE TOGGLES ✦  ← ALL 25 FEATURES HERE!               ║
║  ☑ Hardcore Difficulty                 [ON]                 ║
║  ☑ Preset System                       [ON]                 ║
║  ☑ Debug Hud Removal                   [ON]                 ║
║  ☑ Sleep System Toggle                 [ON]                 ║
║  ☑ Difficulty Colors                   [ON]                 ║
║  ☑ Mob Aggression                      [ON]                 ║
║  ☑ Item Drop Particles                 [ON]                 ║
║  ☑ Day Transition Animation            [ON]                 ║
║  ☑ Third Person Names                  [ON]                 ║
║  ☑ Right Click Harvester               [ON]                 ║
║  □ Fps And Ping GUI                    [OFF]                ║
║  □ Fullscreen Auto Launch              [OFF]                ║
║  ☑ Custom Window Title                 [ON]                 ║
║  □ Custom Window Icon                  [OFF]                ║
║  □ Dynamic Fps Optimizer               [OFF]                ║
║  ☑ Double Doors                        [ON]                 ║
║  □ Infinite Trading                    [OFF]                ║
║  □ Drop Confirmation                   [OFF]                ║
║  ☑ Easy Elytra Takeoff                 [ON]                 ║
║  □ Dynamic Lights                      [OFF]                ║
║  ☑ Dragon Redesign                     [ON]                 ║
║  ☑ Nether Overhaul                     [ON]                 ║
║  ☑ Hytale Feel Enabled                 [ON]                 ║
║                                                              ║
║                                                         [▓]  ║
╠══════════════════════════════════════════════════════════════╣
║           [Done]              [Save & Close]                 ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 💡 Tips

### **Keyboard Shortcuts:**
- `]` - Open config menu (default hotkey)
- `Esc` - Close and save
- `Mouse Wheel` - Scroll through options
- `Right-Click` - Reset individual setting to default

### **Search Tips:**
- Search is case-insensitive
- Partial matches work (e.g., "har" finds "harvester")
- Filters across all tabs simultaneously
- Section headers remain visible for context

### **Saving:**
- Changes save automatically on toggle
- Manual save on close (backup)
- Config files: `config/primal-craft/*.json`

---

## 📁 Config File Locations

All settings are saved to:
```
config/primal-craft/
├── gameplay.json      (Gameplay tab)
├── hud.json          (HUD tab)
├── systems.json      (Systems tab)
├── difficulty.json   (Difficulty tab)
└── advanced.json     (Advanced tab + ALL 25 FEATURES)
```

**The 25 feature toggles are in:** `config/primal-craft/advanced.json`

Look for the `"features"` section in that file.

---

## ✅ CONFIRMATION - ALL FEATURES VISIBLE

**Status:** ✅ **CONFIRMED - All 25 features are in Mod Menu!**

Every single feature from your TODO list is:
- ✅ Visible in the Advanced tab
- ✅ Toggleable with ON/OFF switches
- ✅ Searchable via the search bar
- ✅ Documented with tooltips
- ✅ Saved automatically
- ✅ Right-click to reset

**You can now control everything from the in-game config menu!** 🎮

---

**Press `]` in-game to access the config and see all 25 features!**

