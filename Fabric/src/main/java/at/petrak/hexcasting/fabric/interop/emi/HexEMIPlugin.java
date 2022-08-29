package at.petrak.hexcasting.fabric.interop.emi;

import at.petrak.hexcasting.api.mod.HexItemTags;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.HexRecipeSerializers;
import at.petrak.hexcasting.common.recipe.ingredient.VillagerIngredient;
import at.petrak.hexcasting.interop.utils.PhialRecipeStackBuilder;
import at.petrak.hexcasting.mixin.accessor.AccessorPoiType;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexEMIPlugin implements EmiPlugin {
	private static final ResourceLocation BRAINSWEEP_ID = modLoc("brainsweep");
	public static final ResourceLocation PHIAL_ID = modLoc("craft/battery");
	public static final ResourceLocation EDIFY_ID = modLoc("edify");
	private static final ResourceLocation VILLAGER_LEVELING_ID = modLoc("villager_leveling");
	private static final ResourceLocation VILLAGER_PROFESSION_ID = modLoc("villager_profession");

	private static final ResourceLocation SIMPLIFIED_ICON_BRAINSWEEP = modLoc("textures/gui/brainsweep_emi.png");
	private static final ResourceLocation SIMPLIFIED_ICON_PHIAL = modLoc("textures/gui/phial_emi.png");
	private static final ResourceLocation SIMPLIFIED_ICON_EDIFY = modLoc("textures/gui/edify_emi.png");
	private static final ResourceLocation SIMPLIFIED_ICON_LEVELING = modLoc("textures/gui/villager_leveling.png");
	private static final ResourceLocation SIMPLIFIED_ICON_PROFESSION = modLoc("textures/gui/villager_profession.png");

	public static final EmiRecipeCategory BRAINSWEEP = new EmiRecipeCategory(BRAINSWEEP_ID,
		new PatternRendererEMI(BRAINSWEEP_ID, 16, 16),
		new EmiTexture(SIMPLIFIED_ICON_BRAINSWEEP, 0, 0, 16, 16, 16, 16, 16, 16));

	public static final EmiRecipeCategory PHIAL = new EmiRecipeCategory(PHIAL_ID,
		new PatternRendererEMI(PHIAL_ID, 12, 12).shift(2, 2),
		new EmiTexture(SIMPLIFIED_ICON_PHIAL, 0, 0, 16, 16, 16, 16, 16, 16));

	public static final EmiRecipeCategory EDIFY = new EmiRecipeCategory(EDIFY_ID,
		new PatternRendererEMI(EDIFY_ID, 16, 16).strokeOrder(false),
		new EmiTexture(SIMPLIFIED_ICON_EDIFY, 0, 0, 16, 16, 16, 16, 16, 16));

	public static final EmiRecipeCategory VILLAGER_LEVELING = new EmiRecipeCategory(VILLAGER_LEVELING_ID,
		EmiStack.of(Items.EMERALD),
		new EmiTexture(SIMPLIFIED_ICON_LEVELING, 0, 0, 16, 16, 16, 16, 16, 16));

	public static final EmiRecipeCategory VILLAGER_PROFESSION = new EmiRecipeCategory(VILLAGER_PROFESSION_ID,
		EmiStack.of(Blocks.LECTERN),
		new EmiTexture(SIMPLIFIED_ICON_PROFESSION, 0, 0, 16, 16, 16, 16, 16, 16));

	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(BRAINSWEEP);
		registry.addCategory(PHIAL);
		registry.addCategory(EDIFY);
		registry.addCategory(VILLAGER_LEVELING);
		registry.addCategory(VILLAGER_PROFESSION);
		registry.addWorkstation(BRAINSWEEP, EmiIngredient.of(HexItemTags.WANDS));
		registry.addWorkstation(PHIAL, EmiIngredient.of(HexItemTags.WANDS));
		registry.addWorkstation(EDIFY, EmiIngredient.of(HexItemTags.WANDS));

		for (BrainsweepRecipe recipe : registry.getRecipeManager().getAllRecipesFor(HexRecipeSerializers.BRAINSWEEP_TYPE)) {
			var inputs = EmiIngredient.of(recipe.blockIn().getDisplayedStacks().stream()
				.map(EmiStack::of).toList());
			var villagerInput = VillagerEmiStack.atLevelOrHigher(recipe.villagerIn(), true);
			var output = EmiStack.of(recipe.result().getBlock());
			registry.addRecipe(new EmiBrainsweepRecipe(inputs, villagerInput, output, recipe.getId()));
		}

		if (PhialRecipeStackBuilder.shouldAddRecipe()) {
			registry.addRecipe(new EmiPhialRecipe());
		}

		registry.addRecipe(new EmiEdifyRecipe());

		var basicVillager = new VillagerIngredient(null, null, 1);

		for (VillagerProfession profession : Registry.VILLAGER_PROFESSION) {
			ResourceLocation id = Registry.VILLAGER_PROFESSION.getKey(profession);
			ResourceLocation poiRecipeId = modLoc("villager/poi/" + id.getNamespace() + "/" + id.getPath());
			var manWithJob = new VillagerIngredient(id, null, 1);

			PoiType poi = profession.getJobPoiType();
			Set<BlockState> states = ((AccessorPoiType) poi).hex$matchingStates();
			if (!states.isEmpty()) {
				List<Item> workstations = states.stream()
					.map(BlockState::getBlock)
					.map(Block::asItem)
					.distinct()
					.filter((it) -> it != Items.AIR)
					.toList();

				if (!workstations.isEmpty()) {
					registry.addWorkstation(VILLAGER_LEVELING, EmiIngredient.of(workstations.stream().map(EmiStack::of).toList()));
					registry.addWorkstation(VILLAGER_PROFESSION, EmiIngredient.of(workstations.stream().map(EmiStack::of).toList()));
					registry.addRecipe(new EmiProfessionRecipe(new VillagerEmiStack(basicVillager),
						EmiIngredient.of(workstations.stream().map(EmiStack::of).toList()),
						new VillagerEmiStack(manWithJob), poiRecipeId));

					for (int lvl = 1; lvl < 5; lvl++) {
						ResourceLocation levelRecipeId = modLoc("villager/levelup/" + lvl + "/" + id.getNamespace() + "/" + id.getPath());
						var manWithBadJob = new VillagerIngredient(id, null, lvl);
						var manWithBetterJob = new VillagerIngredient(id, null, lvl + 1);
						registry.addRecipe(new EmiLevelupRecipe(new VillagerEmiStack(manWithBadJob), new VillagerEmiStack(manWithBetterJob), levelRecipeId));
					}
				}
			}
		}
	}

}
