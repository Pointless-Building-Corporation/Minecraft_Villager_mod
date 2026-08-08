import java.util.*;
import java.util.stream.*;

public class Test {
    record RelationNode(UUID genealogyId, String name, String path, String relation, int band) {}

    public static void main(String[] args) {
        Map<UUID, RelationNode> relationMap = new LinkedHashMap<>();
        UUID root = UUID.randomUUID();
        UUID child = UUID.randomUUID();
        
        relationMap.put(root, new RelationNode(root, "Ivar", "", "Self", 0));
        relationMap.put(child, new RelationNode(child, "Ivar", "C", "Child", 1));
        
        List<RelationNode> orderedNodes = relationMap.values().stream()
            .sorted(Comparator
                .comparingInt((RelationNode node) -> priority(node.relation()))
                .thenComparingInt(RelationNode::band)
                .thenComparing(RelationNode::path)
                .thenComparing(RelationNode::name))
            .toList();
            
        System.out.println("Sorted: " + orderedNodes.size());
    }
    
    static int priority(String relation) {
        return relation.equals("Self") ? 0 : 2;
    }
}
