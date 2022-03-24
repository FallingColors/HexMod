package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

// https://github.com/XuulMedia/Flint-Age/blob/4638289130ef80dafe9b6a3fdcb461a72688100f/src/main/java/xuul/flint/datagen/BaseLootTableProvider.java#L61
// auugh mojang whyyyy
public class HexLootTables extends LootTableProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final DataGenerator generator;
    protected final Map<Block, LootTable.Builder> lootTables = new HashMap<>();

    public HexLootTables(DataGenerator pGenerator) {
        super(pGenerator);
        this.generator = pGenerator;
    }

    protected void addTables() {
        dropSelfTable(HexBlocks.EMPTY_IMPETUS.get());
        dropSelfTable(HexBlocks.IMPETUS_RIGHTCLICK.get());
        dropSelfTable(HexBlocks.SLATE_BLOCK.get());

        var slatePool = LootPool.lootPool().name("slate").
            setRolls(ConstantValue.exactly(1))
            .add(LootItem.lootTableItem(HexBlocks.SLATE.get())
                .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                    .copy(BlockEntitySlate.TAG_PATTERN, "BlockEntityTag." + BlockEntitySlate.TAG_PATTERN)));
        lootTables.put(HexBlocks.SLATE.get(), LootTable.lootTable().withPool(slatePool));
    }

    protected void dropSelfTable(Block block) {
        dropSelfTable(block.getRegistryName().getPath(), block);
    }

    protected void dropSelfTable(String name, Block block) {
        var pool = LootPool.lootPool()
            .name(name)
            .setRolls(ConstantValue.exactly(1))
            .add(LootItem.lootTableItem(block));
        var loot = LootTable.lootTable().withPool(pool);

        lootTables.put(block, loot);
    }

    @Override
    public void run(HashCache cache) {
        addTables();

        var tables = new HashMap<ResourceLocation, LootTable>();
        for (var entry : lootTables.entrySet()) {
            tables.put(entry.getKey().getLootTable(), entry.getValue().setParamSet(LootContextParamSets.BLOCK).build());
        }
        writeTables(cache, tables);
    }

    private void writeTables(HashCache cache, Map<ResourceLocation, LootTable> tables) {
        Path outputFolder = this.generator.getOutputFolder();
        tables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
            try {
                DataProvider.save(GSON, cache, LootTables.serialize(lootTable), path);
            } catch (IOException e) {
            }
        });
    }
}
