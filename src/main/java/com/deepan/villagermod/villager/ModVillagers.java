package com.deepan.villagermod.villager;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.ReloadableServerRegistries.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES = 
        DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, villagermod.MOD_ID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = 
        DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, villagermod.MOD_ID);

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        VILLAGER_PROFESSIONS.register(eventBus);
    }
}
