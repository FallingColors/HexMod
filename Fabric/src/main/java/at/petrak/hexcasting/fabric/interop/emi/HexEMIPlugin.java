package at.petrak.hexcasting.fabric.interop.emi;

import at.petrak.hexcasting.api.mod.HexItemTags;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.HexRecipeSerializers;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexEMIPlugin implements EmiPlugin {
	private static final ResourceLocation BRAINSWEEP_ID = modLoc("brainsweep");

	private static final ResourceLocation SIMPLIFIED_ICON = modLoc("textures/gui/brainsweep_emi.png");

	public static final EmiRecipeCategory BRAINSWEEP = new EmiRecipeCategory(BRAINSWEEP_ID,
					new PatternRendererEMI(BRAINSWEEP_ID, 16, 16), (matrices, x, y, delta) -> {
		RenderSystem.setShaderTexture(0, SIMPLIFIED_ICON);
		GuiComponent.blit(matrices, x, y, 0, 0, 16, 16, 16, 16);
	});

	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(BRAINSWEEP);
		registry.addWorkstation(BRAINSWEEP, EmiIngredient.of(HexItemTags.WANDS));

		for (BrainsweepRecipe recipe : registry.getRecipeManager().getAllRecipesFor(HexRecipeSerializers.BRAINSWEEP_TYPE)) {
			var inputs = EmiIngredient.of(recipe.blockIn().getDisplayedStacks().stream()
					.map(EmiStack::of).toList());
			var villagerInput = new VillagerEmiStack(recipe.villagerIn());
			var villagerOutput = new VillagerEmiStack(recipe.villagerIn(), true);
			villagerInput.setRemainder(villagerOutput);
			var output = EmiStack.of(recipe.result().getBlock());
			registry.addRecipe(new EmiBrainsweepRecipe(inputs, villagerInput, output, recipe.getId()));
		}
	}

}
