package at.petrak.hex.items;

import at.petrak.hex.casting.CastingHarness;
import at.petrak.hex.casting.CastingHarness.CastResult;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;

public class ItemWand extends Item {
    public ItemWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, world, entity, slotId, isSelected);

        if (world.isClientSide() || !isSelected) {
            return;
        }

        ServerPlayer player = (ServerPlayer) entity;
        CompoundTag stackTag = stack.getOrCreateTag();
        if (!stackTag.isEmpty() || player.isUsingItem()) {
            CastingHarness harness = CastingHarness.DeserializeFromNBT(stack.getOrCreateTag(), player);
            CastResult res = harness.update();

            if (res instanceof CastResult.Nothing) {
                // Save back the context
                stack.setTag(harness.serializeToNBT());
            } else {
                if (res instanceof CastResult.Success) {
                    CastResult.Success success = (CastResult.Success) res;
                    success.getSpell().cast(harness.getCtx());
                } else if (res instanceof CastResult.Error) {
                    CastResult.Error error = (CastResult.Error) res;
                    player.sendMessage(new TextComponent(error.toString()), Util.NIL_UUID);
                }
                // In any case clear the tag, we're through
                stack.setTag(new CompoundTag());
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 9001;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        return ItemUtils.startUsingInstantly(pLevel, pPlayer, pUsedHand);
    }
}
