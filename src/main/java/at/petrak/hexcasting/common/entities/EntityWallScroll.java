package at.petrak.hexcasting.common.entities;

import at.petrak.hexcasting.client.RenderLib;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityWallScroll extends HangingEntity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Boolean> SHOWS_STROKE_ORDER = SynchedEntityData.defineId(
        EntityWallScroll.class,
        EntityDataSerializers.BOOLEAN);

    public ItemStack scroll;
    public HexPattern pattern;
    public boolean isAncient;
    // Client-side only!
    public List<Vec2> zappyPoints;

    public EntityWallScroll(EntityType<? extends EntityWallScroll> type, Level world) {
        super(type, world);
    }

    public EntityWallScroll(Level world, BlockPos pos, Direction dir, ItemStack scroll) {
        super(HexEntities.WALL_SCROLL.get(), world, pos);
        this.loadDataFromScrollItem(scroll);
        this.setDirection(dir);
    }

    private void loadDataFromScrollItem(ItemStack scroll) {
        this.scroll = scroll;

        var tag = scroll.getTag();
        if (tag != null && tag.contains(ItemScroll.TAG_PATTERN, Tag.TAG_COMPOUND)) {
            this.pattern = HexPattern.DeserializeFromNBT(tag.getCompound(ItemScroll.TAG_PATTERN));
            if (this.level.isClientSide) {
                var pair = RenderLib.getCenteredPattern(pattern, 128, 128, 16f);
                var dots = pair.getSecond();
                this.zappyPoints = RenderLib.makeZappy(dots, 10f, 0.8f, 0f);
            }

            this.isAncient = tag.contains(ItemScroll.TAG_OP_ID, Tag.TAG_STRING);
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
        return 48;
    }

    @Override
    public int getHeight() {
        return 48;
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
        if (handStack.is(HexItems.AMETHYST_DUST.get()) && !this.getShowsStrokeOrder()) {
            if (!pPlayer.getAbilities().instabuild) {
                handStack.shrink(1);
            }
            this.setShowsStrokeOrder(true);

            pPlayer.level.playSound(pPlayer, this, HexSounds.SCROLL_DUST.get(), SoundSource.PLAYERS, 1f, 1f);
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
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        buf.writeVarInt(this.direction.ordinal());
        buf.writeItem(this.scroll);
        buf.writeBoolean(this.getShowsStrokeOrder());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        this.direction = Direction.values()[buf.readVarInt()];
        var scroll = buf.readItem();
        this.setShowsStrokeOrder(buf.readBoolean());

        this.loadDataFromScrollItem(scroll);
        this.setDirection(this.direction);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putByte("direction", (byte) this.direction.ordinal());
        tag.put("scroll", this.scroll.serializeNBT());
        tag.putBoolean("showsStrokeOrder", this.getShowsStrokeOrder());
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.direction = Direction.values()[tag.getByte("direction")];
        var scroll = ItemStack.of(tag.getCompound("scroll"));
        this.setShowsStrokeOrder(tag.getBoolean("showsStrokeOrder"));

        super.readAdditionalSaveData(tag);
        this.setDirection(this.direction);
        this.loadDataFromScrollItem(scroll);
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

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return this.scroll.copy();
    }
}
