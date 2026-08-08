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
                                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("The Guild Master handed you " + rewardName + "!"));
                            }
                        }
                    }
                }
            }
        });
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
