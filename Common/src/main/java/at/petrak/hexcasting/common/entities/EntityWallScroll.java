package at.petrak.hexcasting.common.entities;

import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.RenderLib;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.common.network.MsgNewWallScrollAck;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityWallScroll extends HangingEntity {
    private static final EntityDataAccessor<Boolean> SHOWS_STROKE_ORDER = SynchedEntityData.defineId(
        EntityWallScroll.class,
        EntityDataSerializers.BOOLEAN);

    public ItemStack scroll;
    public HexPattern pattern;
    public boolean isAncient;
    public int blockSize;
    // Client-side only!
    public List<Vec2> zappyPoints;

    public EntityWallScroll(EntityType<? extends EntityWallScroll> type, Level world) {
        super(type, world);
    }

    public EntityWallScroll(Level world, BlockPos pos, Direction dir, ItemStack scroll, boolean showStrokeOrder,
        int blockSize) {
        super(HexEntities.WALL_SCROLL, world, pos);
        this.setDirection(dir);
        this.blockSize = blockSize;
        this.setShowsStrokeOrder(showStrokeOrder);

        this.loadDataFromScrollItem(scroll);
        this.recalculateBoundingBox();
    }

    private void loadDataFromScrollItem(ItemStack scroll) {
        this.scroll = scroll;

        CompoundTag patternTag = NBTHelper.getCompound(scroll, ItemScroll.TAG_PATTERN);
        if (patternTag != null) {
            this.pattern = HexPattern.fromNBT(patternTag);
            if (this.level.isClientSide) {
                var pair = RenderLib.getCenteredPattern(pattern, 128f / 3 * blockSize, 128f / 3 * blockSize,
                    16f / 3 * blockSize);
                var dots = pair.getSecond();
                this.zappyPoints = RenderLib.makeZappy(dots, 10f, 0.8f, 0f, 0f);
            }

            this.isAncient = NBTHelper.hasString(scroll, ItemScroll.TAG_OP_ID);
        } else {
            this.pattern = null;
            this.zappyPoints = null;
            this.isAncient = false;
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHOWS_STROKE_ORDER, false);
    }

    public boolean getShowsStrokeOrder() {
        return this.entityData.get(SHOWS_STROKE_ORDER);
    }

    public void setShowsStrokeOrder(boolean b) {
        this.entityData.set(SHOWS_STROKE_ORDER, b);
    }

    @Override
    public int getWidth() {
        return 16 * blockSize;
    }

    @Override
    public int getHeight() {
        return 16 * blockSize;
    }

    @Override
    public void dropItem(@Nullable Entity pBrokenEntity) {
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (pBrokenEntity instanceof Player player) {
                if (player.getAbilities().instabuild) {
                    return;
                }
            }

            this.spawnAtLocation(this.scroll);
        }
    }

    @Override
    public InteractionResult interactAt(Player pPlayer, Vec3 pVec, InteractionHand pHand) {
        var handStack = pPlayer.getItemInHand(pHand);
        if (handStack.is(HexItems.AMETHYST_DUST) && !this.getShowsStrokeOrder()) {
            if (!pPlayer.getAbilities().instabuild) {
                handStack.shrink(1);
            }
            this.setShowsStrokeOrder(true);

            pPlayer.level.playSound(pPlayer, this, HexSounds.SCROLL_DUST, SoundSource.PLAYERS, 1f, 1f);
            return InteractionResult.SUCCESS;
        }
        return super.interactAt(pPlayer, pVec, pHand);
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return IXplatAbstractions.INSTANCE.toVanillaClientboundPacket(
            new MsgNewWallScrollAck(new ClientboundAddEntityPacket(this),
                pos, direction, scroll, getShowsStrokeOrder(), blockSize));
    }

    public void readSpawnData(BlockPos pos, Direction dir, ItemStack scrollItem,
        boolean showsStrokeOrder, int blockSize) {
        this.pos = pos;
        this.setDirection(dir);
        this.setShowsStrokeOrder(showsStrokeOrder);
        this.blockSize = blockSize;

        this.recalculateBoundingBox();
        this.loadDataFromScrollItem(scrollItem);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putByte("direction", (byte) this.direction.ordinal());
        tag.put("scroll", HexUtils.serializeToNBT(this.scroll));
        tag.putBoolean("showsStrokeOrder", this.getShowsStrokeOrder());
        tag.putInt("blockSize", this.blockSize);
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.direction = Direction.values()[tag.getByte("direction")];
        var scroll = ItemStack.of(tag.getCompound("scroll"));
        this.setShowsStrokeOrder(tag.getBoolean("showsStrokeOrder"));
        if (tag.contains("blockSize")) {
            this.blockSize = tag.getInt("blockSize");
        } else {
            this.blockSize = 3;
        }

        super.readAdditionalSaveData(tag);
        this.setDirection(this.direction);
        this.loadDataFromScrollItem(scroll);
        this.recalculateBoundingBox();
    }

    @Override
    public void moveTo(double pX, double pY, double pZ, float pYaw, float pPitch) {
        this.setPos(pX, pY, pZ);
    }

    @Override
    public void lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements,
        boolean pTeleport) {
        BlockPos blockpos = this.pos.offset(pX - this.getX(), pY - this.getY(), pZ - this.getZ());
        this.setPos(blockpos.getX(), blockpos.getY(), blockpos.getZ());
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return this.scroll.copy();
    }
}
