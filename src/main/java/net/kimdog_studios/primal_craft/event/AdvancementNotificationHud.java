package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.*;

public class AdvancementNotificationHud implements HudRenderCallback {
    private static class AdvancementNotification {
        public String title;
        public String description;
        public String advancementId;
        public ItemStack icon;
        public long spawnTime;
        public float alpha;
        public float offsetY;

        public AdvancementNotification(String title, String description, String advancementId, ItemStack icon) {
            this.title = title;
            this.description = description;
            this.advancementId = advancementId;
            this.icon = icon;
            this.spawnTime = System.currentTimeMillis();
            this.alpha = 0f;
            this.offsetY = -20f;
        }

        public boolean update() {
            long age = System.currentTimeMillis() - spawnTime;

            // Fade in (0-500ms)
            if (age < 500) {
                alpha = age / 500f;
                offsetY = -20f + (age / 500f) * 20f;
            }
            // Hold (500-4000ms)
            else if (age < 4000) {
                alpha = 1f;
                offsetY = 0f;
            }
            // Fade out (4000-5000ms)
            else if (age < 5000) {
                alpha = 1f - ((age - 4000) / 1000f);
                offsetY = ((age - 4000) / 1000f) * 10f;
            }
            // Remove
            else {
                return false;
            }

            return true;
        }
    }

    private static final List<AdvancementNotification> notifications = new ArrayList<>();

    public static void showAdvancementNotification(String title, String description, String advancementId, ItemStack icon) {
        notifications.add(new AdvancementNotification(title, description, advancementId, icon));
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        // Update and render notifications
        Iterator<AdvancementNotification> iterator = notifications.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            AdvancementNotification notif = iterator.next();
            if (!notif.update()) {
                iterator.remove();
                continue;
            }

            // Position: top right, like vanilla advancement
            int notifWidth = 250;
            int notifHeight = 50;
            int x = screenW - notifWidth - 10;
            int y = 10 + (index * 60) + (int) notif.offsetY;

            // Apply alpha
            int alpha = Math.max(0, Math.min(255, (int) (notif.alpha * 255)));
            int bgColor = (alpha << 24) | 0x0a0a0a; // Dark background
            int borderColor = (alpha << 24) | 0xFF9900; // Orange border (advancement color)

            // Draw border
            context.fill(x - 2, y - 2, x + notifWidth + 2, y + notifHeight + 2, borderColor);
            // Draw background
            context.fill(x, y, x + notifWidth, y + notifHeight, bgColor);

            // Draw icon (actual advancement icon like vanilla)
            if (!notif.icon.isEmpty()) {
                context.drawItem(notif.icon, x + 8, y + 8);
            } else {
                // Fallback: draw a colored square if no icon
                int iconColor = (alpha << 24) | 0xFFD700;
                context.fill(x + 5, y + 5, x + 35, y + 35, iconColor);
            }

            // Draw title
            String displayTitle = notif.title;
            if (displayTitle.length() > 25) {
                displayTitle = displayTitle.substring(0, 22) + "...";
            }
            context.drawText(client.textRenderer, displayTitle, x + 45, y + 8, (alpha << 24) | 0xFFFFFF, false);

            // Draw description
            String displayDesc = notif.description;
            if (displayDesc.length() > 30) {
                displayDesc = displayDesc.substring(0, 27) + "...";
            }
            context.drawText(client.textRenderer, displayDesc, x + 45, y + 22, (alpha << 24) | 0xAAAAAA, false);

            // Draw tooltip on hover
            int mouseX = (int) (client.mouse.getX() * screenW / client.getWindow().getWidth());
            int mouseY = (int) (client.mouse.getY() * screenH / client.getWindow().getHeight());

            if (mouseX >= x && mouseX <= x + notifWidth && mouseY >= y && mouseY <= y + notifHeight) {
                // Draw full title and description on hover
                List<Text> tooltip = new ArrayList<>();
                tooltip.add(Text.literal("§6" + notif.title)); // Gold title
                tooltip.add(Text.literal("§7" + notif.description)); // Gray description

                context.drawTooltip(client.textRenderer, tooltip, mouseX, mouseY);
            }

            index++;
        }
    }
}
