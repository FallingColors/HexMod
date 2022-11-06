package at.petrak.hexcasting.datagen.recipe.builders;

import com.google.gson.JsonObject;

public record CompatProcessingOutput(String name, int count, float chance) implements ProcessingOutput {
	@Override
	public JsonObject serialize() {
		JsonObject json = new JsonObject();
		json.addProperty("item", name);
		if (count != 1) {
			json.addProperty("count", count);
		}
		if (chance != 1) {
			json.addProperty("chance", chance);
		}
		return json;
	}
}
