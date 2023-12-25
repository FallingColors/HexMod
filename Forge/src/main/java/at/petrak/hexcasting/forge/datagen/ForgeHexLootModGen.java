package at.petrak.hexcasting.forge.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.loot.HexLootHandler;
import at.petrak.hexcasting.forge.loot.ForgeHexAmethystLootMod;
import at.petrak.hexcasting.forge.loot.ForgeHexLoreLootMod;
import at.petrak.hexcasting.forge.loot.ForgeHexScrollLootMod;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

public class ForgeHexLootModGen extends GlobalLootModifierProvider {
    public ForgeHexLootModGen(PackOutput output) {
        super(output, HexAPI.MOD_ID);
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
                LootTableIdCondition.builder(injection).build(),
            }, HexLootHandler.DEFAULT_LORE_CHANCE));
        }

        add("amethyst_cluster", new ForgeHexAmethystLootMod(new LootItemCondition[]{
            LootTableIdCondition.builder(Blocks.AMETHYST_CLUSTER.getLootTable()).build()
        }, HexLootHandler.DEFAULT_SHARD_MODIFICATION));

    }
}
