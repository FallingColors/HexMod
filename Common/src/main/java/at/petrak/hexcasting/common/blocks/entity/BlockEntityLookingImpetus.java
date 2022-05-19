package at.petrak.hexcasting.common.blocks.entity;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class BlockEntityLookingImpetus extends BlockEntityAbstractImpetus {
    public static final int MAX_LOOK_AMOUNT = 30;
    public static final String TAG_LOOK_AMOUNT = "look_amount";

    private int lookAmount = 0;

    public BlockEntityLookingImpetus(BlockPos pWorldPosition, BlockState pBlockState) {
        super(HexBlockEntities.IMPETUS_LOOK_TILE, pWorldPosition, pBlockState);
    }

    @Override
    public boolean activatorAlwaysInRange() {
        return false;
    }

    // https://github.com/VazkiiMods/Botania/blob/2607bcd31c4eaeb617f7d1b3ec1c1db08f59add4/Common/src/main/java/vazkii/botania/common/block/tile/TileEnderEye.java#L27
    public static void serverTick(Level level, BlockPos pos, BlockState bs, BlockEntityLookingImpetus self) {
        if (bs.getValue(BlockCircleComponent.ENERGIZED)) {
            return;
        }

        int prevLookAmt = self.lookAmount;
        int range = 20;
        var players = level.getEntitiesOfClass(ServerPlayer.class,
            new AABB(pos.offset(-range, -range, -range), pos.offset(range, range, range)));

        ServerPlayer looker = null;
        for (var player : players) {
            // might as well impl this heh
            var hat = player.getItemBySlot(EquipmentSlot.HEAD);
            // sadly `isEnderMask` requires the enderman
            if (!hat.isEmpty() && hat.is(Blocks.CARVED_PUMPKIN.asItem())) {
                continue;
            }

            var lookEnd = player.getLookAngle().scale(range / 1.5f); // add some slop
            var hit = level.clip(new ClipContext(
                player.getEyePosition(),
                player.getEyePosition().add(lookEnd),
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE,
                player
            ));
            if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(pos)) {
                looker = player;
                break;
            }
        }

        var newLook = Mth.clamp(
            prevLookAmt + (looker == null ? -1 : 1),
            0,
            MAX_LOOK_AMOUNT
        );
        if (newLook != prevLookAmt) {
            if (newLook == MAX_LOOK_AMOUNT) {
                self.lookAmount = 0;
                self.activateSpellCircle(looker);
            } else {
                if (newLook % 5 == 1) {
                    var t = (float) newLook / MAX_LOOK_AMOUNT;
                    var pitch = Mth.lerp(t, 0.5f, 1.2f);
                    var volume = Mth.lerp(t, 0.2f, 1.2f);
                    level.playSound(null, pos, HexSounds.IMPETUS_LOOK_TICK, SoundSource.BLOCKS, volume, pitch);
                }
                self.lookAmount = newLook;
                self.setChanged();
            }
        }
    }

    @Override
    protected void saveModData(CompoundTag tag) {
        super.saveModData(tag);
        tag.putInt(TAG_LOOK_AMOUNT, this.lookAmount);
    }

    @Override
    protected void loadModData(CompoundTag tag) {
        super.loadModData(tag);
        this.lookAmount = tag.getInt(TAG_LOOK_AMOUNT);
    }
}
