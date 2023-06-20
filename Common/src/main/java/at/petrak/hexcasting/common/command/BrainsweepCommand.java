package at.petrak.hexcasting.common.command;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;

public class BrainsweepCommand {
    public static void add(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        cmd.then(Commands.literal("brainsweep")
            .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
            .then(Commands.argument("target", EntityArgument.entity()).executes(ctx -> {
                var target = EntityArgument.getEntity(ctx, "target");
                if (target instanceof Mob mob) {
                    if (IXplatAbstractions.INSTANCE.isBrainswept(mob)) {
                        ctx.getSource().sendFailure(
                            Component.translatable("command.hexcasting.brainsweep.fail.already",
                                mob.getDisplayName()));
                        return 0;
                    }
                    HexAPI.instance().brainsweep(mob);
                    ctx.getSource().sendSuccess(
                        () -> Component.translatable("command.hexcasting.brainsweep", mob.getDisplayName()), true);
                    return 1;
                } else {
                    ctx.getSource().sendFailure(
                        Component.translatable("command.hexcasting.brainsweep.fail.badtype",
                            target.getDisplayName()));
                    return 0;
                }
            })));
    }
}
