package at.petrak.hexcasting.mixin;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.loot.HexLootHandler;
import at.petrak.hexcasting.mixin.accessor.AccessorLootTable;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Mixin(ReloadableServerResources.class)
public class MixinReloadableServerResources {
    @Inject(method = "loadResources", at = @At("RETURN"), cancellable = true)
    private static void onLoadResources(CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir) {
        cir.setReturnValue(cir.getReturnValue().thenApply((rsr) -> {
            var amethystTable = rsr.getLootTables().get(Blocks.AMETHYST_CLUSTER.getLootTable());
            var theCoolerAmethystTable = (AccessorLootTable) amethystTable;
            var oldFuncs = theCoolerAmethystTable.hex$getFunctions();
            var newFuncs = Arrays.copyOf(oldFuncs, oldFuncs.length + 1);
            var shardReducer = rsr.getItemModifierManager().get(HexLootHandler.FUNC_AMETHYST_SHARD_REDUCER);
            if (shardReducer != null) {
                newFuncs[newFuncs.length - 1] = shardReducer;
                theCoolerAmethystTable.hex$setFunctions(newFuncs);
                theCoolerAmethystTable.hex$setCompositeFunction(LootItemFunctions.compose(newFuncs));
            } else {
                HexAPI.LOGGER.warn("{} was not found?", HexLootHandler.FUNC_AMETHYST_SHARD_REDUCER);
            }
            return rsr;
        }));
    }
}
