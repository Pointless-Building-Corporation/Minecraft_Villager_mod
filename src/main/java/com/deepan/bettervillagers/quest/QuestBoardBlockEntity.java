package com.deepan.bettervillagers.quest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class QuestBoardBlockEntity extends BlockEntity {
    private final List<Bounty> bounties = new ArrayList<>();

    public QuestBoardBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModQuests.QUEST_BOARD_BLOCK_ENTITY.get(), pos, blockState);
    }

    public List<Bounty> getBounties() {
        return bounties;
    }

    public void addBounty(Bounty bounty) {
        if (this.bounties.size() < 10) { // Limit to 10 active bounties
            this.bounties.add(bounty);
            this.setChanged();
        }
    }

    public void removeBounty(Bounty bounty) {
        this.bounties.remove(bounty);
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag bountiesList = new ListTag();
        for (Bounty bounty : this.bounties) {
            bountiesList.add(bounty.save(registries));
        }
        tag.put("Bounties", bountiesList);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.bounties.clear();
        if (tag.contains("Bounties", Tag.TAG_LIST)) {
            ListTag bountiesList = tag.getList("Bounties", Tag.TAG_COMPOUND);
            for (int i = 0; i < bountiesList.size(); i++) {
                this.bounties.add(Bounty.load(bountiesList.getCompound(i), registries));
            }
        }
    }
}
