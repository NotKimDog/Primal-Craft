package net.kimdog_studios.primal_craft.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple JSON file-backed daily login streak store (global in config dir).
 */
public final class LoginStreakService {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Entry>>() {}.getType();

    private static final Map<Path, Map<String, Entry>> CACHE_PER_WORLD = new HashMap<>();

    public record Entry(int streak, long lastDay) {}
    public record Result(int streak, long lastDay, boolean increased, boolean broken, int previous) {}

    private LoginStreakService() {}

    private static Path getFile(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("kimdog_login_streaks.json");
    }

    private static Map<String, Entry> ensureLoaded(Path file) {
        Map<String, Entry> cache = CACHE_PER_WORLD.get(file);
        if (cache != null) return cache;
        if (Files.isRegularFile(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                Map<String, Entry> loaded = GSON.fromJson(r, MAP_TYPE);
                cache = (loaded != null) ? loaded : new HashMap<>();
            } catch (IOException e) {
                cache = new HashMap<>();
            }
        } else {
            cache = new HashMap<>();
        }
        CACHE_PER_WORLD.put(file, cache);
        return cache;
    }

    private static void save(Path file, Map<String, Entry> cache) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer w = Files.newBufferedWriter(file)) {
                GSON.toJson(cache, MAP_TYPE, w);
            }
        } catch (IOException ignored) {}
    }

    public static Result updateAndGetWithEvent(ServerPlayerEntity player, MinecraftServer server) {
        Path file = getFile(server);
        Map<String, Entry> cache = ensureLoaded(file);
        String key = player.getUuid().toString();
        long today = LocalDate.now(ZoneOffset.UTC).toEpochDay();
        Entry existing = cache.getOrDefault(key, new Entry(0, -1));
        int prev = existing.streak();
        int streak;
        boolean increased = false;
        boolean broken = false;
        if (existing.lastDay == today) {
            streak = existing.streak;
        } else if (existing.lastDay == today - 1) {
            streak = existing.streak + 1;
            increased = true;
        } else {
            broken = existing.lastDay != -1 && existing.streak > 0;
            streak = 1;
        }
        cache.put(key, new Entry(streak, today));
        save(file, cache);
        return new Result(streak, today, increased, broken, prev);
    }
}
