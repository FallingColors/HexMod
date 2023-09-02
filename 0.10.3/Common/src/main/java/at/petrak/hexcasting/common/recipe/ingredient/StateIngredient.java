package at.petrak.hexcasting.common.recipe.ingredient;


import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

// https://github.com/VazkiiMods/Botania/blob/1.18.x/Common/src/main/java/vazkii/botania/api/recipe/StateIngredient.java
// good artists copy and all
public interface StateIngredient extends Predicate<BlockState> {
    @Override
    boolean test(BlockState state);

    BlockState pick(Random random);

    JsonObject serialize();

    void write(FriendlyByteBuf buffer);

    List<ItemStack> getDisplayedStacks();

    /**
     * A description tooltip to display in areas like JEI recipes.
     */
    default List<Component> descriptionTooltip() {
        return Collections.emptyList();
    }

    List<BlockState> getDisplayed();
}