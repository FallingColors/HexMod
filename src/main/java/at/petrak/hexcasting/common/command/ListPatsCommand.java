package at.petrak.hexcasting.common.command;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.hexmath.HexPattern;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ListPatsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hexcasting:listPatterns")
            .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
            .then(Commands.literal("list").executes(ctx -> {
                var bob = new StringBuilder("Patterns in this world:");
                var lookup = PatternRegistry.getPerWorldPatterns(ctx.getSource().getLevel());
                lookup.forEach((sig, opId) -> {
                    bob.append('\n');
                    bob.append(sig);
                    bob.append(": ");
                    bob.append(opId.toString());
                });
                ctx.getSource().sendSuccess(new TextComponent(bob.toString()), true);

                return lookup.size();
            }))
            .then(Commands.literal("give").executes(ctx -> {
                var sender = ctx.getSource().getEntity();
                if (sender instanceof ServerPlayer player) {
                    var lookup = PatternRegistry.getPerWorldPatterns(ctx.getSource().getLevel());

                    lookup.forEach((pattern, opId) -> {
                        var tag = new CompoundTag();
                        tag.putString(ItemScroll.TAG_OP_ID, opId.toString());
                        var prototypePat = PatternRegistry.lookupPattern(opId).getPrototype();
                        tag.put(ItemScroll.TAG_PATTERN,
                            HexPattern.FromAnglesSig(pattern, prototypePat.startDir()).serializeToNBT());

                        var stack = new ItemStack(HexItems.SCROLL.get());
                        stack.setTag(tag);

                        var stackEntity = player.drop(stack, false);
                        if (stackEntity != null) {
                            stackEntity.setNoPickUpDelay();
                            stackEntity.setOwner(player.getUUID());
                        }
                    });

                    ctx.getSource().sendSuccess(
                        new TextComponent(String.format("Gave you all %d scrolls", lookup.size())), true);
                    return lookup.size();
                }

                return 0;
            }))
        );
    }
}
