package at.petrak.hexcasting.common.items;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * Why don't we just use the same API mod on Forge and Fabric? Beats me. botania does it like this.
 * I feel like botnia probably does it this way becase it's older than xplat curios
 */
public interface HexBaubleItem {
    Multimap<Attribute, AttributeModifier> getHexBaubleAttrs(ItemStack stack);
}
