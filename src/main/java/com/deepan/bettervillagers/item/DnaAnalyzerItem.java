package com.deepan.bettervillagers.item;

import com.deepan.bettervillagers.network.DnaAnalyzerPayload;
import com.deepan.bettervillagers.villager.VillagerGenealogySavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class DnaAnalyzerItem extends Item {
    public DnaAnalyzerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, net.minecraft.world.InteractionHand usedHand) {
        if (interactionTarget instanceof Villager villager && player instanceof ServerPlayer serverPlayer) {
            analyzeVillager(serverPlayer, villager);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.bettervillagers.dna_analyzer.tooltip"));
    }

    public static void analyzeVillager(ServerPlayer player, Villager villager) {
        VillagerGenealogySavedData genealogy = VillagerGenealogySavedData.get((net.minecraft.server.level.ServerLevel) villager.level());
        DnaAnalyzerPayload payload = genealogy.buildPayloadFor(villager);
        PacketDistributor.sendToPlayer(player, payload);
    }
}
