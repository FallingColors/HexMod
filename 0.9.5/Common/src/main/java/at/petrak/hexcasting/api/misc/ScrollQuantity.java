package at.petrak.hexcasting.api.misc;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public enum ScrollQuantity {
	NONE(null),
	FEW(modLoc("inject/scroll_loot_few")),
	SOME(modLoc("inject/scroll_loot_some")),
	MANY(modLoc("inject/scroll_loot_many"));

	private final ResourceLocation pool;

	ScrollQuantity(ResourceLocation pool) {
		this.pool = pool;
	}

	@Nullable
	public ResourceLocation getPool() {
		return pool;
	}
}
