package com.deepan.bettervillagers.villager;

import com.deepan.bettervillagers.BetterVillagers;
import com.deepan.bettervillagers.entity.ModEntities;
import com.deepan.bettervillagers.entity.SeatEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SitOnFurnitureBehavior extends Behavior<Villager> {
    private static final int COOLDOWN = 600; // 30 seconds
    private static final int SIT_DURATION = 400; // 20 seconds
    private static final int SIT_CHANCE = 120; // ~1 in 120 checks
    private final Map<UUID, Long> lastSitTimes = new HashMap<>();
    private final Map<UUID, BlockPos> targetSeats = new HashMap<>();

    public SitOnFurnitureBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Villager owner) {
        if (owner.isPassenger() || owner.isBaby() || owner.isSleeping()) return false;

        UUID villagerId = owner.getUUID();
        long gameTime = level.getGameTime();
        long lastSitTime = lastSitTimes.getOrDefault(villagerId, 0L);
        if (gameTime - lastSitTime < COOLDOWN) return false;
        if (owner.getRandom().nextInt(SIT_CHANCE) != 0) return false;

        Optional<BlockPos> seatPos = findNearbySeat(level, owner);
        if (seatPos.isPresent()) {
            BlockPos targetSeat = seatPos.get();
            targetSeats.put(villagerId, targetSeat);
            BetterVillagers.LOGGER.info("Villager {} FOUND SEAT AT {}", owner.getName().getString(), targetSeat);
            return true;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, Villager owner, long gameTime) {
        BlockPos targetSeat = targetSeats.get(owner.getUUID());
        if (targetSeat != null) {
            BetterVillagers.LOGGER.info("Villager {} starting walk to seat at {}", owner.getName().getString(), targetSeat);
            owner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetSeat, 0.4f, 1));
        }
    }

    @Override
    protected void tick(ServerLevel level, Villager owner, long gameTime) {
        UUID villagerId = owner.getUUID();
        BlockPos targetSeat = targetSeats.get(villagerId);
        if (targetSeat == null) {
            return;
        }

        if (owner.blockPosition().closerThan(targetSeat, 2.0)) {
            if (!owner.isPassenger()) {
                SeatEntity seat = ModEntities.SEAT.get().create(level);
                if (seat != null) {
                    seat.setMaxSitTicks(SIT_DURATION);
                    seat.moveTo(targetSeat.getX() + 0.5, targetSeat.getY() + 0.25, targetSeat.getZ() + 0.5);
                    level.addFreshEntity(seat);
                    if (owner.startRiding(seat)) {
                        lastSitTimes.put(villagerId, gameTime);
                        BetterVillagers.LOGGER.info("Villager {} successfully sat on seat at {}", owner.getName().getString(), targetSeat);
                    } else {
                        seat.discard();
                    }
                }
            }
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Villager entity, long gameTime) {
        BlockPos targetSeat = targetSeats.get(entity.getUUID());
        return targetSeat != null && (entity.isPassenger() || entity.blockPosition().closerThan(targetSeat, 12));
    }

    @Override
    protected void stop(ServerLevel level, Villager entity, long gameTime) {
        UUID villagerId = entity.getUUID();
        if (!entity.isPassenger()) {
            targetSeats.remove(villagerId);
        }
    }

    private Optional<BlockPos> findNearbySeat(ServerLevel level, Villager owner) {
        return BlockPos.findClosestMatch(owner.blockPosition(), 20, 5, pos -> isSeatable(level, pos));
    }

    private boolean isSeatable(ServerLevel level, BlockPos pos) {
        if (SeatBlockHelper.isValidSeatBlock(level, pos)) {
            AABB seatBox = new AABB(pos).inflate(0.2D);
            return level.getEntitiesOfClass(SeatEntity.class, seatBox).isEmpty();
        }
        return false;
    }
}
