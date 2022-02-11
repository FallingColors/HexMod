package at.petrak.hexcasting.common.items.colorizer;

import net.minecraft.world.item.Item;

public class ItemPrideColorizer extends Item {
    private final int idx;

    public ItemPrideColorizer(int idx, Properties pProperties) {
        super(pProperties);
        this.idx = idx;
    }

    public int[] getColors() {
        return COLORS[this.idx];
    }

    private static final int[][] COLORS;

    static {
        COLORS = new int[][]{
            {0xeb92ea, 0xffffff, 0x6ac2e4},
            {0xd82f3a, 0xe0883f, 0xebf367, 0x2db418, 0x2f4dd8},
            {0x16a10c, 0x82eb8b, 0xffffff, 0x7a8081},
            {0x333233, 0x9a9fa1, 0xffffff, 0x7210bc},
            {0xdb45ff, 0x9c2bd0, 0x6894d4},
            {0xe278ef, 0xebf367, 0x6ac2e4},
            {0xca78ef, 0xffffff, 0x2db418},
            {0x9a9fa1, 0xfcb1ff, 0xffffff},
            {0xebf367, 0xffffff, 0x7210bc, 0x333233},
            {0xd82f3a, 0xefb87d, 0xffffff, 0xfbacf9},
            {0x9a9fa1, 0xa9ffff, 0xffffff},
            {0xfbacf9, 0xffffff, 0x9c2bd0, 0x333233, 0x2f4dd8},
            {0xebf367, 0x7210bc}, // how to do an intersex gradient escapes me
            {0x7210bc, 0xebf367, 0xffffff, 0x82dceb, 0x2f4dd8}
        };
        for (int[] color : COLORS) {
            for (int i = 0; i < color.length; i++) {
                color[i] |= 0xff_000000;
            }
        }
    }
}
