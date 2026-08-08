package com.deepan.bettervillagers;

import com.deepan.bettervillagers.villager.ModVillagers;
import com.deepan.bettervillagers.item.DnaAnalyzerItem;
import com.deepan.bettervillagers.network.BetterVillagersPayloads;
import com.deepan.bettervillagers.villager.VillagerNameManager;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(BetterVillagers.MODID)
public class BetterVillagers {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "bettervillagers";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "bettervillagers" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "bettervillagers" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "bettervillagers" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<Item> DNA_ANALYZER = ITEMS.register("dna_analyzer", () -> new DnaAnalyzerItem(new Item.Properties().stacksTo(1)));

    // Quest System
    public static final DeferredBlock<Block> QUEST_BOARD = BLOCKS.register("quest_board", () -> new com.deepan.bettervillagers.quest.QuestBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f)));
    public static final DeferredItem<Item> QUEST_BOARD_ITEM = ITEMS.register("quest_board", () -> new BlockItem(QUEST_BOARD.get(), new Item.Properties()));


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public BetterVillagers(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(BetterVillagersPayloads::register);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        
        // Register Attachments
        com.deepan.bettervillagers.network.ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (BetterVillagers) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new VillagerNameManager());

        ModVillagers.register(modEventBus);
        com.deepan.bettervillagers.entity.ModEntities.register(modEventBus);
        com.deepan.bettervillagers.quest.ModQuests.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        
        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(DNA_ANALYZER);
            event.accept(QUEST_BOARD_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onVillagerAnalyzerUse(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof net.minecraft.world.entity.npc.Villager villager)) {
            return;
        }

        if (!(event.getEntity().getItemInHand(event.getHand()).getItem() instanceof DnaAnalyzerItem)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            DnaAnalyzerItem.analyzeVillager(serverPlayer, villager);
        }
    }

    @SubscribeEvent
    public void onQuestGiverInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof net.minecraft.world.entity.npc.Villager villager)) {
            return;
        }

        if (villager.getVillagerData().getProfession() == ModVillagers.GUILD_MASTER.get()) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        }

        if (!event.getLevel().isClientSide() && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            // Determine theme based on villager type
            String themeId = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
            
            // Generate and send the dynamic dialogue payload
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                    serverPlayer, 
                    com.deepan.bettervillagers.quest.server.DialogueManager.generatePayload(serverPlayer, villager, themeId)
            );
        }
    }

    @SubscribeEvent
    public void onVillagerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.npc.Villager villager && villager.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            com.deepan.bettervillagers.villager.VillagerGenealogySavedData.get(serverLevel).markHistorical(villager);
        }
    }
}
