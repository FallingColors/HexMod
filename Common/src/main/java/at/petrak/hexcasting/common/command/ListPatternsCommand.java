package at.petrak.hexcasting.common.command;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.iota.PatternIota;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ListPatternsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hexcasting:patterns")
            .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
            .then(Commands.literal("list").executes(ctx -> {

                var lookup = PatternRegistry.getPerWorldPatterns(ctx.getSource().getLevel());
                var listing = lookup.entrySet()
                    .stream()
                    .sorted((a, b) -> compareResLoc(a.getValue().getFirst(), b.getValue().getFirst()))
                    .toList();

                ctx.getSource().sendSuccess(new TranslatableComponent("command.hexcasting.pats.listing"), false);
                for (var pair : listing) {
                    HexPattern hexPattern = HexPattern.fromAngles(pair.getKey(), pair.getValue().getSecond());
                    ctx.getSource().sendSuccess(new TextComponent(pair.getValue().getFirst().toString())
                        .append(": ")
                        .append(PatternIota.display(hexPattern)), false);
                }


                return lookup.size();
            }))
            .then(Commands.literal("give")
                .then(Commands.argument("patternName", PatternResLocArgument.id()).executes(ctx -> {
                        var sender = ctx.getSource().getEntity();
                        if (sender instanceof ServerPlayer player) {
                            var targetId = ResourceLocationArgument.getId(ctx, "patternName");
                            var pat = PatternResLocArgument.getPattern(ctx, "patternName");


                            var tag = new CompoundTag();
                            tag.putString(ItemScroll.TAG_OP_ID, targetId.toString());
                            tag.put(ItemScroll.TAG_PATTERN,
                                pat.serializeToNBT());

                            var stack = new ItemStack(HexItems.SCROLL_LARGE);
                            stack.setTag(tag);

                            ctx.getSource().sendSuccess(
                                new TranslatableComponent(
                                    "command.hexcasting.pats.specific.success",
                                    stack.getDisplayName(),
                                    targetId),
                                true);

                            var stackEntity = player.drop(stack, false);
                            if (stackEntity != null) {
                                stackEntity.setNoPickUpDelay();
                                stackEntity.setOwner(player.getUUID());
                            }

                            return 1;
                        } else {
                            return 0;
                        }
                    }
                )))
            .then(Commands.literal("giveAll").executes(ctx -> {
                var sender = ctx.getSource().getEntity();
                if (sender instanceof ServerPlayer player) {
                    var lookup = PatternRegistry.getPerWorldPatterns(ctx.getSource().getLevel());

                    lookup.forEach((pattern, entry) -> {
                        var opId = entry.component1();
                        var startDir = entry.component2();

                        var tag = new CompoundTag();
                        tag.putString(ItemScroll.TAG_OP_ID, opId.toString());
                        tag.put(ItemScroll.TAG_PATTERN,
                            HexPattern.fromAngles(pattern, startDir).serializeToNBT());

                        var stack = new ItemStack(HexItems.SCROLL_LARGE);
                        stack.setTag(tag);

                        var stackEntity = player.drop(stack, false);
                        if (stackEntity != null) {
                            stackEntity.setNoPickUpDelay();
                            stackEntity.setOwner(player.getUUID());
                        }
                    });

                    ctx.getSource().sendSuccess(
                        new TranslatableComponent("command.hexcasting.pats.all", lookup.size()), true);
                    return lookup.size();
                } else {
                    return 0;
                }
            }))
        );
    }

    private static int compareResLoc(ResourceLocation a, ResourceLocation b) {
        var ns = a.getNamespace().compareTo(b.getNamespace());
        if (ns != 0) {
            return ns;
        }
        return a.getPath().compareTo(b.getPath());
    }
}
