package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.common.network.MsgUpdateComparatorVisualsAck;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class ItemLens extends Item implements Wearable {

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

    // In fabric impled with extension property?
    @Nullable
    @SoftImplement("forge")
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

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (!pLevel.isClientSide() && pEntity instanceof ServerPlayer player) {
            if (pStack == player.getItemBySlot(EquipmentSlot.HEAD) ||
                pStack == player.getItemBySlot(EquipmentSlot.MAINHAND) ||
                pStack == player.getItemBySlot(EquipmentSlot.OFFHAND)) {
                sendComparatorDataToClient(player);
            }
        }
    }

    private static final Map<ServerPlayer, Pair<BlockPos, Integer>> comparatorDataMap = new WeakHashMap<>();

    private void sendComparatorDataToClient(ServerPlayer player) {
        double reachAttribute = IXplatAbstractions.INSTANCE.getReachDistance(player);
        double distance = player.isCreative() ? reachAttribute : reachAttribute - 0.5;
        var hitResult = player.pick(distance, 0, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            var pos = ((BlockHitResult) hitResult).getBlockPos();
            var state = player.level.getBlockState(pos);
            if (state.is(Blocks.COMPARATOR)) {
                syncComparatorValue(player, pos,
                    state.getDirectSignal(player.level, pos, state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
            } else if (state.hasAnalogOutputSignal()) {
                syncComparatorValue(player, pos, state.getAnalogOutputSignal(player.level, pos));
            } else {
                syncComparatorValue(player, null, -1);
            }
        } else {
            syncComparatorValue(player, null, -1);
        }
    }

    private void syncComparatorValue(ServerPlayer player, BlockPos pos, int value) {
        var previous = comparatorDataMap.get(player);
        if (value == -1) {
            if (previous != null) {
                comparatorDataMap.remove(player);
                IXplatAbstractions.INSTANCE.sendPacketToPlayer(player, new MsgUpdateComparatorVisualsAck(null, -1));
            }
        } else if (previous == null || (!pos.equals(previous.getFirst()) || value != previous.getSecond())) {
            comparatorDataMap.put(player, new Pair<>(pos, value));
            IXplatAbstractions.INSTANCE.sendPacketToPlayer(player, new MsgUpdateComparatorVisualsAck(pos, value));
        }
    }

    @Nullable
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

}
