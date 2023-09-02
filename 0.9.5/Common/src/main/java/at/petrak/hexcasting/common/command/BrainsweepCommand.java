package at.petrak.hexcasting.common.command;

import at.petrak.hexcasting.common.misc.Brainsweeping;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.npc.Villager;

public class BrainsweepCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hexcasting:brainsweep")
            .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
            .then(Commands.argument("villager", EntityArgument.entity()).executes(ctx -> {
                var target = EntityArgument.getEntity(ctx, "villager");
                if (target instanceof Villager v) {
                    if (Brainsweeping.isBrainswept(v)) {
                        ctx.getSource().sendFailure(
                            new TranslatableComponent("command.hexcasting.brainsweep.fail.already",
                                v.getDisplayName()));
                        return 0;
                    }
                    Brainsweeping.brainsweep(v);
                    ctx.getSource().sendSuccess(
                        new TranslatableComponent("command.hexcasting.brainsweep", v.getDisplayName()), true);
                    return 1;
                } else {
                    ctx.getSource().sendFailure(
                        new TranslatableComponent("command.hexcasting.brainsweep.fail.badtype",
                            target.getDisplayName()));
                    return 0;
                }
            })));
    }
}
