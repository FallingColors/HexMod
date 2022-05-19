package at.petrak.hexcasting.forge.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.forge.datagen.lootmods.HexLootModifiers;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public class HexDataGenerators {
    @SubscribeEvent
    public static void generateData(GatherDataEvent ev) {
        if (System.getProperty("hexcasting.xplat_datagen") != null) {
            configureXplatDatagen(ev);
        } else {
            configureForgeDatagen(ev);
        }
    }

    private static void configureXplatDatagen(GatherDataEvent ev) {
        HexAPI.LOGGER.info("Starting cross-platform datagen");

        DataGenerator gen = ev.getGenerator();
        ExistingFileHelper efh = ev.getExistingFileHelper();
        if (ev.includeClient()) {
            gen.addProvider(new HexItemModels(gen, efh));
            gen.addProvider(new HexBlockStatesAndModels(gen, efh));
        }
        if (ev.includeServer()) {
            HexBlockTagProvider blockTagProvider = new HexBlockTagProvider(gen, efh);
            gen.addProvider(new HexRecipes(gen));
            gen.addProvider(new HexLootModifiers(gen));
            gen.addProvider(new HexAdvancements(gen, efh));
            gen.addProvider(blockTagProvider);
            gen.addProvider(new HexItemTagProvider(gen, blockTagProvider, efh));
            gen.addProvider(new HexLootTables(gen));
        }
    }

    private static void configureForgeDatagen(GatherDataEvent ev) {
        HexAPI.LOGGER.info("Starting Forge-specific datagen");

        DataGenerator gen = ev.getGenerator();
        ExistingFileHelper efh = ev.getExistingFileHelper();
        if (ev.includeServer()) {
            gen.addProvider(new HexLootModifiers(gen));
        }
    }
}