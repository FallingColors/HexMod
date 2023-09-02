package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.api.misc.DiscoveryHandlers;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.network.MsgUpdateComparatorVisualsAck;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class ItemLens extends Item implements Wearable {

    static {
        DiscoveryHandlers.addLensPredicate(player -> player.getItemBySlot(EquipmentSlot.MAINHAND).is(HexItems.SCRYING_LENS));
        DiscoveryHandlers.addLensPredicate(player -> player.getItemBySlot(EquipmentSlot.OFFHAND).is(HexItems.SCRYING_LENS));
        DiscoveryHandlers.addLensPredicate(player -> player.getItemBySlot(EquipmentSlot.HEAD).is(HexItems.SCRYING_LENS));

        DiscoveryHandlers.addGridScaleModifier(player -> player.getItemBySlot(EquipmentSlot.MAINHAND).is(HexItems.SCRYING_LENS) ? 0.75f : 1);
        DiscoveryHandlers.addGridScaleModifier(player -> player.getItemBySlot(EquipmentSlot.OFFHAND).is(HexItems.SCRYING_LENS) ? 0.75f : 1);
    }

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

    public static void tickAllPlayers(ServerLevel world) {
        for (ServerPlayer player : world.players()) {
            tickLens(player);
        }
    }

    public static void tickLens(Entity pEntity) {
        if (!pEntity.getLevel().isClientSide() && pEntity instanceof ServerPlayer player && DiscoveryHandlers.hasLens(player)) {
            sendComparatorDataToClient(player);
        }
    }

    private static final Map<ServerPlayer, Pair<BlockPos, Integer>> comparatorDataMap = new WeakHashMap<>();
    private static final Map<ServerPlayer, Pair<BlockPos, Integer>> beeDataMap = new WeakHashMap<>();

    private static void sendComparatorDataToClient(ServerPlayer player) {
        double reachAttribute = IXplatAbstractions.INSTANCE.getReachDistance(player);
        double distance = player.isCreative() ? reachAttribute : reachAttribute - 0.5;
        var hitResult = player.pick(distance, 0, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            var pos = ((BlockHitResult) hitResult).getBlockPos();
            var state = player.level.getBlockState(pos);

            int bee = -1;

            if (state.getBlock() instanceof BeehiveBlock && player.level.getBlockEntity(pos) instanceof BeehiveBlockEntity bees) {
                bee = bees.getOccupantCount();
            }

            if (state.is(Blocks.COMPARATOR)) {
                syncComparatorValue(player, pos,
                    state.getDirectSignal(player.level, pos, state.getValue(BlockStateProperties.HORIZONTAL_FACING)), bee);
            } else if (state.hasAnalogOutputSignal()) {
                syncComparatorValue(player, pos, state.getAnalogOutputSignal(player.level, pos), bee);
            } else {
                syncComparatorValue(player, null, -1, bee);
            }
        } else {
            syncComparatorValue(player, null, -1, -1);
        }
    }

    private static void syncComparatorValue(ServerPlayer player, BlockPos pos, int comparator, int bee) {
        var previousComparator = comparatorDataMap.get(player);
        var previousBee = beeDataMap.get(player);
        if (comparator == -1 && bee == -1) {
            if (previousComparator != null || previousBee != null) {
                comparatorDataMap.remove(player);
                beeDataMap.remove(player);
                IXplatAbstractions.INSTANCE.sendPacketToPlayer(player, new MsgUpdateComparatorVisualsAck(null, -1, -1));
            }
        } else if (previousComparator == null || !pos.equals(previousComparator.getFirst()) || comparator != previousComparator.getSecond() ||
            previousBee == null || !pos.equals(previousBee.getFirst()) || bee != previousBee.getSecond()) {
            comparatorDataMap.put(player, new Pair<>(pos, comparator));
            beeDataMap.put(player, new Pair<>(pos, bee));
            IXplatAbstractions.INSTANCE.sendPacketToPlayer(player, new MsgUpdateComparatorVisualsAck(pos, comparator, bee));
        }
    }

    @Nullable
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

}
