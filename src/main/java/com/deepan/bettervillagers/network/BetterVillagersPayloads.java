package com.deepan.bettervillagers.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class BetterVillagersPayloads {
    private BetterVillagersPayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(DnaAnalyzerPayload.TYPE, DnaAnalyzerPayload.STREAM_CODEC, (payload, context) -> {
            ClientPayloadHandler.handleDataOnClient(payload, context);
        });
        registrar.playToClient(com.deepan.bettervillagers.quest.network.OpenQuestBoardPayload.TYPE, com.deepan.bettervillagers.quest.network.OpenQuestBoardPayload.STREAM_CODEC, (payload, context) -> {
            ClientPayloadHandler.handleQuestBoardOnClient(payload, context);
        });
        registrar.playToClient(com.deepan.bettervillagers.quest.network.OpenDialoguePayload.TYPE, com.deepan.bettervillagers.quest.network.OpenDialoguePayload.STREAM_CODEC, (payload, context) -> {
            ClientPayloadHandler.handleDialogueOnClient(payload, context);
        });
        registrar.playToServer(com.deepan.bettervillagers.quest.network.DialogueActionPayload.TYPE, com.deepan.bettervillagers.quest.network.DialogueActionPayload.STREAM_CODEC, (payload, context) -> {
            ServerPayloadHandler.handleDialogueActionOnServer(payload, context);
        });
        registrar.playToServer(com.deepan.bettervillagers.quest.network.AcceptBountyPayload.TYPE, com.deepan.bettervillagers.quest.network.AcceptBountyPayload.STREAM_CODEC, (payload, context) -> {
            ServerPayloadHandler.handleAcceptBountyOnServer(payload, context);
        });
    }
}
