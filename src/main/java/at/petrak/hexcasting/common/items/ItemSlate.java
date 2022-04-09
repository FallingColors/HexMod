package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.api.item.DataHolder;
import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.hexmath.HexPattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class ItemSlate extends BlockItem implements DataHolder {
    public static final ResourceLocation WRITTEN_PRED = new ResourceLocation(HexMod.MOD_ID, "written");

    public ItemSlate(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        var key = "block." + HexMod.MOD_ID + ".slate." + (hasPattern(pStack) ? "written" : "blank");
        return new TranslatableComponent(key);
    }

    public static boolean hasPattern(ItemStack stack) {
        var tag = stack.getTag();
        if (tag != null && tag.contains("BlockEntityTag")) {
            var bet = tag.getCompound("BlockEntityTag");
            return bet.contains(BlockEntitySlate.TAG_PATTERN);
        }
        return false;
    }

    @Override
    public @Nullable CompoundTag readDatumTag(ItemStack stack) {
        var stackTag = stack.getTag();
        if (stackTag == null || !stackTag.contains("BlockEntityTag")) {
            return null;
        }
        var beTag = stackTag.getCompound("BlockEntityTag");
        if (!beTag.contains(BlockEntitySlate.TAG_PATTERN)) {
            return null;
        }

        var patTag = beTag.getCompound(BlockEntitySlate.TAG_PATTERN);
        var out = new CompoundTag();
        out.put(SpellDatum.TAG_PATTERN, patTag);
        return out;
    }

    @Override
    public boolean canWrite(CompoundTag tag, SpellDatum<?> datum) {
        if (datum == null || datum.getType() != DatumType.PATTERN) {
            return false;
        }

        if (!tag.contains("BlockEntityTag")) {
            return false;
        }
        var beTag = tag.getCompound("BlockEntityTag");
        return !beTag.contains(BlockEntitySlate.TAG_PATTERN);
    }

    @Override
    public void writeDatum(CompoundTag tag, SpellDatum<?> datum) {
        if (this.canWrite(tag, datum) && datum.getPayload() instanceof HexPattern pat) {
            var beTag = tag.getCompound("BlockEntityTag");
            beTag.put(BlockEntitySlate.TAG_PATTERN, pat.serializeToNBT());
        }
    }
}
