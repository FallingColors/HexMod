package at.petrak.hexcasting.api.spell.iota;

import at.petrak.hexcasting.api.spell.SpellList;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
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

    @Override
    public @NotNull Tag serialize() {
        var out = new ListTag();
        for (var subdatum : this.getList()) {
            out.add(HexIotaTypes.serialize(subdatum));
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

            return new ListIota(out);
        }

        @Override
        public Component display(Tag tag) {
            var out = new TextComponent("[").withStyle(ChatFormatting.DARK_PURPLE);
            var list = HexUtils.downcast(tag, ListTag.TYPE);
            for (int i = 0; i < list.size(); i++) {
                Tag sub = list.get(i);
                var csub = HexUtils.downcast(sub, CompoundTag.TYPE);

                out.append(HexIotaTypes.getDisplay(csub));

                if (i < list.size() - 1) {
                    out.append(",");
                }
            }
            out.append(new TextComponent("]").withStyle(ChatFormatting.DARK_PURPLE));
            return out;
        }

        @Override
        public List<FormattedCharSequence> displayWithWidth(Tag tag, int maxWidth, Font font) {
            // We aim to not break one iota between lines
            var listTag = HexUtils.downcast(tag, ListTag.TYPE);

            var start = FormattedCharSequence.forward("[", Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE));
            var cursor = font.width(start);
            var currentLine = new ArrayList<>(List.of(start));
            var out = new ArrayList<FormattedCharSequence>();

            for (int i = 0; i < listTag.size(); i++) {
                Tag subtag = listTag.get(i);
                var cSubtag = HexUtils.downcast(subtag, CompoundTag.TYPE);
                var translation = HexIotaTypes.getDisplay(cSubtag);
                var currentElement = translation.getVisualOrderText();
                String addl;
                if (i < listTag.size() - 1) {
                    addl = ", ";
                } else {
                    // Last go-around, so add the closing bracket
                    addl = "]";
                }
                currentElement = FormattedCharSequence.composite(currentElement,
                    FormattedCharSequence.forward(addl, Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)));

                var width = font.width(currentElement);

                if (cursor + width > maxWidth) {
                    out.add(FormattedCharSequence.composite(currentLine));
                    currentLine = new ArrayList<>();
                    // Indent further lines by two spaces
                    var indentation = FormattedCharSequence.forward("  ", Style.EMPTY);
                    var lineStart = FormattedCharSequence.composite(indentation, currentElement);
                    currentLine.add(lineStart);
                    cursor = font.width(lineStart);
                } else {
                    currentLine.add(currentElement);
                    cursor += width;
                }
            }

            if (!currentLine.isEmpty()) {
                out.add(FormattedCharSequence.composite(currentLine));
            }

            return out;
        }

        @Override
        public int color() {
            return 0xff_aa00aa;
        }
    };
}
