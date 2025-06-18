package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EntityTagIngredient extends BrainsweepeeIngredient {
    public final TagKey<EntityType<?>> entityTypeTag;

    public EntityTagIngredient(TagKey<EntityType<?>> tag) {
        this.entityTypeTag = tag;
    }

    @Override
    public BrainsweepeeIngredientType<?> getType() {
        return BrainsweepeeIngredients.TAG;
    }

    public TagKey<EntityType<?>> getTag() {
        return entityTypeTag;
    }

    @Override
    public boolean test(Entity entity, ServerLevel level) {
        return entity.getType().is(this.entityTypeTag);
    }

    private static String tagKey(ResourceLocation tagLoc) {
        return "tag."
            + tagLoc.getNamespace()
            + "."
            + tagLoc.getPath().replace('/', '.');
    }

    @Override
    public Component getName() {
        String key = tagKey(this.entityTypeTag.location());
        boolean moddersDidAGoodJob = I18n.exists(key);
        return moddersDidAGoodJob
            ? Component.translatable(key)
            : Component.literal("#" + this.entityTypeTag.location());
    }

    @Override
    public List<Component> getTooltip(boolean advanced) {
        ResourceLocation loc = this.entityTypeTag.location();
        String key = tagKey(loc);
        boolean moddersDidAGoodJob = I18n.exists(key);

        var out = new ArrayList<Component>();
        out.add(moddersDidAGoodJob
            ? Component.translatable(key)
            : Component.literal("#" + loc));
        if (advanced && moddersDidAGoodJob) {
            // Print it anyways
            out.add(Component.literal("#" + loc).withStyle(ChatFormatting.DARK_GRAY));
        }

        out.add(BrainsweepeeIngredient.getModNameComponent(loc.getNamespace()));

        return out;
    }

    @Override
    public Entity exampleEntity(Level level) {
        var someEntityTys = BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(this.entityTypeTag).iterator();
        if (someEntityTys.hasNext()) {
            var someTy = someEntityTys.next();
            return someTy.value().create(level);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityTagIngredient that = (EntityTagIngredient) o;
        return Objects.equals(entityTypeTag, that.entityTypeTag);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.entityTypeTag);
    }


    public static class Type implements BrainsweepeeIngredientType<EntityTagIngredient> {
        public static final MapCodec<EntityTagIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TagKey.hashedCodec(Registries.ENTITY_TYPE).fieldOf("tag").forGetter(EntityTagIngredient::getTag)
        ).apply(instance, EntityTagIngredient::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, EntityTagIngredient> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC.map(id -> TagKey.create(Registries.ENTITY_TYPE, id), TagKey::location), EntityTagIngredient::getTag,
                EntityTagIngredient::new
        );

        @Override
        public MapCodec<EntityTagIngredient> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EntityTagIngredient> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
