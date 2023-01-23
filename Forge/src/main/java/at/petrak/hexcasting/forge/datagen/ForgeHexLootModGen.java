package at.petrak.hexcasting.forge.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.loot.HexLootHandler;
import at.petrak.hexcasting.forge.loot.ForgeHexAmethystLootMod;
import at.petrak.hexcasting.forge.loot.ForgeHexScrollLootMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

public class ForgeHexLootModGen extends GlobalLootModifierProvider {
    public ForgeHexLootModGen(DataGenerator gen) {
        super(gen, HexAPI.MOD_ID);
    }

    @Override
    protected void start() {
        for (var injection : HexLootHandler.DEFAULT_SCROLL_INJECTS) {
            var name = "scroll/%s/%s".formatted(injection.injectee().getNamespace(), injection.injectee().getPath());
            add(name, new ForgeHexScrollLootMod(new LootItemCondition[]{
                LootTableIdCondition.builder(injection.injectee()).build(),
            }, injection.countRange()));
        }

        add("amethyst_cluster", new ForgeHexAmethystLootMod(new LootItemCondition[]{
            LootTableIdCondition.builder(Blocks.AMETHYST_CLUSTER.getLootTable()).build()
        }, HexLootHandler.DEFAULT_SHARD_MODIFICATION));
    }
}
