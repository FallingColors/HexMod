package at.petrak.hexcasting.common.entities;

import at.petrak.hexcasting.client.RenderLib;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.hexmath.HexPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityWallScroll extends HangingEntity implements IEntityAdditionalSpawnData {
    public ItemStack scroll;
    public HexPattern pattern;
    public List<Vec2> zappyPoints;
    public boolean isAncient;

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
        if (tag != null) {
            var pattern = HexPattern.DeserializeFromNBT(tag.getCompound(ItemScroll.TAG_PATTERN));
            var pair = RenderLib.getCenteredPattern(pattern, 48, 48, 8f);
            var dots = pair.getSecond();
            this.zappyPoints = RenderLib.makeZappy(dots, 10f, 0.8f, 0f);

            this.isAncient = tag.contains(ItemScroll.TAG_OP_ID);
        } else {
            this.pattern = null;
            this.zappyPoints = null;
            this.isAncient = false;
        }
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
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        this.direction = Direction.values()[buf.readVarInt()];
        var scroll = buf.readItem();
        this.loadDataFromScrollItem(scroll);
        this.setDirection(this.direction);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putByte("direction", (byte) this.direction.ordinal());
        tag.put("scroll", this.scroll.serializeNBT());
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.direction = Direction.values()[tag.getByte("direction")];
        var scroll = ItemStack.EMPTY.copy();
        scroll.deserializeNBT(tag.getCompound("scroll"));
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
