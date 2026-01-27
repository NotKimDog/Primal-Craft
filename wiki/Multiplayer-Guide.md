# Multiplayer Guide (SMP Server Setup)

Complete guide for setting up KimDog SMP on a Minecraft server for multiplayer play.

## 🖥️ Server Installation

### Prerequisites
- Java 21+ installed
- Fabric Server Loader
- 2-4GB RAM allocated
- Port 25565 available
- 1GB disk space minimum

### Step 1: Download Server Files

1. **Get Fabric Server Loader:**
   - Download from [Fabric Website](https://fabricmc.net/)
   - Use version 1.21.11

2. **Download Server JAR:**
   - Get Minecraft Server JAR 1.21.11
   - Save to server directory

3. **Run Installation:**
   ```bash
   java -jar fabric-installer-server.jar install server
   ```

### Step 2: Install Mods

1. **Create mods folder:**
   ```
   server/mods/
   ```

2. **Add Fabric API:**
   - Download for 1.21.11
   - Place JAR in mods folder

3. **Add KimDog SMP:**
   - Download mod JAR
   - Place in mods folder

### Step 3: Configure Server

1. **Edit `server.properties`:**
   ```properties
   # Basic Settings
   motd=KimDog SMP Server
   max-players=10
   difficulty=3
   gamemode=survival
   
   # Recommended for KimDog SMP
   pvp=true
   spawn-protection=0
   online-mode=true
   ```

2. **Edit `server-icon.png`** (optional)
   - 64x64 PNG image
   - Displays in server list

3. **Edit EULA:**
   - Change `eula=false` to `eula=true`

### Step 4: Launch Server

**Windows:**
```batch
java -Xmx3G -Xms2G -jar fabric-server-launch.jar nogui
```

**Linux/Mac:**
```bash
java -Xmx3G -Xms2G -jar fabric-server-launch.jar nogui
```

**With Screen (Linux):**
```bash
screen -S minecraft
java -Xmx3G -Xms2G -jar fabric-server-launch.jar nogui
# Detach: Ctrl+A then D
```

---

## 🔧 Server Configuration

### server.properties Recommendations

```properties
# Core Settings
server-port=25565
difficulty=3
gamemode=survival
online-mode=true

# PvP & Protection
pvp=true
spawn-protection=0
allow-flight=false

# World
world-type=default
world-name=world
level-seed=kimdogsmp

# Resource Management
max-players=10
max-world-size=59999968
simulation-distance=10
view-distance=16

# Survival Features
allow-nether=true
allow-the-end=true
spawn-monsters=true
generate-structures=true
```

### Performance Tuning

**For 5-10 Players:**
```
RAM Allocation: 2-3GB
max-players: 8
simulation-distance: 10
view-distance: 12
```

**For 10-20 Players:**
```
RAM Allocation: 4-6GB
max-players: 16
simulation-distance: 8
view-distance: 10
```

**For 20+ Players:**
```
RAM Allocation: 6-8GB
max-players: 24
simulation-distance: 6
view-distance: 8
Paper Server Recommended
```

---

## 🔐 Security Setup

### Whitelist Configuration

1. **Enable Whitelist:**
   - Edit server.properties
   - Add: `whitelist=true`

2. **Whitelist Players:**
   ```
   /whitelist add <playername>
   /whitelist remove <playername>
   /whitelist list
   ```

3. **Whitelist File:**
   - Edit `whitelist.json`
   - Manual player management

### Port Forwarding

1. **Router Configuration:**
   - Log into router (usually 192.168.1.1)
   - Find Port Forwarding settings
   - Forward 25565 → server machine IP

2. **Firewall Rules:**
   - Windows: Allow Java through firewall
   - Linux: `sudo ufw allow 25565`

3. **Connect:**
   - Share server IP with players
   - Players connect via `serverip:25565`

### Backup Strategy

**Automatic Backups:**
```bash
# Linux cron job
0 0 * * * cd /server && tar -czf backup-$(date +%Y%m%d).tar.gz world/
```

**Manual Backups:**
```bash
# Stop server: /stop
# Copy world folder to backup location
# Restart server
```

**Backup Retention:**
- Keep daily backups for 7 days
- Keep weekly backups for 4 weeks
- Keep monthly backups for 6 months

---

## 👥 Player Management

### Commands

**Player Management:**
```
/kick <player> <reason>     # Remove player
/ban <player> <reason>      # Ban player
/unban <player>             # Unban player
/op <player>                # Make operator
/deop <player>              # Remove operator
```

**World Management:**
```
/save-all                   # Save world
/reload                     # Reload configuration
/stop                       # Stop server safely
```

**Statistics:**
```
/list                       # Show players online
/difficulty <level>         # Change difficulty
/gamemode <mode> <player>   # Change gamemode
```

### Operator Privileges

**Trusted players:**
1. `/op <player>` to make operator
2. They get admin commands
3. Can use `/` commands
4. Can manage properties

**Revoke with:** `/deop <player>`

---

## 📊 Server Monitoring

### Check Server Health

**CPU Usage:**
```bash
# Linux
top -b -n 1 | grep java

# Windows
tasklist | findstr java
```

**Memory Usage:**
```bash
# Linux
free -h

# Windows
systeminfo | findstr Memory
```

**Disk Space:**
```bash
# Linux
df -h

# Windows
dir C: | findstr Dir
```

### Server Logs

**Location:** `logs/latest.log`

**Important Messages:**
- Errors: Red text
- Warnings: Yellow text
- Info: Default color
- Don't ignore red errors!

**Log Management:**
- Server creates new log daily
- Old logs in `logs/` folder
- Compress old logs to save space

---

## 🌐 Network Setup

### Port Forwarding Checklist

- [ ] Router configured
- [ ] Server IP correct
- [ ] Port 25565 forwarded
- [ ] Firewall allows port
- [ ] Server running
- [ ] Can connect from outside network

### Connection Test

1. **Local Test:**
   - `localhost:25565` from same network

2. **External Test:**
   - Use external IP from different network
   - `portchecker.co` to verify port open

3. **DNS (Optional):**
   - Set up custom domain
   - Point to server IP
   - Use domain instead of IP

---

## ⚙️ Advanced Configuration

### Game Rules

```
/gamerule commandFeedback true
/gamerule showDeathMessages true
/gamerule logAdminCommands true
/gamerule doDaylight true
/gamerule doMobLoot true
/gamerule doMobSpawning true
```

### Difficulty Management

```properties
# Hard mode (recommended for SMP)
difficulty=3

# Alternatives:
# 0 = Peaceful
# 1 = Easy  
# 2 = Normal
# 3 = Hard
```

### World Seed

```properties
# Use specific seed for reproducible world
level-seed=kimdogsmp

# Or random:
level-seed=
```

---

## 🐛 Common Server Issues

### Server Won't Start

**Causes:**
- Port already in use
- Java not installed
- Corrupted world
- Out of disk space

**Solutions:**
1. Check port 25565 not in use
2. Verify Java 21+ installed
3. Delete corrupted region file
4. Free up disk space

### Lag Issues

**Causes:**
- Too many players
- High render distance
- Too many entities
- Old world data

**Solutions:**
```
Reduce:
- simulation-distance
- view-distance
- max-players
- entity count (/kill @e[type=!player])
```

### Player Connection Issues

**Can't Connect:**
1. Verify server running
2. Check firewall/port forward
3. Verify IP address correct
4. Restart server

**Slow Connection:**
1. Check network latency
2. Reduce render distance
3. Move closer to server
4. Use wired connection

---

## 📈 Scaling the Server

### Start Small
- 2-5 players on 2GB RAM
- Single core sufficient
- Basic hardware OK

### Scale Up
- 5-10 players → 3GB RAM
- 10-20 players → 4-6GB RAM
- 20+ players → 6-8GB RAM
- Multiple cores recommended

### Advanced Scaling
- Use Paper Server (optimized)
- Add more hardware RAM
- Use NVMe SSD
- Distribute load (Bungeecord)

---

## 📝 SMP Rules Template

**Suggested Rules for Server:**
```
1. No griefing (destroying others' builds)
2. No stealing from other players
3. No hacking/cheating
4. Be respectful to other players
5. No spam in chat
6. Ask permission before building near others
7. Shared resources in designated areas
8. Report issues to admins
9. Backups taken daily
10. Admin decisions are final
```

---

## 🔄 Server Updates

### Updating Mods

1. **Stop Server:** `/stop`
2. **Backup World:** Copy world folder
3. **Update JAR:** Replace mod files
4. **Restart:** Start server again
5. **Verify:** Check all players can connect

### Update Checklist

- [ ] World backup created
- [ ] Players notified
- [ ] New mod version tested
- [ ] Server restarted
- [ ] All players tested connection
- [ ] Backup stored safely

---

## 📞 Server Support Resources

- **Fabric Documentation:** [fabricmc.net](https://fabricmc.net)
- **Minecraft Server Wiki:** [wiki.vg](https://wiki.vg)
- **This Mod Wiki:** See [Troubleshooting](Troubleshooting)

---

**Server ready to launch!** Invite players and start your SMP adventure! 🚀
