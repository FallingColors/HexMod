package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.common.lib.HexAttributes;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ItemLens extends Item implements Wearable {
    public static final AttributeModifier GRID_ZOOM = new AttributeModifier(
        UUID.fromString("59d739b8-d419-45f7-a4ea-0efee0e3adf5"),
        "Scrying Lens Zoom", 1.25, AttributeModifier.Operation.MULTIPLY_BASE);
    public static final AttributeModifier SCRY_SIGHT = new AttributeModifier(
        UUID.fromString("e2e6e5d4-f978-4c11-8fdc-82a5af83385c"),
        "Scrying Lens Sight", 1.0, AttributeModifier.Operation.ADDITION);

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
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        var out = HashMultimap.create(super.getDefaultAttributeModifiers(slot));
        if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            out.put(HexAttributes.GRID_ZOOM, GRID_ZOOM);
            out.put(HexAttributes.SCRY_SIGHT, SCRY_SIGHT);
        }
        return out;
    }

    // In fabric impled with extension property?
    @Nullable
    @SoftImplement("forge")
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Nullable
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

}
