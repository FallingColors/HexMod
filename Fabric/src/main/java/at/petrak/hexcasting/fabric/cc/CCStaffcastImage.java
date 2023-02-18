package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

public class CCStaffcastImage implements Component {
    public static final String TAG_HARNESS = "harness";

    private final ServerPlayer owner;
    private CompoundTag lazyLoadedTag = new CompoundTag();

    public CCStaffcastImage(ServerPlayer owner) {
        this.owner = owner;
    }

    public CastingVM getHarness(InteractionHand hand) {
        var ctx = new CastingEnvironment(this.owner, hand, CastingEnvironment.CastSource.STAFF);
        if (this.lazyLoadedTag.isEmpty()) {
            return new CastingVM(ctx);
        } else {
            return CastingVM.fromNBT(this.lazyLoadedTag, ctx);
        }
    }

    public void setHarness(@Nullable CastingVM harness) {
        this.lazyLoadedTag = harness == null ? new CompoundTag() : harness.serializeToNBT();
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.lazyLoadedTag = tag.getCompound(TAG_HARNESS);
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.put(TAG_HARNESS, this.lazyLoadedTag);
    }
}
