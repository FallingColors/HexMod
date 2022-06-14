package at.petrak.hexcasting.api.spell.iota;

import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class Iota {
    @NotNull
    protected final Object payload;
    @NotNull
    protected final IotaType<?> type;

    protected Iota(@NotNull IotaType<?> type, @NotNull Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public @NotNull
    IotaType<?> getType() {
        return this.type;
    }

    public boolean isTruthy() {
        return true;
    }

    /**
     * Compare this to another object, within a tolerance.
     * <p>
     * Don't call this directly; use the {@link Iota#tolerates} static method.
     */
    abstract public boolean toleratesOther(Iota that);

    /**
     * Serialize this under the {@code data} tag.
     * <p>
     * You probably don't want to call this directly; use {@link HexIotaTypes#serialize}.
     */
    abstract public @NotNull
    Tag serialize();

    public Component display() {
        return this.type.display(this.serialize());
    }

    /**
     * Helper method to see if two iotas have the same type.
     */
    public static boolean typesMatch(Iota a, Iota b) {
        var resA = HexIotaTypes.REGISTRY.getKey(a.getType());
        var resB = HexIotaTypes.REGISTRY.getKey(b.getType());
        return resA != null && resA.equals(resB);
    }

    /**
     * Helper method to see if either iota tolerates the other.
     */
    public static boolean tolerates(Iota a, Iota b) {
        return a.toleratesOther(b) || b.toleratesOther(a);
    }
}
