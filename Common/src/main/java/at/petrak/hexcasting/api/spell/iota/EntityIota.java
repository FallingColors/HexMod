package at.petrak.hexcasting.api.spell.iota;

import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityIota extends Iota {
    protected EntityIota(@NotNull Entity e) {
        super(HexIotaTypes.ENTITY, e);
    }

    public Entity getEntity() {
        return (Entity) this.payload;
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that)
            && that instanceof EntityIota dent
            && this.getEntity() == dent.getEntity();
    }

    @Override
    public @NotNull Tag serialize() {
        var out = new CompoundTag();
        out.putUUID("uuid", this.getEntity().getUUID());
        out.putString("name", Component.Serializer.toJson(this.getEntity().getDisplayName()));
        return out;
    }

    public static IotaType<EntityIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public EntityIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var uuid = NbtUtils.loadUUID(tag);
            var entity = world.getEntity(uuid);
            if (entity == null) {
                return null;
            }
            return new EntityIota(entity);
        }

        @Override
        public Component display(Tag tag) {
            return null;
        }

        @Override
        public int color() {
            return 0;
        }
    };
}
