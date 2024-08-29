package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.SpellList;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a <i>wrapper</i> for {@link SpellList}.
 */
public class ListIota extends Iota {
    public ListIota(@NotNull SpellList list) {
        super(HexIotaTypes.LIST, list);
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

    /**
     * @deprecated
     * use {@link ListIota#TYPE#getCodec} instead.
     */
    @Deprecated
    @Override
    public @NotNull Tag serialize() {
        return HexUtils.serializeWithCodec(this, TYPE.getCodec());
    }

    @Override
    public @Nullable Iterable<Iota> subIotas() {
        return this.getList();
    }

    public static IotaType<ListIota> TYPE = new IotaType<>() {
        @Override
        public Codec<ListIota> getCodec() {
            return SpellList.getCodec().xmap(ListIota::new, ListIota::getList);
        }

        @Override
        public Codec<ListIota> getCodec(ServerLevel world) {
            return SpellList.getCodec(world).xmap(ListIota::new, ListIota::getList);
        }

        /**
         * @deprecated
         * use {@link ListIota#TYPE#getCodec} instead.
         */
        @Deprecated
        @Nullable
        @Override
        public ListIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return HexUtils.deserializeWithCodec(tag, getCodec(world));
        }

        @Override
        public Component display(Tag tag) {
            var out = Component.empty();

            var list = HexUtils.downcast(tag, ListTag.TYPE);
            for (int i = 0; i < list.size(); i++) {
                Tag sub = list.get(i);
                var csub = HexUtils.downcast(sub, CompoundTag.TYPE);

                out.append(IotaType.getDisplay(csub));

                if (i < list.size() - 1) {
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
