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

public class BiomeThemeManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(BiomeThemeManager.class);
    private static final Gson GSON = new GsonBuilder().create();
    
    private final List<BiomeThemeData> themes = new ArrayList<>();

    public BiomeThemeManager() {
        super(GSON, "biome_themes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        themes.clear();
        objectIn.forEach((id, element) -> {
            try {
                BiomeThemeData theme = GSON.fromJson(element, BiomeThemeData.class);
                themes.add(theme);
            } catch (Exception e) {
                LOGGER.error("Failed to parse biome theme {}", id, e);
            }
        });
        LOGGER.info("Loaded {} biome themes", themes.size());
    }

    public BiomeThemeData getThemeForBiome(String biomeId) {
        for (BiomeThemeData theme : themes) {
            if (theme.biomes != null && theme.biomes.contains(biomeId)) {
                return theme;
            }
        }
        return null;
    }
}
