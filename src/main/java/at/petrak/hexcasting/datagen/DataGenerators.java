package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.datagen.lootmods.HexLootModifiers;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public class DataGenerators {
    @SubscribeEvent
    public static void generateData(GatherDataEvent ev) {
        DataGenerator gen = ev.getGenerator();
        ExistingFileHelper efh = ev.getExistingFileHelper();
        if (ev.includeClient()) {
            gen.addProvider(new ItemModels(gen, efh));
        }
        if (ev.includeServer()) {
            gen.addProvider(new Recipes(gen));
        }
        // On both sides
        gen.addProvider(new HexLootModifiers(gen));
        gen.addProvider(new Advancements(gen, efh));
    }
}