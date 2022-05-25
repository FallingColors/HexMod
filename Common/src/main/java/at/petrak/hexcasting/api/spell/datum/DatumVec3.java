package at.petrak.hexcasting.api.spell.datum;

import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DatumVec3 extends SpellDatum {
    public DatumVec3(@NotNull Vec3 datum) {
        super(datum);
    }

    public Vec3 getVec3() {
        return (Vec3) this.datum;
    }

    @Override
    public @NotNull Tag serialize() {
        return null;
    }
}
