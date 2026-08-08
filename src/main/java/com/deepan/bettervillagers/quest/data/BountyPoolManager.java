package com.deepan.bettervillagers.quest.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BountyPoolManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(BountyPoolManager.class);
    private static final Gson GSON = new GsonBuilder().create();
    
    private final Map<String, BountyPoolData> poolsByBiome = new java.util.HashMap<>();
    private final Random random = new Random();

    public BountyPoolManager() {
        super(GSON, "bettervillagers/bounty_pools");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        poolsByBiome.clear();
        objectIn.forEach((id, element) -> {
            try {
                BountyPoolData pool = GSON.fromJson(element, BountyPoolData.class);
                poolsByBiome.put(id.getPath(), pool);
            } catch (Exception e) {
                LOGGER.error("Failed to parse bounty pool {}", id, e);
            }
        });
        LOGGER.info("Loaded {} bounty pools across biomes", poolsByBiome.size());
    }

    public BountyPoolData getPoolForBiome(String biome) {
        return poolsByBiome.getOrDefault(biome, poolsByBiome.get("plains")); // Fallback to plains
    }
}
