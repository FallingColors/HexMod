package at.petrak.hexcasting.api.casting.circles;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.utils.MediaHelper;
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker;
import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Default impl for an impetus, not tecnically necessary but I'm exposing it for ease of use
 * <p>
 * This does assume a great deal so you might have to re-implement a lot of this yourself if you
 * wanna do something wild and new
 */
public abstract class BlockEntityAbstractImpetus extends HexBlockEntity implements WorldlyContainer {
    private static final DecimalFormat DUST_AMOUNT = new DecimalFormat("###,###.##");
    private static final long MAX_CAPACITY = 9_000_000_000_000_000_000L;

    public static final String
        TAG_EXECUTION_STATE = "executor",
        TAG_MEDIA = "media",
        TAG_ERROR_MSG = "errorMsg",
        TAG_ERROR_DISPLAY = "errorDisplay",
        TAG_PIGMENT = "pigment";

    // We might try to load the executor in loadModData when the level doesn't exist yet,
    // so save the tag and load it lazy
    @Nullable CompoundTag lazyExecutionState;
    @Nullable
    protected CircleExecutionState executionState;

    protected long media = 0;

    // these are null together
    @Nullable
    protected Component displayMsg = null;
    @Nullable
    protected ItemStack displayItem = null;
    @Nullable
    protected FrozenPigment pigment = null;


    public BlockEntityAbstractImpetus(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    public Direction getStartDirection() {
        return this.getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Nullable
    public Component getDisplayMsg() {
        return displayMsg;
    }

    public void clearDisplay() {
        this.displayMsg = null;
        this.displayItem = null;
        this.sync();
    }

    public void postDisplay(Component error, ItemStack display) {
        this.displayMsg = error;
        this.displayItem = display;
        this.sync();
    }

    public void postMishap(Component mishapDisplay) {
        this.postDisplay(mishapDisplay, new ItemStack(Items.MUSIC_DISC_11));
    }

    public void postPrint(Component printDisplay) {
        this.postDisplay(printDisplay, new ItemStack(Items.BOOK));
    }

    // Pull this out because we may need to call it both on startup and halfway thru
    public void postNoExits(BlockPos pos) {
        this.postDisplay(
            Component.translatable("hexcasting.tooltip.circle.no_exit",
                Component.literal(pos.toShortString()).withStyle(ChatFormatting.RED)),
            new ItemStack(Items.OAK_SIGN));
    }

    //region execution

    public void tickExecution() {
        if (this.level == null)
            return;

        this.setChanged();

        var state = this.getExecutionState();
        if (state == null) {
            return;
        }

        var shouldContinue = state.tick(this);

        if (!shouldContinue) {
            this.endExecution();
            this.executionState = null;
        } else
            this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), state.getTickSpeed());
    }

    public void endExecution() {
        if (this.executionState == null)
            return;

        this.executionState.endExecution(this);
    }

    /**
     * ONLY CALL THIS WHEN YOU KNOW THE WORLD EXISTS AND ON THE SERVER, lazy-loads it
     */
    public @Nullable CircleExecutionState getExecutionState() {
        if (this.level == null) {
            throw new IllegalStateException("didn't you read the doc comment, don't call this if the level is null");
        }

        if (this.executionState != null)
            return this.executionState;

        if (this.lazyExecutionState != null)
            this.executionState = CircleExecutionState.load(this.lazyExecutionState, (ServerLevel) this.level);

        return this.executionState;
    }

