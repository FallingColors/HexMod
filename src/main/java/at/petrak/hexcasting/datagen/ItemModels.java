package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.items.ItemFocus;
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
        simpleItem(HexItems.SCROLL.get());

        singleTexture(HexItems.WAND.getId().getPath(), new ResourceLocation("item/handheld_rod"),
            "layer0", new ResourceLocation(HexMod.MOD_ID, "item/" + HexItems.WAND.getId().getPath()));

        simpleItem(modLoc("patchouli_book"));

        String[] focusTypes = new String[]{
            "empty", "entity", "double", "vec3", "widget", "list", "pattern"
        };
        for (int i = 0, stringsLength = focusTypes.length; i < stringsLength; i++) {
            String type = focusTypes[i];
            simpleItem(modLoc("focus_" + type));
            simpleItem(modLoc("focus_" + type + "_sealed"));
            getBuilder(HexItems.FOCUS.get().getRegistryName().getPath())
                .override()
                .predicate(ItemFocus.DATATYPE_PRED, -0.01f + i)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/focus_" + type)))
                .end()
                .override()
                .predicate(ItemFocus.DATATYPE_PRED, -0.01f + 100 + i)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/focus_" + type + "_sealed")))
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
