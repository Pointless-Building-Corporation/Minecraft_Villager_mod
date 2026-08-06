package com.deepan.bettervillagers.client.screen;

import com.deepan.bettervillagers.network.DnaAnalyzerPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DnaAnalyzerScreen extends Screen {
    private static final int PANEL_COLOR = 0xCC12151C;
    private static final int PANEL_BORDER = 0xFFB14B4B;
    private static final int ACCENT_COLOR = 0xFFF25D5D;
    private static final int TEXT_COLOR = 0xFFEDEFF5;
    private static final int MUTED_TEXT_COLOR = 0xFF9EA7B3;
    private static final int CONNECTOR_COLOR = 0xCCDC6E6E;
    private static final int NODE_WIDTH = 112;
    private static final int NODE_HEIGHT = 42;
    private static final int ROOT_WIDTH = 136;
    private static final int ROOT_HEIGHT = 52;
    private static final int MARGIN = 28;

    private final DnaAnalyzerPayload payload;
    private double scrollX = 0;
    private double scrollY = 0;

    public DnaAnalyzerScreen(DnaAnalyzerPayload payload) {
        super(Component.translatable("screen.bettervillagers.dna_analyzer"));
        this.payload = payload;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderAnalyzerBackdrop(guiGraphics);

        Map<UUID, PositionedNode> positionedNodes = layoutNodes();
        
        guiGraphics.enableScissor(0, 26, this.width, this.height);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.scrollX, this.scrollY, 0);
        renderEdges(guiGraphics, positionedNodes);
        renderNodes(guiGraphics, positionedNodes);
        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, ACCENT_COLOR);
    }

    private void renderAnalyzerBackdrop(GuiGraphics guiGraphics) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x40101318);
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0x18181E28, 0x28101318);
    }

    private Map<UUID, PositionedNode> layoutNodes() {
        Map<Integer, List<DnaAnalyzerPayload.FamilyTreeNode>> bands = new HashMap<>();
        int minBand = 0;
        int maxBand = 0;
        for (DnaAnalyzerPayload.FamilyTreeNode node : payload.nodes()) {
            bands.computeIfAbsent(node.band(), ignored -> new ArrayList<>()).add(node);
            minBand = Math.min(minBand, node.band());
            maxBand = Math.max(maxBand, node.band());
        }

        double availableHeight = Math.max(1, this.height - 110.0);
        double rawBandSpacing = availableHeight / Math.max(1, (maxBand - minBand + 1));
        final double bandSpacing = Mth.clamp((int) Math.round(rawBandSpacing), 54, 78);
        double centerY = this.height / 2.0 + 8.0;

        Map<UUID, PositionedNode> positioned = new HashMap<>();

        java.util.function.Consumer<Integer> placeBand = (band) -> {
            List<DnaAnalyzerPayload.FamilyTreeNode> bandNodes = bands.get(band);
            if (bandNodes == null) return;

            bandNodes.sort(Comparator.comparingDouble((DnaAnalyzerPayload.FamilyTreeNode node) -> {
                double sum = 0;
                int count = 0;
                for (DnaAnalyzerPayload.FamilyTreeEdge edge : payload.edges()) {
                    if (edge.toId().equals(node.genealogyId()) && positioned.containsKey(edge.fromId())) {
                        sum += positioned.get(edge.fromId()).centerX();
                        count++;
                    }
                    if (edge.fromId().equals(node.genealogyId()) && positioned.containsKey(edge.toId())) {
                        sum += positioned.get(edge.toId()).centerX();
                        count++;
                    }
                }
                return count > 0 ? sum / count : 0.0;
            }).thenComparing(DnaAnalyzerPayload.FamilyTreeNode::name));

            int width = bandNodes.stream().anyMatch(DnaAnalyzerPayload.FamilyTreeNode::root) ? ROOT_WIDTH : NODE_WIDTH;
            int spacing = width + 24;
            double startX = this.width / 2.0 - (bandNodes.size() - 1) * spacing / 2.0;
            double y = centerY + band * bandSpacing;

            for (int index = 0; index < bandNodes.size(); index++) {
                DnaAnalyzerPayload.FamilyTreeNode node = bandNodes.get(index);
                int nodeWidth = node.root() ? ROOT_WIDTH : NODE_WIDTH;
                int nodeHeight = node.root() ? ROOT_HEIGHT : NODE_HEIGHT;
                int x = (int) Math.round(startX + index * spacing - nodeWidth / 2.0);
                int yPos = (int) Math.round(y - nodeHeight / 2.0);
                positioned.put(node.genealogyId(), new PositionedNode(node, x, yPos, nodeWidth, nodeHeight));
            }
        };

        placeBand.accept(0);
        for (int b = 1; b <= maxBand; b++) placeBand.accept(b);
        for (int b = -1; b >= minBand; b--) placeBand.accept(b);

        return positioned;
    }

    private void renderEdges(GuiGraphics guiGraphics, Map<UUID, PositionedNode> positionedNodes) {
        for (DnaAnalyzerPayload.FamilyTreeEdge edge : payload.edges()) {
            PositionedNode from = positionedNodes.get(edge.fromId());
            PositionedNode to = positionedNodes.get(edge.toId());
            if (from == null || to == null) {
                continue;
            }

            int startX = from.centerX();
            int startY = from.bottom();
            int endX = to.centerX();
            int endY = to.top();
            int stagger = Math.abs(from.node().genealogyId().hashCode()) % 12 - 6;
            int midY = startY + (endY - startY) / 2 + stagger;

            drawVerticalLine(guiGraphics, startX, startY, midY, CONNECTOR_COLOR);
            drawHorizontalLine(guiGraphics, startX, endX, midY, CONNECTOR_COLOR);
            drawVerticalLine(guiGraphics, endX, midY, endY, CONNECTOR_COLOR);
        }
    }

    private void renderNodes(GuiGraphics guiGraphics, Map<UUID, PositionedNode> positionedNodes) {
        for (PositionedNode positionedNode : positionedNodes.values().stream()
            .sorted(Comparator.comparingInt(node -> node.node().root() ? 1 : 0))
            .toList()) {
            DnaAnalyzerPayload.FamilyTreeNode node = positionedNode.node();
            guiGraphics.fill(positionedNode.left(), positionedNode.top(), positionedNode.right(), positionedNode.bottom(), PANEL_COLOR);
            guiGraphics.renderOutline(positionedNode.left(), positionedNode.top(), positionedNode.width(), positionedNode.height(), PANEL_BORDER);
            guiGraphics.fill(positionedNode.left() + 6, positionedNode.top() + 6, positionedNode.right() - 6, positionedNode.top() + 8, ACCENT_COLOR);

            guiGraphics.drawCenteredString(this.font, Component.literal(trimText(node.name(), node.root() ? 16 : 13)),
                positionedNode.centerX(), positionedNode.top() + 12, TEXT_COLOR);
            guiGraphics.drawCenteredString(this.font, Component.literal(trimText(node.relation(), 16)),
                positionedNode.centerX(), positionedNode.top() + (node.root() ? 26 : 22), ACCENT_COLOR);
            guiGraphics.drawCenteredString(this.font, Component.literal(trimText(node.villagerType() + " - " + node.status(), 18)),
                positionedNode.centerX(), positionedNode.top() + (node.root() ? 38 : 32), MUTED_TEXT_COLOR);
        }
    }

    private String trimText(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, Math.max(0, maxLength - 1)) + "\u2026";
    }

    private void drawHorizontalLine(GuiGraphics guiGraphics, int startX, int endX, int y, int color) {
        int left = Math.min(startX, endX);
        int width = Math.max(1, Math.abs(endX - startX) + 1);
        guiGraphics.fill(left, y, left + width, y + 2, color);
    }

    private void drawVerticalLine(GuiGraphics guiGraphics, int x, int startY, int endY, int color) {
        int top = Math.min(startY, endY);
        int height = Math.max(1, Math.abs(endY - startY) + 1);
        guiGraphics.fill(x, top, x + 2, top + height, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.scrollX += dragX;
        this.scrollY += dragY;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private record PositionedNode(DnaAnalyzerPayload.FamilyTreeNode node, int left, int top, int width, int height) {
        int right() {
            return left + width;
        }

        int bottom() {
            return top + height;
        }

        int centerX() {
            return left + width / 2;
        }
    }
}
