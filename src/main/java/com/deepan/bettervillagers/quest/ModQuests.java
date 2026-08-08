package com.deepan.bettervillagers.quest;

import com.deepan.bettervillagers.BetterVillagers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModQuests {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BetterVillagers.MODID);

    public static final Supplier<BlockEntityType<QuestBoardBlockEntity>> QUEST_BOARD_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("quest_board", () ->
                    BlockEntityType.Builder.of(QuestBoardBlockEntity::new, BetterVillagers.QUEST_BOARD.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
