package at.petrak.hex.datagen;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.items.HexItems;
import at.petrak.hex.common.items.ItemFocus;
import at.petrak.hex.common.items.ItemPackagedSpell;
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
        simpleItem(HexItems.WAND.get());
        simpleItem(HexItems.SPELLBOOK.get());

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
    }

    public void simpleItem(Item item) {
        simpleItem(item.getRegistryName());
    }

    public void simpleItem(ResourceLocation path) {
        singleTexture(path.getPath(), new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(HexMod.MOD_ID, "item/" + path.getPath()));
    }
}
