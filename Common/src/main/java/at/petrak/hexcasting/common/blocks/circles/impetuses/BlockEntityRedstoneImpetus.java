package at.petrak.hexcasting.common.blocks.circles.impetuses;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class BlockEntityRedstoneImpetus extends BlockEntityAbstractImpetus {
    public static final String TAG_STORED_PLAYER = "stored_player";
    public static final String TAG_STORED_PLAYER_PROFILE = "stored_player_profile";

    private GameProfile storedPlayerProfile = null;
    private UUID storedPlayer = null;

    private GameProfile cachedDisplayProfile = null;
    private ItemStack cachedDisplayStack = null;

    public BlockEntityRedstoneImpetus(BlockPos pWorldPosition, BlockState pBlockState) {
        super(HexBlockEntities.IMPETUS_REDSTONE_TILE, pWorldPosition, pBlockState);
    }

    protected @Nullable
    GameProfile getPlayerName() {
        if (this.level instanceof ServerLevel) {
            Player player = getStoredPlayer();
            if (player != null) {
                return player.getGameProfile();
            }
        }

        return this.storedPlayerProfile;
    }

    public void setPlayer(GameProfile profile, UUID player) {
        this.storedPlayerProfile = profile;
        this.storedPlayer = player;
        this.setChanged();
    }

    public void clearPlayer() {
        this.storedPlayerProfile = null;
        this.storedPlayer = null;
    }

    public void updatePlayerProfile() {
        ServerPlayer player = getStoredPlayer();
        if (player != null) {
            GameProfile newProfile = player.getGameProfile();
            if (!newProfile.equals(this.storedPlayerProfile)) {
                this.storedPlayerProfile = newProfile;
                this.setChanged();
            }
        } else {
            this.storedPlayerProfile = null;
        }
    }

    // just feels wrong to use the protected method
    @Nullable
    public ServerPlayer getStoredPlayer() {
        if (this.storedPlayer == null) {
            return null;
        }
        if (!(this.level instanceof ServerLevel slevel)) {
            HexAPI.LOGGER.error("Called getStoredPlayer on the client");
            return null;
        }
        var e = slevel.getEntity(this.storedPlayer);
        if (e instanceof ServerPlayer player) {
            return player;
        } else {
            HexAPI.LOGGER.error("Entity {} stored in a cleric impetus wasn't a player somehow", e);
            return null;
        }
    }

    public void applyScryingLensOverlay(List<Pair<ItemStack, Component>> lines,
        BlockState state, BlockPos pos, Player observer,
        Level world,
        Direction hitFace) {
        super.applyScryingLensOverlay(lines, state, pos, observer, world, hitFace);

        var name = this.getPlayerName();
        if (name != null) {
            if (!name.equals(cachedDisplayProfile) || cachedDisplayStack == null) {
                cachedDisplayProfile = name;
                var head = new ItemStack(Items.PLAYER_HEAD);
                NBTHelper.put(head, "SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), name));
                head.getItem().verifyTagAfterLoad(head.getOrCreateTag());
                cachedDisplayStack = head;
            }
            lines.add(new Pair<>(cachedDisplayStack,
                Component.translatable("hexcasting.tooltip.lens.impetus.redstone.bound", name.getName())));
        } else {
            lines.add(new Pair<>(new ItemStack(Items.BARRIER),
                Component.translatable("hexcasting.tooltip.lens.impetus.redstone.bound.none")));
        }
    }

    @Override
    protected void saveModData(CompoundTag tag) {
        super.saveModData(tag);
        if (this.storedPlayer != null) {
            tag.putUUID(TAG_STORED_PLAYER, this.storedPlayer);
        }
        if (this.storedPlayerProfile != null) {
            tag.put(TAG_STORED_PLAYER_PROFILE, NbtUtils.writeGameProfile(new CompoundTag(), storedPlayerProfile));
        }
    }

    @Override
    protected void loadModData(CompoundTag tag) {
        super.loadModData(tag);
        if (tag.contains(TAG_STORED_PLAYER, Tag.TAG_INT_ARRAY)) {
            this.storedPlayer = tag.getUUID(TAG_STORED_PLAYER);
        } else {
            this.storedPlayer = null;
        }
        if (tag.contains(TAG_STORED_PLAYER_PROFILE, Tag.TAG_COMPOUND)) {
            this.storedPlayerProfile = NbtUtils.readGameProfile(tag.getCompound(TAG_STORED_PLAYER_PROFILE));
        } else {
            this.storedPlayerProfile = null;
        }
    }
}
