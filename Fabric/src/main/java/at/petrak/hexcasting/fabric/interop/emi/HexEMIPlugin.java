package at.petrak.hexcasting.fabric.interop.emi;

import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry;
import at.petrak.hexcasting.interop.utils.PhialRecipeStackBuilder;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexEMIPlugin implements EmiPlugin {
	private static final ResourceLocation BRAINSWEEP_ID = modLoc("brainsweep");
	public static final ResourceLocation PHIAL_ID = modLoc("craft/battery");
	public static final ResourceLocation EDIFY_ID = modLoc("edify");

	private static final ResourceLocation SIMPLIFIED_ICON_BRAINSWEEP = modLoc("textures/gui/brainsweep_emi.png");
	private static final ResourceLocation SIMPLIFIED_ICON_PHIAL = modLoc("textures/gui/phial_emi.png");
	private static final ResourceLocation SIMPLIFIED_ICON_EDIFY = modLoc("textures/gui/edify_emi.png");

	public static final EmiRecipeCategory BRAINSWEEP = new EmiRecipeCategory(BRAINSWEEP_ID,
		new PatternRendererEMI(BRAINSWEEP_ID, 16, 16),
		new EmiTexture(SIMPLIFIED_ICON_BRAINSWEEP, 0, 0, 16, 16, 16, 16, 16, 16));

	public static final EmiRecipeCategory PHIAL = new EmiRecipeCategory(PHIAL_ID,
		new PatternRendererEMI(PHIAL_ID, 12, 12).shift(2, 2),
		new EmiTexture(SIMPLIFIED_ICON_PHIAL, 0, 0, 16, 16, 16, 16, 16, 16));

	public static final EmiRecipeCategory EDIFY = new EmiRecipeCategory(EDIFY_ID,
		new PatternRendererEMI(EDIFY_ID, 16, 16).strokeOrder(false),
		new EmiTexture(SIMPLIFIED_ICON_EDIFY, 0, 0, 16, 16, 16, 16, 16, 16));

	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(BRAINSWEEP);
		registry.addCategory(PHIAL);
		registry.addCategory(EDIFY);
		registry.addWorkstation(BRAINSWEEP, EmiIngredient.of(HexTags.Items.STAVES));
		registry.addWorkstation(PHIAL, EmiIngredient.of(HexTags.Items.STAVES));
		registry.addWorkstation(EDIFY, EmiIngredient.of(HexTags.Items.STAVES));

		for (BrainsweepRecipe recipe : registry.getRecipeManager()
			.getAllRecipesFor(HexRecipeStuffRegistry.BRAINSWEEP_TYPE)) {
			var inputBlocks = EmiIngredient.of(recipe.blockIn().getDisplayedStacks().stream()
				.map(EmiStack::of).toList());
			var inputEntity = new BrainsweepeeEmiStack(recipe.entityIn());
			var output = EmiStack.of(recipe.result().getBlock());
			registry.addRecipe(new EmiBrainsweepRecipe(inputBlocks, inputEntity, output, recipe.getId()));
		}

		if (PhialRecipeStackBuilder.shouldAddRecipe()) {
			registry.addRecipe(new EmiPhialRecipe());
		}

		registry.addRecipe(new EmiEdifyRecipe());
	}
}
