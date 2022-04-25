package at.petrak.hexcasting.api.mod;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ConstantConditions")
@ApiStatus.Internal
@ObjectHolder("hexcasting")
public class HexApiItems {

	@NotNull
	@ObjectHolder("dye_colorizer_white")
	public static final Item COLORIZER_WHITE = null;

	@NotNull
	@ObjectHolder("dye_colorizer_orange")
	public static final Item COLORIZER_ORANGE = null;

	@NotNull
	@ObjectHolder("dye_colorizer_magenta")
	public static final Item COLORIZER_MAGENTA = null;

	@NotNull
	@ObjectHolder("dye_colorizer_light_blue")
	public static final Item COLORIZER_LIGHT_BLUE = null;

	@NotNull
	@ObjectHolder("dye_colorizer_yellow")
	public static final Item COLORIZER_YELLOW = null;

	@NotNull
	@ObjectHolder("dye_colorizer_lime")
	public static final Item COLORIZER_LIME = null;

	@NotNull
	@ObjectHolder("dye_colorizer_pink")
	public static final Item COLORIZER_PINK = null;

	@NotNull
	@ObjectHolder("dye_colorizer_gray")
	public static final Item COLORIZER_GRAY = null;

	@NotNull
	@ObjectHolder("dye_colorizer_light_gray")
	public static final Item COLORIZER_LIGHT_GRAY = null;

	@NotNull
	@ObjectHolder("dye_colorizer_cyan")
	public static final Item COLORIZER_CYAN = null;

	@NotNull
	@ObjectHolder("dye_colorizer_purple")
	public static final Item COLORIZER_PURPLE = null;

	@NotNull
	@ObjectHolder("dye_colorizer_blue")
	public static final Item COLORIZER_BLUE = null;

	@NotNull
	@ObjectHolder("dye_colorizer_brown")
	public static final Item COLORIZER_BROWN = null;

	@NotNull
	@ObjectHolder("dye_colorizer_green")
	public static final Item COLORIZER_GREEN = null;

	@NotNull
	@ObjectHolder("dye_colorizer_red")
	public static final Item COLORIZER_RED = null;

	@NotNull
	@ObjectHolder("dye_colorizer_black")
	public static final Item COLORIZER_BLACK = null;

	@NotNull
	@ObjectHolder("amethyst_dust")
	public static final Item AMETHYST_DUST = null;

	public static Item getColorizer(DyeColor color) {
		return switch (color) {
			case WHITE -> COLORIZER_WHITE;
			case ORANGE -> COLORIZER_ORANGE;
			case MAGENTA -> COLORIZER_MAGENTA;
			case LIGHT_BLUE -> COLORIZER_LIGHT_BLUE;
			case YELLOW -> COLORIZER_YELLOW;
			case LIME -> COLORIZER_LIME;
			case PINK -> COLORIZER_PINK;
			case GRAY -> COLORIZER_GRAY;
			case LIGHT_GRAY -> COLORIZER_LIGHT_GRAY;
			case CYAN -> COLORIZER_CYAN;
			case PURPLE -> COLORIZER_PURPLE;
			case BLUE -> COLORIZER_BLUE;
			case BROWN -> COLORIZER_BROWN;
			case GREEN -> COLORIZER_GREEN;
			case RED -> COLORIZER_RED;
			default -> COLORIZER_BLACK;
		};
	}
}
