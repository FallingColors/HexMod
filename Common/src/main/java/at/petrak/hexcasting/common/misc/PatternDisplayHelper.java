package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.PatternShapeMatch;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fallback pattern display when Inline mod is not present.
 * Provides pattern name resolution and display components without Inline dependency.
 */
public final class PatternDisplayHelper {

    /**
     * Get the display name for a pattern (e.g. "Great Spells" for a known action).
     */
    @NotNull
    public static Component getPatternName(@NotNull HexPattern pattern) {
        try {
            PatternShapeMatch shapeMatch = PatternRegistryManifest.matchPattern(pattern, null, false);
            if (shapeMatch instanceof PatternShapeMatch.Normal normMatch) {
                return HexAPI.instance().getActionI18n(normMatch.key, false);
            }
            if (shapeMatch instanceof PatternShapeMatch.Special specialMatch) {
                return HexAPI.instance().getSpecialHandlerI18n(specialMatch.key);
            }
        } catch (Exception e) {
            // nop
        }
        return PatternIota.displayNonInline(pattern);
    }

    /**
     * Get a component for displaying a pattern in tooltips/item names.
     * When withHoverAndClick is true, adds hover (scroll preview) and click-to-copy.
     */
    @NotNull
    public static Component getDisplayComponent(@NotNull HexPattern pattern, boolean withHoverAndClick) {
        MutableComponent text = Component.literal(pattern.toString()).withStyle(ChatFormatting.WHITE);
        if (withHoverAndClick) {
            ItemStack scrollStack = new ItemStack(HexItems.SCROLL_MEDIUM);
            HexItems.SCROLL_MEDIUM.writeDatum(scrollStack, new PatternIota(pattern));
            scrollStack.set(DataComponents.CUSTOM_NAME, getPatternName(pattern).copy().withStyle(ChatFormatting.WHITE));
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(scrollStack));
            ClickEvent ce = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, pattern.toString());
            text = text.withStyle(s -> s.withHoverEvent(he).withClickEvent(ce));
        }
        return text;
    }
}
