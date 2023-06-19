package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Holds the pigment item favored by the player
 */
public class CCFavoredPigment implements Component, AutoSyncedComponent {
    public static final String TAG_PIGMENT = "pigment";

    private final Player owner;

    public CCFavoredPigment(Player owner) {
        this.owner = owner;
    }

    private FrozenPigment pigment = FrozenPigment.DEFAULT.get();

    public FrozenPigment getPigment() {
        return pigment;
    }

    public FrozenPigment setPigment(@Nullable FrozenPigment pigment) {
        var old = this.pigment;
        this.pigment = pigment != null ? pigment : FrozenPigment.DEFAULT.get();
        HexCardinalComponents.FAVORED_PIGMENT.sync(this.owner);
        return old;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.pigment = FrozenPigment.fromNBT(tag.getCompound(TAG_PIGMENT));
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.put(TAG_PIGMENT, this.pigment.serializeToNBT());
    }
}
