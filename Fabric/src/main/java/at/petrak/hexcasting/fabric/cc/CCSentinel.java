package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.api.utils.HexUtils;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class CCSentinel implements Component, AutoSyncedComponent {
    public static final String
        TAG_HAS_SENTINEL = "has_sentinel",
        TAG_EXTENDS_RANGE = "extends_range",
        TAG_POSITION = "position",
        TAG_DIMENSION = "dimension";

    private final Player owner;
    private @Nullable Sentinel sentinel = null;

    public CCSentinel(Player owner) {
        this.owner = owner;
    }

    public @Nullable Sentinel getSentinel() {
        return sentinel;
    }

    public void setSentinel(Sentinel sentinel) {
        this.sentinel = sentinel;
        HexCardinalComponents.SENTINEL.sync(this.owner);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        var hasSentinel = tag.getBoolean(TAG_HAS_SENTINEL);
        if (hasSentinel) {
            var extendsRange = tag.getBoolean(TAG_EXTENDS_RANGE);
            var position = HexUtils.vecFromNBT(tag.getCompound(TAG_POSITION));
            var dim = ResourceKey.create(Registries.DIMENSION,
                new ResourceLocation(tag.getString(TAG_DIMENSION)));
            this.sentinel = new Sentinel(extendsRange, position, dim);
        } else {
            this.sentinel = null;
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putBoolean(TAG_HAS_SENTINEL, this.sentinel != null);
        if (this.sentinel != null) {
            tag.putBoolean(TAG_EXTENDS_RANGE, this.sentinel.extendsRange());
            tag.put(TAG_POSITION, HexUtils.serializeToNBT(this.sentinel.position()));
            tag.putString(TAG_DIMENSION, this.sentinel.dimension().location().toString());
        }
    }
}
