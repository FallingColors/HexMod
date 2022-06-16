package at.petrak.hexcasting.common.blocks.entity;

import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class BlockEntityStoredPlayerImpetus extends BlockEntityAbstractImpetus {
    public static final String TAG_STORED_PLAYER = "stored_player";
    public static final String TAG_STORED_PLAYER_PROFILE = "stored_player_profile";

    private GameProfile storedPlayerProfile = null;
    private UUID storedPlayer = null;

    private GameProfile cachedDisplayProfile = null;
    private ItemStack cachedDisplayStack = null;

    public BlockEntityStoredPlayerImpetus(BlockPos pWorldPosition, BlockState pBlockState) {
        super(HexBlockEntities.IMPETUS_STOREDPLAYER_TILE, pWorldPosition, pBlockState);
    }

    @Override
    public boolean activatorAlwaysInRange() {
        return true;
    }

    @Override
    protected @Nullable Player getPlayer() {
        return this.storedPlayer == null ? null : this.level.getPlayerByUUID(this.storedPlayer);
    }

    protected @Nullable GameProfile getPlayerName() {
        Player player = getStoredPlayer();
        if (player != null) {
            return player.getGameProfile();
        }

        return this.storedPlayerProfile;
    }

    public void setPlayer(GameProfile profile, UUID player) {
        this.storedPlayerProfile = profile;
        this.storedPlayer = player;
        this.setChanged();
    }

    public void updatePlayerProfile() {
        Player player = getStoredPlayer();
        if (player != null) {
            GameProfile newProfile = player.getGameProfile();
            if (!newProfile.equals(this.storedPlayerProfile)) {
                this.storedPlayerProfile = newProfile;
                this.setChanged();
            }
        }
    }

    // just feels wrong to use the protected method
    public @Nullable Player getStoredPlayer() {
        return this.getPlayer();
    }

    public void applyScryingLensOverlay(List<Pair<ItemStack, Component>> lines,
        BlockState state, BlockPos pos, LocalPlayer observer,
        ClientLevel world,
        Direction hitFace, InteractionHand lensHand) {
        super.applyScryingLensOverlay(lines, state, pos, observer, world, hitFace, lensHand);

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
                 new TranslatableComponent("hexcasting.tooltip.lens.impetus.storedplayer", name.getName())));
        } else {
            lines.add(new Pair<>(new ItemStack(Items.BARRIER),
                new TranslatableComponent("hexcasting.tooltip.lens.impetus.storedplayer.none")));
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
