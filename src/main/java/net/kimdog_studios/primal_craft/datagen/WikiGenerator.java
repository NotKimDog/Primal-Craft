package net.kimdog_studios.primal_craft.datagen;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.block.ModBlocks;
import net.kimdog_studios.primal_craft.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Automated Wiki Generator
 * Scans mod classes and generates comprehensive wiki documentation
 */
public class WikiGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("primal-craft");
    private static final String WIKI_DIR = "wiki/";

    public static void main(String[] args) {
        generateWiki();
    }

    public static void generateWiki() {
        LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        LOGGER.info("║  AUTOMATED WIKI GENERATION STARTING                        ║");
        LOGGER.info("╚════════════════════════════════════════════════════════════╝");

        try {
            createWikiDirectory();

            LOGGER.info("  📝 Generating Items & Blocks page...");
            generateItemsAndBlocksPage();

            LOGGER.info("  📊 Generating Features page...");
            generateFeaturesPage();

            LOGGER.info("  📖 Updating Home page...");
            updateHomePage();

            LOGGER.info("╔════════════════════════════════════════════════════════════╗");
            LOGGER.info("║  ✅ WIKI GENERATION COMPLETE                               ║");
            LOGGER.info("╚════════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            LOGGER.error("❌ Failed to generate wiki", e);
        }
    }

    private static void createWikiDirectory() {
        File wikiDir = new File(WIKI_DIR);
        if (!wikiDir.exists()) {
            wikiDir.mkdirs();
            LOGGER.info("  📁 Created wiki directory: {}", WIKI_DIR);
        }
    }

    private static void generateItemsAndBlocksPage() throws IOException {
        StringBuilder wiki = new StringBuilder();

        wiki.append("# Items & Blocks (Auto-Generated)\n\n");
        wiki.append("_Last Updated: ").append(new Date()).append("_\n\n");
        wiki.append("---\n\n");

        // Scan blocks
        wiki.append("## 📦 Blocks\n\n");
        List<String> blocks = scanFields(ModBlocks.class);
        wiki.append("**Total Blocks:** ").append(blocks.size()).append("\n\n");

        for (String block : blocks) {
            wiki.append("- **").append(formatName(block)).append("**\n");
        }

        wiki.append("\n---\n\n");

        // Scan items
        wiki.append("## 🎒 Items\n\n");
        List<String> items = scanFields(ModItems.class);
        wiki.append("**Total Items:** ").append(items.size()).append("\n\n");

        // Categorize items
        Map<String, List<String>> categories = categorizeItems(items);

        for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
            wiki.append("### ").append(entry.getKey()).append("\n\n");
            for (String item : entry.getValue()) {
                wiki.append("- **").append(formatName(item)).append("**\n");
            }
            wiki.append("\n");
        }

        writeFile("Items-and-Blocks.md", wiki.toString());
    }

    private static void generateFeaturesPage() throws IOException {
        StringBuilder wiki = new StringBuilder();

        wiki.append("# Features (Auto-Generated)\n\n");
        wiki.append("_Last Updated: ").append(new Date()).append("_\n\n");
        wiki.append("---\n\n");

        wiki.append("## 🎮 Core Systems\n\n");
        wiki.append("### Stamina System\n");
        wiki.append("- Energy management affecting sprint and combat\n");
        wiki.append("- Visual HUD indicator\n");
        wiki.append("- Recovery through rest and sleep\n\n");

        wiki.append("### Thirst Mechanic\n");
        wiki.append("- Water and hydration management\n");
        wiki.append("- Environmental temperature affects thirst\n");
        wiki.append("- Visual thirst indicator\n\n");

        wiki.append("### Temperature System\n");
        wiki.append("- Biome-specific temperatures\n");
        wiki.append("- Affects player health and stamina\n");
        wiki.append("- Armor provides protection\n\n");

        wiki.append("### Wind System\n");
        wiki.append("- Dynamic wind mechanics\n");
        wiki.append("- Visual effects and indicators\n");
        wiki.append("- Weather integration\n\n");

        wiki.append("## 📊 Content Summary\n\n");

        List<String> blocks = scanFields(ModBlocks.class);
        List<String> items = scanFields(ModItems.class);

        wiki.append("| Content Type | Count |\n");
        wiki.append("|--------------|-------|\n");
        wiki.append("| Blocks | ").append(blocks.size()).append(" |\n");
        wiki.append("| Items | ").append(items.size()).append(" |\n");
        wiki.append("| **Total** | **").append(blocks.size() + items.size()).append("** |\n\n");

        writeFile("Features.md", wiki.toString());
    }

    private static void updateHomePage() throws IOException {
        StringBuilder wiki = new StringBuilder();

        wiki.append("# KimDog SMP Wiki (Auto-Generated)\n\n");
        wiki.append("_Last Updated: ").append(new Date()).append("_\n\n");
        wiki.append("---\n\n");

        wiki.append("## 📚 Documentation\n\n");
        wiki.append("- **[Features](Features.md)** - Complete feature list\n");
        wiki.append("- **[Items & Blocks](Items-and-Blocks.md)** - All content\n");
        wiki.append("- **[Getting Started](Getting-Started.md)** - Installation guide\n");
        wiki.append("- **[Systems](Systems.md)** - Detailed mechanics\n\n");

        List<String> blocks = scanFields(ModBlocks.class);
        List<String> items = scanFields(ModItems.class);

        wiki.append("## 📊 Quick Stats\n\n");
        wiki.append("- 🧱 **Blocks:** ").append(blocks.size()).append("+\n");
        wiki.append("- 🎒 **Items:** ").append(items.size()).append("+\n");
        wiki.append("- ✨ **Systems:** 15+\n");
        wiki.append("- 🎮 **Features:** 50+\n\n");

        wiki.append("## 🚀 Quick Start\n\n");
        wiki.append("**New to the mod?**\n");
        wiki.append("1. Read [Getting Started](Getting-Started.md)\n");
        wiki.append("2. Check [Features](Features.md)\n");
        wiki.append("3. Browse [Items & Blocks](Items-and-Blocks.md)\n\n");

        wiki.append("---\n\n");
        wiki.append("**Modrinth:** [KimDog SMP](https://modrinth.com/project/DwBeOr6S)\n");
        wiki.append("**License:** CC0 1.0 Universal\n");

        writeFile("Home.md", wiki.toString());
    }

    private static List<String> scanFields(Class<?> clazz) {
        List<String> fieldNames = new ArrayList<>();

        try {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                    java.lang.reflect.Modifier.isPublic(field.getModifiers())) {
                    fieldNames.add(field.getName());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to scan fields for {}", clazz.getName(), e);
        }

        Collections.sort(fieldNames);
        return fieldNames;
    }

    private static Map<String, List<String>> categorizeItems(List<String> items) {
        Map<String, List<String>> categories = new LinkedHashMap<>();
        categories.put("⚒️ Tools", new ArrayList<>());
        categories.put("🗡️ Weapons", new ArrayList<>());
        categories.put("🛡️ Armor", new ArrayList<>());
        categories.put("🍖 Food", new ArrayList<>());
        categories.put("🌿 Materials", new ArrayList<>());
        categories.put("📦 Other", new ArrayList<>());

        for (String item : items) {
            String lower = item.toLowerCase();

            if (lower.contains("pickaxe") || lower.contains("axe") || lower.contains("shovel") ||
                lower.contains("hoe") || lower.contains("hammer") || lower.contains("chisel")) {
                categories.get("⚒️ Tools").add(item);
            } else if (lower.contains("sword") || lower.contains("bow") || lower.contains("staff") ||
                       lower.contains("tomahawk")) {
                categories.get("🗡️ Weapons").add(item);
            } else if (lower.contains("helmet") || lower.contains("chestplate") ||
                       lower.contains("leggings") || lower.contains("boots") || lower.contains("armor")) {
                categories.get("🛡️ Armor").add(item);
            } else if (lower.contains("berries") || lower.contains("cauliflower") || lower.contains("food")) {
                categories.get("🍖 Food").add(item);
            } else if (lower.contains("garnet") || lower.contains("ashes") || lower.contains("raw")) {
                categories.get("🌿 Materials").add(item);
            } else {
                categories.get("📦 Other").add(item);
            }
        }

        return categories;
    }

    private static String formatName(String fieldName) {
        // Convert UPPER_SNAKE_CASE to Title Case
        String[] parts = fieldName.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(part.substring(0, 1).toUpperCase())
                  .append(part.substring(1).toLowerCase());
        }

        return result.toString();
    }

    private static void writeFile(String filename, String content) throws IOException {
        File file = new File(WIKI_DIR + filename);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            LOGGER.info("    ✓ Generated: {}", filename);
        }
    }
}
