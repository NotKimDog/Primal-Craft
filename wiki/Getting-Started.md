# Getting Started with KimDog SMP

Complete installation and setup guide for KimDog SMP mod.

## 📥 Installation

### Prerequisites
- **Minecraft 1.21.11** (Java Edition)
- **Fabric Loader** installed
- **Fabric API** mod

### Step-by-Step Installation

#### 1. Install Fabric (if not already installed)

**Windows:**
1. Download [Fabric Installer](https://fabricmc.net/use/installer/)
2. Run the installer
3. Select Minecraft 1.21.11
4. Click "Install"
5. Choose your Minecraft installation directory

**macOS/Linux:**
1. Download [Fabric Installer](https://fabricmc.net/use/installer/)
2. Run: `java -jar fabric-installer-version.jar`
3. Select Minecraft 1.21.11
4. Follow prompts

#### 2. Install Fabric API

1. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11
2. Place the JAR file in your `.minecraft/mods/` folder
3. Launch Minecraft with Fabric profile

#### 3. Install KimDog SMP

1. Download KimDog SMP JAR file
2. Place in `.minecraft/mods/` folder
3. Launch Minecraft with Fabric profile

**Done!** The mod will load with all features enabled.

## 🎮 First Launch

On your first launch:
- ✅ All blocks and items are automatically registered
- ✅ Custom recipes are generated
- ✅ World generation features are enabled
- ✅ Default configurations are created

**Check the console** for initialization messages confirming successful load.

## ⚙️ Folder Locations

### Windows
```
%APPDATA%\.minecraft\mods\
```

### macOS
```
~/Library/Application Support/minecraft/mods/
```

### Linux
```
~/.minecraft/mods/
```

## 🔧 Configuration

KimDog SMP works out-of-the-box with default settings. Advanced configuration available through:

1. **Datapack Customization** - Modify recipes and loot tables
2. **Config Files** - Coming in future versions
3. **Command System** - Server-side customization

See [Configuration Guide](Configuration) for details.

## ✅ Verification Checklist

After installation, verify everything is working:

- [ ] Game launches without errors
- [ ] No red error messages in console
- [ ] New items appear in creative inventory
- [ ] New blocks appear in creative inventory
- [ ] Custom biome features visible
- [ ] HUD elements appear (stamina, thirst, etc.)
- [ ] Sounds register correctly

**All checkboxes green?** You're ready to play!

## 🚨 Troubleshooting Installation

### "Mod won't load"
1. Verify Fabric API is installed
2. Check Minecraft version matches (1.21.11)
3. Ensure JAR is in correct mods folder
4. Check console for error messages

### "Missing blocks/items"
1. Delete world and start new world
2. Let Minecraft load fully
3. Check mods folder for duplicate JARs

### "Game crashes on launch"
1. Check crash log in `.minecraft/crash-reports/`
2. See [Troubleshooting](Troubleshooting) page
3. Ensure no mod conflicts

For more help, see [Troubleshooting Guide](Troubleshooting).

## 🌍 Creating a New World

### Recommended World Settings
- **Difficulty:** Hard (for full challenge)
- **World Type:** Single Biome or Default
- **Seed:** Any (or try: `kimdogsmp`)
- **Generate Structures:** ON
- **Bonus Chest:** Optional

### First 10 Minutes
1. Find a good spawn location
2. Gather basic wood and stone
3. Craft a crafting table and furnace
4. Build a basic shelter
5. Explore custom biomes and features

## 📊 Server Setup (Multiplayer)

### Dedicated Server
1. Download server JAR for 1.21.11
2. Install Fabric Server Loader
3. Download Fabric API for server
4. Add KimDog SMP JAR to mods folder
5. Start server and configure

**Recommended Server Properties:**
```
difficulty=3
pvp=true
spawn-protection=0
max-players=20
```

See [Multiplayer Guide](Multiplayer-Guide) for detailed server setup.

## 🎓 Learning the Mod

### Quick Start Path
1. Read [Features Overview](Features) (5 minutes)
2. Check [Stamina System](Systems#Stamina-System) (3 minutes)
3. Check [Thirst System](Systems#Thirst-System) (3 minutes)
4. Explore [Items & Blocks](Items-and-Blocks) (10 minutes)

### Complete Learning Path
1. [All Features](Features) - 20 minutes
2. [All Systems](Systems) - 30 minutes
3. [Items & Blocks Guide](Items-and-Blocks) - 15 minutes
4. [Enchantments](Enchantments) - 10 minutes
5. Play and experiment!

## 🔗 Next Steps

**Ready to play?** → Go to [Features](Features) to learn what's new

**Want server setup?** → See [Multiplayer Guide](Multiplayer-Guide)

**Having issues?** → Check [Troubleshooting](Troubleshooting)

**Need help?** → Visit [FAQ](FAQ)

---

**Having issues with installation?** Check the [Troubleshooting Guide](Troubleshooting) or [FAQ](FAQ).
