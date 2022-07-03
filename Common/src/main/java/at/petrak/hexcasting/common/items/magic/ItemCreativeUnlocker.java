package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.misc.ManaConstants;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemCreativeUnlocker extends Item implements MediaHolderItem {

    public static boolean isDebug(ItemStack stack) {
        return stack.is(HexItems.CREATIVE_UNLOCKER)
            && stack.hasCustomHoverName()
            && stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains("debug");
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

    private static final String TAG_EXTRACTIONS = "extractions";

    public ItemCreativeUnlocker(Properties properties) {
        super(properties);
    }

    @Override
    public int getMedia(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxMedia(ItemStack stack) {
        return Integer.MAX_VALUE - 1;
    }

    @Override
    public void setMedia(ItemStack stack, int media) {
        // NO-OP
    }

    @Override
    public boolean canProvideMedia(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canRecharge(ItemStack stack) {
        return false;
    }

    @Override
    public int withdrawMana(ItemStack stack, int cost, boolean simulate) {
        if (!simulate && isDebug(stack)) {
            int[] arr = NBTHelper.getIntArray(stack, TAG_EXTRACTIONS);
            if (arr == null) {
                arr = new int[0];
            }
            int[] newArr = Arrays.copyOf(arr, arr.length + 1);
            newArr[newArr.length - 1] = cost;
            NBTHelper.putIntArray(stack, TAG_EXTRACTIONS, newArr);
        }

        return cost < 0 ? 1 : cost;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || isDebug(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (isDebug(stack) && !level.isClientSide) {
            int[] arr = NBTHelper.getIntArray(stack, TAG_EXTRACTIONS);
            if (arr != null) {
                NBTHelper.remove(stack, TAG_EXTRACTIONS);
                for (int i : arr) {
                    if (i < 0) {
                        entity.sendMessage(new TranslatableComponent("hexcasting.debug.mana_withdrawn",
                            stack.getDisplayName(),
                            new TranslatableComponent("hexcasting.debug.all_mana").withStyle(ChatFormatting.GRAY))
                            .withStyle(ChatFormatting.LIGHT_PURPLE), Util.NIL_UUID);
                    } else {
                        entity.sendMessage(new TranslatableComponent("hexcasting.debug.mana_withdrawn.with_dust",
                            stack.getDisplayName(),
                            new TextComponent("" + i).withStyle(ChatFormatting.WHITE),
                            new TextComponent(String.format("%.2f", i * 1.0 / ManaConstants.DUST_UNIT)).withStyle(
                                ChatFormatting.WHITE))
                            .withStyle(ChatFormatting.LIGHT_PURPLE), Util.NIL_UUID);
                    }
                }
            }
        }
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

    private static final TextColor HEX_COLOR = TextColor.fromRgb(0xb38ef3);

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
        String prefix = "item.hexcasting.creative_unlocker.";

        Component emphasized = infiniteMedia(level);

        MutableComponent modName = new TranslatableComponent(prefix + "mod_name").withStyle(
            (s) -> s.withColor(HEX_COLOR));

        tooltipComponents.add(
            new TranslatableComponent(prefix + "tooltip.0", emphasized).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(new TranslatableComponent(prefix + "tooltip.1", modName).withStyle(ChatFormatting.GRAY));
    }

    private static void addChildren(Advancement root, List<Advancement> out) {
        for (Advancement kiddo : root.getChildren()) {
            out.add(kiddo);
            addChildren(kiddo, out);
        }
    }
}
