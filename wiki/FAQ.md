# FAQ - Frequently Asked Questions

Common questions and answers about KimDog SMP mod.

## 🎮 Installation & Setup

### Q: How do I install the mod?
**A:** 
1. Install Fabric Loader for Minecraft 1.21.11
2. Download Fabric API mod
3. Download KimDog SMP JAR
4. Place both JARs in `.minecraft/mods/` folder
5. Launch Minecraft with Fabric profile

See [Getting Started](Getting-Started) for detailed instructions.

### Q: What are the requirements?
**A:** 
- Minecraft 1.21.11 (Java Edition)
- Fabric Loader
- Fabric API mod
- 2GB RAM minimum
- 500MB disk space

### Q: Will this work with other mods?
**A:** 
Yes! KimDog SMP is designed to work with other mods. However, conflicts with mods that modify:
- Player attributes
- Sprint mechanics
- Custom HUD
- World generation

may cause issues. Test compatibility before playing.

### Q: Can I use this on a server?
**A:** 
Yes! Install on server in mods folder. Recommended server properties:
```
difficulty=3
pvp=true
max-players=10-20
```

### Q: Is there a modpack version?
**A:** 
Not yet, but you can create your own modpack. See [Development](Development) for guidelines.

---

## 🎯 Gameplay Questions

### Q: What is Stamina used for?
**A:** 
Stamina controls:
- Sprint duration (depletes while sprinting)
- Combat effectiveness (affects damage)
- Attack cooldown
- Climbing speed

