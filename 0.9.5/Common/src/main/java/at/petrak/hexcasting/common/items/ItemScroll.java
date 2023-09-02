package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.api.item.DataHolderItem;
import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.gui.PatternTooltipGreeble;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * TAG_OP_ID and TAG_PATTERN: "Ancient Scroll of %s" (Great Spells)
 * <br>
 * TAG_PATTERN: "Scroll" (custom)
 * <br>
 * (none): "Empty Scroll"
 * <br>
 * TAG_OP_ID: invalid
 */
public class ItemScroll extends Item implements DataHolderItem {
    public static final String TAG_OP_ID = "op_id";
    public static final String TAG_PATTERN = "pattern";
    public static final ResourceLocation ANCIENT_PREDICATE = modLoc("ancient");

    public final int blockSize;

    public ItemScroll(Properties pProperties, int blockSize) {
        super(pProperties);
        this.blockSize = blockSize;
    }

    @Override
    public @Nullable CompoundTag readDatumTag(ItemStack stack) {
        CompoundTag pattern = NBTHelper.getCompound(stack, TAG_PATTERN);
        if (pattern == null) {
            return null;
        }
        var out = new CompoundTag();
        out.put(SpellDatum.TAG_PATTERN, pattern);
        return out;
    }

    @Override
    public boolean canWrite(ItemStack stack, SpellDatum<?> datum) {
        return datum != null && datum.getType() == DatumType.PATTERN && !NBTHelper.hasCompound(stack, TAG_PATTERN);
    }

    @Override
    public void writeDatum(ItemStack stack, SpellDatum<?> datum) {
        if (this.canWrite(stack, datum) && datum.getPayload() instanceof HexPattern pat) {
            NBTHelper.putCompound(stack, TAG_PATTERN, pat.serializeToNBT());
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
            return new TranslatableComponent(descID + ".of",
                new TranslatableComponent("hexcasting.spell." + ResourceLocation.tryParse(ancientId)));
        } else if (NBTHelper.hasCompound(pStack, TAG_PATTERN)) {
            return new TranslatableComponent(descID);
        } else {
            return new TranslatableComponent(descID + ".empty");
        }
    }

    // purposely no hover text

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var compound = NBTHelper.getCompound(stack, ItemScroll.TAG_PATTERN);
        if (compound != null) {
            var pattern = HexPattern.fromNBT(compound);
            return Optional.of(new PatternTooltipGreeble(
                pattern,
                NBTHelper.hasString(stack,
                    ItemScroll.TAG_OP_ID) ? PatternTooltipGreeble.ANCIENT_BG : PatternTooltipGreeble.PRISTINE_BG));
        }

        return Optional.empty();
    }
}
