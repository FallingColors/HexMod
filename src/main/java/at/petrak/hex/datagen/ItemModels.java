package at.petrak.hex.datagen;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.items.HexItems;
import at.petrak.hex.common.items.ItemFocus;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModels extends ItemModelProvider {
    public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, HexMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(HexItems.WAND.get());
        simpleItem(HexItems.SPELLBOOK.get());

        simpleItem(modLoc("focus_empty"));
        simpleItem(modLoc("focus_entity"));
        simpleItem(modLoc("focus_double"));
        simpleItem(modLoc("focus_vec3"));
        simpleItem(modLoc("focus_spell"));
        simpleItem(modLoc("focus_widget"));
        simpleItem(modLoc("focus_list"));
        simpleItem(modLoc("focus_patterns"));
        getBuilder(HexItems.FOCUS.get().getRegistryName().getPath())
                .override()
                .predicate(ItemFocus.PREDICATE, -0.01f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/focus_empty")))
                .end()
                .override()
                .predicate(ItemFocus.PREDICATE, 1 - 0.01f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/focus_entity")))
                .end()
                .override()
                .predicate(ItemFocus.PREDICATE, 2 - 0.01f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/focus_double")))
                .end()
                .override()
                .predicate(ItemFocus.PREDICATE, 3 - 0.01f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/focus_vec3")))
                .end()
                .override()
                .predicate(ItemFocus.PREDICATE, 4 - 0.01f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/focus_spell")))
                .end()
                .override()
                .predicate(ItemFocus.PREDICATE, 5 - 0.01f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/focus_widget")))
                .end()
                .override()
                .predicate(ItemFocus.PREDICATE, 6 - 0.01f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/focus_list")))
                .end()
                .override()
                .predicate(ItemFocus.PREDICATE, 7 - 0.01f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/focus_patterns")))
                .end();
    }

    public void simpleItem(Item item) {
        simpleItem(item.getRegistryName());
    }

    public void simpleItem(ResourceLocation path) {
        singleTexture(path.getPath(), new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(HexMod.MOD_ID, "item/" + path.getPath()));
    }
}
