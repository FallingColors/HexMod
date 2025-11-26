package at.petrak.hexcasting.common.items.storage;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.gui.PatternTooltipComponent;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.common.misc.PatternTooltip;
import at.petrak.hexcasting.interop.inline.InlinePatternData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemSlate extends BlockItem implements IotaHolderItem {
    public static final ResourceLocation WRITTEN_PRED = modLoc("written");

    public ItemSlate(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        var key = "block." + HexAPI.MOD_ID + ".slate." + (hasPattern(pStack) ? "written" : "blank");
        Component patternText = getPattern(pStack)
            .map(pat -> Component.literal(": ").append(new InlinePatternData(pat).asText(false)))
            .orElse(Component.literal(""));
        return Component.translatable(key).append(patternText);
    }

    public static Optional<HexPattern> getPattern(ItemStack stack){
        var bet = stack.get(DataComponents.BLOCK_ENTITY_DATA);

        if (bet != null && bet.contains(BlockEntitySlate.TAG_PATTERN)) {
            var pattern = bet.read(HexPattern.CODEC.fieldOf(BlockEntitySlate.TAG_PATTERN)).result().orElse(null);
            return Optional.ofNullable(pattern);
        }
        return Optional.empty();
    }

    public static boolean hasPattern(ItemStack stack) {
        return getPattern(stack).isPresent();
    }

    @SoftImplement("IForgeItem")
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!hasPattern(stack)) {
            stack.remove(DataComponents.BLOCK_ENTITY_DATA);
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (!hasPattern(pStack)) {
            pStack.remove(DataComponents.BLOCK_ENTITY_DATA);
        }
    }

    @Override
    public @Nullable Iota readIota(ItemStack stack) {
        var bet = stack.get(DataComponents.BLOCK_ENTITY_DATA);;

        if (bet == null || !bet.contains(BlockEntitySlate.TAG_PATTERN)) {
            return null;
        }

        return bet.read(HexPattern.CODEC.fieldOf(BlockEntitySlate.TAG_PATTERN))
                .result()
                .map(PatternIota::new)
                .orElse(null);
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
            if (datum == null) {
                var beData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                beData.update(tag -> {
                    tag.remove(BlockEntitySlate.TAG_PATTERN);
                });

                if(beData.isEmpty())
                    stack.remove(DataComponents.BLOCK_ENTITY_DATA);
            } else if (datum instanceof PatternIota pat) {
                stack.update(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(new CompoundTag()), data -> {
                    data.update(tag ->
                            tag.put(BlockEntitySlate.TAG_PATTERN, HexPattern.CODEC
                                    .encodeStart(NbtOps.INSTANCE, pat.getPattern())
                                    .getOrThrow()
                            )
                    );
                    return data;
                });
            }
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return getPattern(stack).map(pat -> new PatternTooltip(pat, PatternTooltipComponent.SLATE_BG));
    }
}
