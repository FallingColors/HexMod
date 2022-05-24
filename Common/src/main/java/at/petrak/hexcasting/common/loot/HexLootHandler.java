package at.petrak.hexcasting.common.loot;

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
        } else if (
            id.equals(new ResourceLocation("minecraft:chests/jungle_temple"))
                || id.equals(new ResourceLocation("minecraft:chests/simple_dungeon"))
                || id.equals(new ResourceLocation("minecraft:chests/village/village_cartographer"))
        ) {
            addPool.accept(getInjectPool(modLoc("inject/scroll_loot_few")));
        } else if (
            id.equals(new ResourceLocation("minecraft:chests/bastion_treasure"))
                || id.equals(new ResourceLocation("minecraft:chests/shipwreck_map"))
        ) {
            addPool.accept(getInjectPool(modLoc("inject/scroll_loot_some")));
        } else if (id.equals(new ResourceLocation("minecraft:chests/stronghold_library"))
        ) {
            addPool.accept(getInjectPool(modLoc("inject/scroll_loot_many")));
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
