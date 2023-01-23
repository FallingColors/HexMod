package at.petrak.hexcasting.api.misc;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public enum ScrollQuantity {
    NONE(null, 0),
    FEW(modLoc("inject/scroll_loot_few"), 2),
    SOME(modLoc("inject/scroll_loot_some"), 3),
    MANY(modLoc("inject/scroll_loot_many"), 4);

    private final ResourceLocation pool;
    public final int countRange;

    ScrollQuantity(ResourceLocation pool, int countRange) {
        this.pool = pool;
        this.countRange = countRange;
    }

    @Nullable
    public ResourceLocation getPool() {
        return pool;
    }
}
