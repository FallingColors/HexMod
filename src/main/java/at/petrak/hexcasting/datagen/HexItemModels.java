package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.items.*;
import at.petrak.hexcasting.common.items.magic.ItemManaBattery;
import at.petrak.hexcasting.common.items.magic.ItemPackagedSpell;
import at.petrak.paucal.api.datagen.PaucalItemModelProvider;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class HexItemModels extends PaucalItemModelProvider {
    public HexItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, HexMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(HexItems.AMETHYST_DUST.get());
        simpleItem(HexItems.CHARGED_AMETHYST.get());
        simpleItem(HexItems.SUBMARINE_SANDWICH.get());
        simpleItem(HexItems.ABACUS.get());

        simpleItem(modLoc("scroll_pristine"));
        simpleItem(modLoc("scroll_ancient"));
        getBuilder(HexItems.SCROLL.get().getRegistryName().getPath())
            .override()
            .predicate(ItemScroll.ANCIENT_PREDICATE, 0f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/scroll_pristine"))).end()
            .override()
            .predicate(ItemScroll.ANCIENT_PREDICATE, 1f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/scroll_ancient"))).end();

        simpleItem(HexItems.SCRYING_LENS.get());
        getBuilder(HexItems.SCRYING_LENS.get().getRegistryName().getPath())
            .transforms()
            .transform(ModelBuilder.Perspective.HEAD)
            .rotation(0f, 0f, 0f)
            .translation(-2.5f, 0f, -8f)
            .scale(0.4f);

        singleTexture("wand_old", new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/wands/old"));
        singleTexture("wand_bosnia", new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/wands/bosnia"));
        var wands = new Item[]{
            HexItems.WAND_OAK.get(),
            HexItems.WAND_BIRCH.get(),
            HexItems.WAND_SPRUCE.get(),
            HexItems.WAND_JUNGLE.get(),
            HexItems.WAND_DARK_OAK.get(),
            HexItems.WAND_ACACIA.get(),
            HexItems.WAND_AKASHIC.get(),
        };
        var wandKeys = new String[]{
            "oak", "birch", "spruce", "jungle", "dark_oak", "acacia", "akashic"
        };
        for (int i = 0; i < wands.length; i++) {
            Item wand = wands[i];
            String wandKey = wandKeys[i];
            singleTexture(wand.getRegistryName().getPath(), new ResourceLocation("item/handheld_rod"),
                "layer0", modLoc("item/wands/" + wandKey));
            getBuilder(wand.getRegistryName().getPath())
                .override()
                .predicate(ItemWand.FUNNY_LEVEL_PREDICATE, 0)
                .model(new ModelFile.UncheckedModelFile(modLoc("wand_" + wandKey)))
                .end().override()
                .predicate(ItemWand.FUNNY_LEVEL_PREDICATE, 1)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/wand_old")))
                .end().override()
                .predicate(ItemWand.FUNNY_LEVEL_PREDICATE, 2)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/wand_bosnia")))
                .end();
        }

        simpleItem(modLoc("patchouli_book"));

        String[] datumStoredTypes = new String[]{
            "empty", "entity", "double", "vec3", "widget", "list", "pattern"
        };
        // For stupid bad reasons we need to do this in ascending order.
        for (int sealedIdx = 0; sealedIdx <= 1; sealedIdx++) {
            var sealed = sealedIdx == 1;
            for (int i = 0, stringsLength = datumStoredTypes.length; i < stringsLength; i++) {
                var type = datumStoredTypes[i];

                var focusName = "focus_" + type + (sealed ? "_sealed" : "");
                simpleItem(modLoc(focusName));
                getBuilder(HexItems.FOCUS.get().getRegistryName().getPath())
                    .override()
                    .predicate(ItemFocus.DATATYPE_PRED, -0.01f + i + (sealed ? 100 : 0))
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/" + focusName)))
                    .end();
            }
        }

        for (int i = 0, stringsLength = datumStoredTypes.length; i < stringsLength; i++) {
            var type = datumStoredTypes[i];

            var spellbookName = "spellbook_" + type;
            simpleItem(modLoc(spellbookName));
            getBuilder(HexItems.SPELLBOOK.get().getRegistryName().getPath())
                .override()
                .predicate(ItemSpellbook.DATATYPE_PRED, -0.01f + i)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/" + spellbookName)))
                .end();
        }

        Pair<RegistryObject<Item>, String>[] packagers = new Pair[]{
            new Pair(HexItems.CYPHER, "cypher"),
            new Pair(HexItems.TRINKET, "trinket"),
            new Pair(HexItems.ARTIFACT, "artifact"),
        };
        for (Pair<RegistryObject<Item>, String> p : packagers) {
            simpleItem(modLoc(p.getSecond()));
            simpleItem(modLoc(p.getSecond() + "_filled"));
            getBuilder(p.getFirst().get().getRegistryName().getPath())
                .override()
                .predicate(ItemPackagedSpell.HAS_PATTERNS_PRED, -0.01f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/" + p.getSecond())))
                .end()
                .override()
                .predicate(ItemPackagedSpell.HAS_PATTERNS_PRED, 1f - 0.01f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/" + p.getSecond() + "_filled")))
                .end();
        }

        String[] sizeNames = new String[]{
            "small", "medium", "large",
        };
        int maxFill = 4;
        for (int size = 0; size < sizeNames.length; size++) {
            for (int fill = 0; fill <= maxFill; fill++) {
                String name = "phial_" + sizeNames[size] + "_" + fill;
                singleTexture(
                    name,
                    new ResourceLocation("item/generated"),
                    "layer0", new ResourceLocation(HexMod.MOD_ID, "item/phial/" + name));

                float fillProp = (float) fill / maxFill;
                getBuilder(HexItems.BATTERY.getId().getPath()).override()
                    .predicate(ItemManaBattery.MANA_PREDICATE, fillProp)
                    .predicate(ItemManaBattery.MAX_MANA_PREDICATE, size)
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name)))
                    .end();
            }
        }

        for (int i = 0; i < DyeColor.values().length; i++) {
            singleTexture(HexItems.DYE_COLORIZERS.get(DyeColor.values()[i]).getId().getPath(),
                new ResourceLocation("item/generated"),
                "layer0", new ResourceLocation(HexMod.MOD_ID, "item/colorizer/dye" + i));
        }
        for (int i = 0; i < 14; i++) {
            singleTexture(HexItems.PRIDE_COLORIZERS[i].getId().getPath(), new ResourceLocation("item/generated"),
                "layer0", new ResourceLocation(HexMod.MOD_ID, "item/colorizer/pride" + i));
        }
        singleTexture(HexItems.UUID_COLORIZER.getId().getPath(), new ResourceLocation("item/generated"),
            "layer0", new ResourceLocation(HexMod.MOD_ID, "item/colorizer/uuid"));

        simpleItem(modLoc("slate_blank"));
        simpleItem(modLoc("slate_written"));
        getBuilder(HexItems.SLATE.getId().getPath()).override()
            .predicate(ItemSlate.WRITTEN_PRED, 0)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/slate_blank")))
            .end()
            .override()
            .predicate(ItemSlate.WRITTEN_PRED, 1)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/slate_written")))
            .end();

        getBuilder(HexBlocks.AKASHIC_RECORD.getId().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_record")));
        simpleItem(modLoc("akashic_door"));
        getBuilder(HexBlocks.AKASHIC_TRAPDOOR.getId().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_trapdoor_bottom")));
        getBuilder(HexBlocks.AKASHIC_LOG.getId().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_log")));
        getBuilder(HexBlocks.AKASHIC_LOG_STRIPPED.getId().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_log_stripped")));
        getBuilder(HexBlocks.AKASHIC_STAIRS.getId().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_stairs")));
        getBuilder(HexBlocks.AKASHIC_SLAB.getId().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_slab")));
        getBuilder(HexBlocks.AKASHIC_BUTTON.getId().getPath()).parent(
                new ModelFile.UncheckedModelFile(new ResourceLocation("block/button_inventory")))
            .texture("texture", modLoc("block/akashic/planks1"));
        getBuilder(HexBlocks.AKASHIC_PRESSURE_PLATE.getId().getPath())
            .parent(new ModelFile.UncheckedModelFile(modLoc("block/akashic_pressure_plate")));
    }
}
