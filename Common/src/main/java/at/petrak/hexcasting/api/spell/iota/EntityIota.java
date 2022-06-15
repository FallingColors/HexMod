package at.petrak.hexcasting.api.spell.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityIota extends Iota {
    public EntityIota(@NotNull Entity e) {
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
        out.putString("name", Component.Serializer.toJson(this.getEntity().getName()));
        return out;
    }

    @Override
    public Component display() {
        return this.getEntity().getName().copy().withStyle(ChatFormatting.AQUA);
    }

    public static IotaType<EntityIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public EntityIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var ctag = HexUtils.downcast(tag, CompoundTag.TYPE);
            Tag uuidTag = ctag.get("uuid");
            if (uuidTag == null) {
                return null;
            }
            var uuid = NbtUtils.loadUUID(uuidTag);
            var entity = world.getEntity(uuid);
            if (entity == null) {
                return null;
            }
            return new EntityIota(entity);
        }

        @Override
        public Component display(Tag tag) {
            if (!(tag instanceof CompoundTag ctag)) {
                return new TranslatableComponent("hexcasting.spelldata.entity.whoknows");
            }
            if (!ctag.contains("name", Tag.TAG_STRING)) {
                return new TranslatableComponent("hexcasting.spelldata.entity.whoknows");
            }
            var nameJson = ctag.getString("name");
            return Component.Serializer.fromJsonLenient(nameJson).withStyle(ChatFormatting.AQUA);
        }

        @Override
        public int color() {
            return 0xff_55ffff;
        }
    };
}
