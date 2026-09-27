package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

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
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        var pigmentTag = tag.getCompound(TAG_PIGMENT);
        this.pigment = FrozenPigment.CODEC.parse(NbtOps.INSTANCE, pigmentTag).getOrThrow();
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        var pigmentTag = FrozenPigment.CODEC.encodeStart(NbtOps.INSTANCE, this.pigment).getOrThrow();
        tag.put(TAG_PIGMENT, pigmentTag);
    }
}
