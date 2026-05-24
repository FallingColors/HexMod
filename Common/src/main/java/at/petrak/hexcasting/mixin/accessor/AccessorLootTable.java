package at.petrak.hexcasting.mixin.accessor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.function.BiFunction;

@Mixin(LootTable.class)
public interface AccessorLootTable {
    @Accessor("functions")
    List<LootItemFunction> hex$getFunctions();

    @Accessor("functions")
    @Mutable
    void hex$setFunctions(List<LootItemFunction> lifs);

    @Accessor("compositeFunction")
    @Mutable
    void hex$setCompositeFunction(BiFunction<ItemStack, LootContext, ItemStack> bf);
}
