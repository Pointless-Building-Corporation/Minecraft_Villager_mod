package com.deepan.bettervillagers.quest;

import com.deepan.bettervillagers.quest.data.BountyPoolData;
import com.deepan.bettervillagers.quest.data.QuestDataManager;
import com.deepan.bettervillagers.villager.ModVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class PostBountyBehavior extends Behavior<Villager> {
    private static final long COOLDOWN_TICKS = 24000; // 1 Minecraft day
    private static final int MAX_DISTANCE_TO_BOARD = 4;

    public PostBountyBehavior() {
        super(Map.of(
            MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT,
            ModVillagers.LAST_BOUNTY_POST_TIME.get(), MemoryStatus.REGISTERED,
            ModVillagers.HAS_ACTIVE_BOUNTY.get(), MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Villager villager) {
        if (villager.getVillagerData().getProfession() != ModVillagers.GUILD_MASTER.get()) {
            return false;
        }

        boolean hasActive = villager.getBrain().getMemory(ModVillagers.HAS_ACTIVE_BOUNTY.get()).orElse(false);
        if (hasActive) {
            return false;
        }

        long time = level.getGameTime();
        long lastPostTime = villager.getBrain().getMemory(ModVillagers.LAST_BOUNTY_POST_TIME.get()).orElse(0L);
        if (time - lastPostTime < COOLDOWN_TICKS && lastPostTime != 0) {
            return false; // On cooldown
        }

        GlobalPos jobSite = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).orElse(null);
        if (jobSite == null || jobSite.dimension() != level.dimension()) {
            return false;
        }

        double distance = villager.distanceToSqr(jobSite.pos().getX(), jobSite.pos().getY(), jobSite.pos().getZ());
        return distance <= MAX_DISTANCE_TO_BOARD * MAX_DISTANCE_TO_BOARD;
    }

    @Override
    protected void start(ServerLevel level, Villager villager, long gameTime) {
        GlobalPos jobSite = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
        BlockPos pos = jobSite.pos();

        if (level.getBlockEntity(pos) instanceof QuestBoardBlockEntity questBoard) {
            String biome = BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
            BountyPoolData pool = QuestDataManager.BOUNTY_POOLS.getPoolForBiome(biome);
            if (pool != null && pool.objectives != null && !pool.objectives.isEmpty() && pool.rewards != null && !pool.rewards.isEmpty()) {
                BountyPoolData.Objective obj = pool.objectives.get(level.random.nextInt(pool.objectives.size()));
                BountyPoolData.Reward rew = pool.rewards.get(level.random.nextInt(pool.rewards.size()));

                net.minecraft.world.item.Item objectiveItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(obj.item));
                net.minecraft.world.item.Item rewardItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(rew.item));

                int objCount = obj.minCount + level.random.nextInt(Math.max(1, obj.maxCount - obj.minCount + 1));
                int rewCount = rew.minCount + level.random.nextInt(Math.max(1, rew.maxCount - rew.minCount + 1));

                Bounty bounty = new Bounty(new ItemStack(objectiveItem), objCount, new ItemStack(rewardItem), rewCount, villager.getId(), biome, Bounty.BountyStatus.POSTED);
                questBoard.addBounty(bounty);

                villager.getBrain().setMemory(ModVillagers.LAST_BOUNTY_POST_TIME.get(), gameTime);
                villager.getBrain().setMemory(ModVillagers.HAS_ACTIVE_BOUNTY.get(), true);
            }
        }
    }
}
