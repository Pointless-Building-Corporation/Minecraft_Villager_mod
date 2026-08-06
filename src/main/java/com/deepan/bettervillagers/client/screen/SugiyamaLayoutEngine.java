package com.deepan.bettervillagers.client.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SugiyamaLayoutEngine {
    private static final int NODE_WIDTH = 112;
    private static final int NODE_HEIGHT = 42;
    private static final int ROOT_WIDTH = 136;
    private static final int ROOT_HEIGHT = 52;
    private static final int GAP = 48; // Spacing between nodes horizontally
    
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

    private static final int[] FAMILY_COLORS = {
        0xCCFF5252, 0xCC448AFF, 0xCC69F0AE, 0xFFFFAB40, 0xCCE040FB,
        0xCC18FFFF, 0xFFFFFF00, 0xFFFF4081, 0xCC69F0AE, 0xCC536DFE
    };

    public static LayoutResult compute(List<LayoutNode> nodes, List<LayoutEdge> edges, int viewWidth, int viewHeight) {
        Map<Integer, List<LayoutNode>> bands = new HashMap<>();
        int minBand = 0;
        int maxBand = 0;
        for (LayoutNode node : nodes) {
            bands.computeIfAbsent(node.band(), ignored -> new ArrayList<>()).add(node);
            minBand = Math.min(minBand, node.band());
            maxBand = Math.max(maxBand, node.band());
        }

        Map<UUID, Double> currentX = new HashMap<>();
        // Initial arbitrary placement
        for (Map.Entry<Integer, List<LayoutNode>> entry : bands.entrySet()) {
            double x = 0;
            for (LayoutNode n : entry.getValue()) {
                currentX.put(n.id(), x);
                x += 200;
            }
        }

        // Multi-sweep barycenter heuristic for crossing minimization
        for (int iter = 0; iter < 4; iter++) {
            // Top-down sweep
            for (int b = minBand; b <= maxBand; b++) {
                sweepBand(bands.get(b), edges, currentX, false);
            }
            // Bottom-up sweep
            for (int b = maxBand; b >= minBand; b--) {
                sweepBand(bands.get(b), edges, currentX, true);
            }
        }

        // Final coordinate assignment
        double availableHeight = Math.max(1, viewHeight - 110.0);
        double rawBandSpacing = availableHeight / Math.max(1, (maxBand - minBand + 1));
        final double bandSpacing = Math.max(140, Math.min((int) Math.round(rawBandSpacing), 240));
        double centerY = viewHeight / 2.0 + 8.0;

        Map<UUID, PositionedNode> positioned = new HashMap<>();

        for (Map.Entry<Integer, List<LayoutNode>> entry : bands.entrySet()) {
            int band = entry.getKey();
            List<LayoutNode> bandNodes = entry.getValue();
            double y = centerY + band * bandSpacing;
            
            for (LayoutNode node : bandNodes) {
                int nodeWidth = node.root() ? ROOT_WIDTH : NODE_WIDTH;
                int nodeHeight = node.root() ? ROOT_HEIGHT : NODE_HEIGHT;
                int xPos = (int) Math.round(currentX.get(node.id()) - nodeWidth / 2.0);
                int yPos = (int) Math.round(y - nodeHeight / 2.0);
                positioned.put(node.id(), new PositionedNode(node, xPos, yPos, nodeWidth, nodeHeight));
            }
        }

        List<LineSegment> lines = generateEdges(edges, positioned);
        return new LayoutResult(positioned, lines);
    }

    private static void sweepBand(List<LayoutNode> bandNodes, List<LayoutEdge> edges, Map<UUID, Double> currentX, boolean checkingChildren) {
        if (bandNodes == null || bandNodes.isEmpty()) return;
        
        List<List<LayoutNode>> groups = getFamilyGroups(bandNodes, edges);
        
        Map<List<LayoutNode>, Double> groupBarycenter = new HashMap<>();
        for (List<LayoutNode> group : groups) {
            double sum = 0;
            for (LayoutNode n : group) {
                sum += getBarycenter(n, edges, currentX, checkingChildren);
            }
            groupBarycenter.put(group, sum / group.size());
        }
        
        groups.sort(Comparator.comparingDouble(groupBarycenter::get));
        
        double[] groupIdealX = new double[groups.size()];
        double[] groupWidths = new double[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            List<LayoutNode> group = groups.get(i);
            groupIdealX[i] = groupBarycenter.get(group);
            double w = 0;
            for (LayoutNode n : group) w += (n.root() ? ROOT_WIDTH : NODE_WIDTH) + GAP;
            groupWidths[i] = w - GAP;
        }
        
        double[] actualGroupX = new double[groups.size()];
        if (groups.size() > 0) {
            actualGroupX[0] = groupIdealX[0];
            for (int i = 1; i < groups.size(); i++) {
                double minX = actualGroupX[i-1] + groupWidths[i-1]/2.0 + GAP + groupWidths[i]/2.0;
                actualGroupX[i] = Math.max(groupIdealX[i], minX);
            }
            for (int i = groups.size() - 2; i >= 0; i--) {
                double maxX = actualGroupX[i+1] - groupWidths[i+1]/2.0 - GAP - groupWidths[i]/2.0;
                actualGroupX[i] = Math.min(actualGroupX[i], maxX);
            }
        }
        
        for (int i = 0; i < groups.size(); i++) {
            List<LayoutNode> group = groups.get(i);
            double startX = actualGroupX[i] - groupWidths[i]/2.0;
            double currX = startX;
            for (LayoutNode n : group) {
                int w = n.root() ? ROOT_WIDTH : NODE_WIDTH;
                currX += w / 2.0;
                currentX.put(n.id(), currX);
                currX += w / 2.0 + GAP;
            }
        }
    }

    private static double getBarycenter(LayoutNode node, List<LayoutEdge> edges, Map<UUID, Double> currentX, boolean checkingChildren) {
        double sum = 0;
        int count = 0;
        for (LayoutEdge e : edges) {
            UUID target = checkingChildren ? e.toId() : e.fromId();
            UUID source = checkingChildren ? e.fromId() : e.toId();
            if (source.equals(node.id()) && currentX.containsKey(target)) {
                sum += currentX.get(target);
                count++;
            }
        }
        return count > 0 ? sum / count : currentX.getOrDefault(node.id(), 0.0);
    }

    private static List<List<LayoutNode>> getFamilyGroups(List<LayoutNode> bandNodes, List<LayoutEdge> edges) {
        Map<UUID, Set<UUID>> coParents = new HashMap<>();
        for (LayoutNode n : bandNodes) coParents.put(n.id(), new HashSet<>());
        
        Map<UUID, List<UUID>> parentsOfChild = new HashMap<>();
        for (LayoutEdge e : edges) {
            parentsOfChild.computeIfAbsent(e.toId(), k -> new ArrayList<>()).add(e.fromId());
        }
        
        for (List<UUID> parents : parentsOfChild.values()) {
            List<UUID> parentsInBand = parents.stream().filter(coParents::containsKey).collect(Collectors.toList());
            for (int i = 0; i < parentsInBand.size(); i++) {
                for (int j = i + 1; j < parentsInBand.size(); j++) {
                    coParents.get(parentsInBand.get(i)).add(parentsInBand.get(j));
                    coParents.get(parentsInBand.get(j)).add(parentsInBand.get(i));
                }
            }
        }
        
        List<List<LayoutNode>> groups = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        Map<UUID, LayoutNode> nodeMap = bandNodes.stream().collect(Collectors.toMap(LayoutNode::id, n -> n));
        
        for (LayoutNode n : bandNodes) {
            if (!visited.contains(n.id())) {
                List<LayoutNode> group = new ArrayList<>();
                Queue<UUID> q = new LinkedList<>();
                q.add(n.id());
                visited.add(n.id());
                while (!q.isEmpty()) {
                    UUID curr = q.poll();
                    group.add(nodeMap.get(curr));
                    for (UUID neighbor : coParents.get(curr)) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            q.add(neighbor);
                        }
                    }
                }
                groups.add(group);
            }
        }
        return groups;
    }

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

        // To ensure non-overlapping horizontal tracks, we group families by generation (the Y level of their children).
        // Then we assign distinct Y tracks to each family in that generation.
        Map<Integer, List<Set<UUID>>> familiesByGeneration = new HashMap<>();
        for (Set<UUID> parents : families.keySet()) {
            int childY = 0;
            List<UUID> children = families.get(parents);
            for (UUID cId : children) {
                PositionedNode c = positionedNodes.get(cId);
                if (c != null) childY = c.top();
            }
            familiesByGeneration.computeIfAbsent(childY, k -> new ArrayList<>()).add(parents);
        }

        int globalFamilyIndex = 0;
        
        for (Map.Entry<Integer, List<Set<UUID>>> genEntry : familiesByGeneration.entrySet()) {
            List<Set<UUID>> genFamilies = genEntry.getValue();
            
            // Sort genFamilies by their parent's average X to minimize track overlap optionally, 
            // but just assigning sequential tracks is enough for non-overlap.
            int trackIndex = 0;
            int totalTracks = genFamilies.size();
            
            for (Set<UUID> parents : genFamilies) {
                List<UUID> children = families.get(parents);
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
                int color = FAMILY_COLORS[globalFamilyIndex % FAMILY_COLORS.length];
                
                if (parents.size() > 1) {
                    int mateY = positionedNodes.get(parents.iterator().next()).centerY();
                    lines.add(new LineSegment(parentMinX, mateY, parentMaxX, mateY, true, true, color));
                }
                
                int startX = parentCenterX;
                int startY = parents.size() > 1 ? positionedNodes.get(parents.iterator().next()).centerY() : parentY;
                
                int childMinX = Integer.MAX_VALUE;
                int childMaxX = Integer.MIN_VALUE;
                int childY = genEntry.getKey();
                
                for (UUID cId : children) {
                    PositionedNode c = positionedNodes.get(cId);
                    if (c == null) { valid = false; break; }
                    childMinX = Math.min(childMinX, c.centerX());
                    childMaxX = Math.max(childMaxX, c.centerX());
                }
                if (!valid) continue;
                
                int endY = childY;
                // Allocate a dedicated track between parentY and endY
                // We use trackIndex to stagger them perfectly without overlapping.
                int availableSpace = endY - parentY;
                int trackStep = Math.max(4, (availableSpace - 20) / Math.max(1, totalTracks));
                int midY = parentY + 10 + (trackIndex * trackStep);
                
                int busMinX = Math.min(startX, childMinX);
                int busMaxX = Math.max(startX, childMaxX);
                
                lines.add(new LineSegment(startX, startY, startX, midY, false, false, color));
                lines.add(new LineSegment(busMinX, midY, busMaxX, midY, true, false, color));
                
                for (UUID cId : children) {
                    PositionedNode c = positionedNodes.get(cId);
                    lines.add(new LineSegment(c.centerX(), midY, c.centerX(), endY, false, false, color));
                }
                
                trackIndex++;
                globalFamilyIndex++;
            }
        }
        return lines;
    }
}
