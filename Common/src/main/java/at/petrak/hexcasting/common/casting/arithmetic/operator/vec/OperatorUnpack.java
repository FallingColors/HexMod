package at.petrak.hexcasting.common.casting.arithmetic.operator.vec;

import static at.petrak.hexcasting.common.lib.hex.HexIotaTypes.VEC3;

import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBasic;
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OperatorUnpack extends OperatorBasic {
    private OperatorUnpack() {
        super(1, IotaMultiPredicate.all(IotaPredicate.ofType(HexIotaTypes.VEC3)));
    }

    public static OperatorUnpack INSTANCE = new OperatorUnpack();

    @Override
    public @NotNull Iterable<Iota> apply(
            Iterable<? extends Iota> iotas, @NotNull CastingEnvironment env) {
        var it = iotas.iterator();
        var vec = downcast(it.next(), VEC3).getVec3();
        return List.of(new DoubleIota(vec.x), new DoubleIota(vec.y), new DoubleIota(vec.z));
    }
}
