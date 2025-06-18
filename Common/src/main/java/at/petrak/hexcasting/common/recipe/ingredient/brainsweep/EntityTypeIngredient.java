package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;

public class EntityTypeIngredient extends BrainsweepeeIngredient {
    public final EntityType<?> entityType;

    public EntityTypeIngredient(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    @Override
    public BrainsweepeeIngredientType<?> getType() {
        return BrainsweepeeIngredients.ENTITY_TYPE;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    @Override
    public boolean test(Entity entity, ServerLevel level) {
        // entity types are singletons
        return entity.getType() == this.entityType;
    }

    @Override
    public Component getName() {
        return this.entityType.getDescription();
    }

    @Override
    public List<Component> getTooltip(boolean advanced) {
        return List.of(
            this.entityType.getDescription(),
            BrainsweepeeIngredient.getModNameComponent(BuiltInRegistries.ENTITY_TYPE.getKey(this.entityType).getNamespace())
        );
    }

    @Override
    public Entity exampleEntity(Level level) {
        return this.entityType.create(level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityTypeIngredient that = (EntityTypeIngredient) o;
        return Objects.equals(entityType, that.entityType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType);
    }


    public static class Type implements BrainsweepeeIngredientType<EntityTypeIngredient> {
        public static final MapCodec<EntityTypeIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entityType").forGetter(EntityTypeIngredient::getEntityType)
        ).apply(instance, EntityTypeIngredient::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, EntityTypeIngredient> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.ENTITY_TYPE), EntityTypeIngredient::getEntityType,
                EntityTypeIngredient::new
        );

        @Override
        public MapCodec<EntityTypeIngredient> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EntityTypeIngredient> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
