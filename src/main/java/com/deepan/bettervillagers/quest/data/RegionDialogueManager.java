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

import java.util.HashMap;
import java.util.Map;

public class RegionDialogueManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegionDialogueManager.class);
    private static final Gson GSON = new GsonBuilder().create();
    
    private final Map<String, RegionDialogueData> regions = new HashMap<>();
    private static RegionDialogueManager instance;

    public RegionDialogueManager() {
        super(GSON, "region_dialogues");
        instance = this;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        regions.clear();
        objectIn.forEach((id, element) -> {
            try {
                RegionDialogueData data = GSON.fromJson(element, RegionDialogueData.class);
                regions.put(id.getPath(), data);
            } catch (Exception e) {
                LOGGER.error("Failed to parse region dialogue {}", id, e);
            }
        });
        LOGGER.info("Loaded {} region dialogues", regions.size());
    }

    public static RegionDialogueManager getInstance() {
        return instance;
    }

    public RegionDialogueData getRegionDialogue(String themeId) {
        return regions.get(themeId);
    }
}
