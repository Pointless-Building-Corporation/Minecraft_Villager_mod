package com.deepan.bettervillagers.quest;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class Bounty {
    private ItemStack objectiveItem;
    private int countRequired;
    private ItemStack rewardItem;
    private int rewardCount;
    private int posterVillagerId;
    private String biome;
    private BountyStatus status;

    public enum BountyStatus {
        POSTED, ACCEPTED, IN_PROGRESS, READY_TO_TURN_IN
    }

    public Bounty(ItemStack objectiveItem, int countRequired, ItemStack rewardItem, int rewardCount, int posterVillagerId, String biome, BountyStatus status) {
        this.objectiveItem = objectiveItem;
        this.countRequired = countRequired;
        this.rewardItem = rewardItem;
        this.rewardCount = rewardCount;
        this.posterVillagerId = posterVillagerId;
        this.biome = biome;
        this.status = status;
    }

    // Legacy constructor for backwards compatibility or basic creation
    public Bounty(ItemStack objectiveItem, int countRequired, ItemStack rewardItem, int rewardCount) {
        this(objectiveItem, countRequired, rewardItem, rewardCount, -1, "plains", BountyStatus.POSTED);
    }

    public ItemStack getObjectiveItem() {
        return objectiveItem;
    }

    public int getCountRequired() {
        return countRequired;
    }

    public ItemStack getRewardItem() {
        return rewardItem;
    }

    public int getRewardCount() {
        return rewardCount;
    }

    public int getPosterVillagerId() {
        return posterVillagerId;
    }

    public String getBiome() {
        return biome;
    }

    public BountyStatus getStatus() {
        return status;
    }

    public void setStatus(BountyStatus status) {
        this.status = status;
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("ObjectiveItem", this.objectiveItem.save(registries));
        tag.putInt("CountRequired", this.countRequired);
        tag.put("RewardItem", this.rewardItem.save(registries));
        tag.putInt("RewardCount", this.rewardCount);
        tag.putInt("PosterVillagerId", this.posterVillagerId);
        tag.putString("Biome", this.biome);
        tag.putString("Status", this.status.name());
        return tag;
    }

    public static Bounty load(CompoundTag tag, HolderLookup.Provider registries) {
        ItemStack objectiveItem = ItemStack.parse(registries, tag.getCompound("ObjectiveItem")).orElse(ItemStack.EMPTY);
        int countRequired = tag.getInt("CountRequired");
        ItemStack rewardItem = ItemStack.parse(registries, tag.getCompound("RewardItem")).orElse(ItemStack.EMPTY);
        int rewardCount = tag.getInt("RewardCount");
        int posterVillagerId = tag.contains("PosterVillagerId") ? tag.getInt("PosterVillagerId") : -1;
        String biome = tag.contains("Biome") ? tag.getString("Biome") : "plains";
        BountyStatus status = tag.contains("Status") ? BountyStatus.valueOf(tag.getString("Status")) : BountyStatus.POSTED;
        return new Bounty(objectiveItem, countRequired, rewardItem, rewardCount, posterVillagerId, biome, status);
    }
}
