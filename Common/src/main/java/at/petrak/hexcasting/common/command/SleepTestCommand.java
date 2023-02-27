package at.petrak.hexcasting.common.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

public class SleepTestCommand {
    public static void add(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        cmd.then(Commands.literal("sleep")
            .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
            .then(Commands.argument("start", BoolArgumentType.bool()).executes(ctx -> {
                var start = BoolArgumentType.getBool(ctx, "start");
                var user = ctx.getSource().getPlayerOrException();
                if (start) {
                    user.setPose(Pose.SLEEPING);
                    user.setSleepingPos(user.getOnPos());
                    user.setDeltaMovement(Vec3.ZERO);
                    user.hasImpulse = true;
                } else {
                    Vec3 vec3 = user.position();
                    user.setPose(Pose.STANDING);
                    user.setPos(vec3.x, vec3.y, vec3.z);
                    user.clearSleepingPos();
                }

                return 0;
            })));
    }
}
