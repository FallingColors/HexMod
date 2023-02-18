package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.player.AltioraAbility;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class CCAltiora implements Component {
    public static final String
        TAG_ALLOWED = "allowed",
        TAG_GRACE = "grace_period";

    private final ServerPlayer owner;
    @Nullable
    private AltioraAbility altiora = null;

    public CCAltiora(ServerPlayer owner) {
        this.owner = owner;
    }


    @Nullable
    public AltioraAbility getAltiora() {
        return this.altiora;
    }


    public void setAltiora(AltioraAbility altiora) {
        this.altiora = altiora;
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
