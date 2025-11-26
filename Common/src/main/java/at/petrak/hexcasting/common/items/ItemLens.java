package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.common.lib.HexAttributes;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemLens extends Item implements HexBaubleItem { // Wearable,

    // The 0.1 is *additive*

    public static final AttributeModifier GRID_ZOOM = new AttributeModifier(modLoc("scrying_grid_zoom"), 0.33, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    public static final AttributeModifier SCRY_SIGHT = new AttributeModifier(
            modLoc("scrying_sight"), 1.0, AttributeModifier.Operation.ADD_VALUE);

    public ItemLens(Properties pProperties) {
        super(pProperties);
        DispenserBlock.registerBehavior(this, new OptionalDispenseItemBehavior() {
            protected @NotNull
            ItemStack execute(@NotNull BlockSource world, @NotNull ItemStack stack) {
                this.setSuccess(ArmorItem.dispenseArmor(world, stack));
                return stack;
            }
        });
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getHexBaubleAttrs(ItemStack stack) {
        HashMultimap<Attribute, AttributeModifier> out = HashMultimap.create();
        out.put(HexAttributes.GRID_ZOOM, GRID_ZOOM);
        out.put(HexAttributes.SCRY_SIGHT, SCRY_SIGHT);
        return out;
    }

    // In fabric impled with extension property?
    @Nullable
    @SoftImplement("forge")
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

//    @Nullable
//    @Override
//    public SoundEvent getEquipSound() {
//        return SoundEvents.AMETHYST_BLOCK_CHIME;
//    }

}
