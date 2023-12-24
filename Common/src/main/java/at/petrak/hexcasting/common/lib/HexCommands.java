package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.common.command.BrainsweepCommand;
import at.petrak.hexcasting.common.command.ListPerWorldPatternsCommand;
import at.petrak.hexcasting.common.command.RecalcPatternsCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class HexCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var mainCmd = Commands.literal("hexcasting");

        BrainsweepCommand.add(mainCmd);
        ListPerWorldPatternsCommand.add(mainCmd);
        RecalcPatternsCommand.add(mainCmd);

        dispatcher.register(mainCmd);
    }
}
