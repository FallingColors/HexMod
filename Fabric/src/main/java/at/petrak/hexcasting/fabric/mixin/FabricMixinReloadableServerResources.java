package at.petrak.hexcasting.fabric.mixin;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.fabric.loot.FabricHexLootModJankery;
import at.petrak.hexcasting.mixin.accessor.AccessorLootTable;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Mixin(ReloadableServerResources.class)
public class FabricMixinReloadableServerResources {
    // Add the amethyst shard
}
