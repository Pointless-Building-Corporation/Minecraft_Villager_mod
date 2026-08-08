package com.deepan.bettervillagers.quest.data;

import java.util.List;

public class DialogueNodeData {
    public String id;
    public String text;
    public List<Choice> choices;

    public static class Choice {
        public String text;
        public String nextNodeId;
        public String action; // e.g. "TURN_IN_BOUNTY", "CLOSE"
    }
}
