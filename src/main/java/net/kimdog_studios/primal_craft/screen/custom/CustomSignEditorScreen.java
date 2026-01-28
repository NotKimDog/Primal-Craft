package net.kimdog_studios.primal_craft.screen.custom;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class CustomSignEditorScreen extends SignEditScreen {
    @SuppressWarnings("FieldCanBeLocal")
    private final BlockPos pos;
    private final SignBlockEntity signEntity;

    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PADDING = 5;

    // Color options
    private static final Formatting[] COLORS = {
        Formatting.BLACK,
        Formatting.DARK_BLUE,
        Formatting.DARK_GREEN,
        Formatting.DARK_AQUA,
        Formatting.DARK_RED,
        Formatting.DARK_PURPLE,
        Formatting.GOLD,
        Formatting.GRAY,
        Formatting.DARK_GRAY,
        Formatting.BLUE,
        Formatting.GREEN,
        Formatting.AQUA,
        Formatting.RED,
        Formatting.LIGHT_PURPLE,
        Formatting.YELLOW,
        Formatting.WHITE
    };

    // Format options
    private static final Formatting[] FORMATS = {
        Formatting.BOLD,
        Formatting.ITALIC,
        Formatting.UNDERLINE,
        Formatting.STRIKETHROUGH,
        Formatting.OBFUSCATED
    };

    private int selectedLine = 0;

    public CustomSignEditorScreen(BlockPos pos, SignBlockEntity signEntity) {
        super(signEntity, true, false);
        this.pos = pos;
        this.signEntity = signEntity;
    }

    @Override
    protected void init() {
        super.init();

        // Center the GUI on screen
        int centerX = this.width / 2;
        // Start below the vanilla sign editor (approximately 170px from top)
        int startY = 170;

        // Color buttons (4 rows of 4) - centered
        int colorGridWidth = 4 * (BUTTON_WIDTH + PADDING) - PADDING;
        int colorStartX = centerX - colorGridWidth / 2;

        for (int i = 0; i < COLORS.length; i++) {
            int row = i / 4;
            int col = i % 4;
            int x = colorStartX + col * (BUTTON_WIDTH + PADDING);
            int y = startY + row * (BUTTON_HEIGHT + PADDING);

            Formatting color = COLORS[i];
            ButtonWidget button = ButtonWidget.builder(
                Text.literal(getColorName(color)).formatted(color),
                btn -> applyColor(color)
            )
            .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

            this.addDrawableChild(button);
        }

        // Format buttons (below colors)
        int formatStartY = startY + 4 * (BUTTON_HEIGHT + PADDING) + 10;
        for (int i = 0; i < FORMATS.length; i++) {
            Formatting format = FORMATS[i];
            int x = colorStartX + i * (BUTTON_WIDTH + PADDING);
            ButtonWidget button = ButtonWidget.builder(
                Text.literal(getFormatName(format)).formatted(format),
                btn -> toggleFormat(format)
            )
            .dimensions(x, formatStartY, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

            this.addDrawableChild(button);
        }

        // Line selection buttons (below format buttons)
        int lineButtonStartY = formatStartY + BUTTON_HEIGHT + 15;
        for (int i = 0; i < 4; i++) {
            int line = i;
            int x = colorStartX + i * (BUTTON_WIDTH + PADDING);
            ButtonWidget lineButton = ButtonWidget.builder(
                Text.literal("Line " + (i + 1)),
                btn -> selectLine(line)
            )
            .dimensions(x, lineButtonStartY, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();
            this.addDrawableChild(lineButton);
        }

        // Clear formatting button
        ButtonWidget clearButton = ButtonWidget.builder(
            Text.literal("Clear Format"),
            btn -> clearFormatting()
        )
        .dimensions(centerX - 85, lineButtonStartY + BUTTON_HEIGHT + 15, 80, BUTTON_HEIGHT)
        .build();
        this.addDrawableChild(clearButton);

        // Reset button
        ButtonWidget resetButton = ButtonWidget.builder(
            Text.literal("Reset"),
            btn -> selectedLine = 0
        )
        .dimensions(centerX + 5, lineButtonStartY + BUTTON_HEIGHT + 15, 80, BUTTON_HEIGHT)
        .build();
        this.addDrawableChild(resetButton);
    }

    private void selectLine(int line) {
        this.selectedLine = line;
    }

    private void applyColor(Formatting color) {
        if (signEntity == null || client == null) return;

        // Get current text
        SignText signText = signEntity.getFrontText();
        Text currentText = signText.getMessage(selectedLine, false);
        String text = currentText.getString();

        // Create formatting code for the color
        String formattingCode = color.toString();

        // Send packet to server to update sign with formatted text
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
            new net.kimdog_studios.primal_craft.network.UpdateSignTextPayload(pos, selectedLine, text, formattingCode)
        );
    }

    private void toggleFormat(Formatting format) {
        if (signEntity == null || client == null) return;

        SignText signText = signEntity.getFrontText();
        Text currentText = signText.getMessage(selectedLine, false);
        String text = currentText.getString();

        // Create formatting code for the format
        String formattingCode = format.toString();

        // Send packet to server to update sign with formatted text
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
            new net.kimdog_studios.primal_craft.network.UpdateSignTextPayload(pos, selectedLine, text, formattingCode)
        );
    }

    private void clearFormatting() {
        if (signEntity == null || client == null) return;

        SignText signText = signEntity.getFrontText();
        Text currentText = signText.getMessage(selectedLine, false);
        String text = currentText.getString();

        // Remove all formatting by sending empty formatting string
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
            new net.kimdog_studios.primal_craft.network.UpdateSignTextPayload(pos, selectedLine, text, "")
        );
    }

    private String getColorName(Formatting color) {
        return switch (color) {
            case BLACK -> "Black";
            case DARK_BLUE -> "D.Blue";
            case DARK_GREEN -> "D.Green";
            case DARK_AQUA -> "D.Aqua";
            case DARK_RED -> "D.Red";
            case DARK_PURPLE -> "D.Purple";
            case GOLD -> "Gold";
            case GRAY -> "Gray";
            case DARK_GRAY -> "D.Gray";
            case BLUE -> "Blue";
            case GREEN -> "Green";
            case AQUA -> "Aqua";
            case RED -> "Red";
            case LIGHT_PURPLE -> "Pink";
            case YELLOW -> "Yellow";
            case WHITE -> "White";
            default -> "Unknown";
        };
    }

    private String getFormatName(Formatting format) {
        return switch (format) {
            case BOLD -> "Bold";
            case ITALIC -> "Italic";
            case UNDERLINE -> "Underline";
            case STRIKETHROUGH -> "Strike";
            case OBFUSCATED -> "Magic";
            default -> "Unknown";
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render parent screen first (vanilla sign editor)
        super.render(context, mouseX, mouseY, delta);


        // Draw semi-transparent overlay to separate vanilla editor from custom buttons
        context.fill(0, 135, this.width, 145, 0x44000000);

        // Render text label for custom options
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("Color & Format Options:").formatted(Formatting.YELLOW),
            this.width / 2,
            150,
            0xFFFFFF
        );

        // Render selected line indicator
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("Selected Line: " + (selectedLine + 1)).formatted(Formatting.AQUA),
            this.width / 2,
            160,
            0xFFFFFF
        );
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
