package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.data.EntityInlineData;
import com.samsthenerd.inline.api.data.PlayerHeadData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;

public class EntityIota extends Iota {
    private final UUID entityId;
    @Nullable
    private WeakReference<Entity> cachedEntity;
    @Nullable
    private Component entityName;

    public EntityIota(@NotNull Entity e) {
        this(e.getUUID(), getEntityNameWithInline(e));
        this.cachedEntity = new WeakReference<>(e);
    }

    public EntityIota(UUID entityId, @Nullable Component entityName) {
        super(() -> HexIotaTypes.ENTITY);
        this.entityId = entityId;
        this.entityName = entityName;
    }

    public UUID getEntityId() {
        return entityId;
    }

    @Nullable
    public Entity getOrFindEntity(ServerLevel level) {
        // First, let's try to get it from weak reference
        var entity = getCachedEntity();
        if (entity != null)
            return entity;
        // Now let's try to fetch it from the world
        entity = level.getEntity(entityId);
        // Store in weak reference
        if (entity != null)
            cachedEntity = new WeakReference<>(entity);
        return entity;
    }

    @Nullable
    public Entity getCachedEntity() {
        if (cachedEntity != null)
            if (!cachedEntity.refersTo(null))
                return cachedEntity.get();
            else
                cachedEntity = null; // Clear weakref
        return null;
    }

    public @Nullable Component getEntityName() {
        var ent = getCachedEntity();
        if(ent != null) {
            var name = getEntityNameWithInline(ent);
            this.entityName = name;
            return name;
        }

        return getCachedEntityName();
    }

    public @Nullable Component getCachedEntityName() {
        return entityName;
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that)
                && that instanceof EntityIota dent
                && this.getEntityId() == dent.getEntityId();
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    public Component display() {
        var name = getCachedEntityName();
        return name != null ? name.copy().withStyle(ChatFormatting.AQUA) : Component.translatable("hexcasting.spelldata.entity.whoknows");
    }

    @Override
    public int hashCode() {
        return entityId.hashCode();
    }

    private static Component getEntityNameWithInline(Entity entity) {
        MutableComponent baseName = entity.getName().copy();
        Component inlineEnt;
        if(entity instanceof Player player){
            inlineEnt = new PlayerHeadData(new ResolvableProfile(player.getGameProfile())).asText(false);
            inlineEnt = inlineEnt.plainCopy().withStyle(InlineAPI.INSTANCE.withSizeModifier(inlineEnt.getStyle(), 1.5));
        } else {
            inlineEnt = EntityInlineData.fromType(entity.getType()).asText(false);
        }
        return baseName.append(Component.literal(": ")).append(inlineEnt);
    }

    public static IotaType<EntityIota> TYPE = new IotaType<>() {
        public static final MapCodec<EntityIota> CODEC = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        UUIDUtil.CODEC.fieldOf("entityId").forGetter(EntityIota::getEntityId),
                        ComponentSerialization.CODEC.optionalFieldOf("entityName").forGetter(iota -> Optional.ofNullable(iota.getEntityName()))
                ).apply(inst, (a, b) -> new EntityIota(a, b.orElse(null))));
        public static final StreamCodec<RegistryFriendlyByteBuf, EntityIota> STREAM_CODEC =
                StreamCodec.composite(
                        UUIDUtil.STREAM_CODEC, EntityIota::getEntityId,
                        ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), iota -> Optional.ofNullable(iota.getEntityName()),
                        (a, b) -> new EntityIota(a, b.orElse(null))
                );

        @Override
        public MapCodec<EntityIota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EntityIota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            return 0xff_55ffff;
        }
    };
}
