# Troubleshooting Guide

Common issues and solutions for Primal Craft mod.

## 🚨 Installation Issues

### Issue: "Mod won't load - stuck on loading screen"

**Causes:**
- Incompatible Fabric API version
- Missing dependencies
- Corrupted JAR file
- Java version mismatch

**Solutions:**
1. Verify Fabric API is latest version for 1.21.11
2. Delete Primal Craft JAR and download again
3. Check Java version (8+ required)
4. Delete `.minecraft/mods` cache
5. Restart launcher and try again

---

### Issue: "Missing blocks/items in creative inventory"

**Causes:**
- World didn't load properly
- Mod not fully initialized
- Old world generation cache

**Solutions:**
1. Create new world (old world data is kept)
2. Wait 30+ seconds on load screen
3. Check console for error messages
4. Delete `.minecraft/saves` cache for world
5. Restart game fully

---

### Issue: "Game crashes on launch"

**Causes:**
- Mod conflict with other mods
- Corrupted game files
- Insufficient RAM
- Missing dependencies

**Solutions:**
1. **Check Crash Log:**
   - Location: `.minecraft/crash-reports/`
   - Read error message carefully
   - Search error message in wiki

2. **Allocate More RAM:**
   - Launcher Settings → JVM Arguments
   - Change `-Xmx1G` to `-Xmx2G` or higher
   - Restart game

3. **Remove Conflicting Mods:**
   - Move mods to backup folder
   - Test game with only Primal Craft + Fabric API
   - Add mods back one by one

4. **Reinstall Fabric:**
   - Delete `.minecraft/libraries/net/fabricmc/`
   - Rerun Fabric installer
   - Reinstall mods

---

### Issue: "Fabric API not found"

**Causes:**
- Fabric API not installed
- Wrong Fabric API version
- Installation location error

**Solutions:**
1. Download Fabric API for 1.21.11 from Modrinth
2. Place JAR in `.minecraft/mods/` folder
3. Ensure filename contains "fabric-api"
4. Restart launcher
5. Verify in mods list before launching

---

## 🎮 Gameplay Issues

### Issue: "Stamina not visible on HUD"

**Causes:**
- HUD not initialized
- Stamina bar toggled off
- Display settings issue

**Solutions:**
1. Press `H` to toggle HUD elements
2. Verify stamina bar is enabled
3. Restart game
4. Check screen resolution isn't too low
5. Ensure HUD isn't off-screen

---

### Issue: "Taking damage from temperature even with armor"

**Causes:**
- Temperature extreme
- Armor not fully enchanted
- Biome too severe

**Solutions:**
1. Check temperature reading on HUD
2. Wear full armor set (4 pieces needed)
3. Add enchantments to armor pieces
4. Move to less extreme biome
5. Eat warm/cool food to adjust

**Temperature Management:**
- Freezing: Equip armor, eat warm food
- Overheating: Remove armor, eat cool food, find shade

---

### Issue: "Thirst increasing too fast"

**Causes:**
- Hot biome
- High activity level
- Dehydration status

**Solutions:**
1. Check HUD for biome temperature
2. Drink water bottles frequently
3. Avoid sprinting excessively
4. Rest in water or shade
5. Stay in neutral/cool biomes

**Thirst Tips:**
- Carry 16+ water bottles when exploring
- Eat watermelon (reduces thirst)
- Rest in water bodies
- Build near water sources

---

### Issue: "Can't sprint - stamina already empty"

**Causes:**
- Stamina fully depleted
- Sprint cooldown active
- Movement blocked

**Solutions:**
1. Stand still to regenerate stamina
2. Wait for cooldown (3 seconds)
3. Eat food to partially restore
4. Sleep in bed for full recovery
5. Use alternative movement (water, ice)

---

### Issue: "Swing attacks not working"

**Causes:**
- Insufficient stamina
- Attack cooldown active
- Tool not held
- Network lag (multiplayer)

**Solutions:**
1. Check stamina bar (need 5+ stamina)
2. Wait for attack cooldown to finish
3. Ensure weapon is selected
4. Check for network lag
5. Try attacking different entity

---

## 🌍 World Issues

### Issue: "Ores not generating"

**Causes:**
- World created before mod install
- World too new (not enough chunks)
- Ore generation disabled

**Solutions:**
1. Create new world
2. Explore far from spawn (10,000+ blocks)
3. Check server config for ore generation
4. Verify mod is loaded in world

---

### Issue: "Custom blocks/trees missing from world"

**Causes:**
- Old world cache
- Mod not enabled
- Chunk not generated

**Solutions:**
1. Create new world
2. Regenerate chunks (use tool)
3. Verify mod in mods list
4. Check console for load errors

