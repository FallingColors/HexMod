package at.petrak.hexcasting.fabric.interop.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Mostly copypasta from EmiWorldRecipe, to avoid depending on impl
public abstract class EmiVillagerRecipe implements EmiRecipe {
	protected final boolean isCatalyst;
	protected final ResourceLocation id;
	protected final EmiIngredient input;
	protected final EmiIngredient catalyst;
	protected final EmiStack result;

	public EmiVillagerRecipe(EmiIngredient input, EmiIngredient catalyst, VillagerEmiStack result, ResourceLocation id) {
		this(input, catalyst, result, id, true);
	}

	public EmiVillagerRecipe(EmiIngredient input, EmiIngredient catalyst, VillagerEmiStack result, ResourceLocation id, boolean isCatalyst) {
		this.isCatalyst = isCatalyst;
		this.input = input;
		this.catalyst = catalyst;
		this.result = result;
		this.id = id;
		if (isCatalyst) {
			for (EmiStack stack : catalyst.getEmiStacks()) {
				stack.setRemainder(stack);
			}
		}

	}

	@Nullable
	public ResourceLocation getId() {
		return this.id;
	}

	public List<EmiIngredient> getInputs() {
		return this.isCatalyst ? List.of(this.input) : List.of(this.input, this.catalyst);
	}

	public List<EmiIngredient> getCatalysts() {
		return this.isCatalyst ? List.of(this.catalyst) : List.of();
	}

	public List<EmiStack> getOutputs() {
		return List.of(this.result);
	}

	public int getDisplayWidth() {
		return 125;
	}

	public int getDisplayHeight() {
		return 18;
	}

	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.PLUS, 27, 3);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
		widgets.addSlot(this.input, 0, 0);
		widgets.addSlot(this.catalyst, 49, 0).catalyst(this.isCatalyst);
		widgets.addSlot(this.result, 107, 0).recipeContext(this);
	}
}
