package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
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
    public JsonObject serialize() {
        var obj = new JsonObject();
        obj.addProperty("type", Type.ENTITY_TAG.getSerializedName());

        obj.addProperty("tag", this.entityTypeTag.location().toString());

        return obj;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.entityTypeTag.location());
    }

    public static EntityTagIngredient deserialize(JsonObject obj) {
        var tagLoc = ResourceLocation.tryParse(GsonHelper.getAsString(obj, "tag"));
        if (tagLoc == null) {
            throw new IllegalArgumentException("unknown tag " + obj);
        }
        var type = TagKey.create(Registries.ENTITY_TYPE, tagLoc);
        return new EntityTagIngredient(type);
    }

    public static EntityTagIngredient read(FriendlyByteBuf buf) {
        var typeLoc = buf.readResourceLocation();
        var type = TagKey.create(Registries.ENTITY_TYPE, typeLoc);
        return new EntityTagIngredient(type);
    }

    @Override
    public Type ingrType() {
        return Type.ENTITY_TAG;
    }

    @Override
    public String getSomeKindOfReasonableIDForEmi() {
        var resloc = this.entityTypeTag.location();
        return resloc.getNamespace()
            + "//"
            + resloc.getPath();
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
}
