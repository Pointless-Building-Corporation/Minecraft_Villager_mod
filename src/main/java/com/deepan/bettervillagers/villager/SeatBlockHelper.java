package com.deepan.bettervillagers.villager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;
import java.util.Set;

public final class SeatBlockHelper {
    private static final TagKey<Block> SEATABLE_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("bettervillagers", "seatable"));
    private static final TagKey<Block> LEGACY_SEATABLE_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("villagermod", "seatable"));
    private static final Set<String> AUTO_SEAT_KEYWORDS = Set.of(
            "chair",
            "bench",
            "stool",
            "sofa",
            "couch",
            "ottoman"
    );

    private SeatBlockHelper() {
    }

    public static boolean isValidSeatBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!isSeatableBlock(state)) {
            return false;
        }
        return level.getBlockState(pos.above()).isAir();
    }

    public static boolean isSeatableBlock(BlockState state) {
        return state.is(SEATABLE_BLOCKS)
                || state.is(LEGACY_SEATABLE_BLOCKS)
                || state.is(BlockTags.STAIRS)
                || state.is(BlockTags.SLABS)
                || matchesAutoSeatHeuristics(state);
    }

    private static boolean matchesAutoSeatHeuristics(BlockState state) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String path = blockId.getPath().toLowerCase(Locale.ROOT);
        return AUTO_SEAT_KEYWORDS.stream().anyMatch(path::contains);
    }
}
