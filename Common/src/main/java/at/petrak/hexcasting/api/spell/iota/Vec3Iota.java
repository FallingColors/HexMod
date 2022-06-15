package at.petrak.hexcasting.api.spell.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Vec3Iota extends Iota {
    public Vec3Iota(@NotNull Vec3 datum) {
        super(HexIotaTypes.VEC3, datum);
    }

    public Vec3 getVec3() {
        var v = (Vec3) this.payload;
        return new Vec3(
            HexUtils.fixNAN(v.x),
            HexUtils.fixNAN(v.y),
            HexUtils.fixNAN(v.z)
        );
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that)
            && that instanceof Vec3Iota viota
            && this.getVec3().distanceToSqr(viota.getVec3()) < DoubleIota.TOLERANCE * DoubleIota.TOLERANCE;
    }

    @Override
    public @NotNull Tag serialize() {
        return HexUtils.serializeToNBT(this.getVec3());
    }

    public static IotaType<Vec3Iota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public Vec3Iota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return Vec3Iota.deserialize(tag);
        }

        @Override
        public Component display(Tag tag) {
            return Vec3Iota.display(Vec3Iota.deserialize(tag).getVec3());
        }

        @Override
        public int color() {
            return 0xff_ff55ff;
        }
    };

    public static Vec3Iota deserialize(Tag tag) throws IllegalArgumentException {
        var lat = HexUtils.downcast(tag, LongArrayTag.TYPE);
        var vec = HexUtils.vecFromNBT(lat.getAsLongArray());
        return new Vec3Iota(vec);
    }

    public static Component display(double x, double y, double z) {
        return new TextComponent(String.format("(%.2f, %.2f, %.2f)", x, y, z))
            .withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    public static Component display(Vec3 v) {
        return display(v.x, v.y, v.z);
    }
}
