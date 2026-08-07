package com.deepan.bettervillagers.villager;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

public class StrollOnPathBehavior extends Behavior<Villager> {
    private static final int SEARCH_RADIUS = 30;
    private static final int MAX_TRIES = 10;
    private static final int COOLDOWN = 100;
    private long nextOkStartTime;

    public StrollOnPathBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.HOME, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Villager owner) {
        if (level.getGameTime() < this.nextOkStartTime) {
            return false;
        }
        if (owner.isPassenger() || owner.isSleeping()) {
            return false;
        }
        return true;
    }

    @Override
    protected void start(ServerLevel level, Villager owner, long gameTime) {
        this.nextOkStartTime = gameTime + COOLDOWN + owner.getRandom().nextInt(50);
        
        Optional<GlobalPos> homeOpt = owner.getBrain().getMemory(MemoryModuleType.HOME);
        if (homeOpt.isEmpty()) {
            return;
        }
        
        BlockPos homePos = homeOpt.get().pos();
        if (!homePos.closerToCenterThan(owner.position(), SEARCH_RADIUS * 1.5)) {
            return; // Too far from home to care about this behavior
        }

        // Try to find a dirt path within the radius of home
        BlockPos targetPath = null;
        for (int i = 0; i < MAX_TRIES; i++) {
            // Random offset within the 30 block radius
            int dx = owner.getRandom().nextInt(SEARCH_RADIUS * 2) - SEARCH_RADIUS;
            int dz = owner.getRandom().nextInt(SEARCH_RADIUS * 2) - SEARCH_RADIUS;
            
            // Limit Y search to nearby elevations
            int dy = owner.getRandom().nextInt(10) - 5;
            
            BlockPos testPos = homePos.offset(dx, dy, dz);
            
            if (level.getBlockState(testPos).is(Blocks.DIRT_PATH) || level.getBlockState(testPos.below()).is(Blocks.DIRT_PATH)) {
                targetPath = testPos;
                break;
            }
        }

        if (targetPath != null) {
            owner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPath, 0.6f, 1));
        }
    }
}
