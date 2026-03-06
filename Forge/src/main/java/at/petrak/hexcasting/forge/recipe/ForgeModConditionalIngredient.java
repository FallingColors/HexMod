package at.petrak.hexcasting.forge.recipe;

import at.petrak.hexcasting.forge.lib.ForgeHexIngredientTypes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ForgeModConditionalIngredient implements ICustomIngredient {
    public static final ResourceLocation ID = modLoc("mod_conditional");

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

    public static ForgeModConditionalIngredient of(Ingredient main, String modid, Ingredient ifModLoaded) {
        return new ForgeModConditionalIngredient(main, modid, ifModLoaded);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        return toUse.test(input);
    }

    @Override
    public boolean isSimple() {
        return toUse.isSimple();
    }

    @Override
    public @NotNull Stream<ItemStack> getItems() {
        return Arrays.stream(toUse.getItems());
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return ForgeHexIngredientTypes.MOD_CONDITIONAL.get();
    }

    private static final Codec<Ingredient> INGREDIENT_CODEC = Codec.of(
        new Encoder<>() {
            @Override
            public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
                var custom = input.getCustomIngredient();
                JsonElement json;
                if (custom != null) {
                    @SuppressWarnings("unchecked")
                    var codec = (com.mojang.serialization.Codec<Object>) custom.getType().codec().codec();
                    var result = codec.encodeStart(JsonOps.INSTANCE, custom);
                    if (result.result().isPresent()) {
                        json = result.result().get();
                    } else {
                        return DataResult.error(() -> "Failed to encode custom ingredient: " + result.error().get().message());
                    }
                } else {
                    json = serializeVanillaIngredient(input);
                }
                return DataResult.success(new Dynamic<>(JsonOps.INSTANCE, json).convert(ops).getValue());
            }
        },
        new Decoder<>() {
            @Override
            public <T> DataResult<com.mojang.datafixers.util.Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
                return Ingredient.CODEC.parse(ops, input).map(ingr -> com.mojang.datafixers.util.Pair.of(ingr, input));
            }
        }
    );

    private static JsonElement serializeVanillaIngredient(Ingredient input) {
        var items = Arrays.asList(input.getItems());
        if (items.isEmpty()) {
            return new JsonObject();
        }
        var first = items.get(0);
        JsonObject obj = new JsonObject();
        obj.addProperty("item", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        return obj;
    }

    public static final MapCodec<ForgeModConditionalIngredient> CODEC = RecordCodecBuilder.mapCodec(inst ->
        inst.group(
            INGREDIENT_CODEC.fieldOf("default").forGetter(f -> f.main),
            Codec.STRING.fieldOf("modid").forGetter(f -> f.modid),
            INGREDIENT_CODEC.fieldOf("if_loaded").forGetter(f -> f.ifModLoaded)
        ).apply(inst, ForgeModConditionalIngredient::new)
    );

    public static final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, ForgeModConditionalIngredient> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
}
