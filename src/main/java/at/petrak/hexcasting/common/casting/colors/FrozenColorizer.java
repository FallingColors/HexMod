package at.petrak.hexcasting.common.casting.colors;

import at.petrak.hexcasting.common.ContributorList;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.items.colorizer.ItemDyeColorizer;
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

/**
 * A colorizer item and the player who owned it at the time of making the color.
 */
public record FrozenColorizer(Item item, UUID owner) {
    public static final String TAG_ITEM = "item";
    public static final String TAG_OWNER = "owner";

    public static final FrozenColorizer DEFAULT = new FrozenColorizer(HexItems.DYE_COLORIZERS[0].get(), Util.NIL_UUID);

    public CompoundTag serialize() {
        var out = new CompoundTag();
        out.putString(TAG_ITEM, ForgeRegistries.ITEMS.getKey(this.item).toString());
        out.putUUID(TAG_OWNER, this.owner);
        return out;
    }

    public static FrozenColorizer deserialize(CompoundTag tag) {
        try {
            var itemID = new ResourceLocation(tag.getString(TAG_ITEM));
            var item = ForgeRegistries.ITEMS.getValue(itemID);
            var uuid = tag.getUUID(TAG_OWNER);
            return new FrozenColorizer(item, uuid);
        } catch (NullPointerException exn) {
            return FrozenColorizer.DEFAULT;
        }
    }

    public static boolean isColorizer(Item item) {
        return item instanceof ItemDyeColorizer
            || item instanceof ItemPrideColorizer
            || item == HexItems.UUID_COLORIZER.get();
    }

    /**
     * @param time     absolute world time in ticks
     * @param position a position for the icosahedron, a randomish number for particles.
     * @return an AARRGGBB color.
     */
    public int getColor(float time, Vec3 position) {
        if (this.item instanceof ItemDyeColorizer dye) {
            return DyeColor.values()[dye.getDyeIdx()].getTextColor() | 0xff_000000;
        } else if (this.item instanceof ItemPrideColorizer politics) {
            var colors = politics.getColors();
            return morphBetweenColors(colors, new Vec3(0.1, 0.1, 0.1), time / 20 / 20, position);
        } else if (this.item == HexItems.UUID_COLORIZER.get()) {
            var info = ContributorList.getContributor(this.owner);
            if (info != null) {
                return morphBetweenColors(info.getColorizer(), new Vec3(0.1, 0.1, 0.1), time / 20 / 20, position);
            } else {
                // randomly scrungle the bits
                return FastColor.ARGB32.color(255,
                    (int) (this.owner.getLeastSignificantBits() & 0xff),
                    (int) (this.owner.getLeastSignificantBits() >>> 32 & 0xff),
                    (int) (this.owner.getMostSignificantBits() & 0xff));
            }
        }

        return 0xff_ff00dc; // missing color
    }

    private static int morphBetweenColors(int[] colors, Vec3 gradientDir, float time, Vec3 position) {
        float fIdx = Mth.positiveModulo(time + (float) gradientDir.dot(position), 1f) * colors.length;

        int baseIdx = Mth.floor(fIdx);
        float tRaw = fIdx - baseIdx;
//        float t = -(float) (Math.cbrt(Mth.cos(tRaw * Mth.PI)) / 2) + 0.5f;
        float t = tRaw;
        int start = colors[baseIdx % colors.length];
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
}
