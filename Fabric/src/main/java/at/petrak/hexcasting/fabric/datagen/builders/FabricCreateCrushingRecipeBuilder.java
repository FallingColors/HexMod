package at.petrak.hexcasting.fabric.datagen.builders;

import at.petrak.hexcasting.datagen.recipe.builders.CreateCrushingRecipeBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

import java.util.ArrayList;
import java.util.List;

public class FabricCreateCrushingRecipeBuilder extends CreateCrushingRecipeBuilder {
	private final List<ConditionJsonProvider> conditions = new ArrayList<>();

	public FabricCreateCrushingRecipeBuilder whenModLoaded(String modid) {
		return withCondition(DefaultResourceConditions.anyModLoaded(modid));
	}

	public FabricCreateCrushingRecipeBuilder whenModMissing(String modid) {
		return withCondition(DefaultResourceConditions.not(DefaultResourceConditions.anyModLoaded(modid)));
	}

	public FabricCreateCrushingRecipeBuilder withCondition(ConditionJsonProvider condition) {
		conditions.add(condition);
		return this;
	}

	@Override
	public void serializeConditions(JsonObject object) {
		if (!conditions.isEmpty()) {
			JsonArray conds = new JsonArray();
			conditions.forEach(c -> conds.add(c.toJson()));
			object.add(ResourceConditions.CONDITIONS_KEY, conds);
		}
	}
}
