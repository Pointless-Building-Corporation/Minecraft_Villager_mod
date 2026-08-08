package com.deepan.bettervillagers.quest.client;

import com.deepan.bettervillagers.quest.network.OpenDialoguePayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = com.deepan.bettervillagers.BetterVillagers.MODID, value = Dist.CLIENT)
public class ClientDialogueManager {
    private static OpenDialoguePayload currentDialogue = null;
    private static int ticksSinceDialogueReceived = -1;

    public static void setCurrentDialogue(OpenDialoguePayload dialogue) {
        currentDialogue = dialogue;
        ticksSinceDialogueReceived = 0;
    }

    public static OpenDialoguePayload getCurrentDialogue() {
        return currentDialogue;
    }
    
    public static void clearDialogue() {
        currentDialogue = null;
        ticksSinceDialogueReceived = -1;
    }
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (currentDialogue != null) {
            Minecraft mc = Minecraft.getInstance();
            if (ticksSinceDialogueReceived >= 0) {
                ticksSinceDialogueReceived++;
                // If 2 ticks have passed and we still haven't opened a MerchantScreen, open standalone
                if (ticksSinceDialogueReceived > 2) {
                    if (!(mc.screen instanceof net.minecraft.client.gui.screens.inventory.MerchantScreen) 
                     && !(mc.screen instanceof StandaloneDialogueScreen)) {
                        mc.setScreen(new StandaloneDialogueScreen());
                    }
                    ticksSinceDialogueReceived = -1; // stop counting
                }
            } else {
                // Clear dialogue state if we are no longer in a valid dialogue screen
                if (!(mc.screen instanceof net.minecraft.client.gui.screens.inventory.MerchantScreen) 
                 && !(mc.screen instanceof StandaloneDialogueScreen)) {
                    clearDialogue();
                }
            }
        }
    }
}
