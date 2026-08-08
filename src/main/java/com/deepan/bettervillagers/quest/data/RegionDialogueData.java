package com.deepan.bettervillagers.quest.data;

import java.util.List;
import java.util.Map;

public class RegionDialogueData {
    public GuildMasterDialogue guild_master;
    public Map<String, List<String>> professions;
    public List<String> generic;
    public ChildrenDialogue children;

    public static class GuildMasterDialogue {
        public List<String> greetings;
        public String reward;
    }

    public static class ChildrenDialogue {
        public List<String> greetings;
        public ChildrenInteractions interactions;
    }

    public static class ChildrenInteractions {
        public List<String> cookie_success;
        public List<String> cookie_fail_1;
        public List<String> cookie_fail_2;
        public List<String> sugar_rush;
        public List<String> sugar_fail;
        public List<String> toy_success;
        public List<String> toy_fail;
        public List<String> parents;
        public List<String> rps_win;
        public List<String> rps_lose;
        public List<String> rps_tie;
    }
}
