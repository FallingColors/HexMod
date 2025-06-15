package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.client.ClientCastingStack;
import at.petrak.hexcasting.forge.cap.HexCapabilities;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public record CapClientCastingStack(Player player, ClientCastingStack clientCastingStack) implements ClientCastingStack.Provider {

    @Override
    public @NotNull ClientCastingStack provide() {
        return clientCastingStack;
    }

    @SubscribeEvent
    public static void tickClientPlayer(PlayerTickEvent.Pre evt) {
        if (evt.getEntity().level().isClientSide() && !evt.getEntity().isDeadOrDying()) {
            var clientStack = evt.getEntity().getCapability(HexCapabilities.Entity.CLIENT_CASTING_STACK);
            if(clientStack != null)
                clientStack.provide().tick();
        }
    }
}
