package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.item.DataHolderItem;
import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.client.gui.PatternTooltipGreeble;
import at.petrak.hexcasting.api.utils.NBTHelper;
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
        var bet = NBTHelper.getCompound(stack, "BlockEntityTag");
        if (bet != null) {
            return bet.contains(BlockEntitySlate.TAG_PATTERN, Tag.TAG_COMPOUND) &&
                !bet.getCompound(BlockEntitySlate.TAG_PATTERN).isEmpty();
        }
        return false;
    }

    @SoftImplement("IForgeItem")
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!hasPattern(stack))
            NBTHelper.remove(stack, "BlockEntityTag");
        return false;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (!hasPattern(pStack))
            NBTHelper.remove(pStack, "BlockEntityTag");
    }

    @Override
    public @Nullable CompoundTag readDatumTag(ItemStack stack) {
        var bet = NBTHelper.getCompound(stack, "BlockEntityTag");

        if (bet == null || !bet.contains(BlockEntitySlate.TAG_PATTERN, Tag.TAG_COMPOUND)) {
            return null;
        }

        var patTag = bet.getCompound(BlockEntitySlate.TAG_PATTERN);
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
                var beTag = NBTHelper.getOrCreateCompound(stack, "BlockEntityTag");
                beTag.remove(BlockEntitySlate.TAG_PATTERN);
                if (beTag.isEmpty())
                    NBTHelper.remove(stack, "BlockEntityTag");
            } else if (datum.getPayload() instanceof HexPattern pat) {
                var beTag = NBTHelper.getOrCreateCompound(stack, "BlockEntityTag");
                beTag.put(BlockEntitySlate.TAG_PATTERN, pat.serializeToNBT());
            }
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var bet = NBTHelper.getCompound(stack, "BlockEntityTag");

        if (bet != null && bet.contains(BlockEntitySlate.TAG_PATTERN, Tag.TAG_COMPOUND)) {
            var patTag = bet.getCompound(BlockEntitySlate.TAG_PATTERN);
            if (!patTag.isEmpty()) {
                var pattern = HexPattern.fromNBT(patTag);
                return Optional.of(new PatternTooltipGreeble(
                    pattern,
                    PatternTooltipGreeble.SLATE_BG));
            }
        }
        return Optional.empty();
    }
}
