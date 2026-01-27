# Systems - Detailed Mechanics

In-depth technical explanation of how each system works.

## Stamina System (Detailed)

### How Stamina Works

**Stamina Bar**
- Displayed as a green bar on HUD
- Ranges from 0 to 100
- Depletes when sprinting
- Regenerates when standing still

**Sprint Mechanics**
1. Player holds CTRL to sprint
2. Stamina depletes at 2 points per tick
3. At 0 stamina, sprint becomes unavailable
4. Player automatically stops sprinting
5. Cooldown of 3 seconds before sprint available again

**Regeneration**
- Standing still: +1 stamina per tick
- Resting (5+ seconds): +2 stamina per tick
- Sleeping: Full regeneration
- Food: Partial regeneration (5-10 stamina)

**Stamina Drain Sources**
- **Sprint** - 2 per tick (3.6 per second)
- **Heavy Swing Attack** - 10 stamina per swing
- **Long Fall** - 5 stamina per block fallen
- **Swimming** - 1 per tick while moving

### Combat Integration

**Swing Attack Stamina**
- Basic attacks use 5 stamina
- Heavy attacks use 10 stamina
- Damage scales with stamina remaining
- At 0 stamina, attacks are weak

**Attack Cooldown**
- 0.5 second base cooldown
- +0.5 seconds per stamina spent
- Maximum 2 second cooldown
- Resets on successful hit

### Advanced Tips

**Stamina Efficiency**
- Sprint only when necessary
- Use terrain for natural speed
- Combine sprint with water
- Plan rest stops on journeys

**Combat Strategy**
- Never let stamina deplete in PvP
- Manage stamina during boss fights
- Rest between encounters
- Use ranged weapons at low stamina

---

## Thirst System (Detailed)

### How Thirst Works

**Thirst Meter**
- Displayed as a blue bar on HUD
- Ranges from 0 to 100
- Increases naturally over time
- Increases faster in hot biomes

**Thirst Increase**
- Base: +1 per 5 seconds
- Hot biome: +1 per 2 seconds
- After sprinting: +1 per second
- Desert: +1 per 1 second

**Thirst Reduction**
- Drinking water bottle: -40 thirst
- Standing in water: -1 per tick
- Eating watermelon: -20 thirst
- Sleeping: -10 thirst

**Thirst Levels**
- **0-20** - Hydrated (green)
- **20-40** - Normal (yellow)
- **40-60** - Thirsty (orange)
- **60-80** - Very Thirsty (red)
- **80-100** - Dehydrated (dark red)

### Thirst Effects

**At Thirst Level 60+**
- Health regeneration stops
- Movement speed -5%
- Attack speed -10%
- Vision slightly blurred

**At Thirst Level 80+**
- Damage taken increased by 10%
- Hunger increases faster
- Mining speed reduced by 20%
- Nausea effect applied

**At Thirst Level 100**
- Takes 1 damage every 10 seconds
- Cannot sprint
- Vision is severely blurred

### Water Management

**Water Sources**
- Water bottles (crafted)
- Standing in water bodies
- Rain collection
- Wells (future feature)

**Biome-Specific Thirst**
- **Desert** - +1 per tick
- **Savanna** - +0.5 per tick
- **Ocean** - -1 per tick
- **Cold** - -0.5 per tick

### Survival Strategy

**Long Expeditions**
- Carry 16+ water bottles
- Rest frequently in shade
- Travel at night when possible
- Bring watermelon seeds

**Base Building**
- Build near water sources
- Create water storage systems
- Plant watermelon for food+hydration
- Create rest areas

---

## Temperature System (Detailed)

### How Temperature Works

**Temperature Calculation**
1. Start with biome base temperature
2. Adjust for time of day (cooler at night)
3. Modify for current weather
4. Apply player clothing bonus
5. Apply food effect bonus
6. Display final temperature

