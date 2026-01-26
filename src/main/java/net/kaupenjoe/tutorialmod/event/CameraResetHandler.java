package net.kaupenjoe.tutorialmod.event;

public class CameraResetHandler {
    private static float previousYaw = 0.0f;
    private static float previousPitch = 0.0f;
    private static float targetYaw = 0.0f;
    private static float targetPitch = 0.0f;
    private static boolean wasZooming = false;
    private static float resetProgress = 0.0f;
    private static boolean isResetting = false;

    private static final float RESET_SPEED = 0.35f; // Speed of camera centering (cinematic)

    public static void tick() {
        boolean currentlyZooming = ZoomHandler.isZooming();

        // Detect transition from zooming to not zooming
        if (wasZooming && !currentlyZooming) {
            // Start reset
            isResetting = true;
            resetProgress = 0.0f;
        }

        // Detect transition from not zooming to zooming
        if (!wasZooming && currentlyZooming) {
            // Start reset
            isResetting = true;
            resetProgress = 0.0f;
        }

        // Update progress
        if (isResetting) {
            resetProgress += RESET_SPEED;
            if (resetProgress >= 1.0f) {
                resetProgress = 1.0f;
                isResetting = false;
            }
        }

        wasZooming = currentlyZooming;
    }

    public static boolean isResetting() {
        return isResetting;
    }

    public static float getResetProgress() {
        return resetProgress;
    }

    public static void setTargetRotation(float yaw, float pitch) {
        targetYaw = yaw;
        targetPitch = pitch;
    }

    public static float getTargetYaw() {
        return targetYaw;
    }

    public static float getTargetPitch() {
        return targetPitch;
    }
}
