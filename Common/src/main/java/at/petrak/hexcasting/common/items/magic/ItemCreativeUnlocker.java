package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.item.ManaHolderItem;
import at.petrak.hexcasting.api.misc.DiscoveryHandlers;
import at.petrak.hexcasting.api.misc.ManaConstants;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemCreativeUnlocker extends Item implements ManaHolderItem {

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

        DiscoveryHandlers.addManaHolderDiscoverer(harness -> {
            var player = harness.getCtx().getCaster();
            if (!player.isCreative())
                return List.of();

            ItemStack stack = DiscoveryHandlers.findDebugItem(player, DISPLAY_MEDIA);
            if (!stack.isEmpty())
                return List.of(new DebugUnlockerHolder(stack));

            return List.of();
        });
    }

    public static boolean isDebug(ItemStack stack) {
        return isDebug(stack, null);
    }

    public static boolean isDebug(ItemStack stack, String flag) {
        if (!stack.is(HexItems.CREATIVE_UNLOCKER) || !stack.hasCustomHoverName()) {
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
        MutableComponent emphasized = new TextComponent("");
        for (int i = 0; i < emphasis.length(); i++) {
            emphasized.append(rainbow(new TextComponent("" + emphasis.charAt(i)), i, level));
        }

        return emphasized;
    }

    public static final String TAG_EXTRACTIONS = "extractions";
    public static final String TAG_INSERTIONS = "insertions";

    public ItemCreativeUnlocker(Properties properties) {
        super(properties);
    }

    @Override
    public int getMana(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxMana(ItemStack stack) {
        return Integer.MAX_VALUE - 1;
    }

    @Override
    public void setMana(ItemStack stack, int mana) {
        // NO-OP
    }

    @Override
    public boolean manaProvider(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canRecharge(ItemStack stack) {
        return true;
    }

    public static void addToIntArray(ItemStack stack, String tag, int n) {
        int[] arr = NBTHelper.getIntArray(stack, tag);
        if (arr == null) {
            arr = new int[0];
        }
        int[] newArr = Arrays.copyOf(arr, arr.length + 1);
        newArr[newArr.length - 1] = n;
        NBTHelper.putIntArray(stack, tag, newArr);
    }

    @Override
    public int withdrawMana(ItemStack stack, int cost, boolean simulate) {
        // In case it's withdrawn through other means
        if (!simulate && isDebug(stack, DISPLAY_MEDIA)) {
            addToIntArray(stack, TAG_EXTRACTIONS, cost);
        }

        return cost < 0 ? getMana(stack) : cost;
    }

    @Override
    public int insertMana(ItemStack stack, int amount, boolean simulate) {
        // In case it's inserted through other means
        if (!simulate && isDebug(stack, DISPLAY_MEDIA)) {
            addToIntArray(stack, TAG_INSERTIONS, amount);
        }

        return amount < 0 ? getMaxMana(stack) : amount;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || isDebug(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (isDebug(stack, DISPLAY_MEDIA) && !level.isClientSide) {
            debugDisplay(stack, TAG_EXTRACTIONS, "withdrawn", "all_mana", entity);
            debugDisplay(stack, TAG_INSERTIONS, "inserted", "infinite_mana", entity);
        }
    }

    private void debugDisplay(ItemStack stack, String tag, String langKey, String allKey, Entity entity) {
        int[] arr = NBTHelper.getIntArray(stack, tag);
        if (arr != null) {
            NBTHelper.remove(stack, tag);
            for (int i : arr) {
                if (i < 0) {
                    entity.sendMessage(new TranslatableComponent("hexcasting.debug.mana_" + langKey,
                        stack.getDisplayName(),
                        new TranslatableComponent("hexcasting.debug." + allKey).withStyle(ChatFormatting.GRAY))
                        .withStyle(ChatFormatting.LIGHT_PURPLE), Util.NIL_UUID);
                } else {
                    entity.sendMessage(new TranslatableComponent("hexcasting.debug.mana_" + langKey + ".with_dust",
                        stack.getDisplayName(),
                        new TextComponent("" + i).withStyle(ChatFormatting.WHITE),
                        new TextComponent(String.format("%.2f", i * 1.0 / ManaConstants.DUST_UNIT)).withStyle(
                            ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.LIGHT_PURPLE), Util.NIL_UUID);
                }
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (be instanceof BlockEntityAbstractImpetus impetus) {
            impetus.setInfiniteMana();
            context.getLevel().playSound(null, context.getClickedPos(), HexSounds.SPELL_CIRCLE_FIND_BLOCK, SoundSource.PLAYERS, 1f, 1f);
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (level instanceof ServerLevel slevel && consumer instanceof ServerPlayer player) {
            var rootAdv = slevel.getServer().getAdvancements().getAdvancement(modLoc("root"));
            if (rootAdv != null) {
                var children = new ArrayList<Advancement>();
                children.add(rootAdv);
                addChildren(rootAdv, children);

                var adman = player.getAdvancements();

                for (var kid : children) {
                    var progress = adman.getOrStartProgress(kid);
                    if (!progress.isDone()) {
                        for (String crit : progress.getRemainingCriteria()) {
                            adman.award(kid, crit);
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
        TooltipFlag isAdvanced) {
        Component emphasized = infiniteMedia(level);

        MutableComponent modName = new TranslatableComponent("item.hexcasting.creative_unlocker.mod_name").withStyle(
            (s) -> s.withColor(ItemManaHolder.HEX_COLOR));

        tooltipComponents.add(
            new TranslatableComponent("hexcasting.spelldata.onitem", emphasized).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(new TranslatableComponent("item.hexcasting.creative_unlocker.tooltip", modName).withStyle(ChatFormatting.GRAY));
    }

    private static void addChildren(Advancement root, List<Advancement> out) {
        for (Advancement kiddo : root.getChildren()) {
            out.add(kiddo);
            addChildren(kiddo, out);
        }
    }
}
