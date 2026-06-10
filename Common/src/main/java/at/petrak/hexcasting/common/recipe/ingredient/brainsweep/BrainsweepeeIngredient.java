package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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

    public abstract String getSomeKindOfReasonableIDForEmi();

    /**
     * For the benefit of showing to the client, return a list of example entities.
     * <p>
     * Can return empty list in case someone did something stupid with a recipe
     */
    public abstract List<Entity> exampleEntities(Level level);

    /**
     * @deprecated Binary compatibility API. Use exampleEntities instead.
     */
    @Nullable
    @Deprecated
    public final Entity exampleEntity(Level level) {
        try {
            return exampleEntities(level).iterator().next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public static Component getModNameComponent(String namespace) {
        String mod = IXplatAbstractions.INSTANCE.getModName(namespace);
        return Component.literal(mod).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
    }

    private static Map<EntityType<?>, Entity> cachedExampleEntity = new HashMap<>();
    protected static Entity getCachedExampleEntity(EntityType<?> type, Level level) {
        // don't cache for server levels (if any) to prevent wrong side
        if (!level.isClientSide) {
            return type.create(level);
        }
        return cachedExampleEntity.computeIfAbsent(type, t -> t.create(level));
    }
}
