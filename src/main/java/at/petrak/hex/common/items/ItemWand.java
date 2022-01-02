package at.petrak.hex.common.items;

import at.petrak.hex.client.gui.GuiSpellcasting;
import net.minecraft.client.Minecraft;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemWand extends Item {
    public static final String TAG_HARNESS = "harness";

    public ItemWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide()) {
            Minecraft.getInstance().setScreen(new GuiSpellcasting(hand));
        }

        player.awardStat(Stats.ITEM_USED.get(this));

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

}
