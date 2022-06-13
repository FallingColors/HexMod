package at.petrak.hexcasting.api.spell.iota;

import at.petrak.hexcasting.api.spell.SpellList;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * This is a <i>wrapper</i> for {@link SpellList}.
 */
public class ListIota extends Iota {
    public ListIota(@NotNull SpellList list) {
        super(HexIotaTypes.LIST, list);
    }

    public SpellList getList() {
        return (SpellList) this.payload;
    }

    @Override
    public boolean toleratesOther(Iota that) {
        if (!typesMatch(this, that)) {
            return false;
        }
        var a = this.getList();
        if (!(that instanceof ListIota list)) {
            return false;
        }
        var b = list.getList();

        SpellList.SpellListIterator aIter = a.iterator(), bIter = b.iterator();
        for (; ; ) {
            if (!aIter.hasNext() && !bIter.hasNext()) {
                // we ran out together!
                return true;
            }
            if (aIter.hasNext() != bIter.hasNext()) {
                // one remains full before the other
                return false;
            }
            Iota x = aIter.next(), y = bIter.next();
            if (!Iota.tolerates(x, y)) {
                return false;
            }
        }
    }

    @Override
    public @NotNull Tag serialize() {
        var out = new ListTag();
        for (var subdatum : this.getList()) {
            out.add(subdatum.serialize());
        }
        return out;
    }

    public static IotaType<ListIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public ListIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var listTag = HexUtils.downcast(tag, ListTag.TYPE);
            var out = new ArrayList<Iota>(listTag.size());

            for (var sub : listTag) {
                var csub = HexUtils.downcast(sub, CompoundTag.TYPE);
                var subiota = HexIotaTypes.deserialize(csub, world);
                if (subiota == null) {
                    return null;
                }
                out.add(subiota);
            }

            return new ListIota(new SpellList.LList(out));
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
