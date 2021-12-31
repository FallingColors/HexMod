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

        String[] focusTypes = new String[]{
                "empty", "entity", "double", "vec3", "spell", "widget", "list", "pattern"
        };
        for (int i = 0, stringsLength = focusTypes.length; i < stringsLength; i++) {
            String type = focusTypes[i];
            simpleItem(modLoc("focus_" + type));
            getBuilder(HexItems.FOCUS.get().getRegistryName().getPath())
                    .override()
                    .predicate(ItemFocus.PREDICATE, -0.01f + i)
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/focus_" + type)))
                    .end()
                    .override()
                    .predicate(ItemFocus.PREDICATE, -0.01f + 100 + i)
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/focus_" + type + "_sealed")))
                    .end();
            
        }
    }

    public void simpleItem(Item item) {
        simpleItem(item.getRegistryName());
    }

    public void simpleItem(ResourceLocation path) {
        singleTexture(path.getPath(), new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(HexMod.MOD_ID, "item/" + path.getPath()));
    }
}
