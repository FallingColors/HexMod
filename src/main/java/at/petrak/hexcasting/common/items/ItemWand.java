package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.api.item.CasterItem;
import at.petrak.hexcasting.api.player.HexPlayerDataHelper;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.common.network.HexMessages;
import at.petrak.hexcasting.common.network.MsgOpenSpellGuiAck;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class ItemWand extends Item implements CasterItem {

    public ItemWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (world.isClientSide()) {
                player.playSound(HexSounds.FAIL_PATTERN.get(), 1f, 1f);
            } else if (player instanceof ServerPlayer serverPlayer) {
                HexPlayerDataHelper.clearCastingData(serverPlayer);
            }
        }

        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            var harness = HexPlayerDataHelper.getHarness(serverPlayer, hand);
            var patterns = HexPlayerDataHelper.getPatterns(serverPlayer);

            HexMessages.getNetwork().send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new MsgOpenSpellGuiAck(hand, patterns, harness.generateDescs()));
        }

        player.awardStat(Stats.ITEM_USED.get(this));

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

}
