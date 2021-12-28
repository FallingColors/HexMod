package at.petrak.hex.common.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class ItemSpellbook extends Item {
    public static String TAG_SELECTED_PAGE = "page_idx";
    // this is a CompoundTag of string numerical keys to SpellData
    public static String TAG_PAGES = "pages";

    public ItemSpellbook(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
            TooltipFlag isAdvanced) {
        var tag = stack.getOrCreateTag();
        if (tag.contains(TAG_SELECTED_PAGE)) {
            var pageIdx = tag.getInt(TAG_SELECTED_PAGE);
            var pages = tag.getCompound(ItemSpellbook.TAG_PAGES);
            tooltip.add(new TranslatableComponent("hex.spellbook.tooltip.page", pageIdx, HighestPage(pages)));

            var key = String.valueOf(pageIdx);
            if (pages.contains(key)) {
                var datum = pages.getCompound(String.valueOf(pageIdx));
                // I know this is ugly i dont care
                tooltip.add(new TextComponent(datum.toString()));
            }
        }
    }

    public static int HighestPage(CompoundTag tag) {
        var highestKey = tag.getAllKeys().stream().flatMap(s -> {
            try {
                return Stream.of(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                return Stream.empty();
            }
        }).max(Integer::compare);
        return highestKey.orElse(0);
    }

    public static void RotatePageIdx(CompoundTag tag, boolean increase) {
        int newIdx;
        if (tag.contains(ItemSpellbook.TAG_SELECTED_PAGE)) {
            var delta = increase ? 1 : -1;
            newIdx = Math.max(0, tag.getInt(ItemSpellbook.TAG_SELECTED_PAGE) + delta);
        } else {
            newIdx = 0;
        }
        tag.putInt(ItemSpellbook.TAG_SELECTED_PAGE, newIdx);
    }
}
