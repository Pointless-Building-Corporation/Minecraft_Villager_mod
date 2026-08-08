package com.deepan.bettervillagers.quest.data;

import java.util.List;

public class BountyPoolData {
    public List<Objective> objectives;
    public List<Reward> rewards;

    public static class Objective {
        public String item; // e.g. "minecraft:apple"
        public int minCount;
        public int maxCount;
    }

    public static class Reward {
        public String item; // e.g. "minecraft:emerald"
        public int minCount;
        public int maxCount;
    }
}
