package at.petrak.hexcasting.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.util.List;

public class Keybinds {
    public static final String CATEGORY = "category.hexcasting.binds";

    public static KeyMapping spellbookPrev = new KeyMapping(
            "key.hexcasting.spellbook_prev",
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static KeyMapping spellbookNext = new KeyMapping(
            "key.hexcasting.spellbook_next",
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static List<KeyMapping> ALL_BINDS = List.of(spellbookPrev, spellbookNext);

    public static void clientTickEnd() {
        while (spellbookPrev.consumeClick()) {
            ShiftScrollListener.onScroll(-1, false);
        }

        while (spellbookNext.consumeClick()) {
            ShiftScrollListener.onScroll(1, false);
        }
    }
}
