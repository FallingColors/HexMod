package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
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

    /**
     * Turn the saved image into a VM in a player staffcasting environment
     */
    public CastingVM getVM(InteractionHand hand) {
        var img = this.lazyLoadedTag.isEmpty()
            ? new CastingImage()
            : CastingImage.loadFromNbt(this.lazyLoadedTag, this.owner.serverLevel());
        var env = new StaffCastEnv(this.owner, hand);
        return new CastingVM(img, env);
    }

    public void setImage(@Nullable CastingImage image) {
        this.lazyLoadedTag =
            image == null
                ? new CompoundTag()
                : image.serializeToNbt();
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
