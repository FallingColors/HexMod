package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.common.items.storage.ItemAbacus;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.common.msgs.MsgShiftScrollC2S;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * indicates that an item should respond to scrolling
 * @see ItemSpellbook
 * @see ItemAbacus
 * @see MsgShiftScrollC2S
 */
public interface ScrollableItem {
    /**
     * Scroll an item that may or may not be held by a player.
     * @param stack Mutable stack to be scrolled
     * @param delta uhhhhhh idk TODO: figure out the units of this
     * @param modified if control (or equivalent remapped modifier key) is held down
     * @param hand in general base hex usage this is just which hand the item is in.
     *             Addons can use this method without a player, so in those cases consider it
     *             another agreed upon modifier.
     * @param holder the entity that is holding this item. In base hex this will just be the player.
     *               More generally, consider it just an associated entity (ex: could be in an itemframe)
     */
    void scroll(ItemStack stack, int delta, boolean modified, InteractionHand hand, @Nullable Entity holder);
}