**Biome Temperatures**
- **Frozen Ocean** - -20°C
- **Snowy** - -10°C
- **Temperate** - 15°C
- **Savanna** - 25°C
- **Desert** - 35°C
- **Nether** - 45°C (with lava bonus)

**Time of Day Effects**
- **Noon** - +5°C
- **Evening** - -2°C
- **Night** - -10°C
- **Morning** - 0°C (baseline)

### Temperature Effects on Player

**Below 0°C (Freezing)**
- Takes 1 damage every 4 seconds
- Movement speed -10%
- Mine speed -20%
- Stamina drain increased

**0-10°C (Cold)**
- Slow health drain (minor)
- Water freezes (slippery)
- Extra hunger

**20-30°C (Warm)**
- Stamina drains faster
- Thirst increases faster
- No negative effects

**30°C+ (Hot)**
- Takes 1 damage every 8 seconds
- Movement speed -5%
- Stamina depletes 1.5x faster
- Thirst increases 1.5x faster

**45°C+ (Extreme Heat)**
- Takes 1 damage every 3 seconds
- Cannot sprint efficiently
- Heavy armor causes extra damage
- Fire resistance potion helps

### Clothing System

**Armor Temperature Bonus**
- Leather: -2°C per piece
- Iron: -3°C per piece
- Diamond: -4°C per piece
- Netherite: -5°C per piece
- **Full Set Bonus** - Additional -5°C

**Maximum Cold Protection**
- Full Netherite armor: -25°C total
- Protects from almost any freeze

**No Heat Protection**
- Armor has no heat resistance
- Must find shade or cool biomes
- Potions provide resistance

### Food Temperature Effects

**Warm Foods**
- **Meat** (cooked) - +3°C
- **Bread** - +2°C
- **Soup** - +4°C
- **Effect Duration** - 30 seconds

**Cool Foods**
- **Watermelon** - -3°C
- **Berries** - -2°C
- **Ice** - -5°C
- **Effect Duration** - 30 seconds

### Temperature Management

**Staying Warm**
- Wear armor
- Eat warm food
- Stay near fire/lava
- Avoid nighttime in cold biomes
- Sleep in shelter

**Staying Cool**
- Wear less armor
- Eat cool food
- Stay near water
- Avoid direct sunlight
- Rest in shade
- Use ice blocks

---

## Wind System (Detailed)

### How Wind Works

**Wind Speed**
- Ranges from 0 to 100 (units)
- Updates every second
- Influenced by weather

**Wind Calculation**
```
Base: Random variation (0-40)
+ Weather bonus (0-60 during storm)
+ Time factor (varies by hour)
= Current wind speed
```

**Wind Direction**
- Randomly determined
- Changes gradually
- Indicated by particle direction
- Visual line renderer shows direction

### Wind Effects

**On Player Movement**
- Moderate wind: -2% speed
- Strong wind: -5% speed
- Gale: -10% speed
- Directional effect only

**On Projectiles**
- Arrows drift with wind
- Snowballs affected
- Projectile speed modified
- Unpredictability factor

**Visual Effects**
- Particle effects indicate wind
- Line renderer shows wind vector
- Clouds move with wind
- Water surface ripples

**Weather Integration**
- Clear: Wind 0-20
- Cloudy: Wind 5-30
- Rain: Wind 20-60
- Thunderstorm: Wind 40-100

### Wind Line Renderer

**Visual Indicator**
- White line on screen
- Points in wind direction
- Length indicates wind strength
- Updates every tick

**Reading Wind**
- Longer line = stronger wind
- Direction of line = wind direction
- No line = no wind
- Useful for archery

### Advanced Mechanics

**Wind & Combat**
- Arrows affected at range
- Projectiles drift more
- Compensation needed for accuracy
- Skill-based gameplay

**Wind & Movement**
- High wind reduces climb ability
- Water becomes choppy
- Knockback increased
- Fall damage affected

---

## Combat System (Detailed)

### Swing Attack Mechanics

