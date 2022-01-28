package at.petrak.hexcasting.common.casting.colors;

import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.items.colorizer.ItemDyeColorizer;
import at.petrak.hexcasting.common.lib.HexCapabilities;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The colorizer item favored by this player.
 */
public class CapPreferredColorizer implements ICapabilitySerializable<CompoundTag> {
    public static final String CAP_NAME = "preferred_colorizer";
    public static final String TAG_COLOR = "colorizer";

    public ItemStack colorizer;

    public CapPreferredColorizer(ItemStack colorizer) {
        this.colorizer = colorizer;
    }

    /**
     * @param time     absolute world time in ticks
     * @param position a position for the icosahedron, a randomish number for particles.
     * @return an AARRGGBB color.
     */
    public int getColor(LocalPlayer asker, float time, Vec3 position) {
        var proto = this.colorizer.getItem();
        if (proto instanceof ItemDyeColorizer dye) {
            return DyeColor.values()[dye.getDyeIdx()].getTextColor();
        } else if (proto == HexItems.UUID_COLORIZER.get()) {
            
        }

        return 0xff_ff00dc; // missing color
    }

    private int morphBetweenColors(int[] colors, Vec3 gradientDir, float time, Vec3 position) {
        float fIdx = ((time + (float) gradientDir.dot(position)) % 1f) * colors.length;

        int baseIdx = Mth.floor(fIdx);
        float tRaw = fIdx - baseIdx;
        float t = -(float) (Math.cbrt(Mth.cos(tRaw * Mth.PI)) / 2) + 0.5f;
        int start = colors[baseIdx];
        int end = colors[(baseIdx + 1) % colors.length];

        var r1 = FastColor.ARGB32.red(start);
        var g1 = FastColor.ARGB32.green(start);
        var b1 = FastColor.ARGB32.blue(start);
        var a1 = FastColor.ARGB32.alpha(start);
        var r2 = FastColor.ARGB32.red(end);
        var g2 = FastColor.ARGB32.green(end);
        var b2 = FastColor.ARGB32.blue(end);
        var a2 = FastColor.ARGB32.alpha(end);

        var r = Mth.lerp(t, r1, r2);
        var g = Mth.lerp(t, g1, g2);
        var b = Mth.lerp(t, b1, b2);
        var a = Mth.lerp(t, a1, a2);

        return FastColor.ARGB32.color((int) a, (int) r, (int) g, (int) b);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return HexCapabilities.PREFERRED_COLORIZER.orEmpty(cap, LazyOptional.of(() -> this));
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put(TAG_COLOR, this.colorizer.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        var itemTag = nbt.getCompound(TAG_COLOR);
        this.colorizer.deserializeNBT(itemTag);
    }
}
