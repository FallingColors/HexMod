package at.petrak.hexcasting.common.casting.env;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.utils.HexUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public abstract class PlayerBasedCastEnv extends CastingEnvironment {
    protected final ServerPlayer caster;
    protected final InteractionHand castingHand;

    protected PlayerBasedCastEnv(ServerPlayer caster, InteractionHand castingHand) {
        super(caster.getLevel());
        this.caster = caster;
        this.castingHand = castingHand;
    }

    @Override
    protected List<ItemStack> getUsableStacks(StackDiscoveryMode mode) {
        return switch (mode) {
            case QUERY -> {
                var out = new ArrayList<ItemStack>();

                var offhand = this.caster.getItemInHand(HexUtils.otherHand(this.castingHand));
                if (!offhand.isEmpty()) {
                    out.add(offhand);
                }

                // If we're casting from the main hand, try to pick from the slot one to the right of the selected slot
                // Otherwise, scan the hotbar left to right
                var anchorSlot = this.castingHand == InteractionHand.MAIN_HAND
                    ? (this.caster.getInventory().selected + 1) % 9
                    : 0;


                for (int delta = 0; delta < 9; delta++) {
                    var slot = (anchorSlot + delta) % 9;
                    out.add(this.caster.getInventory().getItem(slot));
                }

                yield out;
            }
            case EXTRACTION -> {
                // https://wiki.vg/Inventory is WRONG
                // slots 0-8 are the hotbar
                // for what purpose i cannot imagine
                // http://redditpublic.com/images/b/b2/Items_slot_number.png looks right
                // and offhand is 150 Inventory.java:464
                var out = new ArrayList<ItemStack>();

                // First, the inventory backwards
                Inventory inv = this.caster.getInventory();
                for (int i = inv.getContainerSize(); i >= 0; i--) {
                    if (i != inv.selected) {
                        out.add(inv.getItem(i));
                    }
                }

                // then the offhand, then the selected hand
                out.addAll(inv.offhand);
                out.add(inv.getSelected());

                yield out;
            }
        };
    }

    @Override
    public boolean isVecInRange(Vec3 vec) {
        var sentinel = HexAPI.instance().getSentinel(this.caster);
        if (sentinel != null
            && sentinel.extendsRange()
            && world.dimension() == sentinel.dimension()
            && vec.distanceToSqr(sentinel.position()) <= Action.MAX_DISTANCE_FROM_SENTINEL * Action.MAX_DISTANCE_FROM_SENTINEL
        ) {
            return true;
        }

        return vec.distanceToSqr(this.caster.position()) <= Action.MAX_DISTANCE * Action.MAX_DISTANCE;
    }

    @Override
    public ItemStack getAlternateItem() {
        var otherHand = HexUtils.otherHand(this.castingHand);
        var stack = this.caster.getItemInHand(otherHand);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY.copy();
        } else {
            return stack;
        }
    }

    /**
     * Search the player's inventory for media ADs and use them.
     * <p>
     * TODO it looks like i'm going to strip out DiscoveryHandlers entirely and replace it. Was anyone hoping to make a
     * media source that isn't item-based? Might be worth scanning for ADMediaHolders on the player themself.
     * <p>
     * TODO vis a vis that stop special-casing overcasting
     */
    protected long extractMediaFromInventory(long cost) {

    }
}
