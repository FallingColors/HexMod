package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.HexMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HexRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(
        ForgeRegistries.RECIPE_SERIALIZERS, HexMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<?>> BRAINSWEEP = SERIALIZERS.register("brainsweep",
        BrainsweepRecipe.Serializer::new);
    public static RecipeType<BrainsweepRecipe> BRAINSWEEP_TYPE;

    // Like in the statistics, gotta register it at some point
    @SubscribeEvent
    public static void registerTypes(RegistryEvent.Register<Item> evt) {
        BRAINSWEEP_TYPE = RecipeType.register(HexMod.MOD_ID + ":brainsweep");
    }
}
