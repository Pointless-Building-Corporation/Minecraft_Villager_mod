package com.deepan.bettervillagers.mixin;

import com.deepan.bettervillagers.villager.SitOnFurnitureBehavior;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(VillagerGoalPackages.class)
public class VillagerGoalPackagesMixin {
    @Inject(method = "getIdlePackage", at = @At("RETURN"), cancellable = true)
    private static void bettervillagers$addSittingToIdle(VillagerProfession profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>>> cir) {
        List<Pair<Integer, ? extends Behavior<? super Villager>>> list = new ArrayList<>(cir.getReturnValue());
        list.add(Pair.of(8, new SitOnFurnitureBehavior()));
        cir.setReturnValue(ImmutableList.copyOf(list));
    }

    @Inject(method = "getRestPackage", at = @At("RETURN"), cancellable = true)
    private static void bettervillagers$addSittingToRest(VillagerProfession profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>>> cir) {
        List<Pair<Integer, ? extends Behavior<? super Villager>>> list = new ArrayList<>(cir.getReturnValue());
        list.add(Pair.of(8, new SitOnFurnitureBehavior()));
        cir.setReturnValue(ImmutableList.copyOf(list));
    }
}
