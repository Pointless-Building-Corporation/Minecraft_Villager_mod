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
import java.util.Set;
import java.util.HashSet;

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
    private double zoom = 1.0;

    public DnaAnalyzerScreen(DnaAnalyzerPayload payload) {
        super(Component.translatable("screen.bettervillagers.dna_analyzer"));
        this.payload = payload;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderAnalyzerBackdrop(guiGraphics);

        List<SugiyamaLayoutEngine.LayoutNode> nodes = payload.nodes().stream()
            .map(n -> new SugiyamaLayoutEngine.LayoutNode(n.genealogyId(), n.name(), n.band(), n.root()))
            .toList();
        List<SugiyamaLayoutEngine.LayoutEdge> edges = payload.edges().stream()
            .map(e -> new SugiyamaLayoutEngine.LayoutEdge(e.fromId(), e.toId()))
            .toList();
            
        SugiyamaLayoutEngine.LayoutResult layout = SugiyamaLayoutEngine.compute(nodes, edges, this.width, this.height);
        
        guiGraphics.enableScissor(0, 26, this.width, this.height);
        guiGraphics.pose().pushPose();
        
        guiGraphics.pose().translate(this.width / 2.0f, this.height / 2.0f, 0);
        guiGraphics.pose().scale((float) this.zoom, (float) this.zoom, 1.0f);
        guiGraphics.pose().translate(-this.width / 2.0f + this.scrollX, -this.height / 2.0f + this.scrollY, 0);
        
        for (SugiyamaLayoutEngine.LineSegment line : layout.lines()) {
            if (line.isHorizontal()) {
                drawHorizontalLine(guiGraphics, line.startX(), line.endX(), line.startY(), line.color());
            } else {
                drawVerticalLine(guiGraphics, line.startX(), line.startY(), line.endY(), line.color());
            }
        }
        
        renderNodes(guiGraphics, layout.nodes());
        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, ACCENT_COLOR);
        guiGraphics.drawString(this.font, "Zoom: " + String.format("%.2f", this.zoom), 10, 10, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Scroll: " + String.format("%.2f, %.2f", this.scrollX, this.scrollY), 10, 20, 0xFFFFFF);
    }

    private void renderAnalyzerBackdrop(GuiGraphics guiGraphics) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x40101318);
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0x18181E28, 0x28101318);
    }

    private void renderNodes(GuiGraphics guiGraphics, Map<UUID, SugiyamaLayoutEngine.PositionedNode> positionedNodes) {
        for (DnaAnalyzerPayload.FamilyTreeNode payloadNode : payload.nodes().stream()
            .sorted(Comparator.comparingInt(n -> n.root() ? 1 : 0))
            .toList()) {
            
            SugiyamaLayoutEngine.PositionedNode positionedNode = positionedNodes.get(payloadNode.genealogyId());
            if (positionedNode == null) continue;
            
            DnaAnalyzerPayload.FamilyTreeNode node = payloadNode;
            int relColor = getRelationColor(node.relation());
            
            guiGraphics.fill(positionedNode.left(), positionedNode.top(), positionedNode.right(), positionedNode.bottom(), PANEL_COLOR);
            guiGraphics.renderOutline(positionedNode.left(), positionedNode.top(), positionedNode.width(), positionedNode.height(), relColor);
            guiGraphics.fill(positionedNode.left() + 6, positionedNode.top() + 6, positionedNode.right() - 6, positionedNode.top() + 8, relColor);

            guiGraphics.drawCenteredString(this.font, Component.literal(trimText(node.name(), node.root() ? 16 : 13)),
                positionedNode.centerX(), positionedNode.top() + 12, TEXT_COLOR);
            guiGraphics.drawCenteredString(this.font, Component.literal(trimText(node.relation(), 16)),
                positionedNode.centerX(), positionedNode.top() + (node.root() ? 26 : 22), relColor);
            guiGraphics.drawCenteredString(this.font, Component.literal(trimText(node.villagerType() + " - " + node.status(), 18)),
                positionedNode.centerX(), positionedNode.top() + (node.root() ? 38 : 32), MUTED_TEXT_COLOR);
        }
    }

    private int getRelationColor(String relation) {
        if (relation.equals("Self")) return 0xFFFFD700;
        if (relation.contains("Parent") || relation.contains("Grandparent")) return 0xFF4DA6FF;
        if (relation.contains("Child") || relation.contains("Grandchild")) return 0xFF5CD65C;
        if (relation.equals("Sibling") || relation.equals("Mate")) return 0xFFD279A6;
        return 0xFFA0A0A0;
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.setDragging(true);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.setDragging(false);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.scrollX += dragX / this.zoom;
        this.scrollY += dragY / this.zoom;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double scroll = scrollY != 0 ? scrollY : scrollX;
        if (scroll != 0) {
            this.zoom += Math.signum(scroll) * 0.1;
            this.zoom = Mth.clamp(this.zoom, 0.2, 4.0);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

}
