package com.deepan.bettervillagers.villager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class VillagerGenealogyRecord {
    private static final String ID_KEY = "GenealogyId";
    private static final String VILLAGER_UUID_KEY = "VillagerUuid";
    private static final String NAME_KEY = "Name";
    private static final String TYPE_KEY = "VillagerType";
    private static final String ALIVE_KEY = "AliveKnown";
    private static final String PARENTS_KEY = "Parents";
    private static final String CHILDREN_KEY = "Children";

    private final UUID genealogyId;
    private UUID villagerUuid;
    private String name;
    private String villagerType;
    private boolean aliveKnown;
    private final Set<UUID> parentIds;
    private final Set<UUID> childIds;

    public VillagerGenealogyRecord(UUID genealogyId) {
        this(genealogyId, UUID.randomUUID(), "Villager", "plains", true, new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    public VillagerGenealogyRecord(
        UUID genealogyId,
        UUID villagerUuid,
        String name,
        String villagerType,
        boolean aliveKnown,
        Set<UUID> parentIds,
        Set<UUID> childIds
    ) {
        this.genealogyId = genealogyId;
        this.villagerUuid = villagerUuid;
        this.name = name;
        this.villagerType = villagerType;
        this.aliveKnown = aliveKnown;
        this.parentIds = parentIds;
        this.childIds = childIds;
    }

    public static VillagerGenealogyRecord fromTag(CompoundTag tag) {
        return new VillagerGenealogyRecord(
            tag.getUUID(ID_KEY),
            tag.getUUID(VILLAGER_UUID_KEY),
            tag.getString(NAME_KEY),
            tag.getString(TYPE_KEY),
            tag.getBoolean(ALIVE_KEY),
            readUuidSet(tag.getList(PARENTS_KEY, Tag.TAG_INT_ARRAY)),
            readUuidSet(tag.getList(CHILDREN_KEY, Tag.TAG_INT_ARRAY))
        );
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(ID_KEY, genealogyId);
        tag.putUUID(VILLAGER_UUID_KEY, villagerUuid);
        tag.putString(NAME_KEY, name);
        tag.putString(TYPE_KEY, villagerType);
        tag.putBoolean(ALIVE_KEY, aliveKnown);
        tag.put(PARENTS_KEY, writeUuidSet(parentIds));
        tag.put(CHILDREN_KEY, writeUuidSet(childIds));
        return tag;
    }

    public void updateSnapshot(UUID villagerUuid, String name, String villagerType, boolean aliveKnown) {
        this.villagerUuid = villagerUuid;
        this.name = name;
        this.villagerType = villagerType;
        this.aliveKnown = aliveKnown;
    }

    public UUID genealogyId() {
        return genealogyId;
    }

    public UUID villagerUuid() {
        return villagerUuid;
    }

    public String name() {
        return name;
    }

    public String villagerType() {
        return villagerType;
    }

    public boolean aliveKnown() {
        return aliveKnown;
    }

    public void setAliveKnown(boolean aliveKnown) {
        this.aliveKnown = aliveKnown;
    }

    public Set<UUID> parentIds() {
        return parentIds;
    }

    public Set<UUID> childIds() {
        return childIds;
    }

    private static ListTag writeUuidSet(Set<UUID> values) {
        ListTag listTag = new ListTag();
        for (UUID value : values) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Value", value);
            listTag.add(entry);
        }
        return listTag;
    }

    private static Set<UUID> readUuidSet(ListTag listTag) {
        Set<UUID> values = new LinkedHashSet<>();
        for (Tag tag : listTag) {
            if (tag instanceof CompoundTag compoundTag && compoundTag.hasUUID("Value")) {
                values.add(compoundTag.getUUID("Value"));
            }
        }
        return values;
    }
}
