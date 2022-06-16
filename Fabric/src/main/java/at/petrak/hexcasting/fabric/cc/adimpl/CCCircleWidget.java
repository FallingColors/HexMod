package at.petrak.hexcasting.fabric.cc.adimpl;

import at.petrak.hexcasting.api.addldata.ADCircleWidget;
import at.petrak.hexcasting.api.block.circle.BlockEntitySidedCircleWidget;
import at.petrak.hexcasting.api.circles.BlockEdge;
import at.petrak.hexcasting.api.circles.FlowUpdate;
import at.petrak.hexcasting.api.circles.ICircleState;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public abstract class CCCircleWidget implements ADCircleWidget, Component, AutoSyncedComponent {
    // The serde methods here are no-ops because any and all state should be synced by the implementor.
    @Override
    public void readFromNbt(CompoundTag tag) {
        // NO-OP
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        // NO-OP
    }

    public static class SidedCircleWidgetBased extends CCCircleWidget {
        private final BlockEntitySidedCircleWidget inner;

        public SidedCircleWidgetBased(BlockEntitySidedCircleWidget inner) {
            this.inner = inner;
        }

        @Override
        public boolean acceptsFlow(BlockEdge edge) {
            var margin = inner.getMargin(edge);
            if (margin == null) {
                return false;
            }
            return inner.acceptsFlow(margin);
        }

        @Override
        public EnumSet<BlockEdge> possibleExitDirs(BlockEdge inputEdge) {
            var margin = inner.getMargin(inputEdge);
            if (margin == null) {
                return EnumSet.noneOf(BlockEdge.class);
            }
            return inner.possibleExitDirs(margin);
        }

        @Override
        public @Nullable FlowUpdate onReceiveFlow(BlockEdge inputEdge, BlockEntity sender, ICircleState state) {
            var margin = inner.getMargin(inputEdge);
            if (margin == null) {
                return null;
            }
            return inner.onReceiveFlow(margin, sender, state);
        }
    }
}
