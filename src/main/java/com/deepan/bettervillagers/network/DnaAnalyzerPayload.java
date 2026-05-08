package com.deepan.bettervillagers.network;

import com.deepan.bettervillagers.BetterVillagers;
import com.deepan.bettervillagers.client.screen.DnaAnalyzerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record DnaAnalyzerPayload(
    UUID rootId,
    List<FamilyTreeNode> nodes,
    List<FamilyTreeEdge> edges
) implements CustomPacketPayload {
    public static final Type<DnaAnalyzerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterVillagers.MODID, "dna_analyzer"));
    public static final StreamCodec<FriendlyByteBuf, DnaAnalyzerPayload> STREAM_CODEC = CustomPacketPayload.codec(DnaAnalyzerPayload::write, DnaAnalyzerPayload::new);

    private DnaAnalyzerPayload(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), readNodes(buffer), readEdges(buffer));
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.rootId);
        writeNodes(buffer, this.nodes);
        writeEdges(buffer, this.edges);
    }

    public static void handleClient(DnaAnalyzerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new DnaAnalyzerScreen(payload)));
    }

    private static void writeNodes(FriendlyByteBuf buffer, List<FamilyTreeNode> nodes) {
        buffer.writeVarInt(nodes.size());
        for (FamilyTreeNode node : nodes) {
            buffer.writeUUID(node.genealogyId());
            buffer.writeUtf(node.name());
            buffer.writeUtf(node.villagerType());
            buffer.writeUtf(node.status());
            buffer.writeUtf(node.relation());
            buffer.writeUtf(node.path());
            buffer.writeVarInt(node.band());
            buffer.writeVarInt(node.slot());
            buffer.writeBoolean(node.root());
        }
    }

    private static List<FamilyTreeNode> readNodes(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<FamilyTreeNode> nodes = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            nodes.add(
                new FamilyTreeNode(
                    buffer.readUUID(),
                    buffer.readUtf(),
                    buffer.readUtf(),
                    buffer.readUtf(),
                    buffer.readUtf(),
                    buffer.readUtf(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readBoolean()
                )
            );
        }
        return nodes;
    }

    private static void writeEdges(FriendlyByteBuf buffer, List<FamilyTreeEdge> edges) {
        buffer.writeVarInt(edges.size());
        for (FamilyTreeEdge edge : edges) {
            buffer.writeUUID(edge.fromId());
            buffer.writeUUID(edge.toId());
        }
    }

    private static List<FamilyTreeEdge> readEdges(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<FamilyTreeEdge> edges = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            edges.add(new FamilyTreeEdge(buffer.readUUID(), buffer.readUUID()));
        }
        return edges;
    }

    @Override
    public Type<DnaAnalyzerPayload> type() {
        return TYPE;
    }

    public record FamilyTreeNode(
        UUID genealogyId,
        String name,
        String villagerType,
        String status,
        String relation,
        String path,
        int band,
        int slot,
        boolean root
    ) {
    }

    public record FamilyTreeEdge(UUID fromId, UUID toId) {
    }
}
