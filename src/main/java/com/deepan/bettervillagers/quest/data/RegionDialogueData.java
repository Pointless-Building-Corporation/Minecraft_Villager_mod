package com.deepan.bettervillagers.quest.data;

import java.util.List;
import java.util.Map;

public class RegionDialogueData {
    public GuildMasterDialogue guild_master;
    public Map<String, List<String>> professions;
    public List<String> generic;

    public static class GuildMasterDialogue {
        public List<String> greetings;
        public String reward;
    }
}
