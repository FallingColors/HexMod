package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.misc.DiscoveryHandlers;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.items.ItemLoreFragment;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemCreativeUnlocker extends Item implements MediaHolderItem {

    public static final String DISPLAY_MEDIA = "media";
    public static final String DISPLAY_PATTERNS = "patterns";

    static {
        DiscoveryHandlers.addDebugItemDiscoverer((player, type) -> {
            for (ItemStack item : player.getInventory().items) {
                if (isDebug(item, type)) {
                    return item;
                }
            }

            // Technically possible with commands!
            for (ItemStack item : player.getInventory().armor) {
                if (isDebug(item, type)) {
                    return item;
                }
            }

            for (ItemStack item : player.getInventory().offhand) {
                if (isDebug(item, type)) {
                    return item;
                }
            }
            return ItemStack.EMPTY;
        });
    }

    public static boolean isDebug(ItemStack stack) {
        return isDebug(stack, null);
    }

    public static boolean isDebug(ItemStack stack, String flag) {
        if (!stack.is(HexItems.CREATIVE_UNLOCKER) || !stack.has(DataComponents.CUSTOM_NAME)) {
            return false;
        }
        var keywords = Arrays.asList(stack.getHoverName().getString().toLowerCase(Locale.ROOT).split(" "));
        if (!keywords.contains("debug")) {
            return false;
        }
        return flag == null || keywords.contains(flag);
    }

    public static Component infiniteMedia(Level level) {
        String prefix = "item.hexcasting.creative_unlocker.";

        String emphasis = Language.getInstance().getOrDefault(prefix + "for_emphasis");
        MutableComponent emphasized = Component.empty();
        for (int i = 0; i < emphasis.length(); i++) {
            emphasized.append(rainbow(Component.literal("" + emphasis.charAt(i)), i, level));
        }

        return emphasized;
    }

    public static final String TAG_EXTRACTIONS = "extractions";
    public static final String TAG_INSERTIONS = "insertions";

    public ItemCreativeUnlocker(Properties properties) {
        super(properties);
    }

    @Override
    public long getMedia(ItemStack stack) {
        return Long.MAX_VALUE;
    }

    @Override
    public long getMaxMedia(ItemStack stack) {
        return Long.MAX_VALUE;
    }

    @Override
    public void setMedia(ItemStack stack, long media) {
        // NO-OP
    }

    @Override
    public boolean canProvideMedia(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canRecharge(ItemStack stack) {
        return true;
    }

    public static void addToLongArray(ItemStack stack, DataComponentType<List<Long>> type, long n) {
        var list = stack.getOrDefault(type, new ArrayList<Long>());
        list.add(n);
        stack.set(type, list);
    }

    @Override
    public long withdrawMedia(ItemStack stack, long cost, boolean simulate) {
        // In case it's withdrawn through other means
        if (!simulate && isDebug(stack, DISPLAY_MEDIA)) {
            addToLongArray(stack, HexDataComponents.MEDIA_EXTRACTIONS, cost);
        }

        return cost < 0 ? getMedia(stack) : cost;
    }

    @Override
    public long insertMedia(ItemStack stack, long amount, boolean simulate) {
        // In case it's inserted through other means
        if (!simulate && isDebug(stack, DISPLAY_MEDIA)) {
            addToLongArray(stack, HexDataComponents.MEDIA_INSERTIONS, amount);
        }

        return amount < 0 ? getMaxMedia(stack) : amount;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || isDebug(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (isDebug(stack, DISPLAY_MEDIA) && !level.isClientSide) {
            debugDisplay(stack, HexDataComponents.MEDIA_EXTRACTIONS, "withdrawn", "all_media", entity);
            debugDisplay(stack, HexDataComponents.MEDIA_INSERTIONS, "inserted", "infinite_media", entity);
        }
    }

    private void debugDisplay(ItemStack stack, DataComponentType<List<Long>> type, String langKey, String allKey, Entity entity) {
        var list = stack.get(type);
        if (list != null) {
            stack.remove(type);
            for (long i : list) {
                if (i < 0) {
                    entity.sendSystemMessage(Component.translatable("hexcasting.debug.media_" + langKey,
                            stack.getDisplayName(),
                            Component.translatable("hexcasting.debug." + allKey).withStyle(ChatFormatting.GRAY))
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
                } else {
                    entity.sendSystemMessage(Component.translatable("hexcasting.debug.media_" + langKey + ".with_dust",
                            stack.getDisplayName(),
                            Component.literal("" + i).withStyle(ChatFormatting.WHITE),
                            Component.literal(String.format("%.2f", i * 1.0 / MediaConstants.DUST_UNIT)).withStyle(
                                ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (be instanceof BlockEntityAbstractImpetus impetus) {
            impetus.setInfiniteMedia();
            context.getLevel().playSound(null, context.getClickedPos(), HexSounds.SPELL_CIRCLE_FIND_BLOCK,
                SoundSource.PLAYERS, 1f, 1f);
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (level instanceof ServerLevel slevel && consumer instanceof ServerPlayer player) {
            var names = new ArrayList<>(ItemLoreFragment.NAMES);
            names.add(0, modLoc("root"));
            for (var name : names) {
                var rootAdv = slevel.getServer().getAdvancements().tree().get(name);
                if (rootAdv != null) {
                    var children = new ArrayList<AdvancementNode>();
                    children.add(rootAdv);
                    collectChildrenRecursively(rootAdv, children);

                    var adman = player.getAdvancements();



                    for (var kid : children) {
                        var progress = adman.getOrStartProgress(kid.holder());
                        if (!progress.isDone()) {
                            for (String crit : progress.getRemainingCriteria()) {
                                adman.award(kid.holder(), crit);
                            }
                        }
                    }
                }
            }
        }

        ItemStack copy = stack.copy();
        super.finishUsingItem(stack, level, consumer);
        return copy;
    }

    private static MutableComponent rainbow(MutableComponent component, int shift, Level level) {
        if (level == null) {
            return component.withStyle(ChatFormatting.WHITE);
        }

        return component.withStyle((s) -> s.withColor(
            TextColor.fromRgb(Mth.hsvToRgb((level.getGameTime() + shift) * 2 % 360 / 360F, 1F, 1F))));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
        TooltipFlag isAdvanced) {
        Component emphasized = infiniteMedia(context.level());

        MutableComponent modName = Component.translatable("item.hexcasting.creative_unlocker.mod_name").withStyle(
            (s) -> s.withColor(ItemMediaHolder.HEX_COLOR));

        tooltipComponents.add(
            Component.translatable("hexcasting.spelldata.onitem", emphasized).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.hexcasting.creative_unlocker.tooltip", modName).withStyle(ChatFormatting.GRAY));
    }

    private static void collectChildrenRecursively(AdvancementNode root, List<AdvancementNode> out) {
        for (AdvancementNode kiddo : root.children()) {
            out.add(kiddo);
            collectChildrenRecursively(kiddo, out);
        }
    }
}
