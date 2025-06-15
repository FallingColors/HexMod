package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

// Partially based on:
// https://github.com/SlimeKnights/Mantle/blob/1.18.2/src/main/java/slimeknights/mantle/recipe/ingredient/EntityIngredient.java
// Licensed under MIT
//
// .equals must make sense
public abstract class BrainsweepeeIngredient {

    public abstract BrainsweepeeIngredientType<?> getType();

    public abstract boolean test(Entity entity, ServerLevel level);

    public abstract Component getName();

    public abstract List<Component> getTooltip(boolean advanced);

    /**
     * For the benefit of showing to the client, return an example of the entity.
     * <p>
     * Can return null in case someone did something stupid with a recipe
     */
    @Nullable
    public abstract Entity exampleEntity(Level level);

    public static Component getModNameComponent(String namespace) {
        String mod = IXplatAbstractions.INSTANCE.getModName(namespace);
        return Component.literal(mod).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
    }
}
