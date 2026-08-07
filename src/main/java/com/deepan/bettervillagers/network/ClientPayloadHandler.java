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
}
