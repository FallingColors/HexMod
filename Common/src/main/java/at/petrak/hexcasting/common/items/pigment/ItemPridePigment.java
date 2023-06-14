package at.petrak.hexcasting.common.items.pigment;

import at.petrak.hexcasting.api.addldata.ADPigment;
import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.UUID;

public class ItemPridePigment extends Item implements PigmentItem {
    public enum Type {
        AGENDER(new int[]{0x16a10c, 0xffffff, 0x7a8081, 0x302f30}),
        AROACE(new int[]{0x7210bc, 0xebf367, 0xffffff, 0x82dceb, 0x2f4dd8}),
        AROMANTIC(new int[]{0x16a10c, 0x82eb8b, 0xffffff, 0x7a8081, 0x302f30}),
        ASEXUAL(new int[]{0x333233, 0x9a9fa1, 0xffffff, 0x7210bc}),
        BISEXUAL(new int[]{0xdb45ff, 0x9c2bd0, 0x6894d4}),
        DEMIBOY(new int[]{0x9a9fa1, 0xa9ffff, 0xffffff}),
        DEMIGIRL(new int[]{0x9a9fa1, 0xfcb1ff, 0xffffff}),
        GAY(new int[]{0xd82f3a, 0xe0883f, 0xebf367, 0x2db418, 0x2f4dd8}),
//        ACHILLEAN(new int[]{0x028d6e, 0x22cdad, 0xffffff, 0xe49c7, 0x4f4aca}),
        GENDERFLUID(new int[]{0xfbacf9, 0xffffff, 0x9c2bd0, 0x333233, 0x2f4dd8}),
        GENDERQUEER(new int[]{0xca78ef, 0xffffff, 0x2db418}),
        // how to do an intersex gradient escapes me
        INTERSEX(new int[]{0xebf367, 0x7210bc}),
        LESBIAN(new int[]{0xd82f3a, 0xefb87d, 0xffffff, 0xfbacf9, 0xa30262}),
        NONBINARY(new int[]{0xebf367, 0xffffff, 0x7210bc, 0x333233}),
        PANSEXUAL(new int[]{0xe278ef, 0xebf367, 0x6ac2e4}),
        PLURAL(new int[]{0x30c69f, 0x347ddf, 0x6b3fbe, 0x000000}),
        TRANSGENDER(new int[]{0xeb92ea, 0xffffff, 0x6ac2e4});

        private final int[] colors;

        Type(int[] colors) {
            this.colors = colors;
            for (int i = 0; i < this.colors.length; i++) {
                this.colors[i] |= 0xFF_000000;
            }
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public final Type type;

    public ItemPridePigment(Type type, Properties pProperties) {
        super(pProperties);
        this.type = type;
    }

    @Override
    public ColorProvider provideColor(ItemStack stack, UUID owner) {
        return colorProvider;
    }

    protected MyColorProvider colorProvider = new MyColorProvider();

    protected class MyColorProvider extends ColorProvider {
        @Override
        protected int getRawColor(float time, Vec3 position) {
            return ADPigment.morphBetweenColors(type.colors, new Vec3(0.1, 0.1, 0.1), time / 400, position);
        }
    }
}
