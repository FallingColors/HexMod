package at.petrak.hexcasting.common.blocks.entity;

import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
    public static final String TAG_STORED_PLAYER_NAME = "stored_player_name";

    private String storedPlayerName = null;
    private UUID storedPlayer = null;

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

    protected @Nullable String getPlayerName() {
        Player player = getStoredPlayer();
        if (player != null) {
            return player.getScoreboardName();
        }

        return this.storedPlayerName;
    }

    public void setPlayer(String name, UUID player) {
        this.storedPlayerName = name;
        this.storedPlayer = player;
        this.setChanged();
    }

    public void updatePlayerName() {
        Player player = getStoredPlayer();
        if (player != null) {
            String newName = player.getScoreboardName();
            if (!newName.equals(this.storedPlayerName)) {
                this.storedPlayerName = newName;
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
            var head = new ItemStack(Items.PLAYER_HEAD);
            NBTHelper.putString(head, "SkullOwner", name);
            lines.add(
                new Pair<>(head, new TranslatableComponent("hexcasting.tooltip.lens.impetus.storedplayer", name)));
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
            tag.putUUID(TAG_STORED_PLAYER_NAME, this.storedPlayer);
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
        if (tag.contains(TAG_STORED_PLAYER_NAME, Tag.TAG_STRING)) {
            this.storedPlayerName = tag.getString(TAG_STORED_PLAYER_NAME);
        } else {
            this.storedPlayer = null;
        }
    }
}
