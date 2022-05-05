package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.common.command.BrainsweepCommand;
import at.petrak.hexcasting.common.command.ListPatternsCommand;
import at.petrak.hexcasting.common.command.RecalcPatternsCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HexCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent evt) {
        var dp = evt.getDispatcher();
        ListPatternsCommand.register(dp);
        RecalcPatternsCommand.register(dp);
        BrainsweepCommand.register(dp);
    }
}
