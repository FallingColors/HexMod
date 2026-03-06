package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.client.ClientCastingStack;
import at.petrak.hexcasting.forge.cap.HexCapabilities;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.function.Supplier;

public record CapClientCastingStack(Player player, ClientCastingStack clientCastingStack) implements Supplier<ClientCastingStack> {
    @Override
    public ClientCastingStack get() {
        return clientCastingStack;
    }

    @SubscribeEvent
    public static void tickClientPlayer(PlayerTickEvent.Post evt) {
        var player = evt.getEntity();
        if (player.level().isClientSide() && !player.isDeadOrDying()) {
            var cap = player.getCapability(HexCapabilities.CLIENT_CASTING_STACK);
            if (cap != null) {
                cap.get().tick();
            }
        }
    }
}
