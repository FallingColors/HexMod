package at.petrak.hexcasting.fabric.mixin;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.fabric.loot.FabricHexLootModJankery;
import at.petrak.hexcasting.mixin.accessor.AccessorLootTable;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ReloadableServerResources.class)
public class FabricMixinReloadableServerResources {
    // Add the amethyst shard
    @Inject(method = "loadResources", at = @At("RETURN"), cancellable = true)
    private static void onLoadResources(CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir) {
        cir.setReturnValue(cir.getReturnValue().thenApply((rsr) -> {
            var amethystTable = rsr.getLootData().getLootTable(Blocks.AMETHYST_CLUSTER.getLootTable());
            var theCoolerAmethystTable = (AccessorLootTable) amethystTable;
            var oldFuncs = new java.util.ArrayList<>(theCoolerAmethystTable.hex$getFunctions());
            var shardReducer = rsr.getLootData().getElement(new LootDataId<>(LootDataType.MODIFIER, FabricHexLootModJankery.FUNC_AMETHYST_SHARD_REDUCER));
            if (shardReducer != null) {
                oldFuncs.add(shardReducer);
                theCoolerAmethystTable.hex$setFunctions(oldFuncs);
                theCoolerAmethystTable.hex$setCompositeFunction(LootItemFunctions.compose(oldFuncs));
            } else {
                HexAPI.LOGGER.warn("{} was not found?", FabricHexLootModJankery.FUNC_AMETHYST_SHARD_REDUCER);
            }
            return rsr;
        }));
    }
}
