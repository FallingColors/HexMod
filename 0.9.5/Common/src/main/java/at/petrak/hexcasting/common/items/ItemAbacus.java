package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.api.item.DataHolderItem;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemAbacus extends Item implements DataHolderItem {
    public static final String TAG_VALUE = "value";

    public ItemAbacus(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable CompoundTag readDatumTag(ItemStack stack) {
        var datum = SpellDatum.make(NBTHelper.getDouble(stack, TAG_VALUE));
        return datum.serializeToNBT();
    }

    @Override
    public boolean canWrite(ItemStack stack, SpellDatum<?> datum) {
        return false;
    }

    @Override
    public void writeDatum(ItemStack stack, SpellDatum<?> datum) {
        // nope
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            double oldNum = NBTHelper.getDouble(stack, TAG_VALUE);
            stack.removeTagKey(TAG_VALUE);

            player.playSound(HexSounds.ABACUS_SHAKE, 1f, 1f);

            var key = "hexcasting.tooltip.abacus.reset";
            if (oldNum == 69) {
                key += ".nice";
            }
            player.displayClientMessage(new TranslatableComponent(key), true);

            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        } else {
            return InteractionResultHolder.pass(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        DataHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced);
    }
}
