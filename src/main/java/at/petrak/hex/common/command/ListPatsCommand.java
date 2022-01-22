package at.petrak.hex.common.command;

import at.petrak.hex.api.PatternRegistry;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class ListPatsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hex:listPatterns")
                .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
                .executes(ctx -> {
                    var bob = new StringBuilder("Patterns in this world:");
                    var lookup = PatternRegistry.getPerWorldPatterns(ctx.getSource().getLevel());
                    lookup.forEach((sig, opId) -> {
                        bob.append('\n');
                        bob.append(sig);
                        bob.append(": ");
                        bob.append(opId.toString());
                    });
                    ctx.getSource().sendSuccess(new TextComponent(bob.toString()), true);

                    return 1;
                })
        );
    }
}