---

### Issue: "Performance lag/FPS drops"

**Causes:**
- Too many mods loaded
- Render distance too high
- Old computer
- RAM allocation low

**Solutions:**

**Immediate:**
1. Lower render distance (10-16 chunks)
2. Lower graphics settings
3. Close other applications
4. Allocate more RAM (2-3GB minimum)

**Advanced:**
1. Disable mods not being used
2. Use performance mod (Sodium)
3. Lower simulation distance
4. Reduce particle effects
5. Disable fancy leaves/grass

---

### Issue: "Multiplayer/Server lag"

**Causes:**
- Server underpowered
- Too many players
- World too old
- Network latency

**Solutions:**

**Server Side:**
1. Allocate more RAM to server
2. Reduce player limit
3. Optimize world (prune old chunks)
4. Close unused mods
5. Reduce simulation distance

**Client Side:**
1. Check internet connection
2. Reduce render distance
3. Close bandwidth-heavy apps
4. Move closer to server geographically
5. Use wired connection

---

## 💾 Save & Data Issues

### Issue: "World disappeared/deleted"

**Causes:**
- Accidental deletion
- Corruption
- Launcher issue
- Path changed

**Solutions:**
1. Check Trash/Recycle Bin
2. Check launcher config directory
3. Look in `.minecraft/saves/` folder
4. Check for backup folder
5. Use file recovery tool

**Prevention:**
- Backup saves regularly
- Use cloud storage (optional)
- Keep multiple world copies
- Note world names clearly

---

### Issue: "Can't access old world from another computer"

**Causes:**
- World stored locally
- Different launcher version
- File permission issue

**Solutions:**
1. Copy entire world folder to USB drive
2. Place in new computer's `.minecraft/saves/`
3. Ensure Fabric + mods installed first
4. Verify file permissions are readable
5. Test with new game before important data

---

## 🔊 Audio/Visual Issues

### Issue: "Sounds not playing"

**Causes:**
- Audio disabled
- Sound files missing
- Volume set to 0
- Audio device disconnected

**Solutions:**
1. Check game volume slider
2. Check system volume
3. Verify audio device connected
4. Restart game with audio on
5. Check sound settings in options

---

### Issue: "HUD elements not showing properly"

**Causes:**
- Display too small
- HUD off-screen
- Scaling issue
- Overlay conflict

**Solutions:**
1. Press `H` to toggle HUD visibility
2. Adjust window resolution
3. Check HUD positioning
4. Try different GUI scale
5. Reset HUD to default

---

### Issue: "Visual glitches/flickering"

**Causes:**
- Graphics driver issue
- V-Sync problems
- Frame rate inconsistent
- Mod conflict

**Solutions:**
1. Update GPU drivers
2. Enable V-Sync in settings
3. Limit FPS to monitor refresh rate
4. Disable shader mods temporarily
5. Lower graphics quality

---

## 🐛 Bug Reporting

### Found a Bug?

**Gather Information:**
1. **Version:** Note mod & Minecraft version
2. **Steps to Reproduce:** How to cause bug
3. **Expected Behavior:** What should happen
4. **Actual Behavior:** What actually happened
5. **Log File:** Crash report location

**Report Location:**
- GitHub Issues page
- Discord (when available)
- Modrinth comments section

**Report Template:**
```
Title: [Brief description of bug]

Version: Primal Craft 2.3.0, Minecraft 1.21.11

Steps to Reproduce:
1. Do this
2. Then do this
3. Bug occurs

Expected: Should do X
Actual: Does Y instead

Log: [paste crash log or error]
```

---

## ✅ Verification Checklist

**After installation, verify:**

- [ ] Game launches without crash
- [ ] No red errors in console
- [ ] Items visible in creative
- [ ] Blocks place correctly
- [ ] HUD displays properly
- [ ] Sounds play
- [ ] Stamina/Thirst working
- [ ] Temperature affects gameplay
- [ ] Tools mine correctly
- [ ] Recipes work as expected

**All green?** Installation successful! ✅

---

## 📞 Getting More Help

**If issue persists:**

1. **Check Wiki Pages:**
   - [FAQ](FAQ) - Common questions
   - [Getting Started](Getting-Started) - Setup help
   - [Systems](Systems) - How features work

2. **Search Previous Issues:**
   - GitHub Issues search
   - Discord message history
   - Modrinth discussion

3. **Contact Support:**
   - Open GitHub issue
   - Join Discord server
   - Comment on Modrinth

**Be specific and include:**
- Full error messages
- Minecraft version
- Mod version
- Steps to reproduce
- Log files

---

**Still stuck?** Check the complete [Documentation](Home) or [FAQ](FAQ).
