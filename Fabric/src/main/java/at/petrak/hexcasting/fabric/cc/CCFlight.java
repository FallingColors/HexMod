package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.player.FlightAbility;
import at.petrak.hexcasting.api.utils.HexUtils;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class CCFlight implements Component {
    public static final String
        TAG_ALLOWED = "allowed", // Fake: use this as a null sentinel
        TAG_TIME_LEFT = "time_left",
        TAG_DIMENSION = "dimension",
        TAG_ORIGIN = "origin",
        TAG_RADIUS = "radius";

    private final ServerPlayer owner;
    @Nullable
    private FlightAbility flight = null;

    public CCFlight(ServerPlayer owner) {
        this.owner = owner;
    }


    @Nullable
    public FlightAbility getFlight() {
        return flight;
    }

    public void setFlight(FlightAbility flight) {
        this.flight = flight;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        var allowed = tag.getBoolean(TAG_ALLOWED);
        if (!allowed) {
            this.flight = null;
        } else {
            var timeLeft = tag.getInt(TAG_TIME_LEFT);
            var dim = ResourceKey.create(Registries.DIMENSION,
                new ResourceLocation(tag.getString(TAG_DIMENSION)));
            var origin = HexUtils.vecFromNBT(tag.getLongArray(TAG_ORIGIN));
            var radius = tag.getDouble(TAG_RADIUS);
            this.flight = new FlightAbility(timeLeft, dim, origin, radius);
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putBoolean(TAG_ALLOWED, this.flight != null);
        if (this.flight != null) {
            tag.putInt(TAG_TIME_LEFT, this.flight.timeLeft());
            tag.putString(TAG_DIMENSION, this.flight.dimension().location().toString());
            tag.put(TAG_ORIGIN, HexUtils.serializeToNBT(this.flight.origin()));
            tag.putDouble(TAG_RADIUS, this.flight.radius());
        }
    }
}
