# KimDog SMP Overhaul (Fabric 1.21.11)
A survival-focused Fabric modpack-style overhaul built from Kaupenjoe's tutorial base and expanded with systemic mechanics, new content, and quality-of-life HUD/UI polish.

## Highlights
- **Survival systems**: Stamina (actions drain, regen sync), Thirst (bottle-based recovery), Temperature (ambient + inventory/armor heat, wind, shelter checks), Exhaustion/Fatigue, Sleep/Hunger/Threat/Day-Night overhauls, Environmental hazards.
- **World + content**: New blocks, items, tools, armor sets, particles, sounds, potions/effects, enchantment effects, loot tweaks, custom entities (e.g., Mantis), villager trades, worldgen hooks, vein miner, hammering, brewing/composting/strippable/flamability registrations.
- **HUD & UX**: Stamina/thirst/inventory/temperature overlays, biome + weather notifications, animated chat, typing indicator, login streak badge, swing attack feedback, animated popups and sounds.
- **Networking & systems**: Custom payloads for stamina, temperature, wind, weather/biome notices, login streaks, chat/typing; wind sync + particles; advancement notifications.
- **Commands & utilities**: Vanish and Freecam commands, sprint cooldown handling, swing attack stamina drain, sleep stamina restoration.

## Building & Running
```bash
# Generate data (recommended before builds when content changes)
./gradlew runDatagen

# Build the mod
./gradlew build

# Run the dev client
./gradlew runClient
```
Requires JDK 21+ (Fabric Loader 0.18.4, Minecraft 1.21.11). If you change blocks/items/recipes, rerun `runDatagen` to refresh generated assets in `src/main/generated`.

## Project Layout
- `src/main/java/net/kaupenjoe/tutorialmod/` — mod sources (systems, events, network, HUD, content registries).
- `src/main/resources/` — assets, data, sounds, mixins, lang.
- `src/main/generated/` — generated data (loot, models, tags, recipes).
- `build.gradle` / `gradle.properties` — build config; `gradlew` scripts for tasks.

## Credits
Built on Kaupenjoe's Fabric tutorial framework; expanded and customized for KimDog SMP with additional survival, HUD, and world systems.
