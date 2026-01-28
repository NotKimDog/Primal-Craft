# Development Guide

Information for developers and contributors.

## 👨‍💻 Contributing to KimDog SMP

### License
**CC0 1.0 Universal** - Public Domain

This means:
- ✅ Fork and modify
- ✅ Commercial use allowed
- ✅ No attribution required
- ✅ Redistribute freely

### Getting Started

#### 1. Fork the Repository
- Go to GitHub repository
- Click "Fork"
- Clone to local machine

#### 2. Setup Development Environment

**Requirements:**
- Java 21 JDK
- Gradle (included)
- IDE (IntelliJ IDEA recommended)

**Setup:**
```bash
git clone https://github.com/YOUR-USERNAME/KimDog-SMP-1.21.X
cd KimDog-SMP-1.21.X
./gradlew idea        # For IntelliJ
./gradlew eclipse     # For Eclipse
```

#### 3. Build the Mod
```bash
./gradlew build       # Full build
./gradlew runClient   # Test in client
./gradlew runServer   # Test on server
```

### Project Structure

```
src/main/java/net/kaupenjoe/tutorialmod/
├── block/              # Custom blocks
├── item/               # Custom items
├── entity/             # Mobs and entities
├── event/              # Event handlers
├── system/             # Game systems
├── network/            # Server-client sync
├── util/               # Utility classes
└── TutorialMod.java    # Main class
```

---

## 🐛 Bug Reports

### How to Report a Bug

**Create GitHub Issue with:**

```
Title: [Brief Description]

**Version:**
- Mod Version: 2.3.0
- Minecraft: 1.21.11
- Fabric API: 0.141.2

**Description:**
[Detailed explanation]

**Steps to Reproduce:**
1. Do this
2. Then this
3. Bug occurs

**Expected Behavior:**
[What should happen]

**Actual Behavior:**
[What actually happened]

**Log:**
[Paste error from logs/]
```

### Bug Priority

- **Critical:** Game crash, data loss
- **High:** Major feature broken
- **Medium:** Feature partially broken
- **Low:** Minor issue, workaround exists

---

## 💡 Feature Requests

### Suggest a Feature

**Template:**

```
Title: [Feature Name]

**Description:**
[What feature and why needed]

**Use Case:**
[How would players use this]

**Implementation Ideas:**
[Suggestions for how to do it]

**Related Features:**
[Similar existing features]
```

---

## 🔨 Development Setup

### Building from Source

```bash
# Full clean build
./gradlew clean build

# Run client test
./gradlew runClient

# Run server test
./gradlew runServer

# Generate sources
./gradlew genSources

# Build JAR only
./gradlew jar
```

### IDE Setup

**IntelliJ IDEA:**
1. Import project
2. Run gradle sync
3. Select Fabric SDK
4. Create run configuration
5. Run → Edit Configurations → Add Application

**Eclipse:**
1. Import as Gradle project
2. Wait for gradle sync
3. Run → Run Configurations
4. Add new Java Application

**VS Code:**
1. Install Minecraft extension
2. Open project folder
3. Run gradle tasks from command palette

---

## 📝 Code Style

### Java Code Standards

