package com.deepan.bettervillagers.quest.data;

import com.deepan.bettervillagers.quest.Bounty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerBountyAttachment implements INBTSerializable<CompoundTag> {
    private final List<Bounty> activeBounties = new ArrayList<>();
    private final Map<BlockPos, Integer> boardCompletionCounts = new HashMap<>();

    public List<Bounty> getActiveBounties() {
        return activeBounties;
    }

    public void addBounty(Bounty bounty) {
        this.activeBounties.add(bounty);
    }

    public void removeBounty(Bounty bounty) {
        this.activeBounties.remove(bounty);
    }
    
    public Bounty getBountyForVillager(int villagerId) {
        for (Bounty b : activeBounties) {
            if (b.getPosterVillagerId() == villagerId) {
                return b;
            }
        }
        return null;
    }

    public int getCompletionCount(BlockPos boardPos) {
        return boardCompletionCounts.getOrDefault(boardPos, 0);
    }

    public void incrementCompletionCount(BlockPos boardPos) {
        boardCompletionCounts.put(boardPos, getCompletionCount(boardPos) + 1);
    }

    public void resetCompletionCount(BlockPos boardPos, int deduct) {
        int current = getCompletionCount(boardPos);
        boardCompletionCounts.put(boardPos, Math.max(0, current - deduct));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        
        ListTag bountiesTag = new ListTag();
        for (Bounty bounty : activeBounties) {
            bountiesTag.add(bounty.save(provider));
        }
        tag.put("ActiveBounties", bountiesTag);

        CompoundTag completionsTag = new CompoundTag();
        for (Map.Entry<BlockPos, Integer> entry : boardCompletionCounts.entrySet()) {
            completionsTag.putInt(entry.getKey().asLong() + "", entry.getValue());
        }
        tag.put("Completions", completionsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        activeBounties.clear();
        boardCompletionCounts.clear();

        if (tag.contains("ActiveBounties")) {
            ListTag bountiesTag = tag.getList("ActiveBounties", Tag.TAG_COMPOUND);
            for (int i = 0; i < bountiesTag.size(); i++) {
                activeBounties.add(Bounty.load(bountiesTag.getCompound(i), provider));
            }
        }

        if (tag.contains("Completions")) {
            CompoundTag completionsTag = tag.getCompound("Completions");
            for (String key : completionsTag.getAllKeys()) {
                long posLong = Long.parseLong(key);
                boardCompletionCounts.put(BlockPos.of(posLong), completionsTag.getInt(key));
            }
        }
    }
}
