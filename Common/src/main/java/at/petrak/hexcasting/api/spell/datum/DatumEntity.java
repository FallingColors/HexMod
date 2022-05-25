package at.petrak.hexcasting.api.spell.datum;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatumEntity extends SpellDatum {
    protected DatumEntity(@NotNull Entity e) {
        super(e);
    }

    public Entity getEntity() {
        return (Entity) this.datum;
    }

    @Override
    public boolean equalsOther(SpellDatum that) {
        return that instanceof DatumEntity dent && this.getEntity() == dent.getEntity();
    }

    @Override
    public @NotNull Tag serialize() {
        var out = new CompoundTag();
        out.putUUID("uuid", this.getEntity().getUUID());
        out.putString("name", Component.Serializer.toJson(this.getEntity().getDisplayName()));
        return out;
    }

    public static SpellDatum.Type<DatumEntity> TYPE = new Type<>() {

        @Nullable
        @Override
        public DatumEntity deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return null;
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
