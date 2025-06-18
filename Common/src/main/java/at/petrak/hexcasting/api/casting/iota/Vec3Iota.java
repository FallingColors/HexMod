package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Vec3Iota extends Iota {
    private Vec3 value;
    public Vec3Iota(@NotNull Vec3 datum) {
        super(() -> HexIotaTypes.VEC3);
        this.value = datum;
    }

    public Vec3 getVec3() {
        return new Vec3(
            HexUtils.fixNAN(value.x),
            HexUtils.fixNAN(value.y),
            HexUtils.fixNAN(value.z)
        );
    }

    @Override
    public boolean isTruthy() {
        var v = this.getVec3();
        return !(v.x == 0.0 && v.y == 0.0 && v.z == 0.0);
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that)
            && that instanceof Vec3Iota viota
            && this.getVec3().distanceToSqr(viota.getVec3()) < DoubleIota.TOLERANCE * DoubleIota.TOLERANCE;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public Component display() {
        return Vec3Iota.display(getVec3());
    }

    public static IotaType<Vec3Iota> TYPE = new IotaType<>() {
        public static final MapCodec<Vec3Iota> CODEC = Vec3.CODEC
                .xmap(Vec3Iota::new, Vec3Iota::getVec3)
                .fieldOf("value");
        // TODO replace with Vec3 codec if it will appear somewhere
        public static final StreamCodec<RegistryFriendlyByteBuf, Vec3Iota> STREAM_CODEC =
                ByteBufCodecs.DOUBLE.apply(ByteBufCodecs.list())
                        .map(
                                l -> new Vec3Iota(new Vec3(l.get(0), l.get(1), l.get(2))),
                                iota -> List.of(iota.getVec3().x, iota.getVec3().y, iota.getVec3().z)
                        )
                        .mapStream(b -> b);

        @Override
        public MapCodec<Vec3Iota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, Vec3Iota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            return 0xff_ff3030;
        }
    };

    public static Component display(double x, double y, double z) {
        return Component.literal(String.format("(%.2f, %.2f, %.2f)", x, y, z))
            .withStyle(ChatFormatting.RED);
    }

    public static Component display(Vec3 v) {
        return display(v.x, v.y, v.z);
    }
}
