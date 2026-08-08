package com.deepan.bettervillagers.quest.client;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class BiomeThemeManager {
    private static final Map<String, ResourceLocation> DIALOGUE_TEXTURES = new HashMap<>();
    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.parse("bettervillagers:textures/gui/plains_dialogue.png");

    static {
        DIALOGUE_TEXTURES.put("plains", ResourceLocation.parse("bettervillagers:textures/gui/plains_dialogue.png"));
        DIALOGUE_TEXTURES.put("desert", ResourceLocation.parse("bettervillagers:textures/gui/desert_dialogue.png"));
        DIALOGUE_TEXTURES.put("savanna", ResourceLocation.parse("bettervillagers:textures/gui/savanna_dialogue.png"));
        DIALOGUE_TEXTURES.put("snow", ResourceLocation.parse("bettervillagers:textures/gui/snow_dialogue.png"));
        DIALOGUE_TEXTURES.put("swamp", ResourceLocation.parse("bettervillagers:textures/gui/swamp_dialogue.png"));
        DIALOGUE_TEXTURES.put("taiga", ResourceLocation.parse("bettervillagers:textures/gui/taiga_dialogue.png"));
        DIALOGUE_TEXTURES.put("jungle", ResourceLocation.parse("bettervillagers:textures/gui/jungle_dialogue.png"));
    }

    public static ResourceLocation getDialogueTexture(String themeId) {
        return DIALOGUE_TEXTURES.getOrDefault(themeId, DEFAULT_TEXTURE);
    }
}
