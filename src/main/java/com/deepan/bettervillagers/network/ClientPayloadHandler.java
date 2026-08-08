package com.deepan.bettervillagers.network;

import com.deepan.bettervillagers.client.screen.DnaAnalyzerScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleDataOnClient(DnaAnalyzerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new DnaAnalyzerScreen(payload));
        });
    }

    public static void handleQuestBoardOnClient(com.deepan.bettervillagers.quest.network.OpenQuestBoardPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new com.deepan.bettervillagers.quest.client.QuestBoardScreen(payload.bounties()));
        });
    }

    public static void handleDialogueOnClient(com.deepan.bettervillagers.quest.network.OpenDialoguePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            com.deepan.bettervillagers.quest.client.ClientDialogueManager.setCurrentDialogue(payload);
        });
    }
}
