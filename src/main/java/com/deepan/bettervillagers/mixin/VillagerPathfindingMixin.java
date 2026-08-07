package com.deepan.bettervillagers.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WalkNodeEvaluator.class)
public class VillagerPathfindingMixin {

    @Inject(method = "getPathTypeStatic(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/pathfinder/PathType;", at = @At("RETURN"), cancellable = true)
    private static void onGetPathTypeStatic(Mob mob, BlockPos pos, CallbackInfoReturnable<PathType> cir) {
        if (mob instanceof Villager) {
            if (mob.level().getBlockState(pos).is(Blocks.DIRT_PATH) || mob.level().getBlockState(pos.below()).is(Blocks.DIRT_PATH)) {
                cir.setReturnValue(PathType.WALKABLE);
            }
        }
    }
}
