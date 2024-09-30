package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.data.EntityInlineData;
import com.samsthenerd.inline.api.data.PlayerHeadData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
    public boolean isTruthy() {
        return true;
    }

    @Override
    public @NotNull
    Tag serialize() {
        var out = new CompoundTag();
        out.putUUID("uuid", this.getEntity().getUUID());
        out.putString("name", Component.Serializer.toJson(getEntityNameWithInline(true)));
        return out;
    }

    @Override
    public Component display() {
        return getEntityNameWithInline(false).copy().withStyle(ChatFormatting.AQUA);
    }

    private Component getEntityNameWithInline(boolean fearSerializer){
        MutableComponent baseName = this.getEntity().getName().copy();
        Component inlineEnt = null;
        if(this.getEntity() instanceof Player player){
            inlineEnt = new PlayerHeadData(player.getGameProfile()).asText(!fearSerializer);
            inlineEnt = inlineEnt.plainCopy().withStyle(InlineAPI.INSTANCE.withSizeModifier(inlineEnt.getStyle(), 1.5));
        } else{
            if(fearSerializer){ // we don't want to have to serialize an entity just to display it
                inlineEnt = EntityInlineData.fromType(this.getEntity().getType()).asText(!fearSerializer);
            } else {
                inlineEnt = EntityInlineData.fromEntity(this.getEntity()).asText(!fearSerializer);
            }
        }
        return baseName.append(Component.literal(": ")).append(inlineEnt);
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
                return Component.translatable("hexcasting.spelldata.entity.whoknows");
            }
            if (!ctag.contains("name", Tag.TAG_STRING)) {
                return Component.translatable("hexcasting.spelldata.entity.whoknows");
            }
            var nameJson = ctag.getString("name");
//            return Component.literal(nameJson);
            return Component.Serializer.fromJsonLenient(nameJson).withStyle(ChatFormatting.AQUA);
        }

        @Override
        public int color() {
            return 0xff_55ffff;
        }
    };
}
