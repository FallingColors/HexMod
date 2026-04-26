package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;

public class CCStaffcastImage implements Component {
    public static final String TAG_VM = "harness";

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
            : CastingImage.getCODEC().parse(NbtOps.INSTANCE, lazyLoadedTag).getOrThrow();
        var env = new StaffCastEnv(this.owner, hand);
        return new CastingVM(img, env);
    }

    public void setImage(@Nullable CastingImage image) {
        this.lazyLoadedTag =
            image == null
                ? new CompoundTag()
                : (CompoundTag) CastingImage.getCODEC().encode(image, NbtOps.INSTANCE, new CompoundTag()).getOrThrow();
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider provider) {
        this.lazyLoadedTag = tag.getCompound(TAG_VM);
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put(TAG_VM, this.lazyLoadedTag);
    }
}
