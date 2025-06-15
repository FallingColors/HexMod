package at.petrak.hexcasting.forge.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.loot.HexLootHandler;
import at.petrak.hexcasting.forge.loot.ForgeHexAmethystLootMod;
import at.petrak.hexcasting.forge.loot.ForgeHexLoreLootMod;
import at.petrak.hexcasting.forge.loot.ForgeHexScrollLootMod;
import at.petrak.hexcasting.forge.loot.ForgeHexCypherLootMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public class ForgeHexLootModGen extends GlobalLootModifierProvider {
    public ForgeHexLootModGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, HexAPI.MOD_ID);
    }

    @Override
    protected void start() {
        for (var injection : HexLootHandler.DEFAULT_SCROLL_INJECTS) {
            var name = "scroll/%s/%s".formatted(injection.injectee().getNamespace(), injection.injectee().getPath());
            add(name, new ForgeHexScrollLootMod(new LootItemCondition[]{
                LootTableIdCondition.builder(injection.injectee()).build(),
            }, injection.countRange()));
        }

        for (var injection : HexLootHandler.DEFAULT_LORE_INJECTS) {
            var name = "lore/%s/%s".formatted(injection.getNamespace(), injection.getPath());
            add(name, new ForgeHexLoreLootMod(new LootItemCondition[]{
                LootTableIdCondition.builder(injection).build()
            }, HexLootHandler.DEFAULT_LORE_CHANCE));
        }

        for (var injection : HexLootHandler.DEFAULT_CYPHER_INJECTS) {
            var name = "cypher/%s/%s".formatted(injection.getNamespace(), injection.getPath());
            add(name, new ForgeHexCypherLootMod(new LootItemCondition[]{
                LootTableIdCondition.builder(injection).build()
            }, HexLootHandler.DEFAULT_CYPHER_CHANCE));
        }

        add("amethyst_cluster", new ForgeHexAmethystLootMod(new LootItemCondition[]{
            LootTableIdCondition.builder(Blocks.AMETHYST_CLUSTER.getLootTable().location()).build()
        }, HexLootHandler.DEFAULT_SHARD_MODIFICATION));
    }
}
