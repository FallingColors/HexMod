package at.petrak.hexcasting.api.block.circle;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.misc.FrozenColorizer;
import at.petrak.hexcasting.api.misc.ManaConstants;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.spell.ParticleSpray;
import at.petrak.hexcasting.api.spell.casting.CastingContext;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import at.petrak.hexcasting.api.spell.casting.SpellCircleContext;
import at.petrak.hexcasting.api.spell.iota.PatternIota;
import at.petrak.hexcasting.api.utils.ManaHelper;
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class BlockEntityAbstractImpetus extends HexBlockEntity implements WorldlyContainer {
    public static final String
        TAG_ACTIVATOR = "activator",
        TAG_COLORIZER = "colorizer",
        TAG_NEXT_BLOCK = "next_block",
        TAG_TRACKED_BLOCKS = "tracked_blocks",
        TAG_FOUND_ALL = "found_all",
        TAG_MANA = "mana",
        TAG_LAST_MISHAP = "last_mishap";

    @Nullable
    private UUID activator = null;
    @Nullable
    private FrozenColorizer colorizer = null;
    @Nullable
    private BlockPos nextBlock = null;
    @Nullable
    private List<BlockPos> trackedBlocks = null;
    private transient Set<BlockPos> knownBlocks = null;
    private boolean foundAll = false;
    @Nullable
    private Component lastMishap = null;

    private static final int MAX_CAPACITY = 2_000_000_000;

    private int mana = 0;

    public BlockEntityAbstractImpetus(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    abstract public boolean activatorAlwaysInRange();

    public int getMana() {
        return this.mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    @Nullable
    public Component getLastMishap() {
        return lastMishap;
    }

    public void setLastMishap(@Nullable Component lastMishap) {
        this.lastMishap = lastMishap;
    }

    public void activateSpellCircle(ServerPlayer activator) {
        if (this.nextBlock != null) {
            return;
        }
        this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), this.getTickSpeed());

        this.activator = activator.getUUID();
        this.nextBlock = this.getBlockPos();
        this.trackedBlocks = new ArrayList<>();
        this.knownBlocks = new HashSet<>();
        this.colorizer = IXplatAbstractions.INSTANCE.getColorizer(activator);

        this.level.setBlockAndUpdate(this.getBlockPos(),
            this.getBlockState().setValue(BlockAbstractImpetus.ENERGIZED, true));
        this.stepCircle();
    }

    public void applyScryingLensOverlay(List<Pair<ItemStack, Component>> lines,
        BlockState state, BlockPos pos,
        LocalPlayer observer, ClientLevel world,
        Direction hitFace, InteractionHand lensHand) {
        if (world.getBlockEntity(pos) instanceof BlockEntityAbstractImpetus beai) {
            if (beai.getMana() < 0) {
                lines.add(new Pair<>(new ItemStack(HexItems.AMETHYST_DUST), ItemCreativeUnlocker.infiniteMedia(world)));
            } else {
                var dustCount = (float) beai.getMana() / (float) ManaConstants.DUST_UNIT;
                var dustCmp = new TranslatableComponent("hexcasting.tooltip.lens.impetus.mana",
                    String.format("%.2f", dustCount));
                lines.add(new Pair<>(new ItemStack(HexItems.AMETHYST_DUST), dustCmp));
            }

            var mishap = this.getLastMishap();
            if (mishap != null) {
                lines.add(new Pair<>(new ItemStack(Items.MUSIC_DISC_11), mishap));
            }
        }
    }

    @Override
    protected void saveModData(CompoundTag tag) {
        if (this.activator != null && this.colorizer != null && this.nextBlock != null && this.trackedBlocks != null) {
            tag.putUUID(TAG_ACTIVATOR, this.activator);
            tag.put(TAG_NEXT_BLOCK, NbtUtils.writeBlockPos(this.nextBlock));
            tag.put(TAG_COLORIZER, this.colorizer.serializeToNBT());
            tag.putBoolean(TAG_FOUND_ALL, this.foundAll);

            var trackeds = new ListTag();
            for (var tracked : this.trackedBlocks) {
                trackeds.add(NbtUtils.writeBlockPos(tracked));
            }
            tag.put(TAG_TRACKED_BLOCKS, trackeds);
        }

        tag.putInt(TAG_MANA, this.mana);
        if (this.lastMishap != null) {
            tag.putString(TAG_LAST_MISHAP, Component.Serializer.toJson(this.lastMishap));
        }
    }

    @Override
    protected void loadModData(CompoundTag tag) {
        if (tag.contains(TAG_ACTIVATOR, Tag.TAG_INT_ARRAY) &&
            tag.contains(TAG_COLORIZER, Tag.TAG_COMPOUND) &&
            tag.contains(TAG_NEXT_BLOCK, Tag.TAG_COMPOUND) &&
            tag.contains(TAG_TRACKED_BLOCKS, Tag.TAG_LIST)) {
            this.activator = tag.getUUID(TAG_ACTIVATOR);
            this.colorizer = FrozenColorizer.fromNBT(tag.getCompound(TAG_COLORIZER));
            this.nextBlock = NbtUtils.readBlockPos(tag.getCompound(TAG_NEXT_BLOCK));
            this.foundAll = tag.getBoolean(TAG_FOUND_ALL);
            var trackeds = tag.getList(TAG_TRACKED_BLOCKS, Tag.TAG_COMPOUND);
            this.trackedBlocks = new ArrayList<>(trackeds.size());
            this.knownBlocks = new HashSet<>();
            for (var tracked : trackeds) {
                var pos = NbtUtils.readBlockPos((CompoundTag) tracked);
                this.trackedBlocks.add(pos);
                this.knownBlocks.add(pos);
            }
        } else {
            this.activator = null;
            this.colorizer = null;
            this.nextBlock = null;
            this.foundAll = false;
            this.trackedBlocks = new ArrayList<>();
            this.knownBlocks = new HashSet<>();
        }

        this.mana = tag.getInt(TAG_MANA);
        if (tag.contains(TAG_LAST_MISHAP, Tag.TAG_STRING)) {
            this.lastMishap = Component.Serializer.fromJson(tag.getString(TAG_LAST_MISHAP));
        } else {
            this.lastMishap = null;
        }
    }

    void stepCircle() {
        this.setChanged();

        // haha which silly idiot would have done something like this
        if (this.activator == null || this.colorizer == null || this.nextBlock == null || this.trackedBlocks == null) {
            return;
        }

        var possibleErrorPos = this.checkEverythingOk();
        if (possibleErrorPos != null) {
            this.sfx(possibleErrorPos, false);
            this.stopCasting();
            return;
        }

        if (this.foundAll) {
            this.clearEnergized();
            this.castSpell();
            this.stopCasting();
            return;
        }

        // This should only fail if we remove blocks halfway through casting
        var bsHere = this.level.getBlockState(this.nextBlock);
        if (!this.trackedBlocks.isEmpty() && bsHere.getBlock() instanceof BlockAbstractImpetus) {
            // no two impetuses!
            this.sfx(this.nextBlock, false);
            this.stopCasting();
            return;
        }
        var blockHere = bsHere.getBlock();
        if (!(blockHere instanceof BlockCircleComponent cc)) {
            this.sfx(this.nextBlock, false);
            this.stopCasting();
            return;
        }
        // Awesome we know this block is OK
        var thisNormal = cc.normalDir(this.nextBlock, bsHere, this.level);
        var possibleExits = cc.exitDirections(this.nextBlock, bsHere, this.level);
        BlockPos foundPos = null;
        for (var exit : possibleExits) {
            var neighborPos = this.nextBlock.relative(exit);
            var blockThere = this.level.getBlockState(neighborPos);
            // at this point, we haven't actually added nextBlock to trackedBlocks
            // so, in the smallest circle case (a 2x2), this will have a size of 3 (with this block being the 4th).
            var closedLoop = (this.trackedBlocks.size() >= 3 && this.trackedBlocks.get(0).equals(neighborPos));
            var mightBeOkThere = closedLoop
                || this.trackedBlocks.isEmpty()
                || !this.trackedBlocks.get(this.trackedBlocks.size() - 1).equals(neighborPos);
            if (mightBeOkThere
                && blockThere.getBlock() instanceof BlockCircleComponent cc2
                && cc2.canEnterFromDirection(exit.getOpposite(), thisNormal, neighborPos, blockThere, this.level)
                // another good use for the implies operator 😩
                && (!blockThere.getValue(BlockCircleComponent.ENERGIZED) || this.knownBlocks.contains(neighborPos))) {
                if (foundPos == null) {
                    foundPos = neighborPos;
                    this.foundAll |= closedLoop;
                } else {
                    // uh oh, fork in the road
                    this.sfx(this.nextBlock, false);
                    this.stopCasting();
                    return;
                }
            }
        }
        if (foundPos != null) {
            // pog
            this.trackedBlocks.add(this.nextBlock);
            this.knownBlocks.add(this.nextBlock);
            this.nextBlock = foundPos;
        } else {
            // end of the line
            this.sfx(this.nextBlock, false);
            this.stopCasting();
            return;
        }

        var lastPos = this.trackedBlocks.get(this.trackedBlocks.size() - 1);
        var justTrackedBlock = this.level.getBlockState(lastPos);
        this.level.setBlockAndUpdate(lastPos, justTrackedBlock.setValue(BlockCircleComponent.ENERGIZED, true));
        this.sfx(lastPos, true);

        this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), this.getTickSpeed());
    }

    private void castSpell() {
        var player = this.getPlayer();

        if (player instanceof ServerPlayer splayer) {
            var bounds = getBounds(this.trackedBlocks);

            var ctx = new CastingContext(splayer, InteractionHand.MAIN_HAND,
                new SpellCircleContext(this.getBlockPos(), bounds, this.activatorAlwaysInRange()));
            var harness = new CastingHarness(ctx);

            var makeSound = false;
            BlockPos erroredPos = null;
            for (var tracked : this.trackedBlocks) {
                var bs = this.level.getBlockState(tracked);
                if (bs.getBlock() instanceof BlockCircleComponent cc) {
                    var newPattern = cc.getPattern(tracked, bs, this.level);
                    if (newPattern != null) {
                        var info = harness.executeIota(new PatternIota(newPattern), splayer.getLevel());
                        if (info.getMakesCastSound()) {
                            makeSound = true;
                        }
                        if (!info.getResolutionType().getSuccess()) {
                            erroredPos = tracked;
                            break;
                        }
                    }
                }
            }

            if (makeSound) {
                this.level.playSound(null, this.getBlockPos(), HexSounds.SPELL_CIRCLE_CAST, SoundSource.BLOCKS,
                    2f, 1f);
            }

            if (erroredPos != null) {
                this.sfx(erroredPos, false);
            } else {
                this.setLastMishap(null);
            }

            this.setChanged();
        }
    }

    @Contract(pure = true)
    private static AABB getBounds(List<BlockPos> poses) {
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

    @Nullable
    private BlockPos checkEverythingOk() {
        // if they logged out or changed dimensions or something
        if (this.getPlayer() == null) {
            return this.getBlockPos();
        }

        for (var pos : this.trackedBlocks) {
            if (!(this.level.getBlockState(pos).getBlock() instanceof BlockCircleComponent)) {
                return pos;
            }
        }

        if (this.trackedBlocks.size() > HexConfig.server().maxSpellCircleLength()) {
            return this.trackedBlocks.get(this.trackedBlocks.size() - 1);
        }

        return null;
    }

    private void sfx(BlockPos pos, boolean success) {
        Vec3 vpos;
        Vec3 vecOutDir;

        var bs = this.level.getBlockState(pos);
        if (bs.getBlock() instanceof BlockCircleComponent bcc) {
            var outDir = bcc.normalDir(pos, bs, this.level);
            var height = bcc.particleHeight(pos, bs, this.level);
            vecOutDir = new Vec3(outDir.step());
            vpos = Vec3.atCenterOf(pos).add(vecOutDir.scale(height));
        } else {
            // we probably are doing this because it's an error and we removed a block
            vpos = Vec3.atCenterOf(pos);
            vecOutDir = new Vec3(0, 0, 0);
        }

        if (this.level instanceof ServerLevel serverLevel) {
            var spray = new ParticleSpray(vpos, vecOutDir.scale(success ? 1.0 : 1.5), success ? 0.1 : 0.5,
                Mth.PI / (success ? 4 : 2), success ? 30 : 100);
            spray.sprayParticles(serverLevel,
                success ? this.colorizer : new FrozenColorizer(new ItemStack(HexItems.DYE_COLORIZERS.get(DyeColor.RED)),
                    this.activator));
        }

        var pitch = 1f;
        var sound = HexSounds.SPELL_CIRCLE_FAIL;
        if (success) {
            sound = HexSounds.SPELL_CIRCLE_FIND_BLOCK;
            // This is a good use of my time
            var note = this.trackedBlocks.size() - 1;
            var semitone = this.semitoneFromScale(note);
            pitch = (float) Math.pow(2.0, (semitone - 8) / 12d);
        }
        level.playSound(null, vpos.x, vpos.y, vpos.z, sound, SoundSource.BLOCKS, 1f, pitch);
    }

    protected void clearEnergized() {
        if (this.trackedBlocks != null) {
            for (var tracked : this.trackedBlocks) {
                var bs = this.level.getBlockState(tracked);
                if (bs.getBlock() instanceof BlockCircleComponent) {
                    this.level.setBlockAndUpdate(tracked, bs.setValue(BlockCircleComponent.ENERGIZED, false));
                }
            }
        }
    }

    protected void stopCasting() {
        clearEnergized();

        this.activator = null;
        this.nextBlock = null;
        this.trackedBlocks = null;
        this.foundAll = false;

        // without this check, breaking the block will just immediately replace it with
        // the new unenergized state
        if (this.level.getBlockState(this.getBlockPos()).getBlock() instanceof BlockAbstractImpetus) {
            this.level.setBlockAndUpdate(this.getBlockPos(),
                this.getBlockState().setValue(BlockCircleComponent.ENERGIZED, false));
        }
    }

    @Nullable
    protected Player getPlayer() {
        return this.level.getPlayerByUUID(this.activator);
    }

    protected int getTickSpeed() {
        if (this.trackedBlocks == null) {
            return 10;
        } else {
            return Math.max(2, 10 - trackedBlocks.size() / 3);
        }
    }

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
        insertMana(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        var manamount = extractManaFromItem(stack, true);
        return manamount > 0;
    }

    @Override
    public void clearContent() {
        this.mana = 0;
        this.stopCasting();
        this.sync();
    }

    public int extractManaFromItem(ItemStack stack, boolean simulate) {
        if (this.mana < 0)
            return 0;
        return ManaHelper.extractMana(stack, remainingManaCapacity(), true, simulate);
    }

    public void insertMana(ItemStack stack) {
        if (getMana() >= 0 && !stack.isEmpty() && stack.getItem() == HexItems.CREATIVE_UNLOCKER) {
            setInfiniteMana();
            stack.shrink(1);
        } else {
            var manamount = extractManaFromItem(stack, false);
            if (manamount > 0) {
                this.mana += manamount;
                this.sync();
            }
        }
    }

    public void setInfiniteMana() {
        this.mana = -1;
        this.sync();
    }

    public int remainingManaCapacity() {
        if (this.mana < 0)
            return 0;
        return MAX_CAPACITY - this.mana;
    }
}
