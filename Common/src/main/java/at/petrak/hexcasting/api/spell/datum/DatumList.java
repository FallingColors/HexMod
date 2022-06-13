package at.petrak.hexcasting.api.spell.datum;

import at.petrak.hexcasting.api.spell.SpellList;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * Similar to {@link DatumWidget}, this is a <i>wrapper</i> for {@link SpellList}.
 */
public class DatumList extends Iota {
    public DatumList(@NotNull SpellList list) {
        super(list);
    }

    public SpellList getList() {
        return (SpellList) this.payload;
    }

    @Override
    public boolean toleratesOther(Iota that) {
        var a = this.getList();
        if (!(that instanceof DatumList list)) {
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
            if (!SpellDatums.equalsWithTolerance(x, y)) {
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
}
