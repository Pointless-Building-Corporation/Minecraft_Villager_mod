package com.deepan.bettervillagers.client.screen;

import com.deepan.bettervillagers.network.DnaAnalyzerPayload;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SugiyamaLayoutEngineTest {

    @Test
    public void testHorizontalBusIncludesParentXForSingleChild() {
        // Construct the edge case payload where a child has a single mapped parent in the payload.
        // Parents: Krish, Tar (in band 0)
        // Children: Aditi (child of Krish), AnayaTT (child of Krish, Tar) (in band 1)
        
        UUID krishId = UUID.randomUUID();
        UUID tarId = UUID.randomUUID();
        UUID aditiId = UUID.randomUUID();
        UUID anayaTtId = UUID.randomUUID();
        
        List<SugiyamaLayoutEngine.LayoutNode> nodes = List.of(
            new SugiyamaLayoutEngine.LayoutNode(krishId, "Krish Patel", 0, false),
            new SugiyamaLayoutEngine.LayoutNode(tarId, "Tar", 0, false),
            new SugiyamaLayoutEngine.LayoutNode(aditiId, "Aditi", 1, false),
            new SugiyamaLayoutEngine.LayoutNode(anayaTtId, "Anaya TT", 1, false)
        );
        
        List<SugiyamaLayoutEngine.LayoutEdge> edges = List.of(
            new SugiyamaLayoutEngine.LayoutEdge(krishId, aditiId),
            new SugiyamaLayoutEngine.LayoutEdge(krishId, anayaTtId),
            new SugiyamaLayoutEngine.LayoutEdge(tarId, anayaTtId)
        );
        
        SugiyamaLayoutEngine.LayoutResult result = SugiyamaLayoutEngine.compute(nodes, edges, 800, 600);
        
        // Find the horizontal bus for Krish -> Aditi
        // Family 1: Parents=[Krish], Children=[Aditi]
        SugiyamaLayoutEngine.PositionedNode krish = result.nodes().get(krishId);
        SugiyamaLayoutEngine.PositionedNode aditi = result.nodes().get(aditiId);
        
        boolean foundAditiBus = false;
        
        for (SugiyamaLayoutEngine.LineSegment line : result.lines()) {
            if (line.isHorizontal() && !line.isMateLine()) {
                // Check if this bus connects to Aditi's X
                if (line.startX() <= aditi.centerX() && line.endX() >= aditi.centerX()) {
                    // Check if it ALSO encompasses Krish's X! (This is the bug we fixed)
                    int parentCenterX = krish.centerX(); // Krish is the sole mapped parent in this family
                    
                    int minX = Math.min(line.startX(), line.endX());
                    int maxX = Math.max(line.startX(), line.endX());
                    
                    assertTrue(minX <= parentCenterX && maxX >= parentCenterX, 
                        "Horizontal bus must encompass parent X " + parentCenterX + ". Bus spans " + minX + " to " + maxX);
                    foundAditiBus = true;
                }
            }
        }
        
        assertTrue(foundAditiBus, "Could not find horizontal bus for Aditi");
    }
}
