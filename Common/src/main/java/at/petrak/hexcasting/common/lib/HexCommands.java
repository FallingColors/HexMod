package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.common.command.BrainsweepCommand;
import at.petrak.hexcasting.common.command.ListPatternsCommand;
import at.petrak.hexcasting.common.command.RecalcPatternsCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class HexCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ListPatternsCommand.register(dispatcher);
        RecalcPatternsCommand.register(dispatcher);
        BrainsweepCommand.register(dispatcher);
    }
}
