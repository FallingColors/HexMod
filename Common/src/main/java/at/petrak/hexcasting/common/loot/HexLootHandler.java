package at.petrak.hexcasting.common.loot;

import at.petrak.hexcasting.api.misc.ScrollQuantity;
import at.petrak.hexcasting.api.mod.HexConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.Consumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// https://github.com/VazkiiMods/Botania/blob/1.18.x/Xplat/src/main/java/vazkii/botania/common/loot/LootHandler.java
public class HexLootHandler {
    public static final ResourceLocation FUNC_AMETHYST_SHARD_REDUCER = modLoc("amethyst_shard_reducer");

    public static final ResourceLocation TABLE_INJECT_AMETHYST_CLUSTER = modLoc("inject/amethyst_cluster");

    public static void lootLoad(ResourceLocation id,
        Consumer<LootPool> addPool) {
        if (id.equals(Blocks.AMETHYST_CLUSTER.getLootTable())) {
            addPool.accept(getInjectPool(TABLE_INJECT_AMETHYST_CLUSTER));
        } else {
            ScrollQuantity scrolls = HexConfig.server().scrollsForLootTable(id);
            ResourceLocation injection = scrolls.getPool();
            if (injection != null) {
                addPool.accept(getInjectPool(injection));
            }
        }
    }

    public static LootPool getInjectPool(ResourceLocation entry) {
        return LootPool.lootPool()
            .add(getInjectEntry(entry, 1))
            .setBonusRolls(UniformGenerator.between(0, 1))
            .build();
    }

    private static LootPoolEntryContainer.Builder<?> getInjectEntry(ResourceLocation table, int weight) {
        return LootTableReference.lootTableReference(table)
            .setWeight(weight);
    }
}
