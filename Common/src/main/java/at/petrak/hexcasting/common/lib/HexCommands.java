package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.common.command.BrainsweepCommand;
import at.petrak.hexcasting.common.command.ListPatternsCommand;
import at.petrak.hexcasting.common.command.RecalcPatternsCommand;
import at.petrak.hexcasting.common.command.SleepTestCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class HexCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var mainCmd = Commands.literal("hexcasting");

        BrainsweepCommand.add(mainCmd);
        ListPatternsCommand.add(mainCmd);
        RecalcPatternsCommand.add(mainCmd);
        SleepTestCommand.add(mainCmd);

        dispatcher.register(mainCmd);
    }
}
