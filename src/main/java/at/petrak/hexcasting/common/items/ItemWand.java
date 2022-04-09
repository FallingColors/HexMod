package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.common.casting.CastingContext;
import at.petrak.hexcasting.common.casting.CastingHarness;
import at.petrak.hexcasting.common.casting.ResolvedPattern;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.common.network.HexMessages;
import at.petrak.hexcasting.common.network.MsgOpenSpellGuiAck;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class ItemWand extends Item {
    public static final String TAG_HARNESS = "hexcasting:spell_harness";
    public static final String TAG_PATTERNS = "hexcasting:spell_patterns";

    public ItemWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (world.isClientSide()) {
                player.playSound(HexSounds.FAIL_PATTERN.get(), 1f, 1f);
            } else {
                player.getPersistentData().remove(TAG_HARNESS);
                player.getPersistentData().remove(TAG_PATTERNS);
            }
        }

        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            CompoundTag harnessTag = player.getPersistentData().getCompound(TAG_HARNESS);
            ListTag patternsTag = player.getPersistentData().getList(TAG_PATTERNS, Tag.TAG_COMPOUND);

            var ctx = new CastingContext(serverPlayer, hand);
            CastingHarness harness = CastingHarness.DeserializeFromNBT(harnessTag, ctx);

            List<ResolvedPattern> patterns = new ArrayList<>(patternsTag.size());

            for (int i = 0; i < patternsTag.size(); i++) {
                patterns.add(ResolvedPattern.DeserializeFromNBT(patternsTag.getCompound(i)));
            }

            HexMessages.getNetwork().send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new MsgOpenSpellGuiAck(hand, patterns, harness.generateDescs()));
        }

        player.awardStat(Stats.ITEM_USED.get(this));

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

}
