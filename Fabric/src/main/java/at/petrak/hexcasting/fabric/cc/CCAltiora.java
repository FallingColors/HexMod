package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.player.AltioraAbility;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class CCAltiora implements Component, AutoSyncedComponent {
    public static final String
        TAG_ALLOWED = "allowed",
        TAG_GRACE = "grace_period";

    @Nullable
    private AltioraAbility altiora = null;

    private final Player owner;

    public CCAltiora(Player owner) {
        this.owner = owner;
    }


    @Nullable
    public AltioraAbility getAltiora() {
        return this.altiora;
    }


    public void setAltiora(AltioraAbility altiora) {
        this.altiora = altiora;
        HexCardinalComponents.ALTIORA.sync(this.owner);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        var allowed = tag.getBoolean(TAG_ALLOWED);
        if (!allowed) {
            this.altiora = null;
        } else {
            var grace = tag.getInt(TAG_GRACE);
            this.altiora = new AltioraAbility(grace);
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putBoolean(TAG_ALLOWED, this.altiora != null);
        if (this.altiora != null) {
            tag.putInt(TAG_GRACE, this.altiora.gracePeriod());
        }
    }
}
