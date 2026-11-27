package at.petrak.hexcasting.fabric.mixin;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.fabric.loot.FabricHexLootModJankery;
import at.petrak.hexcasting.mixin.accessor.AccessorLootTable;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Mixin(ReloadableServerResources.class)
public class FabricMixinReloadableServerResources {
    // Add the amethyst shard
    @ModifyReturnValue(method = "loadResources", at = @At("RETURN"))
    private static CompletableFuture<ReloadableServerResources> onLoadResources(CompletableFuture<ReloadableServerResources> future) {
        return future.thenApply((rsr) -> {
            var amethystTable = rsr.getLootData().getLootTable(Blocks.AMETHYST_CLUSTER.getLootTable());
            var theCoolerAmethystTable = (AccessorLootTable) amethystTable;
            var oldFuncs = theCoolerAmethystTable.hex$getFunctions();
            var newFuncs = Arrays.copyOf(oldFuncs, oldFuncs.length + 1);
            var shardReducer = rsr.getLootData().getElement(new LootDataId<>(LootDataType.MODIFIER, FabricHexLootModJankery.FUNC_AMETHYST_SHARD_REDUCER));
            if (shardReducer != null) {
                newFuncs[newFuncs.length - 1] = shardReducer;
                theCoolerAmethystTable.hex$setFunctions(newFuncs);
                theCoolerAmethystTable.hex$setCompositeFunction(LootItemFunctions.compose(newFuncs));
            } else {
                HexAPI.LOGGER.warn("{} was not found?", FabricHexLootModJankery.FUNC_AMETHYST_SHARD_REDUCER);
            }
            return rsr;
        });
    }
}
