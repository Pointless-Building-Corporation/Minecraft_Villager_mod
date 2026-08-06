package com.deepan.bettervillagers.client.screen;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SugiyamaLayoutEngineTest {

    @Test
    public void testSpousesArePlacedAdjacently() {
        UUID parent1 = UUID.randomUUID();
        UUID parent2 = UUID.randomUUID();
        UUID child = UUID.randomUUID();
        UUID sibling = UUID.randomUUID();

        List<SugiyamaLayoutEngine.LayoutNode> nodes = List.of(
            new SugiyamaLayoutEngine.LayoutNode(parent1, "P1", 0, false),
            new SugiyamaLayoutEngine.LayoutNode(sibling, "Sib", 0, false),
            new SugiyamaLayoutEngine.LayoutNode(parent2, "P2", 0, false),
            new SugiyamaLayoutEngine.LayoutNode(child, "C1", 1, false)
        );

        List<SugiyamaLayoutEngine.LayoutEdge> edges = List.of(
            new SugiyamaLayoutEngine.LayoutEdge(parent1, child),
            new SugiyamaLayoutEngine.LayoutEdge(parent2, child)
        );

        SugiyamaLayoutEngine.LayoutResult result = SugiyamaLayoutEngine.compute(nodes, edges, 800, 600);
        
        SugiyamaLayoutEngine.PositionedNode p1Node = result.nodes().get(parent1);
        SugiyamaLayoutEngine.PositionedNode p2Node = result.nodes().get(parent2);
        SugiyamaLayoutEngine.PositionedNode sibNode = result.nodes().get(sibling);

        int minX = Math.min(p1Node.centerX(), p2Node.centerX());
        int maxX = Math.max(p1Node.centerX(), p2Node.centerX());

        assertTrue(sibNode.centerX() < minX || sibNode.centerX() > maxX, 
            "Sibling should not be placed between spouses!");
    }

    @Test
    public void testHorizontalBusRoutingDoesNotOverlap() {
        UUID p1 = UUID.randomUUID(); UUID p2 = UUID.randomUUID();
        UUID p3 = UUID.randomUUID(); UUID p4 = UUID.randomUUID();
        UUID c1 = UUID.randomUUID(); UUID c2 = UUID.randomUUID();

        List<SugiyamaLayoutEngine.LayoutNode> nodes = List.of(
            new SugiyamaLayoutEngine.LayoutNode(p1, "P1", 0, false),
            new SugiyamaLayoutEngine.LayoutNode(p2, "P2", 0, false),
            new SugiyamaLayoutEngine.LayoutNode(p3, "P3", 0, false),
            new SugiyamaLayoutEngine.LayoutNode(p4, "P4", 0, false),
            new SugiyamaLayoutEngine.LayoutNode(c1, "C1", 1, false),
            new SugiyamaLayoutEngine.LayoutNode(c2, "C2", 1, false)
        );

        List<SugiyamaLayoutEngine.LayoutEdge> edges = List.of(
            new SugiyamaLayoutEngine.LayoutEdge(p1, c1),
            new SugiyamaLayoutEngine.LayoutEdge(p2, c1),
            new SugiyamaLayoutEngine.LayoutEdge(p3, c2),
            new SugiyamaLayoutEngine.LayoutEdge(p4, c2)
        );

        SugiyamaLayoutEngine.LayoutResult result = SugiyamaLayoutEngine.compute(nodes, edges, 800, 600);
        
        Set<Integer> horizontalYLevels = new HashSet<>();
        for (SugiyamaLayoutEngine.LineSegment line : result.lines()) {
            if (line.isHorizontal() && !line.isMateLine()) {
                assertTrue(horizontalYLevels.add(line.startY()), "Overlapping horizontal bus lines detected at Y=" + line.startY());
            }
        }
        
        assertEquals(2, horizontalYLevels.size(), "Should have exactly 2 distinct horizontal tracks for 2 families");
    }

    @Test
    public void testCrossingReduction() {
        // A complex cross-over scenario to verify barycenter heuristics
        UUID a = UUID.randomUUID(); UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID(); UUID d = UUID.randomUUID();
        
        List<SugiyamaLayoutEngine.LayoutNode> nodes = List.of(
            new SugiyamaLayoutEngine.LayoutNode(a, "A", 0, false),
            new SugiyamaLayoutEngine.LayoutNode(b, "B", 0, false),
            new SugiyamaLayoutEngine.LayoutNode(c, "C", 1, false),
            new SugiyamaLayoutEngine.LayoutNode(d, "D", 1, false)
        );

        // A connects to D, B connects to C (a cross)
        List<SugiyamaLayoutEngine.LayoutEdge> edges = List.of(
            new SugiyamaLayoutEngine.LayoutEdge(a, d),
            new SugiyamaLayoutEngine.LayoutEdge(b, c)
        );

        SugiyamaLayoutEngine.LayoutResult result = SugiyamaLayoutEngine.compute(nodes, edges, 800, 600);
        
        SugiyamaLayoutEngine.PositionedNode nodeA = result.nodes().get(a);
        SugiyamaLayoutEngine.PositionedNode nodeB = result.nodes().get(b);
        SugiyamaLayoutEngine.PositionedNode nodeC = result.nodes().get(c);
        SugiyamaLayoutEngine.PositionedNode nodeD = result.nodes().get(d);
        
        // If A is to the left of B, D should be to the left of C to avoid crossings.
        boolean aLeftOfB = nodeA.centerX() < nodeB.centerX();
        boolean dLeftOfC = nodeD.centerX() < nodeC.centerX();
        
        assertEquals(aLeftOfB, dLeftOfC, "Crossings were not properly resolved by barycenter heuristic");
    }
}
