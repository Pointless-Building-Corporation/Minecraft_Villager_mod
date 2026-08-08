package com.deepan.bettervillagers.quest.client.gui;

import com.deepan.bettervillagers.quest.network.OpenDialoguePayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class DialoguePanel {
    public static final int MARGIN = 10;

    public static void renderBackground(GuiGraphics graphics, int x, int y, int width, int height) {
        int innerX = x + MARGIN;
        int innerY = y + MARGIN;
        int innerWidth = width - (MARGIN * 2);
        int innerHeight = height - (MARGIN * 2);

        // Draw standard native dark translucent panel with border
        graphics.fill(innerX - 2, innerY - 2, innerX + innerWidth + 2, innerY + innerHeight + 2, 0xFF4A4A4A);
        graphics.fill(innerX - 1, innerY - 1, innerX + innerWidth + 1, innerY + innerHeight + 1, 0xFF1E1E1E);
        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, 0xE0000000);
    }

    public static int renderContent(GuiGraphics graphics, OpenDialoguePayload dialogue, int x, int y, int width, Font font) {
        if (dialogue == null) return 0;
        
        int innerX = x + MARGIN;
        int innerY = y + MARGIN;
        int innerWidth = width - (MARGIN * 2);
        
        // Draw Nameplate text
        Component nameplate = Component.literal("Villager");
        if (graphics != null) {
            graphics.drawString(font, nameplate, innerX + 5, innerY + 5, 0xFFD700, false);
        }
        
        // Draw horizontal line separator
        if (graphics != null) {
            graphics.fill(innerX + 5, innerY + 16, innerX + innerWidth - 5, innerY + 17, 0x55FFFFFF);
        }
        
        // Word wrap dialogue text
        String text = dialogue.text();
        List<FormattedCharSequence> wrappedLines = font.split(Component.literal(text), innerWidth - 10);
        
        int textY = innerY + 22;
        for (FormattedCharSequence line : wrappedLines) {
            if (graphics != null) {
                graphics.drawString(font, line, innerX + 5, textY, 0xFFEEEEEE, true);
            }
            textY += font.lineHeight + 2;
        }
        
        return textY - y; // total content height
    }

    public static void render(GuiGraphics graphics, OpenDialoguePayload dialogue, int x, int y, int width, int height, Font font) {
        graphics.pose().pushPose();
        renderBackground(graphics, x, y, width, height);
        renderContent(graphics, dialogue, x, y, width, font);
        graphics.pose().popPose();
    }
}
