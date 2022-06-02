package at.petrak.hexcasting.fabric.interop.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public record EmiBrainsweepRecipe(EmiIngredient blockInput,
								  EmiIngredient villagerInput,
								  EmiStack output,
								  ResourceLocation id) implements EmiRecipe {
	private static final ResourceLocation OVERLAY = modLoc("textures/gui/brainsweep_jei.png");

	@Override
	public EmiRecipeCategory getCategory() {
		return HexEMIPlugin.BRAINSWEEP;
	}

	@Override
	public @Nullable ResourceLocation getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(blockInput, villagerInput);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(output);
	}

	@Override
	public int getDisplayWidth() {
		return 118;
	}

	@Override
	public int getDisplayHeight() {
		return 85;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(OVERLAY, 0, 0, 118, 85, 0, 0, 118, 85, 128, 128);
		widgets.addSlot(blockInput, 11, 34).drawBack(false).custom(null, 0, 0, 19, 19);

		widgets.add(new TheCoolerSlotWidget(villagerInput, 37, 19, 2.75f).useOffset(false).customShift(-8.5f, 2.485f))
				.drawBack(false).custom(null, 0, 0, 27, 49);

		widgets.addSlot(output, 86, 34).drawBack(false).output(true).recipeContext(this).custom(null, 0, 0, 19, 19);
	}
}
