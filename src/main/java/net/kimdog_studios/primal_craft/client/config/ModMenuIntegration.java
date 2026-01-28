package net.kimdog_studios.primal_craft.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.kimdog_studios.primal_craft.PrimalCraft;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 🎮 Primal Craft - Enhanced Modular Config UI (v3.0)
 *
 * Beautiful, modern configuration screen with:
 * ✨ Clean tabbed interface
 * 🔍 Real-time search functionality
 * 📊 Organized sections
 * 🎨 Color-coded categories
 * ⚡ Smooth scrolling
 * 💾 Live saving
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return PrimalCraftConfigScreen::new;
    }

    public enum ConfigTab {
        GAMEPLAY("🎮 Gameplay", 0x55FF55, "Core survival systems"),
        HUD("🎨 HUD", 0xFF55FF, "Display and UI settings"),
        SYSTEMS("⚙️ Systems", 0xFFFF55, "Tool and feature settings"),
        DIFFICULTY("⚔️ Difficulty", 0xFF5555, "Challenge and scaling"),
        ADVANCED("🔧 Advanced", 0x55FFFF, "Developer options");

        public final String label;
        public final int color;
        public final String description;

        ConfigTab(String label, int color, String description) {
            this.label = label;
            this.color = color;
            this.description = description;
        }
    }

    public static class ConfigScreen extends Screen {
        private final Screen parent;
        private ConfigTab currentTab = ConfigTab.GAMEPLAY;
        private double scrollOffset = 0;
        private double maxScroll = 0;
        private String searchQuery = "";
        private TextFieldWidget searchField;
        private Map<ConfigTab, List<ConfigField>> tabFields = new HashMap<>();
        private Map<ConfigTab, List<ConfigField>> filteredFields = new HashMap<>();

        // Layout constants
        private static final int HEADER_HEIGHT = 80;
        private static final int TAB_HEIGHT = 25;
        private static final int FOOTER_HEIGHT = 80;
        private static final int CONTENT_PADDING = 10;

        public ConfigScreen(Screen parent) {
            super(Text.literal("⚙️ Primal Craft Configuration (v3.0)"));
            this.parent = parent;
            PrimalCraftConfig.init();
            buildFieldMaps();
        }

        private void buildFieldMaps() {
            PrimalCraftConfig.MasterConfig config = PrimalCraftConfig.getConfig();
            tabFields.put(ConfigTab.GAMEPLAY, buildFields(config.gameplay, ""));
            tabFields.put(ConfigTab.HUD, buildFields(config.hud, ""));
            tabFields.put(ConfigTab.SYSTEMS, buildFields(config.systems, ""));
            tabFields.put(ConfigTab.DIFFICULTY, buildFields(config.difficulty, ""));
            tabFields.put(ConfigTab.ADVANCED, buildFields(config.advanced, ""));
            updateFilteredFields();
        }

        private void updateFilteredFields() {
            if (searchQuery.isEmpty()) {
                filteredFields.putAll(tabFields);
            } else {
                String query = searchQuery.toLowerCase();
                for (ConfigTab tab : ConfigTab.values()) {
                    List<ConfigField> original = tabFields.getOrDefault(tab, new ArrayList<>());
                    List<ConfigField> filtered = original.stream()
                        .filter(field -> field.name.toLowerCase().contains(query) || field instanceof SectionHeader)
                        .collect(Collectors.toList());
                    filteredFields.put(tab, filtered);
                }
            }
            scrollOffset = 0;
        }

        private List<ConfigField> buildFields(Object obj, String prefix) {
            List<ConfigField> fields = new ArrayList<>();
            if (obj == null) return fields;

            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    String name = formatFieldName(field.getName());
                    Class<?> type = field.getType();

                    if (type == boolean.class || type == Boolean.class) {
                        fields.add(new BooleanField(name, field, obj, (Boolean) value));
                    } else if (type == float.class || type == Float.class) {
                        fields.add(new FloatField(name, field, obj, (Float) value));
                    } else if (type == int.class || type == Integer.class) {
                        fields.add(new IntField(name, field, obj, (Integer) value));
                    } else if (type == String.class) {
                        String strValue = (String) value;
                        // Check if this is a preset field
                        if (field.getName().equalsIgnoreCase("currentPreset")) {
                            String[] presets = {"EASY", "NORMAL", "HARD", "EXPERT", "NIGHTMARE"};
                            fields.add(new EnumField(name, field, obj, strValue, presets));
                        } else {
                            fields.add(new StringField(name, field, obj, strValue));
                        }
                    } else if (!type.isPrimitive() && !type.isArray()) {
                        String section = formatFieldName(field.getName());
                        fields.add(new SectionHeader(section, 0xFFAA00));
                        fields.addAll(buildFields(value, prefix + field.getName() + "."));
                    }
                } catch (Exception e) {
                    PrimalCraft.LOGGER.warn("[CONFIG_UI] Failed to build field: {}", field.getName());
                }
            }
            return fields;
        }

        private String formatFieldName(String name) {
            String withSpaces = name.replaceAll("([a-z])([A-Z])", "$1 $2");
            if (withSpaces.length() > 0) {
                return withSpaces.substring(0, 1).toUpperCase() + withSpaces.substring(1);
            }
            return withSpaces;
        }

        @Override
        protected void init() {
            super.init();
            this.clearChildren();

            // ═══════════════════════════════════════════════════════════════
            // HEADER & SEARCH BAR
            // ═══════════════════════════════════════════════════════════════

            int searchY = 15;
            int searchWidth = this.width - 40;
            searchField = new TextFieldWidget(this.textRenderer, (this.width - searchWidth) / 2, searchY, searchWidth, 18, Text.literal("Search..."));
            searchField.setMaxLength(50);
            searchField.setText(searchQuery);
            this.addDrawableChild(searchField);

            // ═══════════════════════════════════════════════════════════════
            // TAB BUTTONS
            // ═══════════════════════════════════════════════════════════════

            int tabY = 45;
            int tabHeight = 25;
            int tabSpacing = 2;
            int sidebarWidth = (this.width - 20) / 5;

            for (int i = 0; i < ConfigTab.values().length; i++) {
                ConfigTab tab = ConfigTab.values()[i];
                boolean isActive = tab == currentTab;
                int tabX = 10 + (i * (sidebarWidth + tabSpacing));

                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(tab.label).styled(s ->
                        s.withBold(isActive).withColor(isActive ? 0xFFFFFF : 0xAAAAAA)
                    ),
                    btn -> {
                        currentTab = tab;
                        scrollOffset = 0;
                        searchQuery = "";
                        clearChildren();
                        init();
                    }
                ).dimensions(tabX, tabY, sidebarWidth, tabHeight).build());
            }

            // ═══════════════════════════════════════════════════════════════
            // CONTENT AREA (with proper boundaries)
            // ═══════════════════════════════════════════════════════════════

            int contentStartY = HEADER_HEIGHT + TAB_HEIGHT + CONTENT_PADDING;
            int contentHeight = this.height - contentStartY - FOOTER_HEIGHT - CONTENT_PADDING;
            int buttonWidth = 350;
            int centerX = (this.width - buttonWidth) / 2;
            int y = contentStartY - (int) scrollOffset;
            int spacing = 26;
            int initialY = contentStartY;

            List<ConfigField> fields = filteredFields.getOrDefault(currentTab, new ArrayList<>());

            // Track section positions for background rendering
            int sectionStartY = -1;
            int sectionColor = 0;

            for (ConfigField field : fields) {
                // Only render if visible in content area
                if (y + spacing < contentStartY || y > contentStartY + contentHeight) {
                    y += spacing;
                    continue;
                }

                try {
                    if (field instanceof SectionHeader) {
                        SectionHeader sh = (SectionHeader) field;

                        // Draw section header with enhanced styling
                        this.addDrawableChild(ButtonWidget.builder(
                            Text.literal("  ✦ " + sh.name.toUpperCase() + " ✦").styled(s ->
                                s.withBold(true).withColor(sh.color)
                            ),
                            btn -> {}
                        ).dimensions(centerX - 5, y, buttonWidth + 10, 22).build());
                        y += spacing + 12;
                        sectionStartY = y;
                        sectionColor = sh.color;
                    } else {
                        ClickableWidget widget = field.createWidget(centerX, y, buttonWidth, 20);
                        if (widget != null) {
                            this.addDrawableChild(widget);
                            y += spacing;
                        }
                    }
                } catch (Exception e) {
                    PrimalCraft.LOGGER.warn("[CONFIG_UI] Failed to create widget: {}", e.getMessage());
                }
            }

            maxScroll = Math.max(0, y - initialY - contentHeight);

            // ═══════════════════════════════════════════════════════════════
            // BOTTOM BUTTONS
            // ═══════════════════════════════════════════════════════════════

            int buttonY = this.height - 70;
            int buttonHeight = 20;
            int buttonSpacing = 5;
            int buttonCount = 3;
            int totalButtonWidth = (buttonWidth / buttonCount) - buttonSpacing;

            // Done Button
            this.addDrawableChild(ButtonWidget.builder(Text.literal("✓ Done"), btn -> {
                PrimalCraftConfig.save();
                if (this.client != null) this.client.setScreen(parent);
            }).dimensions(centerX, buttonY, totalButtonWidth, buttonHeight).build());

            // Save Button
            this.addDrawableChild(ButtonWidget.builder(Text.literal("💾 Save"), btn -> {
                PrimalCraftConfig.save();
                PrimalCraft.LOGGER.info("[CONFIG] ✓ Settings saved!");
            }).dimensions(centerX + totalButtonWidth + buttonSpacing, buttonY, totalButtonWidth, buttonHeight).build());

            // Reset Button with tooltip
            ButtonWidget resetBtn = ButtonWidget.builder(Text.literal("↻ Reset All"), btn -> {
                PrimalCraftConfig.init();
                buildFieldMaps();
                searchQuery = "";
                scrollOffset = 0;
                clearChildren();
                init();
                PrimalCraft.LOGGER.info("[CONFIG] ↻ All settings reset to defaults!");
            }).dimensions(centerX + (totalButtonWidth + buttonSpacing) * 2, buttonY, totalButtonWidth, buttonHeight).build();
            resetBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal("Reset all settings to their default values")));
            this.addDrawableChild(resetBtn);
        }

        private void setSearchQuery(String query) {
            searchQuery = query;
            updateFilteredFields();
            scrollOffset = 0;
            clearChildren();
            init();
        }

        private void drawRect(int x1, int y1, int x2, int y2, int color) {
            // Simple rectangle drawing
            if (x1 < x2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;
            }
            if (y1 < y2) {
                int temp = y1;
                y1 = y2;
                y2 = temp;
            }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            // Check if search field text changed
            if (searchField != null && !searchField.getText().equals(searchQuery)) {
                setSearchQuery(searchField.getText());
            }

            // Draw background
            context.fill(0, 0, this.width, this.height, 0xFF1a1a1a);

            // Draw decorative header bar with gradient effect
            context.fill(0, 0, this.width, HEADER_HEIGHT, 0xFF0d0d0d);
            context.fill(0, HEADER_HEIGHT - 2, this.width, HEADER_HEIGHT, currentTab.color);

            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("⚙️ PRIMAL CRAFT CONFIGURATION").styled(s -> s.withColor(0x55FF55).withBold(true)),
                this.width / 2, 8, 0xFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(currentTab.description).styled(s -> s.withColor(currentTab.color)),
                this.width / 2, 25, 0xAAAAAA);

            // Draw version info
            context.drawTextWithShadow(this.textRenderer,
                Text.literal("v3.0").styled(s -> s.withColor(0x666666)),
                this.width - 60, HEADER_HEIGHT - 15, 0xFFFFFF);

            super.render(context, mouseX, mouseY, delta);

            // ═══════════════════════════════════════════════════════════════
            // DRAW SCROLLBAR
            // ═══════════════════════════════════════════════════════════════

            if (maxScroll > 0) {
                int contentStartY = HEADER_HEIGHT + TAB_HEIGHT + CONTENT_PADDING;
                int contentHeight = this.height - contentStartY - FOOTER_HEIGHT - CONTENT_PADDING;
                int scrollbarX = this.width - 8;
                int scrollbarWidth = 6;

                // Draw scrollbar background with gradient
                context.fill(scrollbarX, contentStartY, scrollbarX + scrollbarWidth, contentStartY + contentHeight, 0x22FFFFFF);

                // Draw scrollbar thumb with glow effect
                float scrollProgress = (float) (scrollOffset / maxScroll);
                int thumbHeight = Math.max(20, (int) (contentHeight * (contentHeight / (contentHeight + maxScroll))));
                int thumbY = contentStartY + (int) (scrollProgress * (contentHeight - thumbHeight));

                // Glow effect
                context.fill(scrollbarX - 2, thumbY - 1, scrollbarX + scrollbarWidth + 2, thumbY + thumbHeight + 1, 0x4455FF55);
                // Main thumb
                context.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, 0xFF55FF55);
            }

            // Draw footer separator
            context.fill(0, this.height - FOOTER_HEIGHT - 1, this.width, this.height - FOOTER_HEIGHT, currentTab.color);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
            if (maxScroll > 0) {
                scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - vertical * 15));
                clearChildren();
                init();
                return true;
            }
            return false;
        }

        @Override
        public void close() {
            PrimalCraftConfig.save();
            if (this.client != null) this.client.setScreen(parent);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return true;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // CONFIG FIELD CLASSES
    // ═══════════════════════════════════════════════════════════════════════════════

    abstract static class ConfigField {
        String name;
        Field field;
        Object parent;

        ConfigField(String name, Field field, Object parent) {
            this.name = name;
            this.field = field;
            this.parent = parent;
        }

        abstract ClickableWidget createWidget(int x, int y, int width, int height) throws Exception;

        protected void updateValue(Object newValue) {
            try {
                field.set(parent, newValue);
            } catch (Exception e) {
                PrimalCraft.LOGGER.error("[CONFIG] Failed to update field", e);
            }
        }
    }

    static class BooleanField extends ConfigField {
        boolean value;
        boolean defaultValue;
        String tooltip;

        BooleanField(String name, Field field, Object parent, boolean value) {
            super(name, field, parent);
            this.value = value;
            this.defaultValue = value;
            this.tooltip = generateTooltip(name, "boolean", value ? "Enabled" : "Disabled") + "\n\n[RIGHT-CLICK to reset]";
        }

        @Override
        ClickableWidget createWidget(int x, int y, int width, int height) {
            CyclingButtonWidget widget = CyclingButtonWidget.onOffBuilder(value)
                .build(x, y, width, height,
                    Text.literal(name),
                    (btn, newValue) -> {
                        value = newValue;
                        updateValue(newValue);
                        PrimalCraftConfig.save();
                    });
            widget.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal(tooltip)));
            return widget;
        }

        public void reset() {
            value = defaultValue;
            updateValue(defaultValue);
            PrimalCraftConfig.save();
        }
    }

    static class FloatField extends ConfigField {
        float value;
        float defaultValue;
        String tooltip;

        FloatField(String name, Field field, Object parent, float value) {
            super(name, field, parent);
            this.value = value;
            this.defaultValue = value;
            this.tooltip = generateTooltip(name, "number", String.format("Value: %.2f | Click to increase by 0.1", value)) + "\n\n[RIGHT-CLICK to reset]";
        }

        @Override
        ClickableWidget createWidget(int x, int y, int width, int height) {
            ButtonWidget widget = ButtonWidget.builder(
                Text.literal(String.format("%s: %.2f", name, value)),
                btn -> {
                    value += 0.1f;
                    btn.setMessage(Text.literal(String.format("%s: %.2f", name, value)));
                    updateValue(value);
                    PrimalCraftConfig.save();
                }
            ).dimensions(x, y, width, height).build();
            widget.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal(tooltip)));
            return widget;
        }

        public void reset() {
            value = defaultValue;
            updateValue(defaultValue);
            PrimalCraftConfig.save();
        }
    }

    static class IntField extends ConfigField {
        int value;
        int defaultValue;
        String tooltip;

        IntField(String name, Field field, Object parent, int value) {
            super(name, field, parent);
            this.value = value;
            this.defaultValue = value;
            this.tooltip = generateTooltip(name, "number", String.format("Value: %d | Click to increase by 1", value)) + "\n\n[RIGHT-CLICK to reset]";
        }

        @Override
        ClickableWidget createWidget(int x, int y, int width, int height) {
            ButtonWidget widget = ButtonWidget.builder(
                Text.literal(String.format("%s: %d", name, value)),
                btn -> {
                    value += 1;
                    btn.setMessage(Text.literal(String.format("%s: %d", name, value)));
                    updateValue(value);
                    PrimalCraftConfig.save();
                }
            ).dimensions(x, y, width, height).build();
            widget.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal(tooltip)));
            return widget;
        }

        public void reset() {
            value = defaultValue;
            updateValue(defaultValue);
            PrimalCraftConfig.save();
        }
    }

    static class StringField extends ConfigField {
        String value;
        String tooltip;

        StringField(String name, Field field, Object parent, String value) {
            super(name, field, parent);
            this.value = value;
            this.tooltip = generateTooltip(name, "text", "Value: " + value);
        }

        @Override
        ClickableWidget createWidget(int x, int y, int width, int height) {
            ButtonWidget widget = ButtonWidget.builder(
                Text.literal(name + ": " + value),
                btn -> { /* String editing not implemented */ }
            ).dimensions(x, y, width, height).build();
            widget.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal(tooltip)));
            return widget;
        }
    }

    static class EnumField extends ConfigField {
        String value;
        String defaultValue;
        String[] options;
        int currentIndex;
        int defaultIndex;
        String tooltip;

        EnumField(String name, Field field, Object parent, String value, String[] options) {
            super(name, field, parent);
            this.value = value;
            this.defaultValue = value;
            this.options = options;
            this.currentIndex = java.util.Arrays.asList(options).indexOf(value);
            this.defaultIndex = this.currentIndex;
            if (this.currentIndex < 0) this.currentIndex = 0;
            this.tooltip = generateTooltip(name, "preset", "Current: " + options[currentIndex] + " | Click to cycle through options") + "\n\n[RIGHT-CLICK to reset]";
        }

        @Override
        ClickableWidget createWidget(int x, int y, int width, int height) {
            ButtonWidget widget = ButtonWidget.builder(
                Text.literal(String.format("%s: %s", name, options[currentIndex])),
                btn -> {
                    currentIndex = (currentIndex + 1) % options.length;
                    value = options[currentIndex];
                    btn.setMessage(Text.literal(String.format("%s: %s", name, options[currentIndex])));
                    updateValue(value);
                    PrimalCraftConfig.save();
                }
            ).dimensions(x, y, width, height).build();
            widget.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal(tooltip)));
            return widget;
        }

        public void reset() {
            currentIndex = defaultIndex;
            value = defaultValue;
            updateValue(defaultValue);
            PrimalCraftConfig.save();
        }
    }

    static class SectionHeader extends ConfigField {
        int color;

        SectionHeader(String name, int color) {
            super(name, null, null);
            this.color = color;
        }

        @Override
        ClickableWidget createWidget(int x, int y, int width, int height) {
            return null;
        }
    }

    // Utility method to generate consistent tooltips
    private static String generateTooltip(String name, String type, String details) {
        return String.format("━━━━━━━━━━━━━━━━━━━━━━\n[%s]\n%s\n━━━━━━━━━━━━━━━━━━━━━━\n%s",
            type.toUpperCase(), name, details);
    }
}
