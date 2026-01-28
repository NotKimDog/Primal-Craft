package net.kimdog_studios.primal_craft.client.config;

import net.kimdog_studios.primal_craft.client.config.annotation.ConfigOption;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;
import net.kimdog_studios.primal_craft.PrimalCraft;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Automatic config UI generator using reflection and annotations
 * Eliminates manual widget creation for each config option
 */
public class AutoConfigBuilder {

    /**
     * Scan config object and generate all widgets automatically
     */
    public static Map<ConfigOption.ConfigTab, List<WidgetEntry>> buildWidgetsFromConfig(Object config) {
        Map<ConfigOption.ConfigTab, List<WidgetEntry>> tabWidgets = new HashMap<>();

        // Initialize all tabs
        for (ConfigOption.ConfigTab tab : ConfigOption.ConfigTab.values()) {
            tabWidgets.put(tab, new ArrayList<>());
        }

        // Scan all fields recursively
        scanObject(config, tabWidgets, "");

        // Sort widgets by order within each tab
        for (List<WidgetEntry> widgets : tabWidgets.values()) {
            widgets.sort(Comparator.comparingInt(w -> w.order));
        }

        return tabWidgets;
    }

    private static void scanObject(Object obj, Map<ConfigOption.ConfigTab, List<WidgetEntry>> tabWidgets, String prefix) {
        if (obj == null) return;

        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            ConfigOption annotation = field.getAnnotation(ConfigOption.class);
            if (annotation != null) {
                try {
                    WidgetEntry entry = createWidgetEntry(field, obj, annotation, prefix);
                    if (entry != null) {
                        tabWidgets.get(annotation.tab()).add(entry);
                    }
                } catch (Exception e) {
                    PrimalCraft.LOGGER.error("[AUTO_CONFIG] Failed to create widget for field: " + field.getName(), e);
                }
            } else {
                // Recursively scan nested objects
                try {
                    Object nestedObj = field.get(obj);
                    if (nestedObj != null && !isPrimitive(nestedObj.getClass())) {
                        scanObject(nestedObj, tabWidgets, prefix + field.getName() + ".");
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    private static WidgetEntry createWidgetEntry(Field field, Object parent, ConfigOption annotation, String prefix) throws Exception {
        Class<?> type = field.getType();

        WidgetFactory factory = null;

        if (type == boolean.class || type == Boolean.class) {
            factory = (x, y, width, height) -> createBooleanWidget(field, parent, annotation, x, y, width, height);
        } else if (type == float.class || type == Float.class || type == double.class || type == Double.class) {
            factory = (x, y, width, height) -> createNumericWidget(field, parent, annotation, x, y, width, height);
        } else if (type == int.class || type == Integer.class) {
            factory = (x, y, width, height) -> createIntWidget(field, parent, annotation, x, y, width, height);
        } else if (type == String.class) {
            factory = (x, y, width, height) -> createStringWidget(field, parent, annotation, x, y, width, height);
        }

        if (factory != null) {
            return new WidgetEntry(
                annotation.name(),
                annotation.description(),
                annotation.section(),
                annotation.order(),
                factory
            );
        }

        return null;
    }

    private static ClickableWidget createBooleanWidget(Field field, Object parent, ConfigOption annotation,
                                                    int x, int y, int width, int height) throws Exception {
        boolean currentValue = field.getBoolean(parent);

        var widget = CyclingButtonWidget.onOffBuilder(currentValue)
            .build(x, y, width, height,
                Text.literal(annotation.name()),
                (button, value) -> {
                    try {
                        field.setBoolean(parent, value);
                        PrimalCraft.LOGGER.info("[CONFIG] {} = {}", annotation.name(), value);
                    } catch (Exception e) {
                        PrimalCraft.LOGGER.error("[CONFIG] Failed to set value", e);
                    }
                }
            );

        if (!annotation.description().isEmpty()) {
            widget.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal(annotation.description())
            ));
        }

        return widget;
    }

    private static ClickableWidget createNumericWidget(Field field, Object parent, ConfigOption annotation,
                                                    int x, int y, int width, int height) throws Exception {
        float currentValue = field.getFloat(parent);

        return ButtonWidget.builder(
            Text.literal(String.format("%s: %.2f", annotation.name(), currentValue)),
            button -> {
                try {
                    float current = field.getFloat(parent);
                    float step = (float) ((annotation.max() - annotation.min()) / 20.0);
                    float newValue = current + step;
                    if (newValue > annotation.max()) {
                        newValue = (float) annotation.min();
                    }
                    field.setFloat(parent, newValue);
                    button.setMessage(Text.literal(String.format("%s: %.2f", annotation.name(), newValue)));
                    PrimalCraft.LOGGER.info("[CONFIG] {} = {}", annotation.name(), newValue);
                } catch (Exception e) {
                    PrimalCraft.LOGGER.error("[CONFIG] Failed to update value", e);
                }
            }
        ).dimensions(x, y, width, height).build();
    }

    private static ClickableWidget createIntWidget(Field field, Object parent, ConfigOption annotation,
                                                int x, int y, int width, int height) throws Exception {
        int currentValue = field.getInt(parent);

        return ButtonWidget.builder(
            Text.literal(String.format("%s: %d", annotation.name(), currentValue)),
            button -> {
                try {
                    int current = field.getInt(parent);
                    int step = Math.max(1, (int) ((annotation.max() - annotation.min()) / 20.0));
                    int newValue = current + step;
                    if (newValue > annotation.max()) {
                        newValue = (int) annotation.min();
                    }
                    field.setInt(parent, newValue);
                    button.setMessage(Text.literal(String.format("%s: %d", annotation.name(), newValue)));
                    PrimalCraft.LOGGER.info("[CONFIG] {} = {}", annotation.name(), newValue);
                } catch (Exception e) {
                    PrimalCraft.LOGGER.error("[CONFIG] Failed to update value", e);
                }
            }
        ).dimensions(x, y, width, height).build();
    }

    private static ClickableWidget createStringWidget(Field field, Object parent, ConfigOption annotation,
                                                   int x, int y, int width, int height) throws Exception {
        String currentValue = (String) field.get(parent);

        return ButtonWidget.builder(
            Text.literal(annotation.name() + ": " + currentValue),
            button -> {
                // String editing would require text field - simplified here
                PrimalCraft.LOGGER.info("[CONFIG] {} clicked (string editing not implemented)", annotation.name());
            }
        ).dimensions(x, y, width, height).build();
    }

    private static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() ||
               clazz == Boolean.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Float.class ||
               clazz == Double.class ||
               clazz == String.class;
    }

    /**
     * Widget entry with metadata
     */
    public static class WidgetEntry {
        public final String name;
        public final String description;
        public final String section;
        public final int order;
        public final WidgetFactory factory;

        public WidgetEntry(String name, String description, String section, int order, WidgetFactory factory) {
            this.name = name;
            this.description = description;
            this.section = section;
            this.order = order;
            this.factory = factory;
        }
    }

    @FunctionalInterface
    public interface WidgetFactory {
        ClickableWidget create(int x, int y, int width, int height) throws Exception;
    }
}