**Attack Sequence**
1. Player clicks with weapon
2. Stamina check (need 5+)
3. Stamina deducted
4. Visual hit effect plays
5. Damage calculated
6. Knockback applied
7. Cooldown begins

**Damage Calculation**
```
Base Damage: Weapon type specific
Stamina Multiplier: 0.5-1.5x (based on stamina remaining)
Critical Hit: 1.5x (if enough stamina)
Armor Reduction: Opponent's armor
Final Damage: Base × Stamina × Critical × Armor Reduction
```

**Attack Cooldown**
```
Base: 0.5 seconds
Stamina Penalty: +0.1s per stamina spent
Max Cooldown: 2.0 seconds
Resets on: Hit connecting
```

### Vein Miner

**Activation**
- Sneak + Mine = Vein mine mode
- Works with proper tools
- Pickaxes for ore
- Axes for wood

**Vein Mining Rules**
- Connects blocks of same type
- Maximum 32 blocks per vein
- Same height level preferred
- Works in all directions

**Resource Cost**
- Tool durability: Normal wear × block count
- Time: 10% longer than normal
- Stamina: 2 per block mined
- Efficiency: Faster overall

### Combat Strategy

**Stamina Management**
- Attack when stamina high
- Retreat to rest
- Don't drain completely
- Manage cooldowns

**Positioning**
- Keep distance when low stamina
- Use terrain for advantage
- Circle opponents
- Block with shield

---

## HUD System (Detailed)

### HUD Elements

**Stamina Bar**
- Position: Top-left corner
- Color: Green
- Segments: 10 for each 10%
- Shows depletion in real-time

**Thirst Bar**
- Position: Below stamina
- Color: Blue
- Segments: 10 for each 10%
- Changes color at thresholds

**Temperature Display**
- Position: Top-right corner
- Shows: Current °C
- Color: Blue (cold) to Red (hot)
- Updates constantly

**Status Effects**
- Position: Bottom-left
- Shows: Active effects
- Duration: Time remaining
- Icon: Effect symbol

### HUD Customization

**Toggle Elements**
- Press `H` key to toggle
- Select element to hide/show
- Changes persist

**Repositioning**
- Hold HUD element to drag
- Reposition as desired
- Snap to grid enabled
- Save custom layout

**Scaling**
- Adjust scale 0.5x to 2.0x
- Individual element sizing
- Text size adjustment
- Opacity control (0-100%)

---

## Biome & Weather System (Detailed)

### Biome Temperature Effects

**Temperature Mapping**
Each biome has base temperature that affects player:

- **Frozen River** - -15°C
- **Snowy Plains** - -10°C
- **Birch Forest** - 5°C
- **Plains** - 15°C
- **Forest** - 12°C
- **Desert** - 35°C
- **Savanna** - 25°C
- **Badlands** - 30°C
- **Mountain** - Varies by height
- **Ocean** - 10°C
- **Deep Ocean** - 5°C

### Weather Effects

**Clear Weather**
- Wind: 0-20 units
- Visibility: Maximum
- Temperature: Base
- Movement: Normal

**Cloudy**
- Wind: 5-30 units
- Visibility: Slight reduction
- Temperature: Base - 2°C
- Particle effects: Subtle

**Rain**
- Wind: 20-60 units
- Visibility: Reduced 30%
- Temperature: Base - 5°C
- Sound effects: Rain
- Extinguishes fire

**Thunderstorm**
- Wind: 40-100 units
- Visibility: 50% reduction
- Temperature: Base - 10°C
- Lightning: Danger!
- Intense particle effects

### Environmental Hazards

**Fire Hazard**
- Lava proximity warning
- Campfire damage
- Magma block danger
- Nether hazards

**Water Hazard**
- Drowning timer visible
- Aquatic mob warnings
- Drowning damage
- Suffocation damage

**Fall Damage**
- Shows fall distance
- Displays damage estimate
- Shows safe fall height
- Warns of high jumps

---

**Next:** See [Items & Blocks](Items-and-Blocks) for content listing.
