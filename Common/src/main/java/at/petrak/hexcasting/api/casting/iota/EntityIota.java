package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EntityIota extends Iota {


    record Payload(UUID uuid, Component name) {
    }

    public EntityIota(@NotNull Entity e) {
        this(e.getUUID(), e.getName());
    }

    private EntityIota(@NotNull UUID uuid, @NotNull Component name) {
        this(new Payload(uuid, name));
    }

    private EntityIota(Payload payload) {
        super(HexIotaTypes.ENTITY, payload);
    }

    private Boolean isTrueName = false;

    public Boolean isTrueName() {
        return isTrueName;
    }

    public Component getName() {
        return ((Payload) this.payload).name;
    }

    public UUID getUUID() {
        return ((Payload) this.payload).uuid;
    }

    @Nullable
    public Entity getEntity(ServerLevel world) {
        return world.getEntity(getUUID());
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that)
                && that instanceof EntityIota dent
                && this.getUUID() == dent.getUUID();
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    /**
     * @deprecated Use {@link EntityIota#TYPE#getCodec) instead.
     */
    @Deprecated
    @Override
    public @NotNull
    Tag serialize() {
        return HexUtils.serializeWithCodec(this, TYPE.getCodec());
    }

    @Override
    public Component display() {
        return this.getName().copy().withStyle(ChatFormatting.AQUA);
    }

    public static IotaType<EntityIota> TYPE = new IotaType<>() {

        @Override
        public Codec<EntityIota> getCodec() {
            return RecordCodecBuilder.<Payload>create(instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("uuid").forGetter(Payload::uuid),
                    ExtraCodecs.COMPONENT.fieldOf("name").forGetter(Payload::name)
            ).apply(instance, Payload::new)).xmap(EntityIota::new, iota -> (Payload) iota.payload);
        }

        @Override
        protected boolean validate(EntityIota iota, ServerLevel world) {
            var entity = iota.getEntity(world);
            if (entity == null) {
                return false;
            } else {
                iota.isTrueName = entity instanceof Player;
                return true;
            }
        }

        /**
         * @deprecated
         * Use {@link EntityIota#TYPE#getCodec} instead.
         */
        @Deprecated
        @Nullable
        @Override
        public EntityIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return HexUtils.deserializeWithCodec(tag, getCodec(world));
        }

        @Override
        public Component display(Tag tag) {
            var data = HexUtils.deserializeWithCodec(tag, getCodec());

            return data.getName().copy().withStyle(ChatFormatting.AQUA);
        }

        @Override
        public int color() {
            return 0xff_55ffff;
        }
    };
}
