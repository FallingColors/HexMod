package at.petrak.hexcasting.common.items.storage;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.gui.PatternTooltipComponent;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.common.misc.PatternTooltip;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.interop.inline.InlinePatternData;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
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
    public static final ResourceLocation ANCIENT_PREDICATE = modLoc("ancient");

    public final int blockSize;

    public ItemScroll(Properties pProperties, int blockSize) {
        super(pProperties);
        this.blockSize = blockSize;
    }

    // this produces a scroll that will load the correct pattern for your world once it ticks
    public static ItemStack withPerWorldPattern(ItemStack stack, ResourceKey<ActionRegistryEntry> action) {
        Item item = stack.getItem();
        if (item instanceof ItemScroll) {
            stack.set(HexDataComponents.ACTION, action);
        }

        return stack;
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
                stack.set(HexDataComponents.PATTERN, pat.getPattern());
            } else if (datum == null) {
                stack.remove(HexDataComponents.PATTERN);
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
        var customData = itemstack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            EntityType.updateCustomEntityTag(level, player, scrollEntity, customData);
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
        var ancientAction = pStack.get(HexDataComponents.ACTION);
        if (ancientAction != null) {
            return Component.translatable(descID + ".of",
                Component.translatable("hexcasting.action." + ancientAction.location()));
        } else if (pStack.has(HexDataComponents.PATTERN)) {
            var pattern = pStack.get(HexDataComponents.PATTERN);
            var patternLabel = Component.literal("");
            if (pattern != null) {
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
        if(pStack.has(HexDataComponents.NEEDS_PURCHASE))
            pStack.remove(HexDataComponents.NEEDS_PURCHASE);
        // if op_id is set but there's no stored pattern, attempt to load the pattern on inv tick
        if (pStack.has(HexDataComponents.ACTION) && !pStack.has(HexDataComponents.PATTERN) && pEntity.getServer() != null) {
            var action = pStack.get(HexDataComponents.ACTION);
            var pat = PatternRegistryManifest.getCanonicalStrokesPerWorld(action, pEntity.getServer().overworld());
            pStack.set(HexDataComponents.PATTERN, pat);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (stack.has(HexDataComponents.NEEDS_PURCHASE)) {
            var needsPurchase = Component.translatable("hexcasting.tooltip.scroll.needs_purchase");
            tooltipComponents.add(needsPurchase.withStyle(ChatFormatting.GRAY));
        } else if (stack.has(HexDataComponents.ACTION) && !stack.has(HexDataComponents.PATTERN)) {
            var notLoaded = Component.translatable("hexcasting.tooltip.scroll.pattern_not_loaded");
            tooltipComponents.add(notLoaded.withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var pattern = stack.get(HexDataComponents.PATTERN);
        if (pattern != null && !stack.has(HexDataComponents.NEEDS_PURCHASE)) {
            return Optional.of(new PatternTooltip(
                pattern,
                    stack.has(HexDataComponents.ACTION)
                    ? PatternTooltipComponent.ANCIENT_BG
                    : PatternTooltipComponent.PRISTINE_BG));
        }

        return Optional.empty();
    }
}
