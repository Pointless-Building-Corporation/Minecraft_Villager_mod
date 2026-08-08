package com.deepan.bettervillagers.quest.client;

import com.deepan.bettervillagers.BetterVillagers;
import com.deepan.bettervillagers.quest.Bounty;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class QuestBoardScreen extends Screen {
    private static final ResourceLocation BOARD_TEXTURE = ResourceLocation.fromNamespaceAndPath(BetterVillagers.MODID, "textures/gui/quest_board.png");
    private final List<Bounty> bounties;

    public QuestBoardScreen(List<Bounty> bounties) {
        super(Component.translatable("gui.bettervillagers.quest_board"));
        this.bounties = bounties;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        
        int x = (this.width - 256) / 2;
        int y = (this.height - 256) / 2;
        
        // Draw the background board (assuming 1024x1024 source texture, scaled to 256x256)
        graphics.blit(BOARD_TEXTURE, x, y, 0, 0, 256, 256, 256, 256);
        
        graphics.drawString(this.font, this.title, x + 8, y + 8, 0x404040, false);
        
        int offsetY = y + 24;
        for (Bounty bounty : bounties) {
            graphics.renderItem(bounty.getObjectiveItem(), x + 8, offsetY);
            graphics.drawString(this.font, Component.literal("x" + bounty.getCountRequired() + " -> "), x + 28, offsetY + 4, 0xFFFFFF, true);
            graphics.renderItem(bounty.getRewardItem(), x + 80, offsetY);
            graphics.drawString(this.font, Component.literal("x" + bounty.getRewardCount()), x + 100, offsetY + 4, 0xFFFFFF, true);
            offsetY += 24;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
