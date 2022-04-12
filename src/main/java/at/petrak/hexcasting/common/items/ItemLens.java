package at.petrak.hexcasting.common.items;

import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemLens extends Item implements Wearable {

    public ItemLens(Properties pProperties) {
        super(pProperties);
        DispenserBlock.registerBehavior(this, new OptionalDispenseItemBehavior() {
            protected @NotNull ItemStack execute(@NotNull BlockSource world, @NotNull ItemStack stack) {
                this.setSuccess(ArmorItem.dispenseArmor(world, stack));
                return stack;
            }
        });
    }

    @Nullable
    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
        ItemStack stack = player.getItemBySlot(equipmentslot);
        if (stack.isEmpty()) {
            player.setItemSlot(equipmentslot, itemstack.copy());
            if (!world.isClientSide()) {
                player.awardStat(Stats.ITEM_USED.get(this));
            }

            itemstack.setCount(0);
            return InteractionResultHolder.sidedSuccess(itemstack, world.isClientSide());
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Nullable
    public SoundEvent getEquipSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

}
