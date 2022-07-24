package at.petrak.hexcasting.forge.datagen.builders;

import at.petrak.hexcasting.datagen.recipe.builders.CreateCrushingRecipeBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;

import java.util.ArrayList;
import java.util.List;

public class ForgeCreateCrushingRecipeBuilder extends CreateCrushingRecipeBuilder {
	private final List<ICondition> conditions = new ArrayList<>();

	@Override
	public ForgeCreateCrushingRecipeBuilder whenModLoaded(String modid) {
		return withCondition(new ModLoadedCondition(modid));
	}

	@Override
	public ForgeCreateCrushingRecipeBuilder whenModMissing(String modid) {
		return withCondition(new NotCondition(new ModLoadedCondition(modid)));
	}

	public ForgeCreateCrushingRecipeBuilder withCondition(ICondition condition) {
		conditions.add(condition);
		return this;
	}

	@Override
	public void serializeConditions(JsonObject object) {
		if (!conditions.isEmpty()) {
			JsonArray conds = new JsonArray();
			conditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
			object.add("conditions", conds);
		}
	}
}
