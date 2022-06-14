package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.spell.casting.CastingContext;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Item that holds a list of patterns in it ready to be cast
 */
public abstract class ItemPackagedHex extends ItemMediaHolder implements HexHolderItem {
    public static final String TAG_PROGRAM = "patterns";
    public static final ResourceLocation HAS_PATTERNS_PRED = modLoc("has_patterns");

    public ItemPackagedHex(Properties pProperties) {
        super(pProperties);
    }

    public abstract boolean breakAfterDepletion();

    @Override
    public boolean canRecharge(ItemStack stack) {
        return !breakAfterDepletion();
    }

    @Override
    public boolean canProvideMedia(ItemStack stack) {
        return false;
    }

    @Override
    public boolean hasHex(ItemStack stack) {
        return NBTHelper.hasList(stack, TAG_PROGRAM, Tag.TAG_COMPOUND);
    }

    @Override
    public @Nullable List<Iota> getHex(ItemStack stack, ServerLevel level) {
        var patsTag = NBTHelper.getList(stack, TAG_PROGRAM, Tag.TAG_COMPOUND);

        if (patsTag == null) {
            return null;
        }

        var out = new ArrayList<Iota>();
        for (var patTag : patsTag) {
            CompoundTag tag = NBTHelper.getAsCompound(patTag);
            out.add(HexIotaTypes.deserialize(tag, level));
        }
        return out;
    }

    @Override
    public void writeHex(ItemStack stack, List<Iota> program, int mana) {
        ListTag patsTag = new ListTag();
        for (Iota pat : program) {
            patsTag.add(HexIotaTypes.serialize(pat));
        }

        NBTHelper.putList(stack, TAG_PROGRAM, patsTag);

        withMana(stack, mana, mana);
    }

    @Override
    public void clearHex(ItemStack stack) {
        NBTHelper.remove(stack, ItemPackagedHex.TAG_PROGRAM);
        NBTHelper.remove(stack, TAG_MANA);
        NBTHelper.remove(stack, TAG_MAX_MANA);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        if (!hasHex(stack)) {
            return InteractionResultHolder.fail(stack);
        }

        if (world.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        List<Iota> instrs = getHex(stack, (ServerLevel) world);
        if (instrs == null) {
            return InteractionResultHolder.fail(stack);
        }
        var sPlayer = (ServerPlayer) player;
        var ctx = new CastingContext(sPlayer, usedHand);
        var harness = new CastingHarness(ctx);
        var info = harness.executeIotas(instrs, sPlayer.getLevel());

        boolean broken = breakAfterDepletion() && getMedia(stack) == 0;

        Stat<?> stat;
        if (broken) {
            stat = Stats.ITEM_BROKEN.get(this);
        } else {
            stat = Stats.ITEM_USED.get(this);
        }
        player.awardStat(stat);

        sPlayer.getCooldowns().addCooldown(this, 5);
        if (info.getMakesCastSound()) {
            sPlayer.level.playSound(null, sPlayer.getX(), sPlayer.getY(), sPlayer.getZ(),
                HexSounds.ACTUALLY_CAST, SoundSource.PLAYERS, 1f,
                1f + ((float) Math.random() - 0.5f) * 0.2f);
        }

        if (broken) {
            stack.shrink(1);
            player.broadcastBreakEvent(usedHand);
            return InteractionResultHolder.consume(stack);
        } else {
            return InteractionResultHolder.success(stack);
        }
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 16;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BLOCK;
    }
}
