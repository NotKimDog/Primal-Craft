package net.kimdog_studios.primal_craft.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Automated Modrinth Release Publisher
 * Publishes mod versions to Modrinth using API
 */
public class ModrinthPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger("primal-craft");
    private static final String MODRINTH_API = "https://api.modrinth.com/v2";
    private static final String PROJECT_ID = "DwBeOr6S";
    private static final String API_TOKEN = "mrp_xL44PwGmJIwijtdnrzCZvCyw7eEFH9wHsq4k7t34GUgwMpuXRJrSBARUx1tF";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage:");
            System.out.println("  java ModrinthPublisher check");
            System.out.println("  java ModrinthPublisher <jarFile> <version> <changelog>");
            return;
        }

        if (args[0].equals("check")) {
            boolean connected = checkConnection();
            if (connected) {
                System.out.println("✅ Connected to Modrinth API successfully!");
                System.out.println("   Project: https://modrinth.com/mod/" + PROJECT_ID);
            } else {
                System.out.println("❌ Failed to connect to Modrinth API");
                System.exit(1);
            }
            return;
        }

        if (args.length < 3) {
            System.out.println("Error: Missing arguments");
            System.out.println("Usage: java ModrinthPublisher <jarFile> <version> <changelog>");
            System.exit(1);
        }

        File jarFile = new File(args[0]);
        String version = args[1];
        String changelog = args[2];

        publishRelease(jarFile, version, changelog);
    }

    public static void publishRelease(File jarFile, String version, String changelog) {
        LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        LOGGER.info("║  MODRINTH RELEASE PUBLISHER                                ║");
        LOGGER.info("╚════════════════════════════════════════════════════════════╝");

        try {
            LOGGER.info("  📦 Preparing release...");
            LOGGER.info("    • Version: {}", version);
            LOGGER.info("    • JAR: {}", jarFile.getName());
            LOGGER.info("    • Size: {} MB", String.format("%.2f", jarFile.length() / 1024.0 / 1024.0));

            // Create version
            String versionId = createVersion(jarFile, version, changelog);

            if (versionId != null) {
                LOGGER.info("╔════════════════════════════════════════════════════════════╗");
                LOGGER.info("║  ✅ MODRINTH RELEASE PUBLISHED SUCCESSFULLY                ║");
                LOGGER.info("╚════════════════════════════════════════════════════════════╝");
                LOGGER.info("  🔗 View at: https://modrinth.com/mod/{}/version/{}", PROJECT_ID, versionId);
            } else {
                LOGGER.error("❌ Failed to publish release");
            }

        } catch (Exception e) {
            LOGGER.error("❌ Failed to publish to Modrinth", e);
        }
    }

    private static String createVersion(File jarFile, String version, String changelog) throws IOException {
        LOGGER.info("  🚀 Creating Modrinth version...");

        // Build multipart form data
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();

        URL url = new URL(MODRINTH_API + "/version");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", API_TOKEN);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        // Create JSON metadata
        JsonObject versionData = new JsonObject();
        versionData.addProperty("project_id", PROJECT_ID);
        versionData.addProperty("version_number", version);
        versionData.addProperty("version_title", "KimDog SMP " + version);
        versionData.addProperty("changelog", changelog);
        versionData.addProperty("version_type", detectVersionType(version));
        versionData.addProperty("featured", true);
        versionData.addProperty("status", "listed");

        // Dependencies
        JsonArray dependencies = new JsonArray();
        JsonObject fabricApi = new JsonObject();
        fabricApi.addProperty("project_id", "P7dR8mSH"); // Fabric API
        fabricApi.addProperty("dependency_type", "required");
        dependencies.add(fabricApi);
        versionData.add("dependencies", dependencies);

        // Game versions
        JsonArray gameVersions = new JsonArray();
        gameVersions.add("1.21.11");
        gameVersions.add("1.21.1");
        gameVersions.add("1.21");
        versionData.add("game_versions", gameVersions);

        // Loaders
        JsonArray loaders = new JsonArray();
        loaders.add("fabric");
        versionData.add("loaders", loaders);

        // File parts
        JsonArray fileParts = new JsonArray();
        fileParts.add(jarFile.getName());
        versionData.add("file_parts", fileParts);

        // Build multipart body
        try (OutputStream os = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {

            // Add JSON data part
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"data\"\r\n");
            writer.append("Content-Type: application/json\r\n\r\n");
            writer.append(versionData.toString()).append("\r\n");
            writer.flush();

            // Add file part
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"").append(jarFile.getName()).append("\"; filename=\"").append(jarFile.getName()).append("\"\r\n");
            writer.append("Content-Type: application/java-archive\r\n\r\n");
            writer.flush();

            // Write file bytes
            Files.copy(jarFile.toPath(), os);
            os.flush();

            writer.append("\r\n");
            writer.append("--").append(boundary).append("--").append("\r\n");
            writer.flush();
        }

        // Read response
        int responseCode = conn.getResponseCode();
        LOGGER.info("    • Response Code: {}", responseCode);

        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parse response to get version ID
                String responseStr = response.toString();
                if (responseStr.contains("\"id\"")) {
                    int idStart = responseStr.indexOf("\"id\":\"") + 6;
                    int idEnd = responseStr.indexOf("\"", idStart);
                    String versionId = responseStr.substring(idStart, idEnd);
                    LOGGER.info("    ✅ Version created: {}", versionId);
                    return versionId;
                }
            }
        } else {
            // Read error response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line);
                }
                LOGGER.error("    ❌ Error response: {}", error);
            }
        }

        return null;
    }

    private static String detectVersionType(String version) {
        // Detect from version suffix
        if (version.contains("-patch")) {
            return "release";
        } else if (version.contains("-minor")) {
            return "release";
        } else if (version.contains("-major")) {
            return "release";
        } else if (version.contains("alpha") || version.contains("beta")) {
            return "beta";
        } else {
            return "release";
        }
    }

    public static boolean checkConnection() {
        try {
            URL url = new URL(MODRINTH_API + "/project/" + PROJECT_ID);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", API_TOKEN);

            int responseCode = conn.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            LOGGER.error("Failed to connect to Modrinth API", e);
            return false;
        }
    }
}
