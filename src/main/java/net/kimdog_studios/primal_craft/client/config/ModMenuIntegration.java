package net.kimdog_studios.primal_craft.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Primal Craft - Mod Menu Integration
 *
 * Enhanced interactive configuration screen with:
 * - Toggle buttons for systems
 * - Cycling buttons for preset values
 * - Text fields for custom values with validation
 * - Real-time value clamping
 * - Visual feedback
 *
 * @author KimDog Studios
 * @version 3.0.0
 * @since 2026-01-28
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }

    /**
     * Enhanced interactive configuration screen with tabbed interface
     */
    public static class ConfigScreen extends Screen {
        private final Screen parent;
        private final PrimalCraftConfig.ConfigData config;

        // Text fields for custom input
        private TextFieldWidget staminaRecoveryField;
        private TextFieldWidget staminaDepletionField;
        private TextFieldWidget thirstDepletionField;
        private TextFieldWidget hudScaleField;
        private TextFieldWidget hudOpacityField;

        // Tab state
        private ConfigTab currentTab = ConfigTab.GAMEPLAY;
        private ConfigPreset currentPreset = ConfigPreset.CUSTOM;

        // Scroll support
        private double scrollOffset = 0;
        private double maxScroll = 0;

        // NEW: Enhanced Features
        private TextFieldWidget searchField; // Search/filter box
        private String searchQuery = ""; // Current search query
        private boolean showOnlyModified = false; // Filter: show only changed settings
        private boolean useDarkTheme = true; // Theme toggle

        // Change tracking & history
        private final Map<String, String> changeHistory = new HashMap<>(); // Track all changes
        private final List<String> changeLog = new ArrayList<>(); // Timestamped changelog
        private static final int MAX_HISTORY_ENTRIES = 50;
        private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Keybind favorites (quick access)
        private final List<String> favoriteSettings = new ArrayList<>();

        /**
         * Configuration tabs
         */
        private enum ConfigTab {
            GAMEPLAY("⚙️  Gameplay", 0xFFAA00),
            HUD("🎨 HUD & Display", 0x55FFFF),
            SYSTEMS("🔬 Systems", 0x00FF99),
            DIFFICULTY("⚔️  Difficulty", 0xFF5555),
            PRESETS("⭐ Presets", 0xFFFF55),
            RESET("🔄 Reset", 0xFF6B6B),
            IMPORT_EXPORT("💾 Import/Export", 0x9D84B7),
            HELP("❓ Help", 0x87CEEB),
            ADVANCED("🔧 Advanced", 0xAA55FF);

            final String label;
            final int color;

            ConfigTab(String label, int color) {
                this.label = label;
                this.color = color;
            }
        }

        /**
         * Pre-configured difficulty presets
         */
        private enum ConfigPreset {
            VANILLA("🌳 Vanilla+", "Minimal changes, vanilla-like", 0.5f, 0.5f, 0.5f, 0.5f),
            EASY("😊 Easy", "Relaxed survival experience", 0.6f, 0.6f, 0.6f, 0.6f),
            NORMAL("⚖️ Normal", "Balanced challenge", 1.0f, 1.0f, 1.0f, 1.0f),
            HARD("😰 Hard", "Challenging survival", 1.5f, 1.5f, 1.5f, 1.5f),
            HARDCORE("💀 Hardcore", "Extreme survival", 2.0f, 2.0f, 2.0f, 2.0f),
            CUSTOM("⚙️ Custom", "Your custom settings", -1f, -1f, -1f, -1f);

            final String label;
            final String description;
            final float stamina;
            final float thirst;
            final float temperature;
            final float hazard;

            ConfigPreset(String label, String description, float stamina, float thirst, float temperature, float hazard) {
                this.label = label;
                this.description = description;
                this.stamina = stamina;
                this.thirst = thirst;
                this.temperature = temperature;
                this.hazard = hazard;
            }

            public void apply(PrimalCraftConfig.ConfigData config) {
                if (this.stamina >= 0) {
                    config.difficulty.staminalossDifficulty = this.stamina;
                    config.difficulty.thirstDifficulty = this.thirst;
                    config.difficulty.temperatureDifficulty = this.temperature;
                    config.difficulty.hazardDifficulty = this.hazard;
                }
            }
        }

        public ConfigScreen(Screen parent) {
            super(Text.translatable("config.primal-craft.title"));
            this.parent = parent;
            // Reload config from file to ensure latest values
            net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig.load();
            this.config = net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig.getConfig();
        }

        @Override
        protected void init() {
            super.init();
            PrimalCraft.LOGGER.info("[MOD_MENU] Opening enhanced tabbed configuration screen");
            PrimalCraft.LOGGER.info("[MOD_MENU] Current config state: {}", config);

            // ============================================================
            // SIDEBAR TABS (Fixed on left side - NON-SCROLLABLE)
            // ============================================================
            int sidebarWidth = 130;
            int sidebarButtonWidth = sidebarWidth - 12;  // Account for padding (same as bottom buttons)
            int tabButtonHeight = 20;  // Same as bottom buttons
            int tabButtonSpacing = 5;  // Same as bottom buttons
            int sidebarY = 50;

            for (int i = 0; i < ConfigTab.values().length; i++) {
                ConfigTab tab = ConfigTab.values()[i];
                int tabY = sidebarY + i * (tabButtonHeight + tabButtonSpacing);  // Stack vertically with spacing
                boolean isActive = tab == currentTab;

                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(tab.label).styled(style ->
                        style.withBold(isActive).withColor(isActive ? tab.color : 0x888888)
                    ),
                    button -> {
                        if (currentTab != tab) {
                            saveAllFields();
                            currentTab = tab;
                            clearChildren();
                            init();
                        }
                    }
                ).dimensions(6, tabY, sidebarButtonWidth, tabButtonHeight).build());  // Match bottom buttons positioning
            }

            // ============================================================
            // CONTENT AREA (Based on selected tab - to the right of sidebar)
            // ============================================================
            int contentStartX = sidebarWidth + 20;  // Start after sidebar
            int contentWidth = this.width - contentStartX - 50;
            int buttonWidth = Math.min(contentWidth, 400);
            int centerX = contentStartX + (contentWidth - buttonWidth) / 2;
            int textFieldWidth = 60;
            int buttonHeight = 20;
            int y = 50 - (int)scrollOffset;  // Apply scroll offset, start at top
            int spacing = 24;

            int initialY = y;  // Track starting Y

            switch (currentTab) {
                case GAMEPLAY -> y = initGameplayTab(centerX, y, buttonWidth, textFieldWidth, buttonHeight, spacing);
                case HUD -> y = initHudTab(centerX, y, buttonWidth, textFieldWidth, buttonHeight, spacing);
                case SYSTEMS -> y = initSystemsTab(centerX, y, buttonWidth, buttonHeight, spacing);
                case DIFFICULTY -> y = initDifficultyTab(centerX, y, buttonWidth, buttonHeight, spacing);
                case PRESETS -> y = initPresetsTab(centerX, y, buttonWidth, buttonHeight, spacing);
                case RESET -> y = initResetTab(centerX, y, buttonWidth, buttonHeight, spacing);
                case IMPORT_EXPORT -> y = initImportExportTab(centerX, y, buttonWidth, buttonHeight, spacing);
                case HELP -> y = initHelpTab(centerX, y, buttonWidth, buttonHeight, spacing);
                case ADVANCED -> y = initAdvancedTab(centerX, y, buttonWidth, buttonHeight, spacing);
            }

            // Calculate max scroll based on content height
            int contentHeight = y - initialY;
            int visibleHeight = this.height - 90;  // Space for top and bottom
            maxScroll = Math.max(0, contentHeight - visibleHeight);

            // ============================================================
            // BOTTOM BUTTONS (Always visible)
            // ============================================================
            initBottomButtons();
        }

        /**
         * Initialize Gameplay tab content
         */
        private int initGameplayTab(int x, int y, int buttonWidth, int textFieldWidth, int buttonHeight, int spacing) {
            // === SYSTEM TOGGLES ===
            // Stamina System Toggle
            var staminaBtn = CyclingButtonWidget.onOffBuilder(config.gameplay.staminaSystemEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Stamina System"),
                (button, value) -> {
                    config.gameplay.staminaSystemEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Stamina System: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            );
            staminaBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Enable/disable the stamina depletion and recovery system")
            ));
            this.addDrawableChild(staminaBtn);
            y += spacing;

            // Thirst System Toggle
            var thirstBtn = CyclingButtonWidget.onOffBuilder(config.gameplay.thirstSystemEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Thirst System"),
                (button, value) -> {
                    config.gameplay.thirstSystemEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Thirst System: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            );
            thirstBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Enable/disable the thirst and dehydration system")
            ));
            this.addDrawableChild(thirstBtn);
            y += spacing;

            // Temperature System Toggle
            var tempBtn = CyclingButtonWidget.onOffBuilder(config.gameplay.temperatureSystemEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Temperature System"),
                (button, value) -> {
                    config.gameplay.temperatureSystemEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Temperature System: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            );
            tempBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Enable/disable temperature effects (hot/cold biomes)")
            ));
            this.addDrawableChild(tempBtn);
            y += spacing;

            // Environmental Hazards Toggle
            var hazardBtn = CyclingButtonWidget.onOffBuilder(config.gameplay.environmentalHazardsEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Environmental Hazards"),
                (button, value) -> {
                    config.gameplay.environmentalHazardsEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Environmental Hazards: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            );
            hazardBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Enable/disable environmental damage (weather, altitude, etc)")
            ));
            this.addDrawableChild(hazardBtn);
            y += spacing;

            // Hunger Overhaul Toggle
            var hungerBtn = CyclingButtonWidget.onOffBuilder(config.gameplay.hungerOverhaulEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Hunger Overhaul System"),
                (button, value) -> {
                    config.gameplay.hungerOverhaulEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Hunger Overhaul: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            );
            hungerBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Enable/disable enhanced hunger system with faster depletion")
            ));
            this.addDrawableChild(hungerBtn);
            y += spacing;

            // Exhaustion System Toggle
            var exhaustionBtn = CyclingButtonWidget.onOffBuilder(config.gameplay.exhaustionEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Exhaustion System"),
                (button, value) -> {
                    config.gameplay.exhaustionEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Exhaustion System: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            );
            exhaustionBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Enable/disable player exhaustion from movement and activities")
            ));
            this.addDrawableChild(exhaustionBtn);
            y += spacing + 10;

            // === STAMINA SETTINGS ===
            // Stamina Recovery Rate
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth - textFieldWidth - 5, buttonHeight,
                "Stamina Recovery",
                config.gameplay.staminaRecoveryRate,
                value -> {
                    config.gameplay.staminaRecoveryRate = value.floatValue();
                    if (staminaRecoveryField != null) {
                        staminaRecoveryField.setText(String.format("%.2f", value));
                    }
                }
            ));

            staminaRecoveryField = createNumberField(
                x + buttonWidth - textFieldWidth, y, textFieldWidth, buttonHeight,
                String.format("%.2f", config.gameplay.staminaRecoveryRate),
                value -> config.gameplay.staminaRecoveryRate = clamp(value, 0.1f, 5.0f)
            );
            this.addDrawableChild(staminaRecoveryField);
            y += spacing;

            // Stamina Depletion Rate
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth - textFieldWidth - 5, buttonHeight,
                "Stamina Depletion",
                config.gameplay.staminaDepletionRate,
                value -> {
                    config.gameplay.staminaDepletionRate = value.floatValue();
                    if (staminaDepletionField != null) {
                        staminaDepletionField.setText(String.format("%.2f", value));
                    }
                }
            ));

            staminaDepletionField = createNumberField(
                x + buttonWidth - textFieldWidth, y, textFieldWidth, buttonHeight,
                String.format("%.2f", config.gameplay.staminaDepletionRate),
                value -> config.gameplay.staminaDepletionRate = clamp(value, 0.1f, 5.0f)
            );
            this.addDrawableChild(staminaDepletionField);
            y += spacing;

            // Sprint Stamina Cost
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Sprint Stamina Cost",
                config.gameplay.sprintStaminaCost,
                value -> config.gameplay.sprintStaminaCost = value.floatValue()
            ));
            y += spacing;

            // Jump Stamina Cost
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Jump Stamina Cost",
                config.gameplay.jumpStaminaCost,
                value -> config.gameplay.jumpStaminaCost = value.floatValue()
            ));
            y += spacing;

            // Attack Stamina Cost
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Attack Stamina Cost",
                config.gameplay.attackStaminaCost,
                value -> config.gameplay.attackStaminaCost = value.floatValue()
            ));
            y += spacing;

            // Max Stamina
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Max Stamina",
                config.gameplay.maxStamina,
                value -> config.gameplay.maxStamina = value.intValue()
            ));
            y += spacing;

            // Stamina Regen While Sprinting
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.gameplay.staminaRegenWhileSprinting)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Regen While Sprinting"),
                (button, value) -> config.gameplay.staminaRegenWhileSprinting = value
            ));
            y += spacing + 10;

            // === THIRST SETTINGS ===
            // Thirst Depletion Rate
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth - textFieldWidth - 5, buttonHeight,
                "Thirst Depletion",
                config.gameplay.thirstDepletionRate,
                value -> {
                    config.gameplay.thirstDepletionRate = value.floatValue();
                    if (thirstDepletionField != null) {
                        thirstDepletionField.setText(String.format("%.2f", value));
                    }
                }
            ));

            thirstDepletionField = createNumberField(
                x + buttonWidth - textFieldWidth, y, textFieldWidth, buttonHeight,
                String.format("%.2f", config.gameplay.thirstDepletionRate),
                value -> config.gameplay.thirstDepletionRate = clamp(value, 0.1f, 5.0f)
            );
            this.addDrawableChild(thirstDepletionField);
            y += spacing;

            // Sprint Thirst Multiplier
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Sprint Thirst Multiplier",
                config.gameplay.sprintThirstMultiplier,
                value -> config.gameplay.sprintThirstMultiplier = value.floatValue()
            ));
            y += spacing;

            // Hot Biome Thirst Multiplier
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Hot Biome Thirst Multiplier",
                config.gameplay.hotBiomeThirstMultiplier,
                value -> config.gameplay.hotBiomeThirstMultiplier = value.floatValue()
            ));
            y += spacing;

            // Max Thirst
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Max Thirst",
                config.gameplay.maxThirst,
                value -> config.gameplay.maxThirst = value.intValue()
            ));
            y += spacing;

            // Thirst Affects Health
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.gameplay.thirstAffectsHealth)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Thirst Affects Health"),
                (button, value) -> config.gameplay.thirstAffectsHealth = value
            ));
            y += spacing + 10;

            // === TEMPERATURE SETTINGS ===
            // Temperature Change Rate
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Temperature Change Rate",
                config.gameplay.temperatureChangeRate,
                value -> config.gameplay.temperatureChangeRate = value.floatValue()
            ));
            y += spacing;

            // Temperature Affects Health
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.gameplay.temperatureAffectsHealth)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Temperature Affects Health"),
                (button, value) -> config.gameplay.temperatureAffectsHealth = value
            ));
            y += spacing;

            // Cold Damage
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Cold Damage",
                config.gameplay.coldDamage,
                value -> config.gameplay.coldDamage = value.floatValue()
            ));
            y += spacing;

            // Heat Damage
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Heat Damage",
                config.gameplay.heatDamage,
                value -> config.gameplay.heatDamage = value.floatValue()
            ));
            y += spacing;

            // Clothing Affects Temperature
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.gameplay.clothingAffectsTemperature)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Clothing Affects Temperature"),
                (button, value) -> config.gameplay.clothingAffectsTemperature = value
            ));
            y += spacing + 10;

            // === HUNGER SETTINGS ===
            // Hunger Depletion Multiplier
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Hunger Depletion",
                config.gameplay.hungerDepletionMultiplier,
                value -> config.gameplay.hungerDepletionMultiplier = value.floatValue()
            ));
            y += spacing;

            // Saturation Multiplier
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Saturation Multiplier",
                config.gameplay.saturationMultiplier,
                value -> config.gameplay.saturationMultiplier = value.floatValue()
            ));
            y += spacing;

            // Natural Regeneration
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.gameplay.naturalRegeneration)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Natural Regeneration"),
                (button, value) -> config.gameplay.naturalRegeneration = value
            ));
            y += spacing;

            return y + spacing;  // Return final Y position
        }

        /**
         * Initialize HUD tab content
         */
        private int initHudTab(int x, int y, int buttonWidth, int textFieldWidth, int buttonHeight, int spacing) {
            // === BAR VISIBILITY ===
            // Show Stamina Bar
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.hud.showStaminaBar)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Show Stamina Bar"),
                (button, value) -> {
                    config.hud.showStaminaBar = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Show Stamina Bar: " + (value ? "✓ SHOWN" : "✗ HIDDEN"));
                }
            ));
            y += spacing;

            // Show Thirst Bar
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.hud.showThirstBar)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Show Thirst Bar"),
                (button, value) -> {
                    config.hud.showThirstBar = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Show Thirst Bar: " + (value ? "✓ SHOWN" : "✗ HIDDEN"));
                }
            ));
            y += spacing;

            // Show Temperature Indicator
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.hud.showTemperatureIndicator)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Show Temperature Indicator"),
                (button, value) -> config.hud.showTemperatureIndicator = value
            ));
            y += spacing;

            // Show Effect Icons
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.hud.showEffectIcons)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Show Effect Icons"),
                (button, value) -> config.hud.showEffectIcons = value
            ));
            y += spacing;

            // Show Weather Notifications
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.hud.showWeatherNotifications)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Show Weather Notifications"),
                (button, value) -> config.hud.showWeatherNotifications = value
            ));
            y += spacing;

            // Show Biome Notifications
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.hud.showBiomeNotifications)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Show Biome Notifications"),
                (button, value) -> config.hud.showBiomeNotifications = value
            ));
            y += spacing;

            // Show Debug Info
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.hud.showDebugInfo)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Show Debug Info"),
                (button, value) -> config.hud.showDebugInfo = value
            ));
            y += spacing + 10;

            // === APPEARANCE ===
            // HUD Scale (0.5 - 3.0)
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth - textFieldWidth - 5, buttonHeight,
                "HUD Scale",
                config.hud.hudScale,
                value -> {
                    config.hud.hudScale = value.floatValue();
                    if (hudScaleField != null) {
                        hudScaleField.setText(String.format("%.2f", value));
                    }
                }
            ));

            hudScaleField = createNumberField(
                x + buttonWidth - textFieldWidth, y, textFieldWidth, buttonHeight,
                String.format("%.2f", config.hud.hudScale),
                value -> config.hud.hudScale = clamp(value, 0.5f, 3.0f)
            );
            this.addDrawableChild(hudScaleField);
            y += spacing;

            // HUD Opacity (0.1 - 1.0)
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth - textFieldWidth - 5, buttonHeight,
                "HUD Opacity",
                config.hud.hudOpacity,
                value -> {
                    config.hud.hudOpacity = value.floatValue();
                    if (hudOpacityField != null) {
                        hudOpacityField.setText(String.format("%.2f", value));
                    }
                }
            ));

            hudOpacityField = createNumberField(
                x + buttonWidth - textFieldWidth, y, textFieldWidth, buttonHeight,
                String.format("%.2f", config.hud.hudOpacity),
                value -> config.hud.hudOpacity = clamp(value, 0.1f, 1.0f)
            );
            this.addDrawableChild(hudOpacityField);
            y += spacing;

            // HUD X Offset
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "HUD X Offset",
                config.hud.hudXOffset,
                value -> config.hud.hudXOffset = value.intValue()
            ));
            y += spacing;

            // HUD Y Offset
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "HUD Y Offset",
                config.hud.hudYOffset,
                value -> config.hud.hudYOffset = value.intValue()
            ));
            y += spacing + 10;

            // === ANIMATIONS ===
            // Enable Bar Animations
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.hud.enableBarAnimations)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Enable Bar Animations"),
                (button, value) -> config.hud.enableBarAnimations = value
            ));
            y += spacing;

            // Enable Warning Flash
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.hud.enableWarningFlash)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Enable Warning Flash"),
                (button, value) -> config.hud.enableWarningFlash = value
            ));
            y += spacing;

            // Animation Speed
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Animation Speed",
                config.hud.animationSpeed,
                value -> config.hud.animationSpeed = value.floatValue()
            ));
            y += spacing + 10;

            // === TEXT DISPLAY ===
            // Show Numeric Values
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.hud.showNumericValues)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Show Numeric Values"),
                (button, value) -> config.hud.showNumericValues = value
            ));
            y += spacing;

            // Show Percentages
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.hud.showPercentages)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Show Percentages"),
                (button, value) -> config.hud.showPercentages = value
            ));
            y += spacing;

            // Show Labels
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.hud.showLabels)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Show Labels"),
                (button, value) -> config.hud.showLabels = value
            ));
            y += spacing;

            return y + spacing;  // Return final Y position
        }

        /**
         * Initialize Difficulty tab content
         */
        private int initDifficultyTab(int x, int y, int buttonWidth, int buttonHeight, int spacing) {
            // === CORE DIFFICULTY ===
            // Stamina Difficulty
            this.addDrawableChild(createDifficultyButton(
                x, y, buttonWidth, buttonHeight,
                "Stamina Loss",
                config.difficulty.staminalossDifficulty,
                value -> config.difficulty.staminalossDifficulty = value.floatValue()
            ));
            y += spacing;

            // Thirst Difficulty
            this.addDrawableChild(createDifficultyButton(
                x, y, buttonWidth, buttonHeight,
                "Thirst Difficulty",
                config.difficulty.thirstDifficulty,
                value -> config.difficulty.thirstDifficulty = value.floatValue()
            ));
            y += spacing;

            // Temperature Difficulty
            this.addDrawableChild(createDifficultyButton(
                x, y, buttonWidth, buttonHeight,
                "Temperature",
                config.difficulty.temperatureDifficulty,
                value -> config.difficulty.temperatureDifficulty = value.floatValue()
            ));
            y += spacing;

            // Hazard Difficulty
            this.addDrawableChild(createDifficultyButton(
                x, y, buttonWidth, buttonHeight,
                "Hazards",
                config.difficulty.hazardDifficulty,
                value -> config.difficulty.hazardDifficulty = value.floatValue()
            ));
            y += spacing + 10;

            // === DAMAGE MULTIPLIERS ===
            // Environmental Damage
            this.addDrawableChild(createDifficultyButton(
                x, y, buttonWidth, buttonHeight,
                "Environmental Damage",
                config.difficulty.environmentalDamageMultiplier,
                value -> config.difficulty.environmentalDamageMultiplier = value.floatValue()
            ));
            y += spacing;

            // Dehydration Damage
            this.addDrawableChild(createDifficultyButton(
                x, y, buttonWidth, buttonHeight,
                "Dehydration Damage",
                config.difficulty.dehydrationDamageMultiplier,
                value -> config.difficulty.dehydrationDamageMultiplier = value.floatValue()
            ));
            y += spacing;

            // Freezing Damage
            this.addDrawableChild(createDifficultyButton(
                x, y, buttonWidth, buttonHeight,
                "Freezing Damage",
                config.difficulty.freezingDamageMultiplier,
                value -> config.difficulty.freezingDamageMultiplier = value.floatValue()
            ));
            y += spacing;

            // Heatstroke Damage
            this.addDrawableChild(createDifficultyButton(
                x, y, buttonWidth, buttonHeight,
                "Heatstroke Damage",
                config.difficulty.heatstrokeDamageMultiplier,
                value -> config.difficulty.heatstrokeDamageMultiplier = value.floatValue()
            ));
            y += spacing + 10;

            // === SURVIVAL CHALLENGES ===
            // Hardcore Temperature
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.difficulty.enableHardcoreTemperature)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Hardcore Temperature"),
                (button, value) -> config.difficulty.enableHardcoreTemperature = value
            ));
            y += spacing;

            // Realistic Thirst
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.difficulty.enableRealisticThirst)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Realistic Thirst"),
                (button, value) -> config.difficulty.enableRealisticThirst = value
            ));
            y += spacing;

            // Extreme Weather
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.difficulty.enableExtremeWeather)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Extreme Weather"),
                (button, value) -> config.difficulty.enableExtremeWeather = value
            ));
            y += spacing;

            // Seasonal Difficulty
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.difficulty.enableSeasonalDifficulty)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Seasonal Difficulty"),
                (button, value) -> config.difficulty.enableSeasonalDifficulty = value
            ));
            y += spacing;

            return y;  // Return final Y position
        }

        /**
         * Initialize Systems tab content (Zoom, Veinminer, etc.)
         */
        private int initSystemsTab(int x, int y, int buttonWidth, int buttonHeight, int spacing) {
            // === ZOOM SYSTEM ===
            var zoomToggle = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.systems.zoomEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("🔍 Zoom System"),
                (button, value) -> {
                    config.systems.zoomEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Zoom System: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            ));
            zoomToggle.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Enable/disable the zoom feature (Scroll to zoom in/out)")
            ));
            y += spacing;

            // Zoom Sensitivity
            var zoomSensitivity = createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Zoom Sensitivity",
                config.systems.zoomSensitivity,
                value -> config.systems.zoomSensitivity = value.floatValue()
            );
            zoomSensitivity.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Adjust how fast zoom responds to scroll input")
            ));
            this.addDrawableChild(zoomSensitivity);
            y += spacing;

            // Zoom Speed
            var zoomSpeed = createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Zoom Speed",
                config.systems.zoomSpeed,
                value -> config.systems.zoomSpeed = value.floatValue()
            );
            zoomSpeed.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Control how fast zoom animation occurs")
            ));
            this.addDrawableChild(zoomSpeed);
            y += spacing + 10;

            // === VEINMINER SYSTEM ===
            var veinminerToggle = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.systems.veinminerEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("⛏️ Veinminer"),
                (button, value) -> {
                    config.systems.veinminerEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Veinminer: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            ));
            veinminerToggle.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Enable/disable veinmining (break connected ore blocks)")
            ));
            y += spacing;

            // Veinminer Max Blocks
            var maxBlocks = createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Max Blocks per Vein",
                config.systems.veinminerMaxBlocks,
                value -> config.systems.veinminerMaxBlocks = value.intValue()
            );
            maxBlocks.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Maximum blocks to break in one veinmine action")
            ));
            this.addDrawableChild(maxBlocks);
            y += spacing;

            // Veinminer Speed
            var veinSpeed = createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Veinminer Speed",
                config.systems.veinminerSpeed,
                value -> config.systems.veinminerSpeed = value.floatValue()
            );
            veinSpeed.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("How fast veinminer breaks multiple blocks")
            ));
            this.addDrawableChild(veinSpeed);
            y += spacing;

            // Veinminer Only Ores
            var oresOnly = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.systems.veinminerOresOnly)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Ores Only"),
                (button, value) -> {
                    config.systems.veinminerOresOnly = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Veinminer Ores Only: " + (value ? "✓ ON" : "✗ OFF"));
                }
            ));
            oresOnly.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Only veinmine ore blocks (not stone or other blocks)")
            ));
            y += spacing;

            return y;  // Return final Y position
        }

        /**
         * Initialize Presets tab content
         */
        private int initPresetsTab(int x, int y, int buttonWidth, int buttonHeight, int spacing) {
            // === PRESET PROFILES ===
            for (ConfigPreset preset : ConfigPreset.values()) {
                if (preset == ConfigPreset.CUSTOM) continue;  // Skip custom

                final ConfigPreset presetRef = preset;
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(preset.label + " - " + preset.description),
                    button -> {
                        // Apply preset
                        presetRef.apply(config);
                        currentPreset = presetRef;
                        button.setMessage(Text.literal(preset.label + " ✓"));
                        PrimalCraft.LOGGER.info("[CONFIG] Preset loaded: {} - {}", preset.label, preset.description);

                        // Save config
                        saveAllFields();
                        PrimalCraftConfig.save();
                    }
                ).dimensions(x, y, buttonWidth, buttonHeight).build());

                y += spacing;
            }

            y += 10;

            // === CURRENT PRESET STATUS ===
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Active Preset: " + currentPreset.label),
                button -> {}
            ).dimensions(x, y, buttonWidth, buttonHeight).build());
            y += spacing + 10;

            // === RESET TO CUSTOM ===
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Reset to Custom Settings"),
                button -> {
                    currentPreset = ConfigPreset.CUSTOM;
                    button.setMessage(Text.literal("Reset to Custom ✓"));
                    PrimalCraft.LOGGER.info("[CONFIG] Preset reset to CUSTOM");
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build());
            y += spacing;

            return y;  // Return final Y position
        }

        /**
         * Initialize Reset tab content - Quick reset individual systems
         */
        private int initResetTab(int x, int y, int buttonWidth, int buttonHeight, int spacing) {
            y += 10;

            // === RESET INDIVIDUAL SYSTEMS ===
            var resetGameplay = ButtonWidget.builder(
                Text.literal("↺ Reset Gameplay Settings"),
                button -> {
                    config.gameplay.staminaSystemEnabled = true;
                    config.gameplay.thirstSystemEnabled = true;
                    config.gameplay.temperatureSystemEnabled = true;
                    config.gameplay.environmentalHazardsEnabled = true;
                    config.gameplay.hungerOverhaulEnabled = true;
                    config.gameplay.exhaustionEnabled = true;
                    logChange("Gameplay settings reset to defaults");
                    PrimalCraft.LOGGER.info("[CONFIG] ✓ Gameplay reset");
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build();
            resetGameplay.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Reset all gameplay system toggles to defaults")
            ));
            this.addDrawableChild(resetGameplay);
            y += spacing;

            var resetHUD = ButtonWidget.builder(
                Text.literal("↺ Reset HUD Settings"),
                button -> {
                    config.hud.showStaminaBar = true;
                    config.hud.showThirstBar = true;
                    config.hud.hudScale = 1.0f;
                    config.hud.hudOpacity = 1.0f;
                    logChange("HUD settings reset to defaults");
                    PrimalCraft.LOGGER.info("[CONFIG] ✓ HUD reset");
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build();
            resetHUD.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Reset all HUD display settings to defaults")
            ));
            this.addDrawableChild(resetHUD);
            y += spacing;

            var resetSystems = ButtonWidget.builder(
                Text.literal("↺ Reset System Settings"),
                button -> {
                    config.systems.zoomEnabled = true;
                    config.systems.zoomSensitivity = 1.0f;
                    config.systems.zoomSpeed = 1.0f;
                    config.systems.veinminerEnabled = true;
                    config.systems.veinminerMaxBlocks = 64;
                    config.systems.veinminerSpeed = 1.0f;
                    config.systems.veinminerOresOnly = true;
                    logChange("System settings reset to defaults");
                    PrimalCraft.LOGGER.info("[CONFIG] ✓ Systems reset");
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build();
            resetSystems.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Reset zoom and veinminer settings to defaults")
            ));
            this.addDrawableChild(resetSystems);
            y += spacing;

            var resetDifficulty = ButtonWidget.builder(
                Text.literal("↺ Reset Difficulty"),
                button -> {
                    config.difficulty.staminalossDifficulty = 1.0f;
                    config.difficulty.thirstDifficulty = 1.0f;
                    config.difficulty.temperatureDifficulty = 1.0f;
                    config.difficulty.hazardDifficulty = 1.0f;
                    logChange("Difficulty settings reset to defaults");
                    PrimalCraft.LOGGER.info("[CONFIG] ✓ Difficulty reset");
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build();
            resetDifficulty.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Reset all difficulty multipliers to defaults")
            ));
            this.addDrawableChild(resetDifficulty);
            y += spacing + 10;

            // === RESET ALL ===
            var resetAll = ButtonWidget.builder(
                Text.literal("⚠️  RESET EVERYTHING"),
                button -> {
                    resetToDefaults();
                    logChange("ALL settings reset to defaults");
                    PrimalCraft.LOGGER.info("[CONFIG] ⚠ COMPLETE RESET");
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build();
            resetAll.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("WARNING: Reset all configuration to factory defaults!")
            ));
            this.addDrawableChild(resetAll);
            y += spacing;

            return y;
        }

        /**
         * Initialize Import/Export tab - Backup and share configurations
         */
        private int initImportExportTab(int x, int y, int buttonWidth, int buttonHeight, int spacing) {
            y += 10;

            // === EXPORT CONFIG ===
            var exportBtn = ButtonWidget.builder(
                Text.literal("💾 Export Config"),
                button -> {
                    exportConfig();
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build();
            exportBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Save current config to file for backup/sharing")
            ));
            this.addDrawableChild(exportBtn);
            y += spacing;

            // === IMPORT CONFIG ===
            var importBtn = ButtonWidget.builder(
                Text.literal("📥 Import Config"),
                button -> {
                    importConfig();
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build();
            importBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Load config from file")
            ));
            this.addDrawableChild(importBtn);
            y += spacing + 10;

            // === CHANGE HISTORY ===
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("📋 View Change Log"),
                button -> {
                    String log = String.join("\n", changeLog);
                    PrimalCraft.LOGGER.info("[CONFIG] Recent changes:\n{}", log);
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build());
            y += spacing;

            return y;
        }

        /**
         * Initialize Help tab - Instructions and features
         */
        private int initHelpTab(int x, int y, int buttonWidth, int buttonHeight, int spacing) {
            y += 10;

            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("About Primal Craft v1.0.1"),
                button -> {}
            ).dimensions(x, y, buttonWidth, buttonHeight).build());
            y += spacing;

            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("📚 Controls & Keybinds"),
                button -> {
                    PrimalCraft.LOGGER.info("[HELP] Left-click: Change setting | Right-click: Reset | Scroll: Navigate");
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build());
            y += spacing;

            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("🎮 Toggle Dark Theme"),
                button -> {
                    useDarkTheme = !useDarkTheme;
                    button.setMessage(Text.literal("🎮 " + (useDarkTheme ? "Dark" : "Light") + " Theme"));
                    PrimalCraft.LOGGER.info("[CONFIG] Theme: " + (useDarkTheme ? "DARK" : "LIGHT"));
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build());
            y += spacing;

            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("❓ Show Tips"),
                button -> {
                    PrimalCraft.LOGGER.info("[HELP] Tip: Use search to find options quickly!");
                    PrimalCraft.LOGGER.info("[HELP] Tip: Presets apply all difficulty settings at once!");
                    PrimalCraft.LOGGER.info("[HELP] Tip: Right-click any setting to reset it!");
                }
            ).dimensions(x, y, buttonWidth, buttonHeight).build());
            y += spacing;

            return y;
        }

        /**
         * Log a configuration change with timestamp
         */
        private void logChange(String change) {
            String timestamp = LocalDateTime.now().format(TIME_FORMAT);
            String logEntry = "[" + timestamp + "] " + change;
            changeLog.add(logEntry);

            // Keep only last MAX_HISTORY_ENTRIES
            if (changeLog.size() > MAX_HISTORY_ENTRIES) {
                changeLog.remove(0);
            }

            PrimalCraft.LOGGER.info("[CONFIG_HISTORY] {}", logEntry);
        }

        /**
         * Export current config to a backup file
         */
        private void exportConfig() {
            try {
                Path backupDir = Paths.get("config/primal-craft/backups");
                Files.createDirectories(backupDir);

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                Path backupFile = backupDir.resolve("config_backup_" + timestamp + ".json");

                String configJson = PrimalCraftConfig.toJson(config);
                Files.write(backupFile, configJson.getBytes());

                logChange("Config exported to " + backupFile.getFileName());
                PrimalCraft.LOGGER.info("[CONFIG] ✓ Config exported to: {}", backupFile);
            } catch (IOException e) {
                PrimalCraft.LOGGER.error("[CONFIG] ✗ Export failed", e);
                logChange("Export failed: " + e.getMessage());
            }
        }

        /**
         * Import config from a backup file
         */
        private void importConfig() {
            try {
                Path backupDir = Paths.get("config/primal-craft/backups");
                if (!Files.exists(backupDir)) {
                    PrimalCraft.LOGGER.warn("[CONFIG] No backups found");
                    return;
                }

                var backups = Files.list(backupDir)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted((a, b) -> b.compareTo(a))
                    .limit(1)
                    .collect(Collectors.toList());

                if (!backups.isEmpty()) {
                    String configJson = Files.readString(backups.get(0));
                    PrimalCraftConfig.fromJson(configJson, config);
                    logChange("Config imported from " + backups.get(0).getFileName());
                    PrimalCraft.LOGGER.info("[CONFIG] ✓ Config imported from: {}", backups.get(0));
                }
            } catch (IOException e) {
                PrimalCraft.LOGGER.error("[CONFIG] ✗ Import failed", e);
                logChange("Import failed: " + e.getMessage());
            }
        }

        /**
         * Initialize Advanced tab content
         */
        private int initAdvancedTab(int x, int y, int buttonWidth, int buttonHeight, int spacing) {
            // === GENERAL FEATURES ===
            // Web Dashboard Toggle
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.webDashboardEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("🌐 Web Dashboard"),
                (button, value) -> {
                    config.webDashboardEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Web Dashboard: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            ));
            y += spacing;

            // Auto-save Config Toggle
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.autoSaveConfig)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("💾 Auto-Save Config"),
                (button, value) -> {
                    config.autoSaveConfig = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Auto-Save: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            ));
            y += spacing;

            // Show Tooltips Toggle
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.showDetailedTooltips)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("ℹ️ Detailed Tooltips"),
                (button, value) -> {
                    config.showDetailedTooltips = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Detailed Tooltips: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            ));
            y += spacing;

            // Debug Mode Toggle
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.debugMode)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("🐛 Debug Mode"),
                (button, value) -> {
                    config.debugMode = value;
                    PrimalCraftConfig.setDebugMode(value);
                    PrimalCraft.LOGGER.info("[CONFIG] Debug Mode: " + (value ? "✓ ON" : "✗ OFF"));
                }
            ));
            y += spacing + 10;

            // === PERFORMANCE ===
            // Particle Effects
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.enableParticleEffects)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("✨ Particle Effects"),
                (button, value) -> config.enableParticleEffects = value
            ));
            y += spacing;

            // Sound Effects
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.enableSoundEffects)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("🔊 Sound Effects"),
                (button, value) -> config.enableSoundEffects = value
            ));
            y += spacing;

            // Update Frequency
            this.addDrawableChild(createPresetButton(
                x, y, buttonWidth, buttonHeight,
                "Update Frequency (ticks)",
                config.updateFrequency,
                value -> config.updateFrequency = value.intValue()
            ));
            y += spacing + 10;

            // === COMPATIBILITY ===
            // Compatibility Mode
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.compatibilityMode)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("🛡️ Compatibility Mode"),
                (button, value) -> config.compatibilityMode = value
            ));
            y += spacing;

            // Disable Vanilla Conflicts
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.disableVanillaConflicts)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("⚠️ Disable Vanilla Conflicts"),
                (button, value) -> config.disableVanillaConflicts = value
            ));
            y += spacing + 10;

            // === NOTIFICATIONS ===
            // Chat Notifications
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.enableChatNotifications)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("💬 Chat Notifications"),
                (button, value) -> config.enableChatNotifications = value
            ));
            y += spacing;

            // Screen Notifications
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.enableScreenNotifications)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("📺 Screen Notifications"),
                (button, value) -> config.enableScreenNotifications = value
            ));
            y += spacing;

            // Sound Notifications
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.enableSoundNotifications)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("🔔 Sound Notifications"),
                (button, value) -> config.enableSoundNotifications = value
            ));
            y += spacing;

            return y;  // Return final Y position
        }

        /**
         * Initialize bottom action buttons
         */
        private void initBottomButtons() {
            int sidebarWidth = 130;
            int sidebarButtonWidth = sidebarWidth - 12;  // Account for padding
            int buttonHeight = 20;
            int buttonSpacing = 5;

            // Position buttons at the bottom of the sidebar (going up from bottom)
            int startY = this.height - 90;  // Start from bottom

            // Done button (top of button group)
            this.addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE,
                button -> {
                    saveAllFields();
                    PrimalCraftConfig.save();
                    if (this.client != null) {
                        this.client.setScreen(this.parent);
                    }
                }
            ).dimensions(6, startY, sidebarButtonWidth, buttonHeight).build());

            // Save button
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("💾 Save").styled(style -> style.withColor(0x55FF55)),
                button -> {
                    saveAllFields();
                    PrimalCraftConfig.save();
                    PrimalCraft.LOGGER.info("[CONFIG] ✓ Configuration saved!");
                    if (this.client != null && this.client.player != null) {
                        this.client.player.sendMessage(
                            Text.literal("✓ Configuration saved!").styled(style -> style.withColor(0x55FF55)),
                            false
                        );
                    }
                }
            ).dimensions(6, startY + buttonHeight + buttonSpacing, sidebarButtonWidth, buttonHeight).build());

            // Reset to Defaults button (bottom of button group)
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("↻ Reset").styled(style -> style.withColor(0xFFAA00)),
                button -> {
                    resetToDefaults();
                    if (this.client != null) {
                        this.client.setScreen(new ConfigScreen(parent));
                    }
                    PrimalCraft.LOGGER.info("[CONFIG] ⚠ Reset to default values");
                }
            ).dimensions(6, startY + (buttonHeight + buttonSpacing) * 2, sidebarButtonWidth, buttonHeight).build());
        }

        /**
         * Create a preset cycling button for common rate values (left-click cycles, right-click resets)
         */
        private ButtonWidget createPresetButton(int x, int y, int width, int height,
                                                String label, double currentValue,
                                                java.util.function.Consumer<Double> onChange) {
            // Store the default value
            final double defaultValue = 1.0;

            ButtonWidget button = ButtonWidget.builder(
                Text.literal(label + ": " + getPresetLabel(currentValue)),
                btn -> {
                    // Left-click handler - cycle through presets
                    double newValue = cyclePreset(currentValue);
                    onChange.accept(newValue);
                    btn.setMessage(Text.literal(label + ": " + getPresetLabel(newValue)));
                    PrimalCraft.LOGGER.info("[CONFIG] {} cycled to {} ({}x)", label, getPresetLabel(newValue), String.format("%.2f", newValue));
                }
            ).dimensions(x, y, width, height).build();

            // Note: Right-click reset needs to be handled at the Screen level via mouseClicked override
            // Store button metadata for right-click handling
            button.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Left-click: Cycle values\nRight-click: Reset to " + getPresetLabel(defaultValue))
            ));

            return button;
        }

        /**
         * Create a difficulty-specific cycling button with descriptive labels (left-click cycles, right-click resets)
         */
        private ButtonWidget createDifficultyButton(int x, int y, int width, int height,
                                                    String label, double currentValue,
                                                    java.util.function.Consumer<Double> onChange) {
            // Store the default value
            final double defaultValue = 1.0;

            ButtonWidget button = ButtonWidget.builder(
                Text.literal(label + ": " + getDifficultyLabel(currentValue)),
                btn -> {
                    // Left-click handler - cycle through difficulties
                    double newValue = cycleDifficulty(currentValue);
                    onChange.accept(newValue);
                    btn.setMessage(Text.literal(label + ": " + getDifficultyLabel(newValue)));
                    PrimalCraft.LOGGER.info("[CONFIG] {} set to {} ({}x)", label, getDifficultyLabel(newValue), String.format("%.2f", newValue));
                }
            ).dimensions(x, y, width, height).build();

            // Add tooltip for instructions
            button.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Left-click: Cycle difficulties\nRight-click: Reset to " + getDifficultyLabel(defaultValue))
            ));

            return button;
        }

        /**
         * Create a validated number input field
         */
        private TextFieldWidget createNumberField(int x, int y, int width, int height,
                                                  String initialValue,
                                                  java.util.function.Consumer<Float> onChange) {
            TextFieldWidget field = new TextFieldWidget(
                this.textRenderer,
                x, y, width, height,
                Text.literal("Number Input")
            );

            field.setMaxLength(6);
            field.setText(initialValue);
            field.setChangedListener(text -> {
                try {
                    if (!text.isEmpty()) {
                        float value = Float.parseFloat(text);
                        onChange.accept(value);
                    }
                } catch (NumberFormatException e) {
                    // Invalid input, ignore
                }
            });

            return field;
        }

        /**
         * Cycle through preset values: 0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 2.5, 3.0
         */
        private double cyclePreset(double current) {
            double[] presets = {0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 2.5, 3.0};

            for (int i = 0; i < presets.length; i++) {
                if (Math.abs(current - presets[i]) < 0.01) {
                    return presets[(i + 1) % presets.length];
                }
            }
            return 1.0; // Default if not found
        }

        /**
         * Cycle through difficulty presets: 0.5 (Easy), 1.0 (Normal), 1.5 (Hard), 2.0 (Very Hard), 2.5 (Nightmare)
         */
        private double cycleDifficulty(double current) {
            double[] difficulties = {0.5, 1.0, 1.5, 2.0, 2.5};

            for (int i = 0; i < difficulties.length; i++) {
                if (Math.abs(current - difficulties[i]) < 0.01) {
                    return difficulties[(i + 1) % difficulties.length];
                }
            }
            return 1.0; // Default if not found
        }

        /**
         * Get descriptive label for preset value
         */
        private String getPresetLabel(double value) {
            if (Math.abs(value - 0.25) < 0.01) return "Very Slow (0.25x)";
            if (Math.abs(value - 0.5) < 0.01) return "Slow (0.5x)";
            if (Math.abs(value - 0.75) < 0.01) return "Reduced (0.75x)";
            if (Math.abs(value - 1.0) < 0.01) return "Normal (1.0x)";
            if (Math.abs(value - 1.25) < 0.01) return "Increased (1.25x)";
            if (Math.abs(value - 1.5) < 0.01) return "Fast (1.5x)";
            if (Math.abs(value - 2.0) < 0.01) return "Very Fast (2.0x)";
            if (Math.abs(value - 2.5) < 0.01) return "Extreme (2.5x)";
            if (Math.abs(value - 3.0) < 0.01) return "Maximum (3.0x)";
            return String.format("Custom (%.2fx)", value);
        }

        /**
         * Get descriptive label for difficulty value
         */
        private String getDifficultyLabel(double value) {
            if (Math.abs(value - 0.5) < 0.01) return "Easy (0.5x)";
            if (Math.abs(value - 1.0) < 0.01) return "Normal (1.0x)";
            if (Math.abs(value - 1.5) < 0.01) return "Hard (1.5x)";
            if (Math.abs(value - 2.0) < 0.01) return "Very Hard (2.0x)";
            if (Math.abs(value - 2.5) < 0.01) return "Nightmare (2.5x)";
            return String.format("Custom (%.2fx)", value);
        }

        /**
         * Clamp a value between min and max
         */
        private float clamp(float value, float min, float max) {
            if (value < min) {
                PrimalCraft.LOGGER.warn("[CONFIG] Value {} below minimum {}, clamping", value, min);
                return min;
            }
            if (value > max) {
                PrimalCraft.LOGGER.warn("[CONFIG] Value {} above maximum {}, clamping", value, max);
                return max;
            }
            return value;
        }

        /**
         * Save all text field values
         */
        private void saveAllFields() {
            if (staminaRecoveryField != null && !staminaRecoveryField.getText().isEmpty()) {
                try {
                    float value = Float.parseFloat(staminaRecoveryField.getText());
                    config.gameplay.staminaRecoveryRate = clamp(value, 0.1f, 5.0f);
                } catch (NumberFormatException ignored) {}
            }

            if (staminaDepletionField != null && !staminaDepletionField.getText().isEmpty()) {
                try {
                    float value = Float.parseFloat(staminaDepletionField.getText());
                    config.gameplay.staminaDepletionRate = clamp(value, 0.1f, 5.0f);
                } catch (NumberFormatException ignored) {}
            }

            if (thirstDepletionField != null && !thirstDepletionField.getText().isEmpty()) {
                try {
                    float value = Float.parseFloat(thirstDepletionField.getText());
                    config.gameplay.thirstDepletionRate = clamp(value, 0.1f, 5.0f);
                } catch (NumberFormatException ignored) {}
            }

            if (hudScaleField != null && !hudScaleField.getText().isEmpty()) {
                try {
                    float value = Float.parseFloat(hudScaleField.getText());
                    config.hud.hudScale = clamp(value, 0.5f, 3.0f);
                } catch (NumberFormatException ignored) {}
            }

            if (hudOpacityField != null && !hudOpacityField.getText().isEmpty()) {
                try {
                    float value = Float.parseFloat(hudOpacityField.getText());
                    config.hud.hudOpacity = clamp(value, 0.1f, 1.0f);
                } catch (NumberFormatException ignored) {}
            }
        }

        /**
         * Reset all values to defaults
         */
        private void resetToDefaults() {
            // Gameplay systems
            config.gameplay.staminaSystemEnabled = true;
            config.gameplay.thirstSystemEnabled = true;
            config.gameplay.temperatureSystemEnabled = true;
            config.gameplay.environmentalHazardsEnabled = true;
            config.gameplay.hungerOverhaulEnabled = true;
            config.gameplay.exhaustionEnabled = true;
            config.gameplay.staminaRecoveryRate = 1.0f;
            config.gameplay.staminaDepletionRate = 1.0f;
            config.gameplay.thirstDepletionRate = 1.0f;

            // HUD settings
            config.hud.showStaminaBar = true;
            config.hud.showThirstBar = true;
            config.hud.hudScale = 1.0f;
            config.hud.hudOpacity = 1.0f;

            // Difficulty settings
            config.difficulty.staminalossDifficulty = 1.0f;
            config.difficulty.thirstDifficulty = 1.0f;
            config.difficulty.temperatureDifficulty = 1.0f;
            config.difficulty.hazardDifficulty = 1.0f;

            // Advanced features
            config.webDashboardEnabled = false;
            config.autoSaveConfig = true;
            config.showDetailedTooltips = true;
            config.debugMode = false;

            PrimalCraftConfig.save();
            PrimalCraft.LOGGER.info("[CONFIG] ✓ All settings reset to defaults");
        }

        @Override
        public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
            // Draw main dark background
            context.fill(0, 0, this.width, this.height, 0xC0101010);

            // Draw sidebar background (darker)
            int sidebarWidth = 130;
            context.fill(0, 0, sidebarWidth, this.height, 0xC0050505);

            // Draw vertical line separator
            context.fill(sidebarWidth, 0, sidebarWidth + 2, this.height, 0xFF444444);

            // Title
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                15,
                0xFFFFFF
            );

            // Tab indicator
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Currently viewing: ").formatted(Formatting.GRAY)
                    .append(Text.literal(currentTab.label).styled(style ->
                        style.withColor(currentTab.color).withBold(true)
                    )),
                this.width / 2,
                30,
                0xFFFFFF
            );

            // Render all widgets first (this includes sidebar tabs and content)
            super.render(context, mouseX, mouseY, delta);

            // Now enable scissor test to clip content widgets that scroll beyond boundaries
            int scissorLeft = sidebarWidth + 5;
            int scissorTop = 50;
            int scissorRight = this.width - 5;
            int scissorBottom = this.height - 20;  // More space now that buttons are in sidebar
            context.enableScissor(scissorLeft, scissorTop, scissorRight, scissorBottom);

            // Re-render just the content area with clipping (excludes sidebar tabs)
            for (var child : this.children()) {
                if (child instanceof ButtonWidget button) {
                    // Only clip content buttons, not sidebar tabs
                    if (button.getX() >= scissorLeft) {
                        button.render(context, mouseX, mouseY, delta);
                    }
                }
            }

            // Disable scissor test
            context.disableScissor();

            // Draw scroll indicator if content is scrollable
            if (maxScroll > 0) {
                int scrollBarHeight = 100;
                int scrollBarY = scissorTop + (int)((scissorBottom - scissorTop - scrollBarHeight) * (scrollOffset / maxScroll));
                context.fill(this.width - 10, scrollBarY, this.width - 5, scrollBarY + scrollBarHeight, 0x80FFFFFF);
            }

            // Help text
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Scroll wheel to navigate • Left-click to change • Right-click to reset").formatted(Formatting.DARK_GRAY),
                this.width / 2,
                this.height - 22,
                0x888888
            );
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (maxScroll > 0) {
                // Smooth scrolling with increased sensitivity
                double scrollAmount = verticalAmount * 25; // Increased from 20 for smoother feel
                double oldOffset = scrollOffset;
                scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollAmount));

                // Only re-init if scroll actually changed
                if (scrollOffset != oldOffset) {
                    // Save fields before re-init
                    saveAllFields();

                    // Clear and rebuild widgets with new positions
                    clearChildren();
                    init();

                    return true; // Consume the event
                }
            }
            return false;
        }

        @Override
        public void close() {
            PrimalCraftConfig.save();
            if (this.client != null) {
                this.client.setScreen(this.parent);
            }
        }
    }
}