    public void startExecution(@Nullable ServerPlayer player) {
        if (this.level == null)
            return; // TODO: error here?
        if (this.level.isClientSide)
            return; // TODO: error here?

        if (this.executionState != null) {
            return;
        }
        var result = CircleExecutionState.createNew(this, player);
        if (result.isErr()) {
            var errPos = result.unwrapErr();
            if (errPos == null) {
                ICircleComponent.sfx(this.getBlockPos(), this.getBlockState(), this.level, null, false);
                this.postNoExits(this.getBlockPos());
            } else {
                ICircleComponent.sfx(errPos, this.level.getBlockState(errPos), this.level, null, false);
                this.postDisplay(Component.translatable("hexcasting.tooltip.circle.no_closure",
                        Component.literal(errPos.toShortString()).withStyle(ChatFormatting.RED)),
                    new ItemStack(Items.LEAD));
            }

            return;
        }
        this.executionState = result.unwrap();

        this.clearDisplay();
        var serverLevel = (ServerLevel) this.level;
        serverLevel.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(),
            this.executionState.getTickSpeed());
        serverLevel.setBlockAndUpdate(this.getBlockPos(),
            this.getBlockState().setValue(BlockCircleComponent.ENERGIZED, true));
    }

    @Contract(pure = true)
    protected static AABB getBounds(List<BlockPos> poses) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (var pos : poses) {
            if (pos.getX() < minX) {
                minX = pos.getX();
            }
            if (pos.getY() < minY) {
                minY = pos.getY();
            }
            if (pos.getZ() < minZ) {
                minZ = pos.getZ();
            }
            if (pos.getX() > maxX) {
                maxX = pos.getX();
            }
            if (pos.getY() > maxY) {
                maxY = pos.getY();
            }
            if (pos.getZ() > maxZ) {
                maxZ = pos.getZ();
            }
        }

        return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    //endregion

    //region media handling

    public long getMedia() {
        return this.media;
    }

    public void setMedia(long media) {
        this.media = media;
        sync();
    }

    public long extractMediaFromInsertedItem(ItemStack stack, boolean simulate) {
        if (this.media < 0) {
            return 0;
        }
        return MediaHelper.extractMedia(stack, remainingMediaCapacity(), true, simulate);
    }

    public void insertMedia(ItemStack stack) {
        if (getMedia() >= 0 && !stack.isEmpty() && stack.getItem() == HexItems.CREATIVE_UNLOCKER) {
            setInfiniteMedia();
            stack.shrink(1);
        } else {
            var mediamount = extractMediaFromInsertedItem(stack, false);
            if (mediamount > 0) {
                this.media = Math.min(mediamount + media, MAX_CAPACITY);
                this.sync();
            }
        }
    }

    public void setInfiniteMedia() {
        this.media = -1;
        this.sync();
    }

    public long remainingMediaCapacity() {
        if (this.media < 0) {
            return 0;
        }
        return Math.max(0, MAX_CAPACITY - this.media);
    }

    //endregion


    public FrozenPigment getPigment() {
        if (pigment != null)
            return pigment;
        if (executionState != null && executionState.casterPigment != null)
            return executionState.casterPigment;
        return FrozenPigment.DEFAULT.get();
    }

    public @Nullable FrozenPigment setPigment(@Nullable FrozenPigment pigment) {
        this.pigment = pigment;
        return this.pigment;
    }

    @Override
    protected void saveModData(CompoundTag tag) {
        if (this.executionState != null) {
            tag.put(TAG_EXECUTION_STATE, this.executionState.save());
        }

        tag.putLong(TAG_MEDIA, this.media);

        if (this.displayMsg != null && this.displayItem != null) {
            tag.putString(TAG_ERROR_MSG, Component.Serializer.toJson(this.displayMsg));
            var itemTag = new CompoundTag();
            this.displayItem.save(itemTag);
            tag.put(TAG_ERROR_DISPLAY, itemTag);
        }
        if (this.pigment != null)
            tag.put(TAG_PIGMENT, this.pigment.serializeToNBT());
    }

    @Override
    protected void loadModData(CompoundTag tag) {
        this.executionState = null;
        if (tag.contains(TAG_EXECUTION_STATE, Tag.TAG_COMPOUND)) {
            this.lazyExecutionState = tag.getCompound(TAG_EXECUTION_STATE);
        } else {
            this.lazyExecutionState = null;
        }

        if (tag.contains(TAG_MEDIA, Tag.TAG_LONG)) {
            this.media = tag.getLong(TAG_MEDIA);
        }

        if (tag.contains(TAG_ERROR_MSG, Tag.TAG_STRING) && tag.contains(TAG_ERROR_DISPLAY, Tag.TAG_COMPOUND)) {
            var msg = Component.Serializer.fromJson(tag.getString(TAG_ERROR_MSG));
            var display = ItemStack.of(tag.getCompound(TAG_ERROR_DISPLAY));
            this.displayMsg = msg;
            this.displayItem = display;
        } else {
            this.displayMsg = null;
            this.displayItem = null;
        }
        if (tag.contains(TAG_PIGMENT, Tag.TAG_COMPOUND))
            this.pigment = FrozenPigment.fromNBT(tag.getCompound(TAG_PIGMENT));
    }

    public void applyScryingLensOverlay(List<Pair<ItemStack, Component>> lines,
        BlockState state, BlockPos pos, Player observer, Level world, Direction hitFace) {
        if (world.getBlockEntity(pos) instanceof BlockEntityAbstractImpetus beai) {
            if (beai.getMedia() < 0) {
                lines.add(new Pair<>(new ItemStack(HexItems.AMETHYST_DUST), ItemCreativeUnlocker.infiniteMedia(world)));
            } else {
                var dustCount = (float) beai.getMedia() / (float) MediaConstants.DUST_UNIT;
                var dustCmp = Component.translatable("hexcasting.tooltip.media",
                    DUST_AMOUNT.format(dustCount));
                lines.add(new Pair<>(new ItemStack(HexItems.AMETHYST_DUST), dustCmp));
            }

            if (this.displayMsg != null && this.displayItem != null) {
                lines.add(new Pair<>(this.displayItem, this.displayMsg));
            }
        }
    }

    //region music

    protected int semitoneFromScale(int note) {
        var blockBelow = this.level.getBlockState(this.getBlockPos().below());
        var scale = MAJOR_SCALE;
        if (blockBelow.is(Blocks.CRYING_OBSIDIAN)) {
            scale = MINOR_SCALE;
        } else if (blockBelow.is(BlockTags.DOORS) || blockBelow.is(BlockTags.TRAPDOORS)) {
            scale = DORIAN_SCALE;
        } else if (blockBelow.is(Blocks.PISTON) || blockBelow.is(Blocks.STICKY_PISTON)) {
            scale = MIXOLYDIAN_SCALE;
        } else if (blockBelow.is(Blocks.BLUE_WOOL)
            || blockBelow.is(Blocks.BLUE_CONCRETE) || blockBelow.is(Blocks.BLUE_CONCRETE_POWDER)
            || blockBelow.is(Blocks.BLUE_TERRACOTTA) || blockBelow.is(Blocks.BLUE_GLAZED_TERRACOTTA)
            || blockBelow.is(Blocks.BLUE_STAINED_GLASS) || blockBelow.is(Blocks.BLUE_STAINED_GLASS_PANE)) {
            scale = BLUES_SCALE;
        } else if (blockBelow.is(Blocks.BONE_BLOCK)) {
            scale = BAD_TIME;
        } else if (blockBelow.is(Blocks.COMPOSTER)) {
            scale = SUSSY_BAKA;
        }

        note = Mth.clamp(note, 0, scale.length - 1);
        return scale[note];
    }

    // this is a good use of my time
    private static final int[] MAJOR_SCALE = {0, 2, 4, 5, 7, 9, 11, 12};
    private static final int[] MINOR_SCALE = {0, 2, 3, 5, 7, 8, 11, 12};
    private static final int[] DORIAN_SCALE = {0, 2, 3, 5, 7, 9, 10, 12};
    private static final int[] MIXOLYDIAN_SCALE = {0, 2, 4, 5, 7, 9, 10, 12};
    private static final int[] BLUES_SCALE = {0, 3, 5, 6, 7, 10, 12};
    private static final int[] BAD_TIME = {0, 0, 12, 7, 6, 5, 3, 0, 3, 5};
    private static final int[] SUSSY_BAKA = {5, 8, 10, 11, 10, 8, 5, 3, 7, 5};

    //endregion

    //region item handler contract stuff
    private static final int[] SLOTS = {0};

    @Override
    public int[] getSlotsForFace(Direction var1) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction dir) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int var1, ItemStack var2, Direction var3) {
        return false;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        insertMedia(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        // NO-OP
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (remainingMediaCapacity() == 0) {
            return false;
        }

        if (stack.is(HexItems.CREATIVE_UNLOCKER)) {
            return true;
        }

        var mediamount = extractMediaFromInsertedItem(stack, true);
        return mediamount > 0;
    }

    //endregion
}
