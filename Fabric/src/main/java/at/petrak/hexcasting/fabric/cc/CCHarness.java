package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.spell.casting.CastingContext;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

public class CCHarness implements Component {
    public static final String TAG_HARNESS = "harness";

    private final ServerPlayer owner;
    private CompoundTag lazyLoadedTag = new CompoundTag();

    public CCHarness(ServerPlayer owner) {
        this.owner = owner;
    }

    public CastingHarness getHarness(InteractionHand hand) {
        var ctx = new CastingContext(this.owner, hand);
        if (this.lazyLoadedTag.isEmpty()) {
            return new CastingHarness(ctx);
        } else {
            return CastingHarness.fromNBT(this.lazyLoadedTag, ctx);
        }
    }

    public void setHarness(@Nullable CastingHarness harness) {
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
