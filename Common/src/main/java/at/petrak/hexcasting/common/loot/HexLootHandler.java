package at.petrak.hexcasting.common.loot;

import at.petrak.hexcasting.api.misc.ScrollQuantity;
import at.petrak.hexcasting.api.mod.HexConfig;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// https://github.com/VazkiiMods/Botania/blob/1.18.x/Xplat/src/main/java/vazkii/botania/common/loot/LootHandler.java
// We need to inject dungeon loot (scrolls and lore), make amethyst drop fewer shards, and the extra dust stuff.
// On forge we do it the "right" way with global loot modifiers; modifiable via a datapack.
// On fabric we do the addition of items by mixing in to loot loading and injecting some loot tables we only generate on
// Fabric as addons, and the subtraction with a loot function. so it's customizable for datapack devs.
public class HexLootHandler {
    // TODO: remove ScrollQuantity, use this class as the source of truth for GLM gen on forge and inject/ loot tables
    // on fabric
    public static final EnumMap<ScrollQuantity, List<ResourceLocation>> DEFAULT_INJECTS = Util.make(() -> {
        var map = new EnumMap<ScrollQuantity, List<ResourceLocation>>(ScrollQuantity.class);

        map.put(ScrollQuantity.FEW, List.of(
            new ResourceLocation("minecraft", "chests/jungle_temple"),
            new ResourceLocation("minecraft", "chests/simple_dungeon"),
            new ResourceLocation("minecraft", "chests/village/village_cartographer")));
        map.put(ScrollQuantity.SOME, List.of(
            new ResourceLocation("minecraft", "chests/bastion_treasure"),
            new ResourceLocation("minecraft", "chests/pillager_outpost"),
            new ResourceLocation("minecraft", "chests/shipwreck_map")
        ));
        map.put(ScrollQuantity.MANY, List.of(
            // ancient city chests have amethyst in them, thinking emoji
            new ResourceLocation("minecraft", "chests/ancient_city"),
            new ResourceLocation("minecraft", "chests/stronghold_library")));

        return map;
    });

    public static final ResourceLocation FUNC_AMETHYST_SHARD_REDUCER = modLoc("amethyst_shard_reducer");

    public static final ResourceLocation TABLE_INJECT_AMETHYST_CLUSTER = modLoc("inject/amethyst_cluster");

    public static void lootLoad(ResourceLocation id, Consumer<LootPool.Builder> addPool) {
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

    public static LootPool.Builder getInjectPool(ResourceLocation entry) {
        return LootPool.lootPool()
            .add(getInjectEntry(entry, 1))
            .setBonusRolls(UniformGenerator.between(0, 1));
    }

    private static LootPoolEntryContainer.Builder<?> getInjectEntry(ResourceLocation table, int weight) {
        return LootTableReference.lootTableReference(table)
            .setWeight(weight);
    }
}
