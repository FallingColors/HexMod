package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.client.ClientCastingStack;
import net.minecraft.core.HolderLookup;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class CCClientCastingStack implements Component, ClientTickingComponent {

    public CCClientCastingStack(Player owner) {
    }

    private final ClientCastingStack clientCastingStack = new ClientCastingStack();

    public ClientCastingStack getClientCastingStack() {
        return clientCastingStack;
    }

    @Override
    public void clientTick() {
        clientCastingStack.tick();
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) { }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) { }
}
