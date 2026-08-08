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

    public static OpenDialoguePayload generatePayload(net.minecraft.server.level.ServerPlayer player, Villager villager, String themeId) {
        VillagerProfession profession = villager.getVillagerData().getProfession();
        List<OpenDialoguePayload.DialogueOption> options = new ArrayList<>();
        String text;

        com.deepan.bettervillagers.quest.data.RegionDialogueData regionData = 
            com.deepan.bettervillagers.quest.data.RegionDialogueManager.getInstance().getRegionDialogue(themeId);
            
        // Fallback to plains if theme is missing or invalid
        if (regionData == null) {
            regionData = com.deepan.bettervillagers.quest.data.RegionDialogueManager.getInstance().getRegionDialogue("plains");
        }

        if (profession == ModVillagers.GUILD_MASTER.get()) {
            String greeting = "Greetings, traveler.";
            String rewardDesc = "You've proven yourself ten times over. Here is a rare reward!";
            
            if (regionData != null && regionData.guild_master != null) {
                if (regionData.guild_master.greetings != null && !regionData.guild_master.greetings.isEmpty()) {
                    greeting = regionData.guild_master.greetings.get(RANDOM.nextInt(regionData.guild_master.greetings.size()));
                }
                if (regionData.guild_master.reward != null) {
                    rewardDesc = regionData.guild_master.reward;
                }
            }

            net.minecraft.core.GlobalPos jobSite = villager.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE).orElse(null);
            if (jobSite != null) {
                com.deepan.bettervillagers.quest.data.PlayerBountyAttachment attachment = player.getData(com.deepan.bettervillagers.network.ModAttachments.PLAYER_BOUNTIES);
                int completions = attachment.getCompletionCount(jobSite.pos());
                
                if (completions >= 10) {
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

        } else if (villager.isBaby()) {
            if (regionData != null && regionData.children != null && regionData.children.greetings != null && !regionData.children.greetings.isEmpty()) {
                text = regionData.children.greetings.get(RANDOM.nextInt(regionData.children.greetings.size()));
            } else {
                text = "I'm just a kid!";
            }
            options.add(new OpenDialoguePayload.DialogueOption("Have a cookie.", "GIVE_CHILD_COOKIE"));
            options.add(new OpenDialoguePayload.DialogueOption("I brought you some sugar.", "GIVE_CHILD_SUGAR"));
            options.add(new OpenDialoguePayload.DialogueOption("Want to trade toys?", "TRADE_CHILD_TOYS"));
            options.add(new OpenDialoguePayload.DialogueOption("Let's play Rock, Paper, Scissors!", "PLAY_RPS"));
            options.add(new OpenDialoguePayload.DialogueOption("Where are your parents?", "ASK_PARENTS"));
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
                // Normal Villager Meta-Comedy / Cultural Dialogue
                String profName = BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession).getPath();
                
                boolean hasProfession = !profName.equals("none") && !profName.equals("nitwit");
                
                if (regionData != null && regionData.professions != null && regionData.professions.containsKey(profName)) {
                    List<String> profLines = regionData.professions.get(profName);
                    if (!profLines.isEmpty()) {
                        // If employed, strictly use profession lines (unless we force generic for some reason, but user wants strict)
                        // If unemployed (nitwit/none), maybe use generic lines sometimes? User didn't specify, but keeping them strictly to their pool is safest.
                        text = profLines.get(RANDOM.nextInt(profLines.size()));
                    } else {
                        text = getGenericLine(regionData);
                    }
                } else {
                    text = getGenericLine(regionData);
                }
                options.add(new OpenDialoguePayload.DialogueOption(getFunnyFarewell(), "NONE"));
            }
        }

        return new OpenDialoguePayload(villager.getId(), themeId, text, options);
    }

    private static String getGenericLine(com.deepan.bettervillagers.quest.data.RegionDialogueData regionData) {
        if (regionData != null && regionData.generic != null && !regionData.generic.isEmpty()) {
            return regionData.generic.get(RANDOM.nextInt(regionData.generic.size()));
        }
        return "The weather is strange today.";
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
