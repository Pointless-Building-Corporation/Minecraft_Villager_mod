package com.deepan.bettervillagers.villager;

import com.deepan.bettervillagers.BetterVillagers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.ReloadableServerRegistries.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES = 
        DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, BetterVillagers.MODID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = 
        DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, BetterVillagers.MODID);
    public static final DeferredRegister<net.minecraft.world.entity.ai.memory.MemoryModuleType<?>> MEMORY_MODULE_TYPES = 
        DeferredRegister.create(BuiltInRegistries.MEMORY_MODULE_TYPE, BetterVillagers.MODID);

    public static final java.util.function.Supplier<net.minecraft.world.entity.ai.memory.MemoryModuleType<Long>> LAST_BOUNTY_POST_TIME = 
        MEMORY_MODULE_TYPES.register("last_bounty_post_time", () -> new net.minecraft.world.entity.ai.memory.MemoryModuleType<>(java.util.Optional.empty()));
        
    public static final java.util.function.Supplier<net.minecraft.world.entity.ai.memory.MemoryModuleType<Boolean>> HAS_ACTIVE_BOUNTY = 
        MEMORY_MODULE_TYPES.register("has_active_bounty", () -> new net.minecraft.world.entity.ai.memory.MemoryModuleType<>(java.util.Optional.empty()));

    public static final java.util.function.Supplier<PoiType> QUEST_BOARD_POI = 
        POI_TYPES.register("quest_board", () -> new PoiType(
            com.google.common.collect.ImmutableSet.copyOf(BetterVillagers.QUEST_BOARD.get().getStateDefinition().getPossibleStates()), 
            1, 1));
            
    public static final java.util.function.Supplier<VillagerProfession> GUILD_MASTER = 
        VILLAGER_PROFESSIONS.register("guild_master", () -> new VillagerProfession(
            "guild_master", 
            x -> x.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.POINT_OF_INTEREST_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(BetterVillagers.MODID, "quest_board"))), 
            x -> x.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.POINT_OF_INTEREST_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(BetterVillagers.MODID, "quest_board"))), 
            com.google.common.collect.ImmutableSet.of(), 
            com.google.common.collect.ImmutableSet.of(), 
            net.minecraft.sounds.SoundEvents.VILLAGER_WORK_LIBRARIAN));

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        VILLAGER_PROFESSIONS.register(eventBus);
        MEMORY_MODULE_TYPES.register(eventBus);
    }
}
