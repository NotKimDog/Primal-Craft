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
    private static class ConfigScreen extends Screen {
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

        /**
         * Configuration tabs
         */
        private enum ConfigTab {
            GAMEPLAY("⚙️  Gameplay", 0xFFAA00),
            HUD("🎨 HUD & Display", 0x55FFFF),
            DIFFICULTY("⚔️  Difficulty", 0xFF5555),
            ADVANCED("🔧 Advanced", 0xAA55FF);

            final String label;
            final int color;

            ConfigTab(String label, int color) {
                this.label = label;
                this.color = color;
            }
        }

        protected ConfigScreen(Screen parent) {
            super(Text.translatable("config.primal-craft.title"));
            this.parent = parent;
            this.config = PrimalCraftConfig.getConfig();
        }

        @Override
        protected void init() {
            super.init();
            PrimalCraft.LOGGER.info("[MOD_MENU] Opening enhanced tabbed configuration screen");

            // ============================================================
            // TAB BUTTONS (Top of screen)
            // ============================================================
            int tabButtonWidth = 150;
            int tabSpacing = 5;
            int tabStartX = (this.width - (tabButtonWidth * 4 + tabSpacing * 3)) / 2;
            int tabY = 35;

            for (int i = 0; i < ConfigTab.values().length; i++) {
                ConfigTab tab = ConfigTab.values()[i];
                int tabX = tabStartX + i * (tabButtonWidth + tabSpacing);
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
                ).dimensions(tabX, tabY, tabButtonWidth, 20).build());
            }

            // ============================================================
            // CONTENT AREA (Based on selected tab)
            // ============================================================
            int contentWidth = this.width - 100;
            int buttonWidth = Math.min(contentWidth, 400);
            int centerX = (this.width - buttonWidth) / 2;
            int textFieldWidth = 60;
            int buttonHeight = 20;
            int y = 70;
            int spacing = 24;

            switch (currentTab) {
                case GAMEPLAY -> initGameplayTab(centerX, y, buttonWidth, textFieldWidth, buttonHeight, spacing);
                case HUD -> initHudTab(centerX, y, buttonWidth, textFieldWidth, buttonHeight, spacing);
                case DIFFICULTY -> initDifficultyTab(centerX, y, buttonWidth, buttonHeight, spacing);
                case ADVANCED -> initAdvancedTab(centerX, y, buttonWidth, buttonHeight, spacing);
            }

            // ============================================================
            // BOTTOM BUTTONS (Always visible)
            // ============================================================
            initBottomButtons();
        }

        /**
         * Initialize Gameplay tab content
         */
        private void initGameplayTab(int x, int y, int buttonWidth, int textFieldWidth, int buttonHeight, int spacing) {
            // Stamina System Toggle
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.gameplay.staminaSystemEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.translatable("config.primal-craft.stamina_system"),
                (button, value) -> {
                    config.gameplay.staminaSystemEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Stamina System: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            ));
            y += spacing;

            // Thirst System Toggle
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.gameplay.thirstSystemEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.translatable("config.primal-craft.thirst_system"),
                (button, value) -> {
                    config.gameplay.thirstSystemEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Thirst System: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            ));
            y += spacing;

            // Temperature System Toggle
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.gameplay.temperatureSystemEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.translatable("config.primal-craft.temperature_system"),
                (button, value) -> {
                    config.gameplay.temperatureSystemEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Temperature System: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            ));
            y += spacing;

            // Environmental Hazards Toggle
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.gameplay.environmentalHazardsEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.translatable("config.primal-craft.environmental_hazards"),
                (button, value) -> {
                    config.gameplay.environmentalHazardsEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Environmental Hazards: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            ));
            y += spacing;

            // Hunger Overhaul Toggle
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.gameplay.hungerOverhaulEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Hunger Overhaul System"),
                (button, value) -> {
                    config.gameplay.hungerOverhaulEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Hunger Overhaul: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            ));
            y += spacing;

            // Exhaustion System Toggle
            this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.gameplay.exhaustionEnabled)
            .build(x, y, buttonWidth, buttonHeight,
                Text.literal("Exhaustion System"),
                (button, value) -> {
                    config.gameplay.exhaustionEnabled = value;
                    PrimalCraft.LOGGER.info("[CONFIG] Exhaustion System: " + (value ? "✓ ENABLED" : "✗ DISABLED"));
                }
            ));
            y += spacing + 10;

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
        }

        /**
         * Initialize HUD tab content
         */
        private void initHudTab(int x, int y, int buttonWidth, int textFieldWidth, int buttonHeight, int spacing) {
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
            y += spacing + 10;

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
        }

        /**
         * Initialize Difficulty tab content
         */
        private void initDifficultyTab(int x, int y, int buttonWidth, int buttonHeight, int spacing) {
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
        }

        /**
         * Initialize Advanced tab content
         */
        private void initAdvancedTab(int x, int y, int buttonWidth, int buttonHeight, int spacing) {
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
        }

        /**
         * Initialize bottom action buttons
         */
        private void initBottomButtons() {
            int bottomY = this.height - 30;
            int bottomButtonWidth = 100;

            // Reset to Defaults button
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("↻ Reset").styled(style -> style.withColor(0xFFAA00)),
                button -> {
                    resetToDefaults();
                    if (this.client != null) {
                        this.client.setScreen(new ConfigScreen(parent));
                    }
                    PrimalCraft.LOGGER.info("[CONFIG] ⚠ Reset to default values");
                }
            ).dimensions(this.width / 2 - bottomButtonWidth * 2 - 10, bottomY, bottomButtonWidth, 20).build());

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
            ).dimensions(this.width / 2 - bottomButtonWidth - 5, bottomY, bottomButtonWidth, 20).build());

            // Done button
            this.addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE,
                button -> {
                    saveAllFields();
                    PrimalCraftConfig.save();
                    if (this.client != null) {
                        this.client.setScreen(this.parent);
                    }
                }
            ).dimensions(this.width / 2 + 5, bottomY, bottomButtonWidth, 20).build());
        }

        /**
         * Create a preset cycling button for common rate values
         */
        private ButtonWidget createPresetButton(int x, int y, int width, int height,
                                                String label, double currentValue,
                                                java.util.function.Consumer<Double> onChange) {
            return ButtonWidget.builder(
                Text.literal(label + ": " + getPresetLabel(currentValue)),
                button -> {
                    double newValue = cyclePreset(currentValue);
                    onChange.accept(newValue);
                    button.setMessage(Text.literal(label + ": " + getPresetLabel(newValue)));
                    PrimalCraft.LOGGER.info("[CONFIG] {} cycled to {} ({}x)", label, getPresetLabel(newValue), String.format("%.2f", newValue));
                }
            ).dimensions(x, y, width, height).build();
        }

        /**
         * Create a difficulty-specific cycling button with descriptive labels
         */
        private ButtonWidget createDifficultyButton(int x, int y, int width, int height,
                                                    String label, double currentValue,
                                                    java.util.function.Consumer<Double> onChange) {
            return ButtonWidget.builder(
                Text.literal(label + ": " + getDifficultyLabel(currentValue)),
                button -> {
                    double newValue = cycleDifficulty(currentValue);
                    onChange.accept(newValue);
                    button.setMessage(Text.literal(label + ": " + getDifficultyLabel(newValue)));
                    PrimalCraft.LOGGER.info("[CONFIG] {} set to {} ({}x)", label, getDifficultyLabel(newValue), String.format("%.2f", newValue));
                }
            ).dimensions(x, y, width, height).build();
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
            // Draw simple dark background
            context.fill(0, 0, this.width, this.height, 0xC0101010);

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
                60,
                0xFFFFFF
            );

            // Help text
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Click tabs to switch categories • Hover over buttons for info").formatted(Formatting.DARK_GRAY),
                this.width / 2,
                this.height - 45,
                0x888888
            );

            super.render(context, mouseX, mouseY, delta);
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
