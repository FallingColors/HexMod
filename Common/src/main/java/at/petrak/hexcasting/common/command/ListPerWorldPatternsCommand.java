package at.petrak.hexcasting.common.command;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.server.ScrungledPatternsSave;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

public class ListPerWorldPatternsCommand {
    public static void add(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        cmd.then(Commands.literal("perWorldPatterns")
            .requires(dp -> dp.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("list")
                .executes(ctx -> list(ctx.getSource())))
            .then(Commands.literal("give")
                .then(Commands.argument("patternName", PatternResKeyArgument.id())
                    .executes(ctx ->
                        giveOne(ctx.getSource(),
                            getDefaultTarget(ctx.getSource()),
                                PatternResKeyArgument.getPatternKey(ctx, "patternName"),
                                PatternResKeyArgument.getPattern(ctx, "patternName")))
                    .then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx ->
                            giveOne(ctx.getSource(),
                                EntityArgument.getPlayers(ctx, "targets"),
                                    PatternResKeyArgument.getPatternKey(ctx, "patternName"),
                                    PatternResKeyArgument.getPattern(ctx, "patternName"))))))
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

        var keys = IXplatAbstractions.INSTANCE.getActionRegistry().registryKeySet();
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
            var ow = source.getLevel().getServer().overworld();
            var save = ScrungledPatternsSave.open(ow);
            Registry<ActionRegistryEntry> regi = IXplatAbstractions.INSTANCE.getActionRegistry();

            int count = 0;
            for (var entry : regi.entrySet()) {
                var key = entry.getKey();
                if (HexUtils.isOfTag(regi, key, HexTags.Actions.PER_WORLD_PATTERN)) {
                    var found = save.lookupReverse(key);
                    var signature = found.getFirst();
                    var startDir = found.getSecond().canonicalStartDir();
                    var pat = HexPattern.fromAngles(signature, startDir);

                    var stack = new ItemStack(HexItems.SCROLL_LARGE);
                    stack.set(HexDataComponents.ACTION, key);
                    stack.set(HexDataComponents.PATTERN, pat);

                    for (var player : targets) {
                        var stackEntity = player.drop(stack, false);
                        if (stackEntity != null) {
                            stackEntity.setNoPickUpDelay();
                            stackEntity.setThrower(player);
                        }

                        count++;
                    }
                }
            }

            int finalCount = count;
            source.sendSuccess(() ->
                Component.translatable("command.hexcasting.pats.all",
                    finalCount,
                    targets.size() == 1 ? targets.iterator().next().getDisplayName() : targets.size()),
                true);
            return count;
        } else {
            return 0;
        }
    }

    private static int giveOne(CommandSourceStack source, Collection<ServerPlayer> targets,
                               ResourceKey<ActionRegistryEntry> actionKey, HexPattern pat) {
        if (!targets.isEmpty()) {
            var stack = new ItemStack(HexItems.SCROLL_LARGE);
            stack.set(HexDataComponents.ACTION, actionKey);
            stack.set(HexDataComponents.PATTERN, pat);

            source.sendSuccess(() ->
                Component.translatable(
                    "command.hexcasting.pats.specific.success",
                    stack.getDisplayName(),
                        actionKey.location(),
                    targets.size() == 1 ? targets.iterator().next().getDisplayName() : targets.size()),
                true);

            for (var player : targets) {
                var stackEntity = player.drop(stack, false);
                if (stackEntity != null) {
                    stackEntity.setNoPickUpDelay();
                    stackEntity.setThrower(player);
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
