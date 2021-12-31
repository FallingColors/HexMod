package at.petrak.hex.common.items;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.casting.CastingContext;
import at.petrak.hex.common.casting.CastingHarness;
import at.petrak.hex.hexmath.HexPattern;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Item that holds a list of patterns in it ready to be cast
 */
public abstract class ItemPackagedSpell extends ItemManaHolder {
    public static final String TAG_PATTERNS = "patterns";
    public static final ResourceLocation HAS_PATTERNS_PRED = new ResourceLocation(HexMod.MOD_ID, "has_patterns");

    public ItemPackagedSpell(Properties pProperties) {
        super(pProperties);
    }

    abstract boolean singleUse();

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        var tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_PATTERNS)) {
            return InteractionResultHolder.fail(stack);
        }

        if (world.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        var sPlayer = (ServerPlayer) player;
        var ctx = new CastingContext(sPlayer, usedHand);
        var harness = new CastingHarness(ctx);
        List<HexPattern> patterns = getPatterns(tag);
        for (var pattern : patterns) {
            var res = harness.update(pattern);
            if (res instanceof CastingHarness.CastResult.Error) {
                CastingHarness.CastResult.Error error = (CastingHarness.CastResult.Error) res;
                sPlayer.sendMessage(new TextComponent(error.getExn().getMessage()), Util.NIL_UUID);
            } else if (res instanceof CastingHarness.CastResult.Cast) {
                CastingHarness.CastResult.Cast cast = (CastingHarness.CastResult.Cast) res;
                for (var spell : cast.getSpells()) {
                    spell.cast(ctx);
                }
            }
            if (res.shouldQuit()) {
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

        player.getCooldowns().addCooldown(this, 20);

        if (singleUse()) {
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

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        var tag = pStack.getOrCreateTag();
        return !singleUse() && tag.contains(TAG_PATTERNS);
    }


    private static List<HexPattern> getPatterns(CompoundTag tag) {
        var out = new ArrayList<HexPattern>();
        var patsTag = tag.getList(TAG_PATTERNS, Tag.TAG_COMPOUND);
        for (var patTag : patsTag) {
            out.add(HexPattern.DeserializeFromNBT((CompoundTag) patTag));
        }
        return out;
    }
}
