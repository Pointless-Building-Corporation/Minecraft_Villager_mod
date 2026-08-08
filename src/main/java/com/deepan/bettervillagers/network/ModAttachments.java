package com.deepan.bettervillagers.network;

import com.deepan.bettervillagers.BetterVillagers;
import com.deepan.bettervillagers.quest.data.PlayerBountyAttachment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, BetterVillagers.MODID);

    public static final Supplier<AttachmentType<PlayerBountyAttachment>> PLAYER_BOUNTIES = ATTACHMENT_TYPES.register("player_bounties",
            () -> AttachmentType.serializable(PlayerBountyAttachment::new).build());
}
