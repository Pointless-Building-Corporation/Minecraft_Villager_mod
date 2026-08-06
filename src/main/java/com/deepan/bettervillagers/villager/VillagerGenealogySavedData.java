package com.deepan.bettervillagers.villager;

import com.deepan.bettervillagers.network.DnaAnalyzerPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VillagerGenealogySavedData extends SavedData {
    private static final String DATA_NAME = "bettervillagers_genealogy";
    private static final String RECORDS_KEY = "Records";
    private static final String GENEALOGY_ID_KEY = "GenealogyId";
    private static final int MAX_TREE_NODES = 96;

    private final Map<UUID, VillagerGenealogyRecord> records = new LinkedHashMap<>();

    public static VillagerGenealogySavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
            new Factory<>(VillagerGenealogySavedData::new, VillagerGenealogySavedData::load),
            DATA_NAME
        );
    }

    private static VillagerGenealogySavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        VillagerGenealogySavedData data = new VillagerGenealogySavedData();
        ListTag listTag = tag.getList(RECORDS_KEY, Tag.TAG_COMPOUND);
        for (Tag entry : listTag) {
            if (entry instanceof CompoundTag compoundTag) {
                VillagerGenealogyRecord record = VillagerGenealogyRecord.fromTag(compoundTag);
                data.records.put(record.genealogyId(), record);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        for (VillagerGenealogyRecord record : this.records.values()) {
            listTag.add(record.save());
        }
        tag.put(RECORDS_KEY, listTag);
        return tag;
    }

    public DnaAnalyzerPayload buildPayloadFor(Villager villager) {
        ServerLevel level = (ServerLevel) villager.level();
        MinecraftServer server = level.getServer();
        UUID rootId = ensureVillagerRecord(villager, getEffectiveBaseName(villager));

        Map<UUID, RelationNode> relationMap = buildRelationMap(rootId);
        List<RelationNode> orderedNodes = relationMap.values().stream()
            .sorted(Comparator
                .comparingInt((RelationNode node) -> relationPriority(node.relation()))
                .thenComparingInt(RelationNode::band)
                .thenComparing(RelationNode::path)
                .thenComparing(RelationNode::name))
            .toList();

        Map<Integer, Integer> bandCounts = new HashMap<>();
        List<DnaAnalyzerPayload.FamilyTreeNode> payloadNodes = new ArrayList<>(orderedNodes.size());
        for (RelationNode node : orderedNodes) {
            int slot = bandCounts.merge(node.band(), 1, Integer::sum) - 1;
            payloadNodes.add(new DnaAnalyzerPayload.FamilyTreeNode(
                node.genealogyId(),
                node.name(),
                simplifyType(node.villagerType()),
                resolveStatus(server, node.record()),
                node.relation(),
                node.path(),
                node.band(),
                slot,
                node.genealogyId().equals(rootId)
            ));
        }

        Set<UUID> included = relationMap.keySet();
        List<DnaAnalyzerPayload.FamilyTreeEdge> edges = new ArrayList<>();
        for (RelationNode node : orderedNodes) {
            for (UUID childId : node.record().childIds()) {
                if (included.contains(childId)) {
                    edges.add(new DnaAnalyzerPayload.FamilyTreeEdge(node.genealogyId(), childId));
                }
            }
        }

        return new DnaAnalyzerPayload(rootId, payloadNodes, edges);
    }

    public UUID ensureVillagerRecord(Villager villager, String baseName) {
        UUID genealogyId = getOrCreateGenealogyId(villager);
        VillagerGenealogyRecord record = records.computeIfAbsent(genealogyId, VillagerGenealogyRecord::new);
        updateRecordSnapshot(record, villager, baseName, true);
        return genealogyId;
    }

    public UUID ensureVillagerRecord(Villager villager, String baseName, Collection<Villager> parents) {
        UUID childId = ensureVillagerRecord(villager, baseName);
        for (Villager parent : parents) {
            UUID parentId = ensureVillagerRecord(parent, getEffectiveBaseName(parent));
            linkParentChild(parentId, childId);
        }
        return childId;
    }

    public void markHistorical(Villager villager) {
        UUID genealogyId = readGenealogyId(villager);
        if (genealogyId == null) {
            return;
        }

        VillagerGenealogyRecord record = records.get(genealogyId);
        if (record != null && record.aliveKnown()) {
            record.setAliveKnown(false);
            setDirty();
        }
    }

    private Map<UUID, RelationNode> buildRelationMap(UUID rootId) {
        Map<UUID, RelationNode> relationMap = new LinkedHashMap<>();
        VillagerGenealogyRecord root = records.get(rootId);
        if (root == null) {
            return relationMap;
        }

        Deque<PathStep> queue = new ArrayDeque<>();
        queue.add(new PathStep(rootId, ""));
        relationMap.put(rootId, createRelationNode(root, "", "Self", 0));

        while (!queue.isEmpty() && relationMap.size() < MAX_TREE_NODES) {
            PathStep step = queue.removeFirst();
            VillagerGenealogyRecord current = records.get(step.id());
            if (current == null) {
                continue;
            }

            for (UUID parentId : current.parentIds()) {
                considerPath(parentId, step.path() + "P", relationMap, queue);
            }
            for (UUID childId : current.childIds()) {
                considerPath(childId, step.path() + "C", relationMap, queue);
            }
        }

        return relationMap;
    }

    private void considerPath(UUID targetId, String path, Map<UUID, RelationNode> relationMap, Deque<PathStep> queue) {
        VillagerGenealogyRecord record = records.get(targetId);
        if (record == null || path.isBlank()) {
            return;
        }

        String relation = classifyRelation(path);
        int band = computeBand(path, relation);
        RelationNode candidate = createRelationNode(record, path, relation, band);
        RelationNode existing = relationMap.get(targetId);

        if (existing == null || isBetterPath(candidate, existing)) {
            relationMap.put(targetId, candidate);
            if (path.length() < 6 && relationMap.size() < MAX_TREE_NODES) {
                queue.addLast(new PathStep(targetId, path));
            }
        }
    }

    private RelationNode createRelationNode(VillagerGenealogyRecord record, String path, String relation, int band) {
        return new RelationNode(record.genealogyId(), record.name(), record.villagerType(), record, path, relation, band);
    }

    private boolean isBetterPath(RelationNode candidate, RelationNode existing) {
        if (candidate.path().length() != existing.path().length()) {
            return candidate.path().length() < existing.path().length();
        }

        int candidatePriority = relationPriority(candidate.relation());
        int existingPriority = relationPriority(existing.relation());
        if (candidatePriority != existingPriority) {
            return candidatePriority < existingPriority;
        }

        return candidate.path().compareTo(existing.path()) < 0;
    }

    private String classifyRelation(String path) {
        if (path.chars().allMatch(character -> character == 'P')) {
            return ancestorRelation(path.length());
        }
        if (path.chars().allMatch(character -> character == 'C')) {
            return descendantRelation(path.length());
        }
        return switch (path) {
            case "CP" -> "Mate";
            case "PC" -> "Sibling";
            case "PPC" -> "Aunt/Uncle";
            case "PPPC" -> "Great-Aunt/Uncle";
            case "PCC" -> "Niece/Nephew";
            case "PCCC" -> "Great-Niece/Nephew";
            case "PPCC" -> "First Cousin";
            case "PPPCCC" -> "Second Cousin";
            case "PPCCC", "PPPCC" -> "First Cousin 1x Removed";
            default -> "Distant Relative";
        };
    }

    private int computeBand(String path, String relation) {
        if ("Mate".equals(relation)) return 0;
        return path.chars().map(character -> character == 'C' ? 1 : -1).sum();
    }

    private String ancestorRelation(int depth) {
        if (depth == 1) {
            return "Parent";
        }
        if (depth == 2) {
            return "Grandparent";
        }
        return "Great-".repeat(depth - 2) + "Grandparent";
    }

    private String descendantRelation(int depth) {
        if (depth == 1) {
            return "Child";
        }
        if (depth == 2) {
            return "Grandchild";
        }
        return "Great-".repeat(depth - 2) + "Grandchild";
    }

    private int relationPriority(String relation) {
        return switch (relation) {
            case "Self" -> 0;
            case "Mate" -> 1;
            case "Parent", "Child" -> 2;
            case "Sibling" -> 3;
            case "Grandparent", "Grandchild" -> 4;
            case "Aunt/Uncle", "Niece/Nephew", "First Cousin" -> 5;
            default -> 6;
        };
    }

    private void updateRecordSnapshot(VillagerGenealogyRecord record, Villager villager, String baseName, boolean alive) {
        String villagerTypeName = villager.getVillagerData().getType().toString();
        String oldName = record.name();
        String oldType = record.villagerType();
        boolean oldAlive = record.aliveKnown();
        UUID oldUuid = record.villagerUuid();

        record.updateSnapshot(villager.getUUID(), baseName, villagerTypeName, alive);
        if (!oldName.equals(baseName) || !oldType.equals(villagerTypeName) || oldAlive != alive || !oldUuid.equals(villager.getUUID())) {
            setDirty();
        }
    }

    private UUID getOrCreateGenealogyId(Villager villager) {
        UUID genealogyId = readGenealogyId(villager);
        if (genealogyId != null) {
            return genealogyId;
        }

        UUID created = UUID.randomUUID();
        villager.getPersistentData().putUUID(GENEALOGY_ID_KEY, created);
        return created;
    }

    private UUID readGenealogyId(Villager villager) {
        return villager.getPersistentData().hasUUID(GENEALOGY_ID_KEY) ? villager.getPersistentData().getUUID(GENEALOGY_ID_KEY) : null;
    }

    private void linkParentChild(UUID parentId, UUID childId) {
        VillagerGenealogyRecord parent = records.computeIfAbsent(parentId, VillagerGenealogyRecord::new);
        VillagerGenealogyRecord child = records.computeIfAbsent(childId, VillagerGenealogyRecord::new);
        boolean changed = parent.childIds().add(childId) | child.parentIds().add(parentId);
        if (changed) {
            setDirty();
        }
    }

    private String simplifyType(String villagerType) {
        int separator = villagerType.lastIndexOf(':');
        return separator >= 0 ? villagerType.substring(separator + 1) : villagerType;
    }

    private String resolveStatus(MinecraftServer server, VillagerGenealogyRecord record) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(record.villagerUuid()) instanceof Villager) {
                return "Alive";
            }
        }

        return record.aliveKnown() ? "Unloaded" : "Historical";
    }

    private String getEffectiveBaseName(Villager villager) {
        String visibleName = villager.hasCustomName() ? villager.getCustomName().getString() : villager.getName().getString();
        return visibleName.startsWith("Hobo ") ? visibleName.substring("Hobo ".length()).trim() : visibleName;
    }

    private record PathStep(UUID id, String path) {
    }

    private record RelationNode(
        UUID genealogyId,
        String name,
        String villagerType,
        VillagerGenealogyRecord record,
        String path,
        String relation,
        int band
    ) {
    }
}
