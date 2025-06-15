package at.petrak.hexcasting.forge.recipe;

import at.petrak.hexcasting.forge.lib.ForgeHexIngredientTypes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ForgeModConditionalIngredient implements ICustomIngredient {
    public static final MapCodec<ForgeModConditionalIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("if_loaded").forGetter(ForgeModConditionalIngredient::getMain),
            Codec.STRING.fieldOf("modid").forGetter(ForgeModConditionalIngredient::getModid),
            Ingredient.CODEC.fieldOf("default").forGetter(ForgeModConditionalIngredient::getIfModLoaded)
    ).apply(inst, ForgeModConditionalIngredient::of));
    public static final StreamCodec<RegistryFriendlyByteBuf, ForgeModConditionalIngredient> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ForgeModConditionalIngredient::getMain,
            ByteBufCodecs.STRING_UTF8, ForgeModConditionalIngredient::getModid,
            Ingredient.CONTENTS_STREAM_CODEC, ForgeModConditionalIngredient::getIfModLoaded,
            ForgeModConditionalIngredient::new
    );

    private final Ingredient main;
    private final String modid;
    private final Ingredient ifModLoaded;

    private final Ingredient toUse;

    protected ForgeModConditionalIngredient(Ingredient main, String modid, Ingredient ifModLoaded) {
        this.main = main;
        this.modid = modid;
        this.ifModLoaded = ifModLoaded;
        this.toUse = IXplatAbstractions.INSTANCE.isModPresent(modid) ? ifModLoaded : main;
    }

    public String getModid() {
        return modid;
    }

    public Ingredient getMain() {
        return main;
    }

    public Ingredient getIfModLoaded() {
        return ifModLoaded;
    }

    public Ingredient getToUse() {
        return toUse;
    }

    /**
     * Creates a new ingredient matching the given stack
     */
    public static ForgeModConditionalIngredient of(Ingredient main, String modid, Ingredient ifModLoaded) {
        return new ForgeModConditionalIngredient(main, modid, ifModLoaded);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        return toUse.test(input);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Arrays.stream(toUse.getItems());
    }

    @Override
    public boolean isSimple() {
        return toUse.isSimple();
    }

    @Override
    public IngredientType<?> getType() {
        return ForgeHexIngredientTypes.MOD_CONDITIONAL_INGREDIENT.get();
    }
}
