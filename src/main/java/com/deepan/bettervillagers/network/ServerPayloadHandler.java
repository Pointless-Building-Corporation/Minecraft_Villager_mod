package com.deepan.bettervillagers.network;

import com.deepan.bettervillagers.quest.network.DialogueActionPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {
    public static void handleDialogueActionOnServer(DialogueActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                Entity entity = player.serverLevel().getEntity(payload.entityId());
                if (entity instanceof net.minecraft.world.entity.npc.Villager villager) {
                    if ("BOUNTY_IN_PROGRESS".equals(payload.actionId())) {
                        com.deepan.bettervillagers.quest.data.PlayerBountyAttachment attachment = player.getData(com.deepan.bettervillagers.network.ModAttachments.PLAYER_BOUNTIES);
                        com.deepan.bettervillagers.quest.Bounty bounty = attachment.getBountyForVillager(villager.getId());
                        if (bounty != null && bounty.getStatus() == com.deepan.bettervillagers.quest.Bounty.BountyStatus.ACCEPTED) {
                            bounty.setStatus(com.deepan.bettervillagers.quest.Bounty.BountyStatus.IN_PROGRESS);
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Quest updated: Bring the items to the villager."));
                        }
                    } else if ("TURN_IN_BOUNTY".equals(payload.actionId())) {
                        com.deepan.bettervillagers.quest.data.PlayerBountyAttachment attachment = player.getData(com.deepan.bettervillagers.network.ModAttachments.PLAYER_BOUNTIES);
                        com.deepan.bettervillagers.quest.Bounty bounty = attachment.getBountyForVillager(villager.getId());
                        
                        if (bounty != null && bounty.getStatus() == com.deepan.bettervillagers.quest.Bounty.BountyStatus.IN_PROGRESS) {
                            net.minecraft.world.item.Item requiredItem = bounty.getObjectiveItem().getItem();
                            int requiredCount = bounty.getCountRequired();
                            
                            int hasCount = 0;
                            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
                                if (stack.getItem() == requiredItem) {
                                    hasCount += stack.getCount();
                                }
                            }
                            
                            if (hasCount >= requiredCount) {
                                // Consume items
                                int toRemove = requiredCount;
                                for (int i = 0; i < player.getInventory().getContainerSize() && toRemove > 0; i++) {
                                    net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
                                    if (stack.getItem() == requiredItem) {
                                        int removed = Math.min(toRemove, stack.getCount());
                                        stack.shrink(removed);
                                        toRemove -= removed;
                                    }
                                }
                                
                                // Give Reward
                                player.getInventory().add(bounty.getRewardItem().copy());
                                
                                // Reset Villager Memory
                                villager.getBrain().setMemory(com.deepan.bettervillagers.villager.ModVillagers.HAS_ACTIVE_BOUNTY.get(), false);
                                
                                // Record board completion
                                net.minecraft.core.GlobalPos boardPos = villager.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE).orElse(null);
                                if (boardPos != null) {
                                    attachment.incrementCompletionCount(boardPos.pos());
                                }
                                
                                // Remove bounty
                                attachment.removeBounty(bounty);
                                
                                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Bounty Complete! Here is your reward."));
                            } else {
                                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("You don't have enough items (Need " + requiredCount + ")."));
                            }
                        }
                    } else if ("GUILD_MASTER_REWARD".equals(payload.actionId())) {
                        net.minecraft.core.GlobalPos jobSite = villager.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE).orElse(null);
                        if (jobSite != null) {
                            com.deepan.bettervillagers.quest.data.PlayerBountyAttachment attachment = player.getData(com.deepan.bettervillagers.network.ModAttachments.PLAYER_BOUNTIES);
                            int completions = attachment.getCompletionCount(jobSite.pos());
                            
                            if (completions >= 10) {
                                attachment.resetCompletionCount(jobSite.pos(), 10);
                                
                                // Biome-specific rewards
                                String biome = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
                                String rewardName;
                                switch (biome) {
                                    case "plains" -> {
                                        player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE, 1));
                                        rewardName = "a Golden Apple";
                                    }
                                    case "desert" -> {
                                        player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_BLOCK, 2));
                                        rewardName = "2 Gold Blocks";
                                    }
                                    case "savanna" -> {
                                        player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SADDLE, 1));
                                        player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 1));
                                        rewardName = "a Saddle and a Diamond";
                                    }
                                    case "snow" -> {
                                        player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE, 1));
                                        rewardName = "a Diamond Pickaxe";
                                    }
                                    case "swamp" -> {
                                        player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.HEART_OF_THE_SEA, 1));
                                        rewardName = "a Heart of the Sea";
                                    }
                                    case "taiga" -> {
                                        player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_BLOCK, 1));
                                        rewardName = "a Diamond Block";
                                    }
                                    case "jungle" -> {
                                        player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD_BLOCK, 3));
                                        rewardName = "3 Emerald Blocks";
                                    }
                                    default -> {
                                        player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 2));
                                        rewardName = "2 Diamonds";
                                    }
                                }
                                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Guild Master rewards you with " + rewardName + "!"));
                            } else {
                                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("You have " + completions + "/10 quests completed for this board."));
                            }
                        }
                    } else if ("GIVE_CHILD_COOKIE".equals(payload.actionId())) {
                        boolean hasCookie = false;
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
                            if (stack.getItem() == net.minecraft.world.item.Items.COOKIE) {
                                stack.shrink(1);
                                hasCookie = true;
                                break;
                            }
                        }
                        
                        String themeId = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
                        java.util.List<com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption> options = new java.util.ArrayList<>();
                        String text;
                        if (hasCookie) {
                            text = getInteractionLine(themeId, "cookie_success", "A cookie! You're the best!");
                            player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIRT, 1));
                            options.add(new com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption("Thanks.", "NONE"));
                        } else {
                            text = getInteractionLine(themeId, "cookie_fail_1", "Liar! You don't have a cookie!");
                            options.add(new com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption("Wait, don't tell him!", "CHILD_NO_COOKIE_2"));
                        }
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.deepan.bettervillagers.quest.network.OpenDialoguePayload(villager.getId(), themeId, text, options));
                    } else if ("CHILD_NO_COOKIE_2".equals(payload.actionId())) {
                        String themeId = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
                        java.util.List<com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption> options = new java.util.ArrayList<>();
                        options.add(new com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption("Oh.", "NONE"));
                        String text = getInteractionLine(themeId, "cookie_fail_2", "Too late. You're dead.");
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.deepan.bettervillagers.quest.network.OpenDialoguePayload(villager.getId(), themeId, text, options));
                    } else if ("ASK_PARENTS".equals(payload.actionId())) {
                        String themeId = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
                        java.util.List<com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption> options = new java.util.ArrayList<>();
                        options.add(new com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption("Oh.", "NONE"));
                        String text = getInteractionLine(themeId, "parents", "They went to get milk 50 chunks ago.");
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.deepan.bettervillagers.quest.network.OpenDialoguePayload(villager.getId(), themeId, text, options));
                    } else if ("GIVE_CHILD_SUGAR".equals(payload.actionId())) {
                        boolean hasSugar = false;
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
                            if (stack.getItem() == net.minecraft.world.item.Items.SUGAR) {
                                stack.shrink(1);
                                hasSugar = true;
                                break;
                            }
                        }
                        
                        String themeId = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
                        java.util.List<com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption> options = new java.util.ArrayList<>();
                        if (hasSugar) {
                            villager.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 20 * 60 * 3, 1));
                            options.add(new com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption("Uh oh...", "NONE"));
                            String text = getInteractionLine(themeId, "sugar_rush", "SUGAAAAAR! I CAN SEE SOUNDS!");
                            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.deepan.bettervillagers.quest.network.OpenDialoguePayload(villager.getId(), themeId, text, options));
                        } else {
                            options.add(new com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption("Oops.", "NONE"));
                            String text = getInteractionLine(themeId, "sugar_fail", "You're empty-handed! Stop teasing me!");
                            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.deepan.bettervillagers.quest.network.OpenDialoguePayload(villager.getId(), themeId, text, options));
                        }
                    } else if ("TRADE_CHILD_TOYS".equals(payload.actionId())) {
                        boolean hasToy = false;
                        java.util.List<net.minecraft.world.item.Item> toys = java.util.List.of(
                            net.minecraft.world.item.Items.STICK, 
                            net.minecraft.world.item.Items.SLIME_BALL, 
                            net.minecraft.world.item.Items.CLAY_BALL, 
                            net.minecraft.world.item.Items.STRING
                        );
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
                            if (toys.contains(stack.getItem())) {
                                stack.shrink(1);
                                hasToy = true;
                                break;
                            }
                        }

                        String themeId = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
                        java.util.List<com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption> options = new java.util.ArrayList<>();
                        if (hasToy) {
                            java.util.Random rand = new java.util.Random();
                            net.minecraft.world.item.Item reward = (rand.nextFloat() < 0.2f) ? net.minecraft.world.item.Items.EMERALD : net.minecraft.world.item.Items.DANDELION;
                            player.getInventory().add(new net.minecraft.world.item.ItemStack(reward, 1));
                            options.add(new com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption("Thanks, kid.", "NONE"));
                            String text = getInteractionLine(themeId, "toy_success", "A toy! Yay! I found this shiny thing, you take it!");
                            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.deepan.bettervillagers.quest.network.OpenDialoguePayload(villager.getId(), themeId, text, options));
                        } else {
                            options.add(new com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption("My bad.", "NONE"));
                            String text = getInteractionLine(themeId, "toy_fail", "You don't have any toys!");
                            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.deepan.bettervillagers.quest.network.OpenDialoguePayload(villager.getId(), themeId, text, options));
                        }
                    } else if ("PLAY_RPS".equals(payload.actionId())) {
                        String themeId = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
                        java.util.List<com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption> options = new java.util.ArrayList<>();
                        options.add(new com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption("Rock", "RPS_ROCK"));
                        options.add(new com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption("Paper", "RPS_PAPER"));
                        options.add(new com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption("Scissors", "RPS_SCISSORS"));
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.deepan.bettervillagers.quest.network.OpenDialoguePayload(villager.getId(), themeId, "Rock, Paper, Scissors! Shoot!", options));
                    } else if (payload.actionId().startsWith("RPS_")) {
                        String playerChoice = payload.actionId().substring(4);
                        java.util.List<String> choices = java.util.List.of("ROCK", "PAPER", "SCISSORS");
                        String childChoice = choices.get(new java.util.Random().nextInt(3));
                        
                        String themeId = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()).getPath();
                        String result;
                        if (playerChoice.equals(childChoice)) {
                            result = getInteractionLine(themeId, "rps_tie", "We tied! I chose " + childChoice + " too!");
                        } else if ((playerChoice.equals("ROCK") && childChoice.equals("SCISSORS")) ||
                                   (playerChoice.equals("PAPER") && childChoice.equals("ROCK")) ||
                                   (playerChoice.equals("SCISSORS") && childChoice.equals("PAPER"))) {
                            result = getInteractionLine(themeId, "rps_lose", "You won... I chose " + childChoice + ". No fair, you cheated!");
                        } else {
                            result = getInteractionLine(themeId, "rps_win", "I WON! I chose " + childChoice + "! You lose!");
                            player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK, 1));
                        }

                        java.util.List<com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption> options = new java.util.ArrayList<>();
                        options.add(new com.deepan.bettervillagers.quest.network.OpenDialoguePayload.DialogueOption("Good game.", "NONE"));
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.deepan.bettervillagers.quest.network.OpenDialoguePayload(villager.getId(), themeId, result, options));
                    }
                }
            }
        });
    }

    private static String getInteractionLine(String themeId, String interactionType, String fallback) {
        com.deepan.bettervillagers.quest.data.RegionDialogueData data = com.deepan.bettervillagers.quest.data.RegionDialogueManager.getInstance().getRegionDialogue(themeId);
        if (data == null || data.children == null || data.children.interactions == null) return fallback;
        
        java.util.List<String> pool = null;
        switch (interactionType) {
            case "cookie_success": pool = data.children.interactions.cookie_success; break;
            case "cookie_fail_1": pool = data.children.interactions.cookie_fail_1; break;
            case "cookie_fail_2": pool = data.children.interactions.cookie_fail_2; break;
            case "sugar_rush": pool = data.children.interactions.sugar_rush; break;
            case "sugar_fail": pool = data.children.interactions.sugar_fail; break;
            case "toy_success": pool = data.children.interactions.toy_success; break;
            case "toy_fail": pool = data.children.interactions.toy_fail; break;
            case "parents": pool = data.children.interactions.parents; break;
            case "rps_win": pool = data.children.interactions.rps_win; break;
            case "rps_lose": pool = data.children.interactions.rps_lose; break;
            case "rps_tie": pool = data.children.interactions.rps_tie; break;
        }
        
        if (pool == null || pool.isEmpty()) return fallback;
        return pool.get(new java.util.Random().nextInt(pool.size()));
    }

    public static void handleAcceptBountyOnServer(com.deepan.bettervillagers.quest.network.AcceptBountyPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                net.minecraft.world.level.block.entity.BlockEntity be = player.serverLevel().getBlockEntity(payload.boardPos());
                if (be instanceof com.deepan.bettervillagers.quest.QuestBoardBlockEntity board) {
                    java.util.List<com.deepan.bettervillagers.quest.Bounty> bounties = board.getBounties();
                    com.deepan.bettervillagers.quest.Bounty toAccept = null;
                    for (com.deepan.bettervillagers.quest.Bounty b : bounties) {
                        if (b.getPosterVillagerId() == payload.posterVillagerId()) {
                            toAccept = b;
                            break;
                        }
                    }
                    if (toAccept != null) {
                        board.removeBounty(toAccept);
                        toAccept.setStatus(com.deepan.bettervillagers.quest.Bounty.BountyStatus.ACCEPTED);
                        
                        com.deepan.bettervillagers.quest.data.PlayerBountyAttachment attachment = player.getData(com.deepan.bettervillagers.network.ModAttachments.PLAYER_BOUNTIES);
                        attachment.addBounty(toAccept);
                        
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Bounty Accepted!"));
                    }
                }
            }
        });
    }
}
