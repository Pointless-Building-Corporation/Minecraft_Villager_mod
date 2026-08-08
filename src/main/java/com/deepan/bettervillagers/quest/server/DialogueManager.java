package com.deepan.bettervillagers.quest.server;

import com.deepan.bettervillagers.quest.network.OpenDialoguePayload;
import com.deepan.bettervillagers.villager.ModVillagers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DialogueManager {
    private static final Random RANDOM = new Random();

    // Lore for Quest Givers (Mix of serious/cultural and quest-related)
    private static final Map<String, String[]> QUEST_GIVER_LORE = new HashMap<>();
    static {
        QUEST_GIVER_LORE.put("plains", new String[]{
            "Namaste, traveler. The winds are gentle today. Have you come seeking a bounty?",
            "The balance of the plains is disrupted. We need your help to restore harmony.",
            "Greetings. I have urgent tasks that require a hero's touch. Are you interested?"
        });
        QUEST_GIVER_LORE.put("desert", new String[]{
            "Salaam! The heat of the sun reveals many secrets... and many bounties. Need work?",
            "Our oasis is under threat. The sands whisper of danger. Will you assist us?",
            "Water is scarce, but emeralds flow for those willing to brave the dunes."
        });
        QUEST_GIVER_LORE.put("savanna", new String[]{
            "The spirits of the ancestors watch over the savanna. They ask for your strength today.",
            "A warrior is always welcome here. We have bounties that need collecting.",
            "The acacia trees cast long shadows. Darkness brews, and we need your help."
        });
        QUEST_GIVER_LORE.put("snow", new String[]{
            "Brrr! Quickly, share the warmth of the hearth. We have cold, hard bounties for you.",
            "The frost takes many, but you look sturdy. Interested in some dangerous work?",
            "The old gods favor those who survive the blizzard. Prove your worth."
        });
        QUEST_GIVER_LORE.put("swamp", new String[]{
            "The waters are murky, and the spirits restless... I have tasks for the brave.",
            "Watch your step. The mud hides many dangers. Care to take on a bounty?",
            "We survive by the skin of our teeth out here. We could use your help."
        });
        QUEST_GIVER_LORE.put("taiga", new String[]{
            "The forests are deep and hold ancient grudges. We have work if you're looking.",
            "Foraging is dangerous lately. We've posted bounties. Interested?",
            "Welcome to the deep woods. We have tasks that only an outsider can handle."
        });
        QUEST_GIVER_LORE.put("jungle", new String[]{
            "The canopy hides beasts of old. We need someone to clear them out.",
            "Our temples are overrun. The bounties are high if you're brave enough.",
            "The vines strangle our crops. We need your help, traveler."
        });
    }

    // Meta-aware comedic lines for regular villagers based on profession
    private static final Map<VillagerProfession, String[]> FUNNY_LINES = new HashMap<>();
    static {
        FUNNY_LINES.put(VillagerProfession.FARMER, new String[]{
            "Why do you always sprint through my wheat? Walking is perfectly fine!",
            "I swear, if you jump on my farmland one more time...",
            "Do you really need 64 carrots in your pocket? Won't they go bad?",
            "I spend all day planting seeds just for you to trample them."
        });
        FUNNY_LINES.put(VillagerProfession.LIBRARIAN, new String[]{
            "I'll trade you emeralds for paper, but please stop pulling it from the swamp.",
            "Bookshelves are for reading, not for enchanting swords with unbreaking magic.",
            "Do you ever actually READ the books I sell you, or just rub them on your pickaxe?",
            "Please stop bringing me enchanted books that just say 'Curse of Vanishing'."
        });
        FUNNY_LINES.put(VillagerProfession.CLERIC, new String[]{
            "I can cure zombie villagers, but I can't cure your habit of jumping off cliffs.",
            "Rotten flesh? Again? What are you even doing with all this?",
            "The glowstone isn't meant to be eaten, please stop staring at it.",
            "I sell potions of healing. You look like you need about ten of them."
        });
        FUNNY_LINES.put(VillagerProfession.WEAPONSMITH, new String[]{
            "Stop asking if I can forge an 'Infinity Sword'. That's not how physics works.",
            "Yes, my iron swords are better than your wooden ones. Obviously.",
            "I don't know how you carry a cubic meter of iron, and frankly, I'm terrified.",
            "You punch trees with your bare hands? Have you considered buying an axe?"
        });
        FUNNY_LINES.put(VillagerProfession.ARMORER, new String[]{
            "Chainmail is a fashion statement, okay? It's not supposed to be practical.",
            "How do you fit an entire suit of iron armor in your backpack?",
            "Stop leaving your broken leather boots in my shop.",
            "I'd sell you Netherite, but let's be honest, you can't afford it."
        });
        FUNNY_LINES.put(VillagerProfession.BUTCHER, new String[]{
            "I don't ask where you get the raw porkchops, you don't ask what's in the stew.",
            "Please stop leading cows into my house.",
            "I'm out of coal. Could you maybe not hoard all of it?",
            "Yes, I sell cooked meat. No, I don't know why you eat golden carrots instead."
        });
        FUNNY_LINES.put(VillagerProfession.MASON, new String[]{
            "Can you stop building dirt houses? I literally sell bricks.",
            "You carry 36 stacks of cobblestone? Your spine must be made of bedrock.",
            "Quartz is beautiful, but please stop asking if I went to the Nether for it.",
            "I heard you placed a block of water that flows infinitely. Mind blown."
        });
        FUNNY_LINES.put(VillagerProfession.NITWIT, new String[]{
            "I was going to get a job, but standing on this bed is much more fun.",
            "Why work when I can just watch you run in circles all day?",
            "Everyone calls me a nitwit, but at least I'm not the one punching trees.",
            "Do you ever wonder if we're all just trapped in a blocky simulation?"
        });
        FUNNY_LINES.put(VillagerProfession.NONE, new String[]{
            "I'm waiting for a mysterious wooden block to fall from the sky so I can find my life's purpose.",
            "I applied for the Librarian job, but apparently I need a 'Lectern'. What even is that?",
            "Have you seen any unowned composters lying around?",
            "I'm currently unemployed, but I'm accepting donations of emeralds."
        });
    }

    private static final String[] GENERIC_FUNNY = new String[]{
        "Why can't I uncross my arms? It's a medical condition.",
        "Please close the door behind you. I don't want a zombie in my living room.",
        "Have you ever noticed the sun is perfectly square? Freaky.",
        "I heard a loud hiss yesterday... I haven't seen my neighbor since.",
        "Do you ever sleep, or do you just stare at a bed for 3 seconds and it's morning?",
        "I once saw a man build a portal out of crying obsidian. He didn't go far."
    };

    // Biome-specific reward descriptions for 10-bounty completion
    private static final Map<String, String> BIOME_REWARD_DESCRIPTIONS = new HashMap<>();
    static {
        BIOME_REWARD_DESCRIPTIONS.put("plains", "The harvest has been plentiful thanks to you. Accept this Golden Apple as a token of our gratitude!");
        BIOME_REWARD_DESCRIPTIONS.put("desert", "The sands remember your deeds, traveler. Take these Gold Blocks — forged from the heart of the dunes.");
        BIOME_REWARD_DESCRIPTIONS.put("savanna", "The ancestors smile upon your bravery. A Saddle and a Diamond — ride forth with honor!");
        BIOME_REWARD_DESCRIPTIONS.put("snow", "You've weathered the blizzards alongside us. This Diamond Pickaxe was forged in the coldest forge.");
        BIOME_REWARD_DESCRIPTIONS.put("swamp", "The murky waters part for you, hero. The Heart of the Sea chose you as its keeper.");
        BIOME_REWARD_DESCRIPTIONS.put("taiga", "The deep woods whisper your name. Take this Diamond Block — carved from the mountain's core.");
        BIOME_REWARD_DESCRIPTIONS.put("jungle", "The canopy bows to your courage! These Emerald Blocks were hidden in the oldest temple.");
    }

    public static OpenDialoguePayload generatePayload(net.minecraft.server.level.ServerPlayer player, Villager villager, String themeId) {
        VillagerProfession profession = villager.getVillagerData().getProfession();
        List<OpenDialoguePayload.DialogueOption> options = new ArrayList<>();
        String text;

        if (profession == ModVillagers.GUILD_MASTER.get()) {
            // Pick a biome-specific greeting
            String[] loreLines = QUEST_GIVER_LORE.getOrDefault(themeId, QUEST_GIVER_LORE.get("plains"));
            String greeting = (loreLines != null && loreLines.length > 0) 
                ? loreLines[RANDOM.nextInt(loreLines.length)] 
                : "Greetings, traveler.";

            net.minecraft.core.GlobalPos jobSite = villager.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE).orElse(null);
            if (jobSite != null) {
                com.deepan.bettervillagers.quest.data.PlayerBountyAttachment attachment = player.getData(com.deepan.bettervillagers.network.ModAttachments.PLAYER_BOUNTIES);
                int completions = attachment.getCompletionCount(jobSite.pos());
                
                if (completions >= 10) {
                    String rewardDesc = BIOME_REWARD_DESCRIPTIONS.getOrDefault(themeId, "You've proven yourself ten times over. Here is a rare reward!");
                    text = greeting + " " + rewardDesc;
                    options.add(new OpenDialoguePayload.DialogueOption("Claim Reward", "GUILD_MASTER_REWARD"));
                } else {
                    text = greeting + " You've completed " + completions + "/10 bounties for your next reward.";
                    options.add(new OpenDialoguePayload.DialogueOption("I'll get to work.", "NONE"));
                }
            } else {
                text = greeting + " ...But I seem to have lost my board. How tragic.";
                options.add(new OpenDialoguePayload.DialogueOption("Uh, bye.", "NONE"));
            }

        } else {
            // Check if this villager has an active bounty for this player
            com.deepan.bettervillagers.quest.data.PlayerBountyAttachment attachment = player.getData(com.deepan.bettervillagers.network.ModAttachments.PLAYER_BOUNTIES);
            com.deepan.bettervillagers.quest.Bounty bounty = attachment.getBountyForVillager(villager.getId());
            
            if (bounty != null) {
                if (bounty.getStatus() == com.deepan.bettervillagers.quest.Bounty.BountyStatus.ACCEPTED) {
                    text = "Ah, you took my request from the board! I need " + bounty.getCountRequired() + " of those items. Can you do it?";
                    options.add(new OpenDialoguePayload.DialogueOption("I'll get right on it!", "BOUNTY_IN_PROGRESS"));
                } else if (bounty.getStatus() == com.deepan.bettervillagers.quest.Bounty.BountyStatus.IN_PROGRESS) {
                    text = "Do you have my items yet? Please hurry!";
                    options.add(new OpenDialoguePayload.DialogueOption("Turn In Bounty", "TURN_IN_BOUNTY"));
                    options.add(new OpenDialoguePayload.DialogueOption("Still looking.", "NONE"));
                } else {
                    text = "Thank you again for your help!";
                    options.add(new OpenDialoguePayload.DialogueOption(getFunnyFarewell(), "NONE"));
                }
            } else {
                // Normal Villager Meta-Comedy
                String[] profLines = FUNNY_LINES.get(profession);
                if (profLines != null && profLines.length > 0 && RANDOM.nextBoolean()) {
                    text = profLines[RANDOM.nextInt(profLines.length)];
                } else {
                    text = GENERIC_FUNNY[RANDOM.nextInt(GENERIC_FUNNY.length)];
                }
                options.add(new OpenDialoguePayload.DialogueOption(getFunnyFarewell(), "NONE"));
            }
        }

        return new OpenDialoguePayload(villager.getId(), themeId, text, options);
    }

    private static String getFunnyFarewell() {
        String[] farewells = new String[]{
            "Leave",
            "Goodbye",
            "I must go punch trees",
            "See ya",
            "Farewell",
            "I need to go hoard dirt"
        };
        return farewells[RANDOM.nextInt(farewells.length)];
    }
}
