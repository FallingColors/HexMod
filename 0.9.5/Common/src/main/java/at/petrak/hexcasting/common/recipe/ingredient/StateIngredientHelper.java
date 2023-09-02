package at.petrak.hexcasting.common.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class StateIngredientHelper {
    public static StateIngredient of(Block block) {
        return new StateIngredientBlock(block);
    }

    public static StateIngredient of(BlockState state) {
        return new StateIngredientBlockState(state);
    }

    public static StateIngredient of(TagKey<Block> tag) {
        return of(tag.location());
    }

    public static StateIngredient of(ResourceLocation id) {
        return new StateIngredientTag(id);
    }

    public static StateIngredient of(Collection<Block> blocks) {
        return new StateIngredientBlocks(blocks);
    }

    public static StateIngredient tagExcluding(TagKey<Block> tag, StateIngredient... excluded) {
        return new StateIngredientTagExcluding(tag.location(), List.of(excluded));
    }

    public static StateIngredient deserialize(JsonObject object) {
        switch (GsonHelper.getAsString(object, "type")) {
            case "tag":
                return new StateIngredientTag(new ResourceLocation(GsonHelper.getAsString(object, "tag")));
            case "block":
                return new StateIngredientBlock(
                    Registry.BLOCK.get(new ResourceLocation(GsonHelper.getAsString(object, "block"))));
            case "state":
                return new StateIngredientBlockState(readBlockState(object));
            case "blocks":
                List<Block> blocks = new ArrayList<>();
                for (JsonElement element : GsonHelper.getAsJsonArray(object, "blocks")) {
                    blocks.add(Registry.BLOCK.get(new ResourceLocation(element.getAsString())));
                }
                return new StateIngredientBlocks(blocks);
            case "tag_excluding":
                ResourceLocation tag = new ResourceLocation(GsonHelper.getAsString(object, "tag"));
                List<StateIngredient> ingr = new ArrayList<>();
                for (JsonElement element : GsonHelper.getAsJsonArray(object, "exclude")) {
                    ingr.add(deserialize(GsonHelper.convertToJsonObject(element, "exclude entry")));
                }
                return new StateIngredientTagExcluding(tag, ingr);
            default:
                throw new JsonParseException("Unknown type!");
        }
    }

    /**
     * Deserializes a state ingredient, but removes air from its data,
     * and returns null if the ingredient only matched air.
     */
    @Nullable
    public static StateIngredient tryDeserialize(JsonObject object) {
        StateIngredient ingr = deserialize(object);
        if (ingr instanceof StateIngredientTag sit) {
            if (sit.resolve().findAny().isEmpty()) {
                return null;
            }
            return ingr;
        }
        if (ingr instanceof StateIngredientBlock || ingr instanceof StateIngredientBlockState) {
            if (ingr.test(Blocks.AIR.defaultBlockState())) {
                return null;
            }
        } else if (ingr instanceof StateIngredientBlocks sib) {
            Collection<Block> blocks = sib.blocks;
            List<Block> list = new ArrayList<>(blocks);
            if (list.removeIf(b -> b == Blocks.AIR)) {
                if (list.size() == 0) {
                    return null;
                }
                return of(list);
            }
        }
        return ingr;
    }

    public static StateIngredient read(FriendlyByteBuf buffer) {
        switch (buffer.readVarInt()) {
            case 0:
                int count = buffer.readVarInt();
                Set<Block> set = new HashSet<>();
                for (int i = 0; i < count; i++) {
                    int id = buffer.readVarInt();
                    Block block = Registry.BLOCK.byId(id);
                    set.add(block);
                }
                return new StateIngredientBlocks(set);
            case 1:
                return new StateIngredientBlock(Registry.BLOCK.byId(buffer.readVarInt()));
            case 2:
                return new StateIngredientBlockState(Block.stateById(buffer.readVarInt()));
            default:
                throw new IllegalArgumentException("Unknown input discriminator!");
        }
    }

    /**
     * Writes data about the block state to the provided json object.
     */
    public static JsonObject serializeBlockState(BlockState state) {
        CompoundTag nbt = NbtUtils.writeBlockState(state);
        renameTag(nbt, "Name", "name");
        renameTag(nbt, "Properties", "properties");
        Dynamic<net.minecraft.nbt.Tag> dyn = new Dynamic<>(NbtOps.INSTANCE, nbt);
        return dyn.convert(JsonOps.INSTANCE).getValue().getAsJsonObject();
    }

    /**
     * Reads the block state from the provided json object.
     */
    public static BlockState readBlockState(JsonObject object) {
        CompoundTag nbt = (CompoundTag) new Dynamic<>(JsonOps.INSTANCE, object).convert(NbtOps.INSTANCE).getValue();
        renameTag(nbt, "name", "Name");
        renameTag(nbt, "properties", "Properties");
        String name = nbt.getString("Name");
        ResourceLocation id = ResourceLocation.tryParse(name);
        if (id == null || !Registry.BLOCK.getOptional(id).isPresent()) {
            throw new IllegalArgumentException("Invalid or unknown block ID: " + name);
        }
        return NbtUtils.readBlockState(nbt);
    }

    @Deprecated
    @Nonnull
    public static List<ItemStack> toStackList(StateIngredient input) {
        return input.getDisplayedStacks();
    }

    private static void renameTag(CompoundTag tag, String from, String to) {
        var t = tag.get(from);
        if (t != null) {
            tag.remove(from);
            tag.put(to, t);
        }
    }
}