package at.petrak.hexcasting.common.items.storage;

import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemAbacus extends Item implements IotaHolderItem {

    public ItemAbacus(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable Iota readIota(ItemStack stack) {
        return new DoubleIota(stack.getOrDefault(HexDataComponents.ABACUS_VALUE, 0.0));
    }

    @Override
    public boolean writeable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canWrite(ItemStack stack, Iota datum) {
        return false;
    }

    @Override
    public void writeDatum(ItemStack stack, Iota datum) {
        // nope
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            Double oldNum = stack.get(HexDataComponents.ABACUS_VALUE);
            stack.remove(HexDataComponents.ABACUS_VALUE);

            player.playSound(HexSounds.ABACUS_SHAKE, 1f, 1f);

            var key = "hexcasting.tooltip.abacus.reset";
            if (oldNum != null && oldNum == 69) {
                key += ".nice";
            }
            player.displayClientMessage(Component.translatable(key), true);

            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        } else {
            return InteractionResultHolder.pass(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        IotaHolderItem.appendHoverText(this, stack, tooltipComponents, tooltipFlag);
    }
}
