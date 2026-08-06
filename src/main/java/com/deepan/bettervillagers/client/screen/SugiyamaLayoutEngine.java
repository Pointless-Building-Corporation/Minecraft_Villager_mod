package com.deepan.bettervillagers.client.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SugiyamaLayoutEngine {
    private static final int NODE_WIDTH = 112;
    private static final int NODE_HEIGHT = 42;
    private static final int ROOT_WIDTH = 136;
    private static final int ROOT_HEIGHT = 52;
    
    public record LayoutNode(UUID id, String name, int band, boolean root) {}
    public record LayoutEdge(UUID fromId, UUID toId) {}
    
    public record PositionedNode(LayoutNode node, int left, int top, int width, int height) {
        public int right() { return left + width; }
        public int bottom() { return top + height; }
        public int centerX() { return left + width / 2; }
        public int centerY() { return top + height / 2; }
    }

    public record LineSegment(int startX, int startY, int endX, int endY, boolean isHorizontal, boolean isMateLine, int color) {}

    public record LayoutResult(Map<UUID, PositionedNode> nodes, List<LineSegment> lines) {}

    public static LayoutResult compute(List<LayoutNode> nodes, List<LayoutEdge> edges, int viewWidth, int viewHeight) {
        Map<Integer, List<LayoutNode>> bands = new HashMap<>();
        int minBand = 0;
        int maxBand = 0;
        for (LayoutNode node : nodes) {
            bands.computeIfAbsent(node.band(), ignored -> new ArrayList<>()).add(node);
            minBand = Math.min(minBand, node.band());
            maxBand = Math.max(maxBand, node.band());
        }

        double availableHeight = Math.max(1, viewHeight - 110.0);
        double rawBandSpacing = availableHeight / Math.max(1, (maxBand - minBand + 1));
        final double bandSpacing = Math.max(140, Math.min((int) Math.round(rawBandSpacing), 240));
        double centerY = viewHeight / 2.0 + 8.0;
        double centerX = viewWidth / 2.0;

        Map<UUID, PositionedNode> positioned = new HashMap<>();

        java.util.function.Consumer<Integer> placeBand = (band) -> {
            List<LayoutNode> bandNodes = bands.get(band);
            if (bandNodes == null) return;

            Map<UUID, Double> idealX = new HashMap<>();
            for (LayoutNode node : bandNodes) {
                double sum = 0;
                int count = 0;
                for (LayoutEdge edge : edges) {
                    if (edge.toId().equals(node.id()) && positioned.containsKey(edge.fromId())) {
                        sum += positioned.get(edge.fromId()).centerX();
                        count++;
                    }
                }
                if (count == 0) {
                    for (LayoutEdge edge : edges) {
                        if (edge.fromId().equals(node.id()) && positioned.containsKey(edge.toId())) {
                            sum += positioned.get(edge.toId()).centerX();
                            count++;
                        }
                    }
                }
                idealX.put(node.id(), count > 0 ? sum / count : centerX);
            }

            bandNodes.sort(Comparator.comparingDouble((LayoutNode n) -> idealX.get(n.id()))
                .thenComparing(LayoutNode::name));

            double[] actualX = new double[bandNodes.size()];
            if (bandNodes.size() > 0) {
                int[] widths = new int[bandNodes.size()];
                for (int i = 0; i < bandNodes.size(); i++) {
                    widths[i] = bandNodes.get(i).root() ? ROOT_WIDTH : NODE_WIDTH;
                }
                int GAP = 48; // Spacing between nodes

                actualX[0] = idealX.get(bandNodes.get(0).id());
                for (int i = 1; i < bandNodes.size(); i++) {
                    double minX = actualX[i-1] + (widths[i-1] + widths[i])/2.0 + GAP;
                    double ideal = idealX.get(bandNodes.get(i).id());
                    actualX[i] = Math.max(ideal, minX);
                }
                
                for (int i = bandNodes.size() - 2; i >= 0; i--) {
                    double maxX = actualX[i+1] - (widths[i+1] + widths[i])/2.0 - GAP;
                    actualX[i] = Math.min(actualX[i], maxX);
                }
            }

            double y = centerY + band * bandSpacing;

            for (int index = 0; index < bandNodes.size(); index++) {
                LayoutNode node = bandNodes.get(index);
                int nodeWidth = node.root() ? ROOT_WIDTH : NODE_WIDTH;
                int nodeHeight = node.root() ? ROOT_HEIGHT : NODE_HEIGHT;
                int xPos = (int) Math.round(actualX[index] - nodeWidth / 2.0);
                int yPos = (int) Math.round(y - nodeHeight / 2.0);
                positioned.put(node.id(), new PositionedNode(node, xPos, yPos, nodeWidth, nodeHeight));
            }
        };

        placeBand.accept(0);
        for (int b = 1; b <= maxBand; b++) placeBand.accept(b);
        for (int b = -1; b >= minBand; b--) placeBand.accept(b);

        List<LineSegment> lines = generateEdges(edges, positioned);
        return new LayoutResult(positioned, lines);
    }

    private static final int[] FAMILY_COLORS = {
        0xCCFF5252, 0xCC448AFF, 0xCC69F0AE, 0xFFFFAB40, 0xCCE040FB,
        0xCC18FFFF, 0xFFFFFF00, 0xFFFF4081, 0xCC69F0AE, 0xCC536DFE
    };

    private static List<LineSegment> generateEdges(List<LayoutEdge> edges, Map<UUID, PositionedNode> positionedNodes) {
        List<LineSegment> lines = new ArrayList<>();
        Map<Set<UUID>, List<UUID>> families = new HashMap<>();
        Map<UUID, Set<UUID>> childToParents = new HashMap<>();
        
        for (LayoutEdge edge : edges) {
            childToParents.computeIfAbsent(edge.toId(), k -> new HashSet<>()).add(edge.fromId());
        }
        
        for (Map.Entry<UUID, Set<UUID>> entry : childToParents.entrySet()) {
            families.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        int familyIndex = 0;
        for (Map.Entry<Set<UUID>, List<UUID>> entry : families.entrySet()) {
            Set<UUID> parents = entry.getKey();
            List<UUID> children = entry.getValue();
            if (parents.isEmpty() || children.isEmpty()) continue;
            
            int parentMinX = Integer.MAX_VALUE;
            int parentMaxX = Integer.MIN_VALUE;
            int parentY = 0;
            boolean valid = true;
            
            for (UUID pId : parents) {
                PositionedNode p = positionedNodes.get(pId);
                if (p == null) { valid = false; break; }
                parentMinX = Math.min(parentMinX, p.centerX());
                parentMaxX = Math.max(parentMaxX, p.centerX());
                parentY = p.bottom();
            }
            if (!valid) continue;
            
            int parentCenterX = parentMinX + (parentMaxX - parentMinX) / 2;
            
            int color = FAMILY_COLORS[familyIndex % FAMILY_COLORS.length];
            
            if (parents.size() > 1) {
                int mateY = positionedNodes.get(parents.iterator().next()).centerY();
                lines.add(new LineSegment(parentMinX, mateY, parentMaxX, mateY, true, true, color));
            }
            
            int startX = parentCenterX;
            int startY = parents.size() > 1 ? positionedNodes.get(parents.iterator().next()).centerY() : parentY;
            
            int childMinX = Integer.MAX_VALUE;
            int childMaxX = Integer.MIN_VALUE;
            int childY = 0;
            
            for (UUID cId : children) {
                PositionedNode c = positionedNodes.get(cId);
                if (c == null) { valid = false; break; }
                childMinX = Math.min(childMinX, c.centerX());
                childMaxX = Math.max(childMaxX, c.centerX());
                childY = c.top();
            }
            if (!valid) continue;
            
            int endY = childY;
            int stagger = (familyIndex % 8) * 12 - 42;
            int midY = parentY + (endY - parentY) / 2 + stagger;
            
            int busMinX = Math.min(startX, childMinX);
            int busMaxX = Math.max(startX, childMaxX);
            
            lines.add(new LineSegment(startX, startY, startX, midY, false, false, color));
            lines.add(new LineSegment(busMinX, midY, busMaxX, midY, true, false, color));
            
            for (UUID cId : children) {
                PositionedNode c = positionedNodes.get(cId);
                lines.add(new LineSegment(c.centerX(), midY, c.centerX(), endY, false, false, color));
            }
            
            familyIndex++;
        }
        return lines;
    }
}
