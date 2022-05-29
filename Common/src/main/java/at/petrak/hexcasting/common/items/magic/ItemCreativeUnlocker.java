package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.item.ManaHolderItem;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemCreativeUnlocker extends Item implements ManaHolderItem {
    public ItemCreativeUnlocker(Properties properties) {
        super(properties);
    }

    @Override
    public int getMana(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxMana(ItemStack stack) {
        return Integer.MAX_VALUE;
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
        return false;
    }

    @Override
    public int withdrawMana(ItemStack stack, int cost, boolean simulate) {
        return cost < 0 ? 1 : cost;
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

    private static Style rainbow(Style style, Level level) {
        if (level == null) {
            return style.withColor(ChatFormatting.WHITE);
        }

        return style.withColor(TextColor.fromRgb(Mth.hsvToRgb(level.getGameTime() * 2 % 360 / 360F, 1F, 1F)));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
        TooltipFlag isAdvanced) {
        String prefix = "item.hexcasting.creative_unlocker.";
        tooltipComponents.add(new TranslatableComponent(prefix + "tooltip.0",
                new TranslatableComponent(prefix + "for_emphasis").withStyle((s) -> rainbow(s, level)))
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(new TranslatableComponent(prefix + "tooltip.1",
                new TranslatableComponent(prefix + "mod_name").withStyle((s) -> s.withColor(HEX_COLOR)))
                .withStyle(ChatFormatting.GRAY));
    }

    private static void addChildren(Advancement root, List<Advancement> out) {
        for (Advancement kiddo : root.getChildren()) {
            out.add(kiddo);
            addChildren(kiddo, out);
        }
    }
}
