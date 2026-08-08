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

public class DialogueManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DialogueManager.class);
    private static final Gson GSON = new GsonBuilder().create();
    
    private final Map<ResourceLocation, DialogueNodeData> nodes = new HashMap<>();

    public DialogueManager() {
        super(GSON, "dialogue");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        nodes.clear();
        objectIn.forEach((id, element) -> {
            try {
                DialogueNodeData node = GSON.fromJson(element, DialogueNodeData.class);
                node.id = id.toString();
                nodes.put(id, node);
            } catch (Exception e) {
                LOGGER.error("Failed to parse dialogue node {}", id, e);
            }
        });
        LOGGER.info("Loaded {} dialogue nodes", nodes.size());
    }

    public DialogueNodeData getNode(ResourceLocation id) {
        return nodes.get(id);
    }
}
