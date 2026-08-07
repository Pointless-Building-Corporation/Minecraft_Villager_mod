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
    }
}
