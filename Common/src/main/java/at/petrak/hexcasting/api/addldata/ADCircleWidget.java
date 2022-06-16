package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.circles.BlockEdge;
import at.petrak.hexcasting.api.circles.FlowUpdate;
import at.petrak.hexcasting.api.circles.ICircleState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Additional data attached to block entities that make them work as parts in a Spell Circle.
 * <p>
 * Yes, sadly they do actually have to be block entities. Thank Forge.
 * <p>
 * I would love to call this something less silly than "widget" but the word "component" is already taken by
 * Fabric.
 * <p>
 * See also {@link at.petrak.hexcasting.api.circles api.circles}.
 */
public interface ADCircleWidget {
    /**
     * Whether this block accepts flow from the given edge.
     * <p>
     * This should be immutably based on the block's state. A single slate would output {@code true} for
     * all the edges it is connected to, for example.
     */
    boolean acceptsFlow(BlockEdge edge);

    /**
     * Directions this block <i>can</i> disperse flow to.
     * <p>
     * This should be immutably based on the block's state. A directrix would output both of the edges it can
     * exit from, for example.
     */
    EnumSet<BlockEdge> possibleExitDirs(BlockEdge inputEdge);

    /**
     * What this block should do upon receiving the flow.
     * <p>
     * Returning {@code null} signals there was a problem somehow, like if it got an input from an edge it didn't
     * expect. In that case the circle will throw a {@link at.petrak.hexcasting.api.spell.mishaps.MishapMessedUpSpellCircle
     * MishapMessedUpSpellCircle}
     */
    @Nullable
    FlowUpdate onReceiveFlow(BlockEdge inputEdge, BlockEntity sender, ICircleState state);
}
