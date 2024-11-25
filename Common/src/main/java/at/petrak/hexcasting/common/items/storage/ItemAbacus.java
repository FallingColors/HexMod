package at.petrak.hexcasting.common.items.storage;

import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.item.ScrollableItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemAbacus extends Item implements IotaHolderItem, ScrollableItem {
    public static final String TAG_VALUE = "value";

    public ItemAbacus(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable
    CompoundTag readIotaTag(ItemStack stack) {
        var datum = new DoubleIota(NBTHelper.getDouble(stack, TAG_VALUE));
        return IotaType.serialize(datum);
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
            double oldNum = NBTHelper.getDouble(stack, TAG_VALUE);
            stack.removeTagKey(TAG_VALUE);

            player.playSound(HexSounds.ABACUS_SHAKE, 1f, 1f);

            var key = "hexcasting.tooltip.abacus.reset";
            if (oldNum == 69) {
                key += ".nice";
            }
            player.displayClientMessage(Component.translatable(key), true);

            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        } else {
            return InteractionResultHolder.pass(stack);
        }
    }

    @Override
    public void scroll(ItemStack stack, int delta, boolean modified, InteractionHand hand, @Nullable Entity holder){

        var increase = delta < 0;
        double num = NBTHelper.getDouble(stack, ItemAbacus.TAG_VALUE);

        double shiftDelta;
        float pitch;
        if (hand == InteractionHand.MAIN_HAND) {
            shiftDelta = modified ? 10 : 1;
            pitch = modified ? 0.7f : 0.9f;
        } else {
            shiftDelta = modified ? 0.01 : 0.1;
            pitch = modified ? 1.3f : 1.0f;
        }

        int scale = Math.max((int) Math.floor(Math.abs(delta)), 1);

        num += scale * shiftDelta * (increase ? 1 : -1);
        NBTHelper.putDouble(stack, ItemAbacus.TAG_VALUE, num);

        pitch *= (increase ? 1.05f : 0.95f);
        pitch += (Math.random() - 0.5) * 0.1;
        if(holder != null){
            holder.level().playSound(null, holder.getX(), holder.getY(), holder.getZ(),
                HexSounds.ABACUS, SoundSource.PLAYERS, 0.5f, pitch);
        }

        var datumTag = HexItems.ABACUS.readIotaTag(stack);
        if (datumTag != null) {
            var popup = IotaType.getDisplay(datumTag);
            if(holder instanceof ServerPlayer player){
                player.displayClientMessage(
                    Component.translatable("hexcasting.tooltip.abacus", popup).withStyle(ChatFormatting.GREEN), true);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced);
    }
}
