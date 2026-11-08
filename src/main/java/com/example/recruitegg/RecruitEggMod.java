package com.example.recruitegg;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Optional;

@Mod(RecruitEggMod.MODID)
public class RecruitEggMod {
    public static final String MODID = "recruitegg";
    public static final long COOLDOWN_MS = 5L * 60 * 60 * 1000; // 5 hours
    public static final ResourceLocation EGG = ResourceLocation.parse("recruits:recruit_spawn_egg");

    public RecruitEggMod(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("recruiteggs").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(EGG);
            if (item.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("Recruits mod not found (recruits:recruit_spawn_egg)."));
                return 0;
            }
            CooldownData data = CooldownData.get(ctx.getSource().getServer());
            long now = System.currentTimeMillis();
            long remaining = data.remaining(player.getUUID(), now);
            if (remaining > 0) {
                long s = remaining / 1000;
                ctx.getSource().sendFailure(Component.literal(String.format(
                        "You must wait %dh %dm %ds before using this again.", s / 3600, (s % 3600) / 60, s % 60)));
                return 0;
            }
            ItemStack stack = new ItemStack(item.get(), 64);
            if (!player.getInventory().add(stack) && !stack.isEmpty()) {
                player.drop(stack, false);
            }
            data.set(player.getUUID(), now);
            ctx.getSource().sendSuccess(() -> Component.literal("You received 64 Recruit Spawn Eggs!"), false);
            return 1;
        }));
    }
}
