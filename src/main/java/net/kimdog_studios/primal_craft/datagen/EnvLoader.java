package net.kimdog_studios.primal_craft.datagen;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple .env file loader for loading environment variables from .env file
 * This keeps sensitive tokens out of source code
 */
public class EnvLoader {
    private static final Map<String, String> ENV_VARS = new HashMap<>();
    private static boolean loaded = false;

    /**
     * Load environment variables from .env file
     * File should be in the project root directory
     */
    public static void load() {
        if (loaded) return;

        try {
            File envFile = new File(".env");
            if (!envFile.exists()) {
                System.err.println("⚠️  WARNING: .env file not found!");
                System.err.println("   Please create a .env file in the project root with:");
                System.err.println("   MODRINTH_API_TOKEN=your_token_here");
                return;
            }

            // Read and parse .env file
            Files.readAllLines(envFile.toPath()).forEach(line -> {
                // Skip empty lines and comments
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    return;
                }

                // Parse KEY=VALUE
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    ENV_VARS.put(key, value);
                }
            });

            loaded = true;
            System.out.println("✅ Loaded environment variables from .env file");

        } catch (Exception e) {
            System.err.println("❌ Failed to load .env file: " + e.getMessage());
        }
    }

    /**
     * Get environment variable from .env file or system environment
     * Priority: .env file > System environment variable
     */
    public static String get(String key) {
        if (!loaded) {
            load();
        }

        // First check .env file
        if (ENV_VARS.containsKey(key)) {
            return ENV_VARS.get(key);
        }

        // Then check system environment
        return System.getenv(key);
    }

    /**
     * Get environment variable or default value
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }
}