**Naming Conventions:**
- Classes: `PascalCase` (e.g., `StaminaSystem`)
- Methods: `camelCase` (e.g., `getStamina()`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_STAMINA`)
- Variables: `camelCase` (e.g., `currentStamina`)

**Formatting:**
- 4 spaces for indentation
- Braces on same line
- Max 100 chars per line
- Use meaningful variable names

**Comments:**
```java
// Single line comment
/* Multi-line
   comment */
/** Javadoc comment */
```

### Structure Template

```java
public class MySystem {
    private static final Logger LOGGER = LoggerFactory.getLogger("primal-craft");
    public static final int CONSTANT = 100;
    
    private int field;
    
    public MySystem() {
        // Constructor
    }
    
    public void method() {
        // Implementation
    }
}
```

---

## 🧪 Testing

### Unit Testing

```bash
./gradlew test
```

### Manual Testing

**Create test world:**
1. `./gradlew runClient`
2. Create new world
3. Enable creative mode
4. Test features

**Test Checklist:**
- [ ] All blocks place
- [ ] All items exist
- [ ] Recipes work
- [ ] Mobs spawn
- [ ] Events fire
- [ ] Networking works

### Performance Testing

```bash
# Use profiler
./gradlew runClient --profile

# Check logs for performance issues
cat logs/latest.log | grep performance
```

---

## 📦 Version Management

### Versioning Scheme

**Format:** `MAJOR.MINOR.PATCH`

- **MAJOR:** Breaking changes
- **MINOR:** New features
- **PATCH:** Bugfixes

**Examples:**
- 2.3.0 - Current version
- 2.3.1 - Bugfix patch
- 2.4.0 - New features
- 3.0.0 - Major rewrite

### Version File

Location: `gradle.properties`
```properties
mod_version=2.3.0
```

### Release Process

1. **Update Version:**
   - Edit `gradle.properties`
   - Update `fabric.mod.json`
   - Update `README`

2. **Build Release:**
   ```bash
   ./gradlew build
   ```

3. **Test Release:**
   - Test JAR in clean environment
   - Verify all features
   - Check compatibility

4. **Release:**
   - GitHub Release
   - Modrinth Upload
   - CurseForge Upload

---

## 🔗 Pulling Changes

### Submitting Changes

**Process:**
1. Fork repository
2. Create feature branch: `git checkout -b feature/name`
3. Make changes
4. Commit: `git commit -m "feature: Description"`
5. Push: `git push origin feature/name`
6. Create Pull Request

**PR Template:**

```
## Description
[What this PR does]

## Changes
- Change 1
- Change 2

## Testing
- [ ] Tested in client
- [ ] Tested on server
- [ ] No errors in log

## Closes
#[Issue number]
```

### PR Requirements

- [ ] Code compiles without errors
- [ ] No deprecated methods used
- [ ] Changes tested
- [ ] Documentation updated
- [ ] No breaking changes (or documented)

---

## 📚 Architecture Notes

### System Structure

**Event-Driven:**
- Use Fabric events
- Register handlers
- Listen for changes

**Network Sync:**
- Server → Client payloads
- Client → Server payloads
- Network IDs must be unique

**Data Storage:**
- Use data attachments
- Sync with network
- Save/load from world

### Key Classes

**TutorialMod** - Main mod class, initializes all systems

**Event Handlers** - Listen for and respond to events

**Network Payloads** - Server-client communication

**Systems** - Encapsulated game feature groups

---

## 🐛 Common Issues & Solutions

### Gradle Build Fails

```bash
# Clear cache
./gradlew clean

# Rebuild
./gradlew build
```

### Client Won't Run

```bash
# Regenerate IDE files
./gradlew idea
# Or for Eclipse:
./gradlew eclipse
```

### Weird Network Errors

- Check payload IDs are unique
- Verify CODEC registration
- Test on clean world

### Mods Not Loading

- Check dependencies installed
- Verify Fabric API version
- Check mod order

---

## 📖 Documentation

### Code Documentation

Use Javadoc for public methods:

```java
/**
 * Calculates stamina loss from sprinting.
 *
 * @param duration Time sprinting in ticks
 * @return Stamina lost
 */
public int calculateStaminaLoss(int duration) {
    return duration / 2;
}
```

### Wiki Updates

When making changes:
1. Update relevant wiki pages
2. Add to changelog
3. Update README if major
4. Link related features

---

## 🚀 Deployment

### Release Checklist

- [ ] Version updated
- [ ] JAR built successfully
- [ ] Tested on clean world
- [ ] No console errors
- [ ] Works with Fabric API
- [ ] Changelog written
- [ ] README updated
- [ ] Wiki updated

### Release Upload

**Modrinth:**
1. Login to Modrinth
2. Go to project
3. Click "Create Version"
4. Upload JAR
5. Add changelog
6. Publish

**CurseForge:**
1. Login to CurseForge
2. Go to project
3. Click "Upload File"
4. Select JAR
5. Fill metadata
6. Publish

**GitHub:**
1. Go to Releases
2. Click "New Release"
3. Select version tag
4. Add description
5. Upload JAR
6. Publish

---

## 📞 Getting Help

### Development Questions

- **GitHub Discussions** - Ask questions
- **Discord** - Real-time chat (when available)
- **Issues** - Technical problems

### Resources

- [Fabric Documentation](https://fabricmc.net/develop/)
- [Minecraft Wiki](https://minecraft.wiki)
- [Java Documentation](https://docs.oracle.com/javase/)

---

## 🎓 Learning Path

**New Contributor?**

1. Read [Code Structure](#project-structure)
2. Browse [existing code](GitHub)
3. Run [development setup](#development-setup)
4. Make small changes
5. Submit PR
6. Iterate based on feedback

---

**Welcome to the development team!** 🚀

For questions, create an issue on GitHub.