See [Systems - Stamina](Systems#Stamina-System-Detailed) for details.

### Q: How do I restore Stamina?
**A:** 
Stamina restores by:
- Standing still (+1 per second)
- Resting 5+ seconds (+2 per second)
- Sleeping (full restoration)
- Eating food (5-10 stamina)

### Q: What is Thirst and how do I manage it?
**A:** 
Thirst is a survival mechanic. Manage by:
- Drinking water bottles (-40 thirst)
- Standing in water (-1 per tick)
- Eating watermelon (-20 thirst)
- Avoiding hot biomes

See [Systems - Thirst](Systems#Thirst-System-Detailed) for details.

### Q: How does Temperature affect me?
**A:** 
Temperature affects:
- Health regeneration (affected at extremes)
- Movement speed (reduced in extremes)
- Thirst (increases in heat)
- Stamina (drains faster in heat)

Manage with armor and food. See [Systems - Temperature](Systems#Temperature-System-Detailed).

### Q: What do the HUD bars mean?
**A:** 
- **Green Bar:** Stamina level
- **Blue Bar:** Thirst level
- **Red Number (top right):** Temperature in °C
- **Left Side Icons:** Active status effects

### Q: How do I zoom in?
**A:** 
Use mouse scroll wheel:
- **Scroll Up:** Zoom in
- **Scroll Down:** Zoom out
- **Scroll Middle:** Return to normal

### Q: Can I turn off features I don't like?
**A:** 
Currently features are always on, but you can:
- Disable HUD elements (Press `H`)
- Adjust difficulties/effects in config
- Hide individual HUD bars

Future versions will have more toggles.

---

## 💎 Items & Crafting

### Q: Where do I find Pink Garnet?
**A:** 
Pink Garnet spawns at:
- **Overworld:** Y: 0-64 (common)
- **Deepslate:** Y: -64 to -16 (rare)
- **Nether:** Anywhere (moderate)
- **End:** Anywhere (common)

Mine with Iron pickaxe or better.

### Q: How do I craft tools?
**A:** 
Example for Sword:
```
Crafting Pattern:
    [G]
    [G]
    [S]
Result: Pink Garnet Sword
```

All recipes available in:
- JEI/REI mod
- Recipe book in inventory
- Wiki [Items & Blocks](Items-and-Blocks) page

### Q: What's the best tool?
**A:** 
Depends on task:
- **Mining:** Pickaxe with Efficiency V
- **Damage:** Sword with Sharpness V
- **AOE Mining:** Hammer for 3x3 area
- **Farming:** Hoe for quick tilling

### Q: How do I make armor?
**A:** 
Craft full set:
- **Helmet:** 5 ore + 1 leather
- **Chestplate:** 8 ore
- **Leggings:** 7 ore
- **Boots:** 4 ore

19 ore total for full set.

### Q: Can I dye armor?
**A:** 
Not directly, but use Armor Trims:
- **Kaupen Trim:** Unique pattern
- **Vanilla Trims:** All standard patterns
- Use Smithing Table to apply

### Q: What enchantments are best?
**A:** 
Top 3 universal:
1. **Mending** - Repairs with XP
2. **Unbreaking III** - Lasts 3x longer
3. **Efficiency V** - Fastest mining

See [Enchantments](Enchantments) guide.

---

## 🌍 World & Exploration

### Q: What biomes are new?
**A:** 
Same vanilla biomes, but with:
- Custom temperature values
- Enhanced weather effects
- Custom ore distribution
- Modified hazards

See [Systems - Biome](Systems#Biome--Weather-System-Detailed).

### Q: Where are the best places to build?
**A:** 
Recommended biomes:
- **Plains:** Neutral temperature
- **Forest:** Good supplies
- **Near Water:** Good for fishing
- **Avoid:** Deserts (hot), Frozen (cold)

### Q: Are there new structures?
**A:** 
No new structures in this version, but loot tables are enhanced with:
- Custom drops
- Rare items
- Pink Garnet loot

### Q: How does world generation work?
**A:** 
World generates with:
- Vanilla structures (temples, villages)
- Custom ore distribution
- Enhanced terrain features
- Custom trees (Driftwood)

All procedurally generated.

---

## ⚔️ Combat Questions

### Q: How does swing attack work?
**A:** 
1. Click to attack
2. Stamina is consumed (5+ required)
3. Cooldown applies (0.5-2 seconds)
4. Damage based on remaining stamina
5. Critical hits possible with full stamina

### Q: What is vein mining?
**A:** 
Sneak + mine to break entire vein:
- Works with pickaxes/axes
- Same durability as normal mining
- +2 stamina per block
- Faster overall

### Q: How do I win at PvP?
**A:** 
Tips:
1. **Manage Stamina:** Never deplete
2. **Circle Enemies:** Use terrain
3. **Use Weapons:** Hammer for 3x3
4. **Keep Distance:** When low stamina
5. **Armor Up:** Full enchanted armor

### Q: Can I fight the Ender Dragon?
**A:** 
Yes! Recommended:
- **Armor:** Full enchanted (Protection IV)
- **Weapon:** Sword with Sharpness V
- **Food:** Stacks of cooked meat
- **Tools:** Pickaxe for resources

### Q: Are there boss fights?
**A:** 
Currently only vanilla bosses:
- Ender Dragon
- Wither

Custom bosses planned for future.

---

## 🛠️ Technical Questions

### Q: Is this multiplayer safe?
**A:** 
Yes, but:
- Run reputable servers only
- Backup world saves
- Enable whitelist on private servers
- Monitor for griefing

### Q: How much RAM does it use?
**A:** 
Typical usage:
- **Vanilla:** 1.5GB
- **+ Mod:** 2-2.5GB
- **+ Other Mods:** 3-4GB

Allocate accordingly in launcher.

### Q: Why is it laggy?
**A:** 
Check:
1. RAM allocation
2. Render distance (16 chunks recommended)
3. Simulation distance
4. Other mod conflicts
5. World size/age

### Q: Can I report bugs?
**A:** 
Yes! Please include:
- Mod version
- Minecraft version
- Crash log (if applicable)
- Steps to reproduce
- Expected vs actual behavior

See [Troubleshooting](Troubleshooting) page.

### Q: How do I update the mod?
**A:** 
1. Backup world saves
2. Delete old mod JAR
3. Download new version
4. Place in mods folder
5. Launch Minecraft
6. Old world should work fine

---

## 📊 Configuration

### Q: Can I change settings?
**A:** 
Limited configuration available:
- HUD customization
- Effect toggles (coming soon)
- Server properties
- Datapack modifications

See [Configuration](Configuration) guide.

### Q: Can I disable specific features?
**A:** 
Currently:
- ✅ Hide HUD bars
- ❌ Disable Stamina system
- ❌ Disable Thirst
- ❌ Disable Temperature

Full toggles planned.

### Q: How do I change mod settings on server?
**A:** 
Server config (coming soon):
- Difficulty modifiers
- Damage scaling
- Resource generation rates
- Feature toggles

---

## 🤝 Community & Support

### Q: Where can I get help?
**A:** 
1. Check [Troubleshooting](Troubleshooting) page
2. Search FAQ (this page)
3. Check [Documentation](Home)
4. Report issue on GitHub

### Q: Can I contribute to the mod?
**A:** 
Yes! See [Development](Development) guide:
- Code contributions
- Bug reporting
- Translation help
- Feedback & ideas

### Q: Is source code available?
**A:** 
Yes! Open source under CC0 1.0 Universal.
Visit GitHub for full source.

### Q: Can I make a fork/derivative?
**A:** 
Absolutely! CC0 license allows:
- Modifications
- Derivatives
- Redistribution
- Commercial use

Just mention original creator.

---

## ❓ Still Have Questions?

**Not answered here?**
1. Check specific feature pages
2. See [Troubleshooting](Troubleshooting)
3. Search [Documentation](Home)
4. Ask on Discord/GitHub

---

**Last Updated:** January 2026
