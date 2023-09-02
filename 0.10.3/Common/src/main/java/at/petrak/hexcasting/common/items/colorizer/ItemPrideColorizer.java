package at.petrak.hexcasting.common.items.colorizer;

import at.petrak.hexcasting.api.addldata.ADColorizer;
import at.petrak.hexcasting.api.item.ColorizerItem;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Locale;
import java.util.UUID;

public class ItemPrideColorizer extends Item implements ColorizerItem {
    public enum Type {
        AGENDER,
        AROACE,
        AROMANTIC,
        ASEXUAL,
        BISEXUAL,
        DEMIBOY,
        DEMIGIRL,
        GAY,
        GENDERFLUID,
        GENDERQUEER,
        INTERSEX,
        LESBIAN,
        NONBINARY,
        PANSEXUAL,
        PLURAL,
        TRANSGENDER;

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public final Type type;

    public ItemPrideColorizer(Type type, Properties pProperties) {
        super(pProperties);
        this.type = type;
    }

    @Override
    public int color(ItemStack stack, UUID owner, float time, Vec3 position) {
        return ADColorizer.morphBetweenColors(getColors(), new Vec3(0.1, 0.1, 0.1), time / 20 / 20, position);
    }

    public int[] getColors() {
        return COLORS.get(this.type);
    }

    private static final EnumMap<Type, int[]> COLORS = Util.make(() -> {
        var out = new EnumMap<Type, int[]>(Type.class);

        out.put(Type.AGENDER, new int[]{0x16a10c, 0xffffff, 0x7a8081, 0x302f30});
        out.put(Type.AROACE, new int[]{0x7210bc, 0xebf367, 0xffffff, 0x82dceb, 0x2f4dd8});
        out.put(Type.AROMANTIC, new int[]{0x16a10c, 0x82eb8b, 0xffffff, 0x7a8081, 0x302f30});
        out.put(Type.ASEXUAL, new int[]{0x333233, 0x9a9fa1, 0xffffff, 0x7210bc});
        out.put(Type.BISEXUAL, new int[]{0xdb45ff, 0x9c2bd0, 0x6894d4});
        out.put(Type.DEMIBOY, new int[]{0x9a9fa1, 0xa9ffff, 0xffffff});
        out.put(Type.DEMIGIRL, new int[]{0x9a9fa1, 0xfcb1ff, 0xffffff});
        out.put(Type.GAY, new int[]{0xd82f3a, 0xe0883f, 0xebf367, 0x2db418, 0x2f4dd8});
        out.put(Type.GENDERFLUID, new int[]{0xfbacf9, 0xffffff, 0x9c2bd0, 0x333233, 0x2f4dd8});
        out.put(Type.GENDERQUEER, new int[]{0xca78ef, 0xffffff, 0x2db418});
        out.put(Type.INTERSEX, new int[]{0xebf367, 0x7210bc}); // how to do an intersex gradient escapes me
        out.put(Type.LESBIAN, new int[]{0xd82f3a, 0xefb87d, 0xffffff, 0xfbacf9, 0xa30262});
        out.put(Type.NONBINARY, new int[]{0xebf367, 0xffffff, 0x7210bc, 0x333233});
        out.put(Type.PANSEXUAL, new int[]{0xe278ef, 0xebf367, 0x6ac2e4});
        out.put(Type.PLURAL, new int[]{0x30c69f, 0x347ddf, 0x6b3fbe, 0x000000});
        out.put(Type.TRANSGENDER, new int[]{0xeb92ea, 0xffffff, 0x6ac2e4});

        for (int[] color : out.values()) {
            for (int i = 0; i < color.length; i++) {
                color[i] |= 0xff_000000;
            }
        }

        return out;
    });
}
