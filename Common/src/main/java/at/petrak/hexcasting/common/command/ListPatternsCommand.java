package at.petrak.hexcasting.common.command;

import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

public class ListPatternsCommand {
    public static void add(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        cmd.then(Commands.literal("patterns")
            .requires(dp -> dp.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("list")
                .executes(ctx -> list(ctx.getSource())))
            .then(Commands.literal("give")
                .then(Commands.argument("patternName", PatternResLocArgument.id())
                    .executes(ctx ->
                        giveOne(ctx.getSource(),
                            getDefaultTarget(ctx.getSource()),
                            ResourceLocationArgument.getId(ctx, "patternName"),
                            PatternResLocArgument.getPattern(ctx, "patternName")))
                    .then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx ->
                            giveOne(ctx.getSource(),
                                EntityArgument.getPlayers(ctx, "targets"),
                                ResourceLocationArgument.getId(ctx, "patternName"),
                                PatternResLocArgument.getPattern(ctx, "patternName"))))))
            .then(Commands.literal("giveAll")
                .executes(ctx ->
                    giveAll(ctx.getSource(),
                        getDefaultTarget(ctx.getSource())))
                .then(Commands.argument("targets", EntityArgument.players())
                    .executes(ctx ->
                        giveAll(ctx.getSource(),
                            EntityArgument.getPlayers(ctx, "targets")))))
        );
    }

    private static Collection<ServerPlayer> getDefaultTarget(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return List.of(player);
        }
        return List.of();
    }

    private static int list(CommandSourceStack source) {
        var keys = PatternRegistryManifest.getAllPerWorldActions();
        var listing = keys
            .stream()
            .sorted((a, b) -> compareResLoc(a.location(), b.location()))
            .toList();

        var ow = source.getLevel().getServer().overworld();
        source.sendSuccess(() -> Component.translatable("command.hexcasting.pats.listing"), false);
        for (var key : listing) {
            var pat = PatternRegistryManifest.getCanonicalStrokesPerWorld(key, ow);

            source.sendSuccess(() -> Component.literal(key.location().toString())
                .append(": ")
                .append(new PatternIota(pat).display()), false);
        }

        return keys.size();
    }

    private static int giveAll(CommandSourceStack source, Collection<ServerPlayer> targets) {
        if (!targets.isEmpty()) {
            var lookup = PatternRegistryManifest.getAllPerWorldActions();
            var ow = source.getLevel().getServer().overworld();

            lookup.forEach(key -> {
                var pat = PatternRegistryManifest.getCanonicalStrokesPerWorld(key, ow);

                var tag = new CompoundTag();
                tag.putString(ItemScroll.TAG_OP_ID, key.location().toString());
                tag.put(ItemScroll.TAG_PATTERN, pat.serializeToNBT());

                var stack = new ItemStack(HexItems.SCROLL_LARGE);
                stack.setTag(tag);

                for (var player : targets) {
                    var stackEntity = player.drop(stack, false);
                    if (stackEntity != null) {
                        stackEntity.setNoPickUpDelay();
                        stackEntity.setThrower(player.getUUID());
                    }
                }
            });

            source.sendSuccess(() ->
                Component.translatable("command.hexcasting.pats.all",
                    lookup.size(),
                    targets.size() == 1 ? targets.iterator().next().getDisplayName() : targets.size()),
                true);
            return lookup.size();
        } else {
            return 0;
        }
    }

    private static int giveOne(CommandSourceStack source, Collection<ServerPlayer> targets,
        ResourceLocation patternName, HexPattern pat) {
        if (!targets.isEmpty()) {
            var tag = new CompoundTag();
            tag.putString(ItemScroll.TAG_OP_ID, patternName.toString());
            tag.put(ItemScroll.TAG_PATTERN, pat.serializeToNBT());

            var stack = new ItemStack(HexItems.SCROLL_LARGE);
            stack.setTag(tag);

            source.sendSuccess(() ->
                Component.translatable(
                    "command.hexcasting.pats.specific.success",
                    stack.getDisplayName(),
                    patternName,
                    targets.size() == 1 ? targets.iterator().next().getDisplayName() : targets.size()),
                true);

            for (var player : targets) {
                var stackEntity = player.drop(stack, false);
                if (stackEntity != null) {
                    stackEntity.setNoPickUpDelay();
                    stackEntity.setThrower(player.getUUID());
                }
            }

            return targets.size();
        } else {
            return 0;
        }
    }

    private static int compareResLoc(ResourceLocation a, ResourceLocation b) {
        var ns = a.getNamespace().compareTo(b.getNamespace());
        if (ns != 0) {
            return ns;
        }
        return a.getPath().compareTo(b.getPath());
    }
}
