package at.petrak.hexcasting.common.recipe.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StateIngredientBlock implements StateIngredient {
    private final Block block;

    public StateIngredientBlock(Block block) {
        this.block = block;
    }

    @Override
    public boolean test(BlockState blockState) {
        return block == blockState.getBlock();
    }

    @Override
    public BlockState pick(Random random) {
        return block.defaultBlockState();
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "block");
        object.addProperty("block", Registry.BLOCK.getKey(block).toString());
        return object;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(1);
        buffer.writeVarInt(Registry.BLOCK.getId(block));
    }

    @Override
    public List<ItemStack> getDisplayedStacks() {
        if (block.asItem() == Items.AIR) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new ItemStack(block));
    }

    @Override
    public List<BlockState> getDisplayed() {
        return Collections.singletonList(block.defaultBlockState());
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return block == ((StateIngredientBlock) o).block;
    }

    @Override
    public int hashCode() {
        return block.hashCode();
    }

    @Override
    public String toString() {
        return "StateIngredientBlock{" + block + "}";
    }
}