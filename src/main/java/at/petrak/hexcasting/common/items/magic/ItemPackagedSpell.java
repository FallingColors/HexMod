package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.api.item.SpellHolderItem;
import at.petrak.hexcasting.api.spell.casting.CastingContext;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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

/**
 * Item that holds a list of patterns in it ready to be cast
 */
public abstract class ItemPackagedSpell extends ItemManaHolder implements SpellHolderItem {
    public static final String TAG_PATTERNS = "patterns";
    public static final ResourceLocation HAS_PATTERNS_PRED = new ResourceLocation(HexMod.MOD_ID, "has_patterns");

    public ItemPackagedSpell(Properties pProperties) {
        super(pProperties);
    }

    public abstract boolean singleUse();

    @Override
    public boolean manaProvider(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable List<HexPattern> getPatterns(ItemStack stack) {
        if (!stack.hasTag())
            return null;
        CompoundTag tag = stack.getTag();
        if (!tag.contains(TAG_PATTERNS, Tag.TAG_LIST))
            return null;

        var out = new ArrayList<HexPattern>();
        var patsTag = tag.getList(TAG_PATTERNS, Tag.TAG_COMPOUND);
        for (var patTag : patsTag) {
            out.add(HexPattern.DeserializeFromNBT((CompoundTag) patTag));
        }
        return out;
    }

    @Override
    public void writePatterns(ItemStack stack, List<HexPattern> patterns, int mana) {
        ListTag patsTag = new ListTag();
        for (HexPattern pat : patterns)
            patsTag.add(pat.serializeToNBT());

        stack.getOrCreateTag().put(ItemPackagedSpell.TAG_PATTERNS, patsTag);

        withMana(stack, mana, mana);
    }

    @Override
    public void clearPatterns(ItemStack stack) {
        stack.removeTagKey(ItemPackagedSpell.TAG_PATTERNS);
        withMana(stack, 0, 0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        var tag = stack.getOrCreateTag();
        List<HexPattern> patterns = getPatterns(stack);
        if (patterns == null) {
            return InteractionResultHolder.fail(stack);
        }

        if (world.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        var sPlayer = (ServerPlayer) player;
        var ctx = new CastingContext(sPlayer, usedHand);
        var harness = new CastingHarness(ctx);
        for (var pattern : patterns) {
            var info = harness.executeNewPattern(pattern, sPlayer.getLevel());
            if (info.getWasPrevPatternInvalid()) {
                break;
            }
        }

        Stat<?> stat;
        if (singleUse()) {
            stat = Stats.ITEM_BROKEN.get(this);
        } else {
            stat = Stats.ITEM_USED.get(this);
        }
        player.awardStat(stat);

        sPlayer.getCooldowns().addCooldown(this, 5);
        sPlayer.level.playSound(null, sPlayer.getX(), sPlayer.getY(), sPlayer.getZ(),
            HexSounds.ACTUALLY_CAST.get(), SoundSource.PLAYERS, 1f,
            1f + ((float) Math.random() - 0.5f) * 0.2f);

        if (singleUse()) {
            stack.shrink(1);
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
