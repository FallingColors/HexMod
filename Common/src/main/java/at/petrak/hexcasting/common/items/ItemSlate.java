package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.item.DataHolderItem;
import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.client.gui.PatternTooltipGreeble;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemSlate extends BlockItem implements DataHolderItem {
    public static final ResourceLocation WRITTEN_PRED = modLoc("written");

    public ItemSlate(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        var key = "block." + HexAPI.MOD_ID + ".slate." + (hasPattern(pStack) ? "written" : "blank");
        return new TranslatableComponent(key);
    }

    public static boolean hasPattern(ItemStack stack) {
        var tag = stack.getTag();
        if (tag != null && tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            var bet = tag.getCompound("BlockEntityTag");
            return bet.contains(BlockEntitySlate.TAG_PATTERN, Tag.TAG_COMPOUND) && !bet.getCompound(
                BlockEntitySlate.TAG_PATTERN).isEmpty();
        }
        return false;
    }

    // TODO: what the hell does this do and how to do it on forge
    @SoftImplement("forge")
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        var tag = stack.getTagElement("BlockEntityTag");
        if (tag != null && tag.isEmpty()) {
            stack.removeTagKey("BlockEntityTag");
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        var tag = pStack.getTagElement("BlockEntityTag");
        if (tag != null && tag.isEmpty()) {
            pStack.removeTagKey("BlockEntityTag");
        }
    }

    @Override
    public @Nullable CompoundTag readDatumTag(ItemStack stack) {
        var stackTag = stack.getTag();
        if (stackTag == null || !stackTag.contains("BlockEntityTag")) {
            return null;
        }
        var beTag = stackTag.getCompound("BlockEntityTag");
        if (!beTag.contains(BlockEntitySlate.TAG_PATTERN, Tag.TAG_COMPOUND)) {
            return null;
        }

        var patTag = beTag.getCompound(BlockEntitySlate.TAG_PATTERN);
        if (patTag.isEmpty()) {
            return null;
        }
        var out = new CompoundTag();
        out.put(SpellDatum.TAG_PATTERN, patTag);
        return out;
    }

    @Override
    public boolean canWrite(ItemStack stack, SpellDatum<?> datum) {
        return datum == null || datum.getType() == DatumType.PATTERN;
    }

    @Override
    public void writeDatum(ItemStack stack, SpellDatum<?> datum) {
        if (this.canWrite(stack, datum)) {
            if (datum == null) {
                var beTag = stack.getOrCreateTagElement("BlockEntityTag");
                beTag.remove(BlockEntitySlate.TAG_PATTERN);
                if (beTag.isEmpty()) {
                    stack.removeTagKey("BlockEntityTag");
                }
            } else if (datum.getPayload() instanceof HexPattern pat) {
                var beTag = stack.getOrCreateTagElement("BlockEntityTag");
                beTag.put(BlockEntitySlate.TAG_PATTERN, pat.serializeToNBT());
            }
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (ItemSlate.hasPattern(stack)) {
            var tag = stack.getOrCreateTag()
                .getCompound("BlockEntityTag")
                .getCompound(BlockEntitySlate.TAG_PATTERN);
            var pattern = HexPattern.DeserializeFromNBT(tag);
            return Optional.of(new PatternTooltipGreeble(
                pattern,
                PatternTooltipGreeble.SLATE_BG));
        }
        return Optional.empty();
    }
}
