package at.petrak.hexcasting.common.command;

import at.petrak.hexcasting.api.PatternRegistryBak;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class RecalcPatternsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hexcasting:recalcPatterns")
            .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
            .executes(ctx -> {
                var world = ctx.getSource().getServer().overworld();
                var ds = world.getDataStorage();
                ds.set(PatternRegistryBak.TAG_SAVED_DATA, PatternRegistryBak.Save.create(world.getSeed()));

                ctx.getSource().sendSuccess(
                    new TranslatableComponent("command.hexcasting.recalc"), true);
                return 1;
            }));
    }
}
