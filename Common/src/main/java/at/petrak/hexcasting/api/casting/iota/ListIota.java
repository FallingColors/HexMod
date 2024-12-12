package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.casting.SpellList;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.api.mod.HexConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

/**
 * This is a <i>wrapper</i> for {@link SpellList}.
 */
public class ListIota extends Iota {
    private final int depth;
    private final int size;

    public ListIota(@NotNull SpellList list) {
        super(HexIotaTypes.LIST, list);
        int maxChildDepth = 0;
        int totalSize = 1;
        for (Iota iota : list) {
            totalSize += iota.size();
            maxChildDepth = max(maxChildDepth, iota.depth());
        }
        depth = maxChildDepth + 1;
        size = totalSize;
    }

    public ListIota(@NotNull List<Iota> list) {
        this(new SpellList.LList(list));
    }

    public SpellList getList() {
        return (SpellList) this.payload;
    }

    @Override
    public boolean isTruthy() {
        return this.getList().getNonEmpty();
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
            out.add(IotaType.serialize(subdatum));
        }
        return out;
    }

    @Override
    public @Nullable Iterable<Iota> subIotas() {
        return this.getList();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int depth() {
        return depth;
    }

    public static IotaType<ListIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public ListIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var listTag = HexUtils.downcast(tag, ListTag.TYPE);
            var out = new ArrayList<Iota>(listTag.size());

            for (var sub : listTag) {
                var csub = HexUtils.downcast(sub, CompoundTag.TYPE);
                var subiota = IotaType.deserialize(csub, world);
                if (subiota == null) {
                    return null;
                }
                out.add(subiota);
            }

            return new ListIota(out);
        }

        @Override
        public Component display(Tag tag) {
            var out = Component.empty();
            var list = HexUtils.downcast(tag, ListTag.TYPE);
            for (int i = 0; i < list.size(); i++) {
                Tag sub = list.get(i);
                var csub = HexUtils.downcast(sub, CompoundTag.TYPE);

                out.append(IotaType.getDisplay(csub));

                // only add a comma between 2 non-patterns (commas don't look good with Inline patterns)
                // TODO: maybe add a method on IotaType to allow it to opt out of commas?
                if (i < list.size() - 1) {
                    var thisIotaNeedsComma = IotaType.getTypeFromTag(csub) != PatternIota.TYPE;
                    var nextIotaNeedsComma = IotaType.getTypeFromTag(HexUtils.downcast(list.get(i+1), CompoundTag.TYPE)) != PatternIota.TYPE;
                    var alwaysShowCommas = HexConfig.client().alwaysShowListCommas();
                    if (thisIotaNeedsComma || nextIotaNeedsComma || alwaysShowCommas)
                        out.append(", ");
                }
            }
            return Component.translatable("hexcasting.tooltip.list_contents", out).withStyle(ChatFormatting.DARK_PURPLE);
        }

        @Override
        public int color() {
            return 0xff_aa00aa;
        }
    };
}
