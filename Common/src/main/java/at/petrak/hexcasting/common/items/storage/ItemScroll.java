package at.petrak.hexcasting.common.items.storage;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.gui.PatternTooltipComponent;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.common.misc.PatternTooltip;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.interop.inline.InlinePatternData;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * TAG_OP_ID and TAG_PATTERN: "Ancient Scroll of %s" (per-world pattern preloaded)
 * <br>
 * TAG_OP_ID: "Ancient Scroll of %s" (per-world pattern loaded on inv tick)
 * <br>
 * TAG_PATTERN: "Scroll" (custom)
 * <br>
 * (none): "Empty Scroll"
 */
public class ItemScroll extends Item implements IotaHolderItem {
    public static final String TAG_OP_ID = "op_id";
    public static final String TAG_PATTERN = "pattern";
    public static final String TAG_NEEDS_PURCHASE = "needs_purchase";
    public static final ResourceLocation ANCIENT_PREDICATE = modLoc("ancient");

    public final int blockSize;

    public ItemScroll(Properties pProperties, int blockSize) {
        super(pProperties);
        this.blockSize = blockSize;
    }

    // this produces a scroll that will load the correct pattern for your world once it ticks
    public static ItemStack withPerWorldPattern(ItemStack stack, String op_id) {
        Item item = stack.getItem();
        if (item instanceof ItemScroll)
            NBTHelper.putString(stack, TAG_OP_ID, op_id);

        return stack;
    }

    @Override
    public @Nullable
    CompoundTag readIotaTag(ItemStack stack) {
        CompoundTag pattern = NBTHelper.getCompound(stack, TAG_PATTERN);
        if (pattern == null) {
            return null;
        }
        // We store only the data part of the iota; pretend the rest of it's there
        var out = new CompoundTag();
        out.putString(HexIotaTypes.KEY_TYPE, "hexcasting:pattern");
        out.put(HexIotaTypes.KEY_DATA, pattern);
        return out;
    }

    @Override
    public boolean writeable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canWrite(ItemStack stack, Iota datum) {
        return datum instanceof PatternIota || datum == null;
    }

    @Override
    public void writeDatum(ItemStack stack, Iota datum) {
        if (this.canWrite(stack, datum)) {
            if (datum instanceof PatternIota pat) {
                NBTHelper.putCompound(stack, TAG_PATTERN, pat.getPattern().serializeToNBT());
            } else if (datum == null) {
                NBTHelper.remove(stack, TAG_PATTERN);
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        var posClicked = ctx.getClickedPos();
        var direction = ctx.getClickedFace();
        var posInFront = posClicked.relative(direction);
        Player player = ctx.getPlayer();
        ItemStack itemstack = ctx.getItemInHand();
        if (player != null && !this.mayPlace(player, direction, itemstack, posInFront)) {
            return InteractionResult.FAIL;
        }
        var level = ctx.getLevel();
        var scrollStack = itemstack.copy();
        scrollStack.setCount(1);
        var scrollEntity = new EntityWallScroll(level, posInFront, direction, scrollStack, false, this.blockSize);

        // i guess
        var stackTag = itemstack.getTag();
        if (stackTag != null) {
            EntityType.updateCustomEntityTag(level, player, scrollEntity, stackTag);
        }

        if (scrollEntity.survives()) {
            if (!level.isClientSide) {
                scrollEntity.playPlacementSound();
                level.gameEvent(player, GameEvent.ENTITY_PLACE, posClicked);
                level.addFreshEntity(scrollEntity);
            }

            itemstack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return InteractionResult.CONSUME;
        }
    }

    // [VanillaCopy] of HangingEntityItem
    protected boolean mayPlace(Player pPlayer, Direction pDirection, ItemStack pHangingEntityStack, BlockPos pPos) {
        return !pDirection.getAxis().isVertical() && pPlayer.mayUseItemAt(pPos, pDirection, pHangingEntityStack);
    }

    @Override
    public Component getName(ItemStack pStack) {
        var descID = this.getDescriptionId(pStack);
        var ancientId = NBTHelper.getString(pStack, TAG_OP_ID);
        if (ancientId != null) {
            return Component.translatable(descID + ".of",
                Component.translatable("hexcasting.action." + ResourceLocation.tryParse(ancientId)));
        } else if (NBTHelper.hasCompound(pStack, TAG_PATTERN)) {
            var compound = NBTHelper.getCompound(pStack, TAG_PATTERN);
            var patternLabel = Component.literal("");
            if (compound != null) {
                var pattern = HexPattern.fromNBT(compound);
                patternLabel = Component.literal(": ").append(new InlinePatternData(pattern).asText(false));
            }
            return Component.translatable(descID).append(patternLabel);
        } else {
            return Component.translatable(descID + ".empty");
        }
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        // the needs_purchase tag is used so you can't see the pattern on scrolls sold by a wandering trader
        // once you put the scroll into your inventory, this removes the tag to reveal the pattern
        if (NBTHelper.getBoolean(pStack, TAG_NEEDS_PURCHASE)) {
            NBTHelper.remove(pStack, TAG_NEEDS_PURCHASE);
        }
        // if op_id is set but there's no stored pattern, attempt to load the pattern on inv tick
        if (NBTHelper.hasString(pStack, TAG_OP_ID) && !NBTHelper.hasCompound(pStack, TAG_PATTERN) && pEntity.getServer() != null) {
            var opID = ResourceLocation.tryParse(NBTHelper.getString(pStack, TAG_OP_ID));
            if (opID == null) {
                // if the provided op_id is invalid, remove it so we don't keep trying every tick
                NBTHelper.remove(pStack, TAG_OP_ID);
                return;
            }
            var patternKey = ResourceKey.create(IXplatAbstractions.INSTANCE.getActionRegistry().key(), opID);
            var pat = PatternRegistryManifest.getCanonicalStrokesPerWorld(patternKey, pEntity.getServer().overworld());
            NBTHelper.put(pStack, TAG_PATTERN, pat.serializeToNBT());
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        if (NBTHelper.getBoolean(pStack, TAG_NEEDS_PURCHASE)) {
            var needsPurchase = Component.translatable("hexcasting.tooltip.scroll.needs_purchase");
            pTooltipComponents.add(needsPurchase.withStyle(ChatFormatting.GRAY));
        } else if (NBTHelper.hasString(pStack, TAG_OP_ID) && !NBTHelper.hasCompound(pStack, TAG_PATTERN)) {
            var notLoaded = Component.translatable("hexcasting.tooltip.scroll.pattern_not_loaded");
            pTooltipComponents.add(notLoaded.withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var compound = NBTHelper.getCompound(stack, TAG_PATTERN);
        if (compound != null && !NBTHelper.getBoolean(stack, TAG_NEEDS_PURCHASE)) {
            var pattern = HexPattern.fromNBT(compound);
            return Optional.of(new PatternTooltip(
                pattern,
                NBTHelper.hasString(stack, TAG_OP_ID)
                    ? PatternTooltipComponent.ANCIENT_BG
                    : PatternTooltipComponent.PRISTINE_BG));
        }

        return Optional.empty();
    }
}
