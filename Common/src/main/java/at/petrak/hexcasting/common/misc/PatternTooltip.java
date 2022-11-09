package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * Used for displaying patterns on the tooltips for scrolls and slates.
 */
public record PatternTooltip(HexPattern pattern, ResourceLocation background) implements TooltipComponent {
}
