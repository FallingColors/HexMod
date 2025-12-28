package at.petrak.hexcasting.fabric.loot;

import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.loot.AddPerWorldPatternToScrollFunc;
import at.petrak.hexcasting.common.loot.AddHexToAncientCypherFunc;
import at.petrak.hexcasting.fabric.FabricHexInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;
import static at.petrak.hexcasting.common.loot.HexLootHandler.TABLE_INJECT_AMETHYST_CLUSTER;

public class FabricHexLootModJankery {
    public static final ResourceLocation FUNC_AMETHYST_SHARD_REDUCER = modLoc("amethyst_shard_reducer");
    public static final ResourceLocation RANDOM_SCROLL_TABLE = modLoc("random_scroll");
    public static final ResourceLocation RANDOM_CYPHER_TABLE = modLoc("random_cypher");

    public static void lootLoad(ResourceLocation id, Consumer<LootPool.Builder> addPool) {
        if (id.equals(Blocks.AMETHYST_CLUSTER.getLootTable())) {
            addPool.accept(makeAmethystInjectPool());
        } else if (id.equals(RANDOM_SCROLL_TABLE)) {
            // -1 weight = guaranteed spawn
            addPool.accept(makeScrollAddPool(-1));
        } else if (id.equals(RANDOM_CYPHER_TABLE)) {
            // 1 chance = guaranteed spawn
            addPool.accept(makeCypherAddPool(1));
        }

        int countRange = FabricHexInitializer.CONFIG.server.scrollRangeForLootTable(id);
        if (countRange != -1) {
            addPool.accept(makeScrollAddPool(countRange));
        }

        if (FabricHexInitializer.CONFIG.server.shouldInjectLore(id)) {
            addPool.accept(makeLoreAddPool(FabricHexInitializer.CONFIG.server.loreChance()));
        }

        if (FabricHexInitializer.CONFIG.server.shouldInjectCyphers(id)) {
            addPool.accept(makeCypherAddPool(FabricHexInitializer.CONFIG.server.cypherChance()));
        }
    }

    @NotNull
    private static LootPool.Builder makeAmethystInjectPool() {
        return LootPool.lootPool()
            .add(LootTableReference.lootTableReference(TABLE_INJECT_AMETHYST_CLUSTER));
    }

    private static LootPool.Builder makeScrollAddPool(int range) {
        return LootPool.lootPool()
            .setRolls(range < 0 ? ConstantValue.exactly(1) : UniformGenerator.between(-range, range))
            .add(LootItem.lootTableItem(HexItems.SCROLL_LARGE))
            .apply(() -> new AddPerWorldPatternToScrollFunc(new LootItemCondition[0]));
    }

    private static LootPool.Builder makeLoreAddPool(double chance) {
        return LootPool.lootPool()
            .when(LootItemRandomChanceCondition.randomChance((float) chance))
            .setRolls(ConstantValue.exactly(1))
            .add(LootItem.lootTableItem(HexItems.LORE_FRAGMENT));
    }

    private static LootPool.Builder makeCypherAddPool(double chance) {
        return LootPool.lootPool()
            .when(LootItemRandomChanceCondition.randomChance((float) chance))
            .setRolls(ConstantValue.exactly(1))
            .add(LootItem.lootTableItem(HexItems.ANCIENT_CYPHER))
            .apply(() -> new AddHexToAncientCypherFunc(new LootItemCondition[0]));
    }
}
