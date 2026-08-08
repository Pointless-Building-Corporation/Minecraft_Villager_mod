package com.deepan.bettervillagers.quest.client;

import com.deepan.bettervillagers.quest.client.gui.DialoguePanel;
import com.deepan.bettervillagers.quest.network.DialogueActionPayload;
import com.deepan.bettervillagers.quest.network.OpenDialoguePayload;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StandaloneDialogueScreen extends Screen {

    public StandaloneDialogueScreen() {
        super(Component.translatable("screen.bettervillagers.dialogue"));
    }

    @Override
    protected void init() {
        super.init();
        OpenDialoguePayload dialogue = ClientDialogueManager.getCurrentDialogue();
        if (dialogue == null) {
            this.onClose();
            return;
        }

        int panelWidth = 276;
        int panelHeight = 140;
        int x = (this.width - panelWidth) / 2;
        int y = (this.height - panelHeight) / 2;
        
        int innerX = x + DialoguePanel.MARGIN;
        int innerY = y + DialoguePanel.MARGIN;
        int innerWidth = panelWidth - (DialoguePanel.MARGIN * 2);

        int innerHeight = panelHeight - (DialoguePanel.MARGIN * 2);

        // Rebuild dynamic buttons from dialogue options
        int bottomY = innerY + innerHeight - 30;
        int buttonWidth = innerWidth - 10;
        
        List<OpenDialoguePayload.DialogueOption> options = dialogue.options();
        for (int i = 0; i < options.size(); i++) {
            OpenDialoguePayload.DialogueOption option = options.get(i);
            int yOffset = bottomY - ((options.size() - i) * 20) - ((options.size() - i - 1) * 5); // 20 height, 5 padding
            
            this.addRenderableWidget(Button.builder(Component.literal(option.text()), button -> {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new com.deepan.bettervillagers.quest.network.DialogueActionPayload(dialogue.entityId(), option.action())
                );
                this.onClose();
            }).bounds(innerX + 5, yOffset, buttonWidth, 20).build());
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Only render the translucent dark gradient, bypassing the vanilla blur shader
        this.renderTransparentBackground(graphics);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render custom background (transparent only, no blur)
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        
        OpenDialoguePayload dialogue = ClientDialogueManager.getCurrentDialogue();
        if (dialogue == null) {
            this.onClose();
            return;
        }

        int panelWidth = 276;
        int panelHeight = 140;
        int x = (this.width - panelWidth) / 2;
        int y = (this.height - panelHeight) / 2;

        // Render the main panel background and text
        DialoguePanel.render(graphics, dialogue, x, y, panelWidth, panelHeight, this.font);

        // Render standard widgets (our buttons)
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        ClientDialogueManager.clearDialogue();
    }
}
