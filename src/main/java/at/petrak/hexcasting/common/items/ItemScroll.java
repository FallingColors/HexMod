package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.HexMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * TAG_OP_ID and TAG_PATTERN: "Ancient Scroll of %s" (Great Spells)
 * <br>
 * TAG_PATTERN: "Scroll" (custom)
 * <br>
 * (none): "Empty Scroll"
 * <br>
 * TAG_OP_ID: invalid
 */
public class ItemScroll extends Item {
    public static final String TAG_OP_ID = "op_id";
    public static final String TAG_PATTERN = "pattern";
    public static final ResourceLocation ANCIENT_PREDICATE = new ResourceLocation(HexMod.MOD_ID, "ancient");

    public ItemScroll(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        var tag = pStack.getOrCreateTag();
        if (tag.contains(TAG_OP_ID)) {
            return new TranslatableComponent("item.hexcasting.scroll.of",
                new TranslatableComponent("hexcasting.spell." + ResourceLocation.tryParse(tag.getString(TAG_OP_ID))));
        } else if (tag.contains(TAG_PATTERN)) {
            return new TranslatableComponent("item.hexcasting.scroll");
        } else {
            return new TranslatableComponent("item.hexcasting.scroll.empty");
        }
    }

}
