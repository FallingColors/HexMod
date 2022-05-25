package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.misc.FrozenColorizer;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Holds the colorizer item favored by the player
 */
public class CCFavoredColorizer implements Component, AutoSyncedComponent {
    public static final String TAG_COLORIZER = "colorizer";

    private final Player owner;

    public CCFavoredColorizer(Player owner) {
        this.owner = owner;
    }

    private FrozenColorizer colorizer = FrozenColorizer.DEFAULT.get();

    public FrozenColorizer getColorizer() {
        return colorizer;
    }

    public void setColorizer(FrozenColorizer colorizer) {
        this.colorizer = colorizer;
        HexCardinalComponents.FAVORED_COLORIZER.sync(this.owner);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.colorizer = FrozenColorizer.fromNBT(tag.getCompound(TAG_COLORIZER));
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.put(TAG_COLORIZER, this.colorizer.serializeToNBT());
    }
}
