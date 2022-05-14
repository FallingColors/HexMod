package at.petrak.hexcasting.common.blocks.entity;

import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus;
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

    private UUID storedPlayer = null;

    public BlockEntityStoredPlayerImpetus(BlockPos pWorldPosition, BlockState pBlockState) {
        super(HexBlockEntities.IMPETUS_STOREDPLAYER_TILE, pWorldPosition, pBlockState);
    }

    @Override
    public boolean activatorAlwaysInRange() {
        return true;
    }

    @Override
    protected @Nullable
    Player getPlayer() {
        return this.storedPlayer == null ? null : this.level.getPlayerByUUID(this.storedPlayer);
    }

    public void setPlayer(UUID player) {
        this.storedPlayer = player;
        this.setChanged();
    }

    // just feels wrong to use the protected method
    public @Nullable
    Player getStoredPlayer() {
        return this.getPlayer();
    }

    public void applyScryingLensOverlay(List<Pair<ItemStack, Component>> lines,
        BlockState state, BlockPos pos, LocalPlayer observer,
        ClientLevel world,
        Direction hitFace, InteractionHand lensHand) {
        super.applyScryingLensOverlay(lines, state, pos, observer, world, hitFace, lensHand);

        var bound = this.getPlayer();
        if (bound != null) {
            String name = bound.getScoreboardName();
            var head = new ItemStack(Items.PLAYER_HEAD);
            head.getOrCreateTag().putString("SkullOwner", name);
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
    }
}
