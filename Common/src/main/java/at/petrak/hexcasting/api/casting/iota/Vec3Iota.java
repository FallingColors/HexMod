package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Vec3Iota extends Iota {

    private record Vec3CompoundRepresentation(double x, double y, double z) {
        static Codec<Vec3> CODEC = RecordCodecBuilder.<Vec3CompoundRepresentation>create(
                        instance -> instance.group(
                                Codec.DOUBLE.fieldOf("x").forGetter(Vec3CompoundRepresentation::x),
                                Codec.DOUBLE.fieldOf("y").forGetter(Vec3CompoundRepresentation::y),
                                Codec.DOUBLE.fieldOf("z").forGetter(Vec3CompoundRepresentation::z)
                        ).apply(instance, Vec3CompoundRepresentation::new))
                .xmap(Vec3CompoundRepresentation::toVec3, Vec3CompoundRepresentation::fromVec3);

        static Vec3CompoundRepresentation fromVec3(Vec3 vec) {
            return new Vec3CompoundRepresentation(vec.x, vec.y, vec.z);
        }

        Vec3 toVec3() {
            return new Vec3(x, y, z);
        }
    }

    private static final Codec<Vec3> Vec3CursedCodec = Codec.LONG.listOf().xmap(
            list -> new Vec3(
                    Double.longBitsToDouble(list.get(0)),
                    Double.longBitsToDouble(list.get(1)),
                    Double.longBitsToDouble(list.get(2))),
            vec -> List.of(
                    Double.doubleToLongBits(vec.x),
                    Double.doubleToLongBits(vec.y),
                    Double.doubleToLongBits(vec.z))
    );

    public Vec3Iota(@NotNull Vec3 datum) {
        super(HexIotaTypes.VEC3, datum);
    }

    public Vec3 getVec3() {
        var v = (Vec3) this.payload;
        return new Vec3(HexUtils.fixNAN(v.x), HexUtils.fixNAN(v.y), HexUtils.fixNAN(v.z));
    }

    @Override
    public boolean isTruthy() {
        var v = this.getVec3();
        return !(v.x == 0.0 && v.y == 0.0 && v.z == 0.0);
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that) && that instanceof Vec3Iota viota && this.getVec3().distanceToSqr(viota.getVec3()) < DoubleIota.TOLERANCE * DoubleIota.TOLERANCE;
    }

    /**
     * @deprecated use {@link Vec3Iota#TYPE#getCodec} instead.
     */
    @Deprecated
    @Override
    public @NotNull Tag serialize() {
        return HexUtils.serializeWithCodec(this, TYPE.getCodec());
    }

    public static IotaType<Vec3Iota> TYPE = new IotaType<>() {

        @Override
        public Codec<Vec3Iota> getCodec() {
            return Codec.either(Vec3CompoundRepresentation.CODEC, Vec3CursedCodec).flatXmap(either -> {
                if (either.left().isPresent()) {
                    return DataResult.success(new Vec3Iota(either.left().get()));
                } else if (either.right().isPresent()) {
                    return DataResult.success(new Vec3Iota(either.right().get()));
                } else return DataResult.error(() -> "Not a valid Vec3 format: " + either);
            }, iota -> DataResult.success(Either.left(iota.getVec3())));
        }

        /**
         * @deprecated
         * use {@link Vec3Iota#TYPE#getCodec} instead.
         */
        @Deprecated
        @Nullable
        @Override
        public Vec3Iota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return HexUtils.deserializeWithCodec(tag, getCodec(world));
        }

        @Override
        public Component display(Tag tag) {
            return Vec3Iota.display(HexUtils.deserializeWithCodec(tag, getCodec()).getVec3());
        }

        @Override
        public int color() {
            return 0xff_ff3030;
        }
    };

    public static Vec3Iota deserialize(Tag tag) throws IllegalArgumentException {
        Vec3 vec;
        if (tag.getType() == LongArrayTag.TYPE) {
            var lat = HexUtils.downcast(tag, LongArrayTag.TYPE);
            vec = HexUtils.vecFromNBT(lat.getAsLongArray());
        } else vec = HexUtils.vecFromNBT(HexUtils.downcast(tag, CompoundTag.TYPE));
        return new Vec3Iota(vec);
    }

    public static Component display(double x, double y, double z) {
        return Component.literal(String.format("(%.2f, %.2f, %.2f)", x, y, z)).withStyle(ChatFormatting.RED);
    }

    public static Component display(Vec3 v) {
        return display(v.x, v.y, v.z);
    }
}
