package com.deepan.bettervillagers.quest.data;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(modid = com.deepan.bettervillagers.BetterVillagers.MODID)
public class QuestDataManager {
    
    public static final BountyPoolManager BOUNTY_POOLS = new BountyPoolManager();
    public static final DialogueManager DIALOGUES = new DialogueManager();
    public static final BiomeThemeManager BIOME_THEMES = new BiomeThemeManager();

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(BOUNTY_POOLS);
        event.addListener(DIALOGUES);
        event.addListener(BIOME_THEMES);
    }
}
