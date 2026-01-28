package net.kimdog_studios.primal_craft.util;

import net.kimdog_studios.primal_craft.PrimalCraft;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized logging utility for consistent, detailed logging patterns across the mod.
 * Provides helpers for performance timing, event counting, state transitions, and more.
 */
public class LoggingHelper {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final Map<String, Long> OPERATION_START_TIMES = new ConcurrentHashMap<>();
    private static final Map<String, Integer> EVENT_COUNTERS = new ConcurrentHashMap<>();
    private static final Map<String, Object> STATE_TRACKING = new ConcurrentHashMap<>();

    private LoggingHelper() {}

    /**
     * Get the mod logger
     */
    public static Logger logger() {
        return PrimalCraft.LOGGER;
    }

    /**
     * Log a detailed operation start with timing
     */
    public static void startOperation(String operationName) {
        long startTime = System.nanoTime();
        OPERATION_START_TIMES.put(operationName, startTime);
        PrimalCraft.LOGGER.trace("⏱️  [START] {} - Beginning operation", operationName);
    }

    /**
     * Log operation completion with elapsed time
     */
    public static long endOperation(String operationName) {
        Long startTime = OPERATION_START_TIMES.remove(operationName);
        if (startTime == null) {
            PrimalCraft.LOGGER.warn("⚠️  [TIMING] No start time found for operation: {}", operationName);
            return 0;
        }

        long elapsedNanos = System.nanoTime() - startTime;
        double elapsedMs = elapsedNanos / 1_000_000.0;

        String level;
        if (elapsedMs > 100) {
            level = "🐢 [SLOW]";
        } else if (elapsedMs > 50) {
            level = "⚠️  [MODERATE]";
        } else {
            level = "✅ [FAST]";
        }

        PrimalCraft.LOGGER.debug("{} {} - Completed in {:.2f}ms", level, operationName, elapsedMs);
        return elapsedNanos;
    }

    /**
     * Increment event counter for tracking event frequency
     */
    public static void incrementEventCounter(String eventName) {
        int count = EVENT_COUNTERS.getOrDefault(eventName, 0) + 1;
        EVENT_COUNTERS.put(eventName, count);
        if (count % 100 == 0) {
            PrimalCraft.LOGGER.trace("📊 [EVENT_COUNT] {} - Occurrences: {}", eventName, count);
        }
    }

    /**
     * Reset event counter
     */
    public static int resetEventCounter(String eventName) {
        return EVENT_COUNTERS.remove(eventName) != null ? EVENT_COUNTERS.get(eventName) : 0;
    }

    /**
     * Get current event count
     */
    public static int getEventCount(String eventName) {
        return EVENT_COUNTERS.getOrDefault(eventName, 0);
    }

    /**
     * Track state transitions with detailed logging
     */
    public static void trackStateChange(String entity, String oldState, String newState) {
        STATE_TRACKING.put(entity, newState);
        PrimalCraft.LOGGER.debug("🔄 [STATE_CHANGE] {} - Transition: {} -> {}", entity, oldState, newState);
    }

    /**
     * Get current tracked state
     */
    public static Object getTrackedState(String entity) {
        return STATE_TRACKING.get(entity);
    }

    /**
     * Log a major system initialization
     */
    public static void logSystemInit(String systemName) {
        PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        PrimalCraft.LOGGER.info("║  INITIALIZING {}                                           ║", padString(systemName, 58));
        PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");
    }

    /**
     * Log system initialization complete
     */
    public static void logSystemInitComplete(String systemName, long elapsedMs) {
        PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        PrimalCraft.LOGGER.info("║  {} INITIALIZATION COMPLETE                       ║", padString(systemName, 44));
        PrimalCraft.LOGGER.info("║  Total Time: {}ms                                         ║", padString(String.valueOf(elapsedMs), 40));
        PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");
    }

    /**
     * Log a major section header
     */
    public static void logHeader(String title) {
        String padded = padString(title, 58);
        PrimalCraft.LOGGER.info("═══════════════════════════════════════════════════════════════");
        PrimalCraft.LOGGER.info("  {}", padded);
        PrimalCraft.LOGGER.info("═══════════════════════════════════════════════════════════════");
    }

    /**
     * Log a subsection
     */
    public static void logSubsection(String title) {
        PrimalCraft.LOGGER.info("  ▌ {}", title);
    }

    /**
     * Log an item in a list
     */
    public static void logItem(String item) {
        PrimalCraft.LOGGER.info("    ├─ {}", item);
    }

    /**
     * Log the final item in a list
     */
    public static void logItemFinal(String item) {
        PrimalCraft.LOGGER.info("    └─ {}", item);
    }

    /**
     * Log a detailed message with timestamp
     */
    public static void logDetailed(String category, String message, Object... args) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        PrimalCraft.LOGGER.debug("[{}] [{}] {}", timestamp, category, String.format(message, args));
    }

    /**
     * Log an error with full stack trace context
     */
    public static void logError(String context, Throwable throwable) {
        PrimalCraft.LOGGER.error("❌ [ERROR] {} - {}: {}",
            context, throwable.getClass().getSimpleName(), throwable.getMessage(), throwable);
    }

    /**
     * Log a warning with context
     */
    public static void logWarning(String context, String message) {
        PrimalCraft.LOGGER.warn("⚠️  [WARNING] {} - {}", context, message);
    }

    /**
     * Log successful operation
     */
    public static void logSuccess(String context, String message) {
        PrimalCraft.LOGGER.info("✅ [SUCCESS] {} - {}", context, message);
    }

    /**
     * Log a debug trace with context
     */
    public static void logTrace(String context, String message, Object... args) {
        PrimalCraft.LOGGER.trace("🔍 [TRACE] {} - {}", context, String.format(message, args));
    }

    /**
     * Log network synchronization event
     */
    public static void logNetworkSync(String payloadName, String direction, String details) {
        PrimalCraft.LOGGER.trace("🌐 [NETWORK] {} {} - {}", payloadName, direction, details);
    }

    /**
     * Log player action
     */
    public static void logPlayerAction(String playerName, String action, String details) {
        PrimalCraft.LOGGER.debug("👤 [PLAYER] {} - {} [{}]", playerName, action, details);
    }

    /**
     * Log performance metrics summary
     */
    public static void logPerformanceMetrics(String systemName, int operationCount, long totalTimeMs) {
        double avgTime = operationCount > 0 ? (double) totalTimeMs / operationCount : 0;
        PrimalCraft.LOGGER.info("📈 [PERFORMANCE] {} - Operations: {}, Total: {}ms, Avg: {:.2f}ms",
            systemName, operationCount, totalTimeMs, avgTime);
    }

    /**
     * Pad a string to a specific width for formatting
     */
    private static String padString(String str, int width) {
        if (str.length() >= width) {
            return str.substring(0, width);
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < width) {
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Format a number with thousands separator
     */
    public static String formatNumber(long num) {
        return String.format("%,d", num);
    }

    /**
     * Format a decimal with 2 places
     */
    public static String formatDecimal(double num) {
        return String.format("%.2f", num);
    }
}
