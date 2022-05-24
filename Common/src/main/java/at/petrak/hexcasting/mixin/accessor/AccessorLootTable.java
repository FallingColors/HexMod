package at.petrak.hexcasting.mixin.accessor;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootTable.class)
public interface AccessorLootTable {
    @Accessor("functions")
    LootItemFunction[] hex$getFunctions();

    @Accessor("functions")
    @Mutable
    void hex$setFunctions(LootItemFunction[] lifs);
}
