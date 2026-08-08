package com.deepan.bettervillagers.mixin;

import com.deepan.bettervillagers.quest.client.ClientDialogueManager;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int bettervillagers$modifyMouseXRender(int mouseX) {
        if (ClientDialogueManager.getCurrentDialogue() != null && (Object) this instanceof MerchantScreen screen) {
            int newLeftPos = 20;
            int shift = ((screen.width - 276) / 2) - newLeftPos;
            return mouseX + shift;
        }
        return mouseX;
    }

    @ModifyVariable(method = "mouseClicked", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double bettervillagers$modifyMouseXClick(double mouseX) {
        if (ClientDialogueManager.getCurrentDialogue() != null && (Object) this instanceof MerchantScreen screen) {
            int newLeftPos = 20;
            int shift = ((screen.width - 276) / 2) - newLeftPos;
            return mouseX + shift;
        }
        return mouseX;
    }

    @ModifyVariable(method = "mouseReleased", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double bettervillagers$modifyMouseXRelease(double mouseX) {
        if (ClientDialogueManager.getCurrentDialogue() != null && (Object) this instanceof MerchantScreen screen) {
            int newLeftPos = 20;
            int shift = ((screen.width - 276) / 2) - newLeftPos;
            return mouseX + shift;
        }
        return mouseX;
    }

    @ModifyVariable(method = "mouseDragged", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double bettervillagers$modifyMouseXDrag(double mouseX) {
        if (ClientDialogueManager.getCurrentDialogue() != null && (Object) this instanceof MerchantScreen screen) {
            int newLeftPos = 20;
            int shift = ((screen.width - 276) / 2) - newLeftPos;
            return mouseX + shift;
        }
        return mouseX;
    }
}
