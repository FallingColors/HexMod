package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.client.ClientCastingStack;
import at.petrak.hexcasting.forge.cap.HexCapabilities;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.function.Supplier;

public record CapClientCastingStack(Player player, ClientCastingStack clientCastingStack) implements Supplier<ClientCastingStack> {
    @Override
    public ClientCastingStack get() {
        return clientCastingStack;
    }

    @SubscribeEvent
    public static void tickClientPlayer(TickEvent.PlayerTickEvent evt) {
        if (evt.side == LogicalSide.CLIENT)
            evt.player.getCapability(HexCapabilities.CLIENT_CASTING_STACK).resolve()
                .get().get().tick();
    }
}
