package com.deepan.bettervillagers.entity;

import com.deepan.bettervillagers.villager.SeatBlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.SynchedEntityData;

public class SeatEntity extends Entity {
    private static final String MAX_SIT_TICKS_TAG = "MaxSitTicks";
    private static final String LIVED_TICKS_TAG = "LivedTicks";
    private int maxSitTicks = 400;
    private int livedTicks;

    public SeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setMaxSitTicks(int maxSitTicks) {
        this.maxSitTicks = Math.max(1, maxSitTicks);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.maxSitTicks = Math.max(1, compound.getInt(MAX_SIT_TICKS_TAG));
        this.livedTicks = Math.max(0, compound.getInt(LIVED_TICKS_TAG));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt(MAX_SIT_TICKS_TAG, this.maxSitTicks);
        compound.putInt(LIVED_TICKS_TAG, this.livedTicks);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.livedTicks++;

            if (!this.getPassengers().isEmpty() && (this.livedTicks >= this.maxSitTicks || !this.hasValidSeatBlock())) {
                this.removePassengersFromSeat();
                this.discard();
                return;
            }

            if (this.getPassengers().isEmpty()) {
                this.discard();
            }
        }
    }

    private boolean hasValidSeatBlock() {
        BlockPos seatPos = this.blockPosition();
        return SeatBlockHelper.isValidSeatBlock(this.level(), seatPos);
    }

    private void removePassengersFromSeat() {
        for (Entity passenger : this.getPassengers().stream().toList()) {
            passenger.stopRiding();
        }
    }
}
