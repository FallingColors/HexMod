package at.petrak.hexcasting.common.lib;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;

/**
 * Data component types for 1.21+ item/entity storage.
 * Replaces NBT-based storage for Iota and related data.
 * <p>
 * The platform (NeoForge/Fabric) must register these to
 * {@code Registries.DATA_COMPONENT_TYPE} during mod init, e.g.:
 * {@code event.register(Registries.DATA_COMPONENT_TYPE, HexAPI.modLoc("stack_data"), () -> HexDataComponents.STACK_DATA);}
 */
public final class HexDataComponents {
    /**
     * Holds a {@link CompoundTag} for item stack custom data (Iota, sealed, variant, etc.).
     * Used so that {@link at.petrak.hexcasting.api.utils.NBTHelper} can read/write
     * via this component on 1.21+ instead of the removed ItemStack tag.
     */
    public static final DataComponentType<CompoundTag> STACK_DATA = DataComponentType.<CompoundTag>builder()
        .persistent(CompoundTag.CODEC)
        .build();

    private HexDataComponents() {}
}
