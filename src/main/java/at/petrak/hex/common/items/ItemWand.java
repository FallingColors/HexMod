package at.petrak.hex.common.items;

import at.petrak.hex.HexMod;
import at.petrak.hex.client.gui.GuiSpellcasting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemWand extends Item {
    public static final String TAG_MANA = "mana";
    public static final String TAG_MAX_MANA = "maxMana";
    public static final String TAG_HARNESS = "harness";

    public ItemWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide()) {
            Minecraft.getInstance().setScreen(new GuiSpellcasting(hand));
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        var tag = pStack.getOrCreateTag();
        var mana = tag.getInt(TAG_MANA);
        var maxMana = tag.getInt(TAG_MAX_MANA);
        if (mana < maxMana) {
            tag.putInt(TAG_MANA, Math.min(maxMana, mana + HexMod.CONFIG.wandRechargeRate.get()));
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        var tag = pStack.getOrCreateTag();
        var mana = tag.getInt(TAG_MANA);
        var maxMana = tag.getInt(TAG_MAX_MANA);
        float amt;
        if (maxMana == 0) {
            amt = 0f;
        } else {
            amt = ((float) mana) / ((float) maxMana);
        }

        var r = Mth.lerp(amt, 149f, 112f);
        var g = Mth.lerp(amt, 196f, 219f);
        var b = Mth.lerp(amt, 174f, 212f);
        return Mth.color(r / 255f, g / 255f, b / 255f);
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        var tag = pStack.getOrCreateTag();
        var mana = tag.getInt(TAG_MANA);
        var maxMana = tag.getInt(TAG_MAX_MANA);
        float amt;
        if (maxMana == 0) {
            amt = 0f;
        } else {
            amt = ((float) mana) / ((float) maxMana);
        }
        return Math.round(13f * amt);
    }
}
