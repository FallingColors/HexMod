package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.lib.HexAttributes;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.common.msgs.MsgClearSpiralPatternsS2C;
import at.petrak.hexcasting.common.msgs.MsgOpenSpellGuiS2C;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemStaff extends Item {
    // 0 = normal. 1 = old. 2 = cherry preview
    public static final ResourceLocation FUNNY_LEVEL_PREDICATE = ResourceLocation.fromNamespaceAndPath(HexAPI.MOD_ID, "funny_level");

    public ItemStaff(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (player.getAttributeValue(HexAttributes.FEEBLE_MIND) > 0){
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        if (player.isShiftKeyDown()) {
            if (world.isClientSide()) {
                player.playSound(HexSounds.STAFF_RESET, 1f, 1f);
            } else if (player instanceof ServerPlayer serverPlayer) {
                IXplatAbstractions.INSTANCE.clearCastingData(serverPlayer);
                var packet = new MsgClearSpiralPatternsS2C(player.getUUID());
                IXplatAbstractions.INSTANCE.sendPacketToPlayer(serverPlayer, packet);
                IXplatAbstractions.INSTANCE.sendPacketTracking(serverPlayer, packet);
            }
        }

        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            var vm = IXplatAbstractions.INSTANCE.getStaffcastVM(serverPlayer, hand);
            var patterns = IXplatAbstractions.INSTANCE.getPatternsSavedInUi(serverPlayer);

            var userData = vm.getImage().getUserData();
            CompoundTag ravenmind = null;
            if(userData.contains(HexAPI.RAVENMIND_USERDATA))
                ravenmind = userData.getCompound(HexAPI.RAVENMIND_USERDATA);

            IXplatAbstractions.INSTANCE.sendPacketToPlayer(serverPlayer,
                new MsgOpenSpellGuiS2C(hand, patterns, vm.getImage().getStack(), ravenmind,
                    0)); // TODO: Fix!
        }

        player.awardStat(Stats.ITEM_USED.get(this));
//        player.gameEvent(GameEvent.ITEM_INTERACT_START);

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

}
