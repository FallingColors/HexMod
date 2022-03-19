package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.items.ItemFocus;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.common.items.magic.ItemManaBattery;
import at.petrak.hexcasting.common.items.magic.ItemPackagedSpell;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ItemModels extends ItemModelProvider {
    public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, HexMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(HexItems.SPELLBOOK.get());
        simpleItem(HexItems.AMETHYST_DUST.get());
        simpleItem(HexItems.CHARGED_AMETHYST.get());
        simpleItem(HexItems.SUBMARINE_SANDWICH.get());
        simpleItem(HexItems.SCRYING_LENS.get());
        simpleItem(HexItems.ABACUS.get());
        simpleItem(HexItems.SLATE.get());

        simpleItem(modLoc("scroll_pristine"));
        simpleItem(modLoc("scroll_ancient"));
        getBuilder(HexItems.SCROLL.get().getRegistryName().getPath())
            .override()
            .predicate(ItemScroll.ANCIENT_PREDICATE, 0f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/scroll_pristine"))).end()
            .override()
            .predicate(ItemScroll.ANCIENT_PREDICATE, 1f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/scroll_ancient"))).end();

        singleTexture(HexItems.WAND.getId().getPath(), new ResourceLocation("item/handheld_rod"),
            "layer0", new ResourceLocation(HexMod.MOD_ID, "item/" + HexItems.WAND.getId().getPath()));

        simpleItem(modLoc("patchouli_book"));

        String[] focusTypes = new String[]{
            "empty", "entity", "double", "vec3", "widget", "list", "pattern"
        };
        // For stupid bad reasons we need to do this in ascending order.
        for (int sealedIdx = 0; sealedIdx <= 1; sealedIdx++) {
            var sealed = sealedIdx == 1;
            for (int i = 0, stringsLength = focusTypes.length; i < stringsLength; i++) {
                var type = focusTypes[i];
                var name = "focus_" + type + (sealed ? "_sealed" : "");
                simpleItem(modLoc(name));
                getBuilder(HexItems.FOCUS.get().getRegistryName().getPath())
                    .override()
                    .predicate(ItemFocus.DATATYPE_PRED, -0.01f + i + (sealed ? 100 : 0))
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name)))
                    .end();
            }
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

        for (int i = 0; i < 16; i++) {
            singleTexture(HexItems.DYE_COLORIZERS[i].getId().getPath(), new ResourceLocation("item/generated"),
                "layer0", new ResourceLocation(HexMod.MOD_ID, "item/colorizer/dye" + i));
        }
        for (int i = 0; i < 14; i++) {
            singleTexture(HexItems.PRIDE_COLORIZERS[i].getId().getPath(), new ResourceLocation("item/generated"),
                "layer0", new ResourceLocation(HexMod.MOD_ID, "item/colorizer/pride" + i));
        }
        singleTexture(HexItems.UUID_COLORIZER.getId().getPath(), new ResourceLocation("item/generated"),
            "layer0", new ResourceLocation(HexMod.MOD_ID, "item/colorizer/uuid"));
    }

    public void simpleItem(Item item) {
        simpleItem(item.getRegistryName());
    }

    public void simpleItem(ResourceLocation path) {
        singleTexture(path.getPath(), new ResourceLocation("item/generated"),
            "layer0", new ResourceLocation(HexMod.MOD_ID, "item/" + path.getPath()));
    }

    public void brandishedItem(Item item) {
        brandishedItem(item.getRegistryName());
    }

    public void brandishedItem(ResourceLocation path) {
        singleTexture(path.getPath(), new ResourceLocation("item/handheld"),
            "layer0", new ResourceLocation(HexMod.MOD_ID, "item/" + path.getPath()));
    }
}
