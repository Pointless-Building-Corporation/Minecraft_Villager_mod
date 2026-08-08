package com.deepan.bettervillagers.mixin;

import com.deepan.bettervillagers.quest.client.ClientDialogueManager;
import com.deepan.bettervillagers.quest.client.gui.DialoguePanel;
import com.deepan.bettervillagers.quest.network.DialogueActionPayload;
import com.deepan.bettervillagers.quest.network.OpenDialoguePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(MerchantScreen.class)
public class MerchantScreenMixin {

    @Unique
    private List<Button> bettervillagers$buttons = new ArrayList<>();
    @Unique
    private OpenDialoguePayload bettervillagers$lastDialogue = null;
    @Unique
    private double bettervillagers$scrollOffset = 0;
    
    @Unique
    private int bettervillagers$panelHeight = 140;

    @Inject(method = "init", at = @At("HEAD"))
    private void bettervillagers$onInit(CallbackInfo ci) {
        bettervillagers$lastDialogue = null;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void bettervillagers$preRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (ClientDialogueManager.getCurrentDialogue() != null) {
            MerchantScreen screen = (MerchantScreen) (Object) this;
            int newLeftPos = 20;
            int shift = ((screen.width - 276) / 2) - newLeftPos;
            
            graphics.pose().pushPose();
            graphics.pose().translate(-shift, 0, 0);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void bettervillagers$renderDialogue(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        OpenDialoguePayload dialogue = ClientDialogueManager.getCurrentDialogue();
        if (dialogue == null) {
            bettervillagers$buttons.clear();
            bettervillagers$lastDialogue = null;
            return;
        }

        graphics.pose().popPose(); // Restore original un-shifted transform for the dialogue panel

        MerchantScreen screen = (MerchantScreen) (Object) this;
        
        int newLeftPos = 20;
        int panelX = newLeftPos + 276 + 10;
        int panelWidth = Math.max(100, screen.width - panelX - 20);
        
        int panelHeight = screen.height - 40;
        int panelY = 20;
        bettervillagers$panelHeight = panelHeight;

        if (bettervillagers$lastDialogue != dialogue) {
            bettervillagers$lastDialogue = dialogue;
            bettervillagers$buttons.clear();
            bettervillagers$scrollOffset = 0;
            
            java.util.List<com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption> options = dialogue.options();
            for (int i = 0; i < options.size(); i++) {
                com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption option = options.get(i);
                
                bettervillagers$buttons.add(net.minecraft.client.gui.components.Button.builder(net.minecraft.network.chat.Component.literal(option.text()), button -> {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new com.deepan.bettervillagers.quest.network.DialogueActionPayload(dialogue.entityId(), option.action())
                    );
                    ClientDialogueManager.clearDialogue();
                }).bounds(0, 0, panelWidth - (DialoguePanel.MARGIN * 2), 20).build());
            }
        }
        
        int innerX = panelX + DialoguePanel.MARGIN;
        int innerY = panelY + DialoguePanel.MARGIN;
        int innerWidth = panelWidth - (DialoguePanel.MARGIN * 2);

        // Render the main panel background
        DialoguePanel.renderBackground(graphics, panelX, panelY, panelWidth, panelHeight);
        
        // Calculate max scroll
        int fakeContentHeight = DialoguePanel.renderContent(null, dialogue, panelX, panelY, panelWidth, screen.getMinecraft().font); // dummy call to get size without graphics? We can't pass null graphics easily in Minecraft if it crashes.
        
        // Render content inside scissor
        graphics.enableScissor(innerX, innerY, innerX + innerWidth, innerY + panelHeight - (DialoguePanel.MARGIN * 2));
        graphics.pose().pushPose();
        graphics.pose().translate(0, -bettervillagers$scrollOffset, 0);
        
        int contentHeight = DialoguePanel.renderContent(graphics, dialogue, panelX, panelY, panelWidth, screen.getMinecraft().font);
        
        // Render dynamic buttons below text
        int buttonStartY = panelY + contentHeight + 10;
        int currentButtonY = buttonStartY;
        
        for (net.minecraft.client.gui.components.Button button : bettervillagers$buttons) {
            button.setPosition(innerX, currentButtonY);
            button.render(graphics, mouseX, (int)(mouseY + bettervillagers$scrollOffset), partialTick);
            currentButtonY += 25;
        }
        
        int totalContentHeight = (currentButtonY - panelY) + DialoguePanel.MARGIN;
        
        graphics.pose().popPose();
        graphics.disableScissor();

        // Ensure scroll offset bounds are correct
        double maxScroll = Math.max(0, totalContentHeight - panelHeight);
        if (bettervillagers$scrollOffset > maxScroll) bettervillagers$scrollOffset = maxScroll;
        if (bettervillagers$scrollOffset < 0) bettervillagers$scrollOffset = 0;

        // Render scrollbar if necessary
        if (maxScroll > 0) {
            int scrollbarX = panelX + panelWidth - 5;
            int scrollbarY = innerY;
            int scrollbarHeight = panelHeight - (DialoguePanel.MARGIN * 2);
            int handleHeight = Math.max(10, (int)(scrollbarHeight * ((float)panelHeight / totalContentHeight)));
            int handleY = scrollbarY + (int)((scrollbarHeight - handleHeight) * (bettervillagers$scrollOffset / maxScroll));
            
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + 2, scrollbarY + scrollbarHeight, 0xFF333333);
            graphics.fill(scrollbarX, handleY, scrollbarX + 2, handleY + handleHeight, 0xFFAAAAAA);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void bettervillagers$onDialogueClick(double mouseX, double mouseY, int buttonId, CallbackInfoReturnable<Boolean> cir) {
        if (ClientDialogueManager.getCurrentDialogue() == null) return;

        double adjustedMouseY = mouseY + bettervillagers$scrollOffset;
        MerchantScreen screen = (MerchantScreen) (Object) this;
        
        int newLeftPos = 20;
        int panelX = newLeftPos + 276 + 10;
        int panelWidth = Math.max(100, screen.width - panelX - 20);
        
        int panelHeight = screen.height - 40;
        int panelY = 20;
        int innerY = panelY + DialoguePanel.MARGIN;
        
        for (Button button : bettervillagers$buttons) {
            // Only allow clicks if the mouse is actually inside the clipped visual region
            if (mouseY >= innerY && mouseY <= innerY + panelHeight - (DialoguePanel.MARGIN * 2)) {
                if (button.mouseClicked(mouseX, adjustedMouseY, buttonId)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
        
        // If clicking inside the dialogue bounds, block click from falling through
        if (mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= panelY && mouseY <= panelY + panelHeight) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void bettervillagers$onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (ClientDialogueManager.getCurrentDialogue() == null) return;
        
        MerchantScreen screen = (MerchantScreen) (Object) this;
        
        int newLeftPos = 20;
        int panelX = newLeftPos + 276 + 10;
        int panelWidth = Math.max(100, screen.width - panelX - 20);
        
        int panelHeight = screen.height - 40;
        int panelY = 20;
        
        if (mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= panelY && mouseY <= panelY + panelHeight) {
            bettervillagers$scrollOffset -= scrollY * 15; // 15 pixels per scroll tick
            cir.setReturnValue(true);
        }
    }
}
