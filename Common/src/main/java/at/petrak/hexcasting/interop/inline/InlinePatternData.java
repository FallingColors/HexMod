package at.petrak.hexcasting.interop.inline;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.serialization.Codec;
import com.samsthenerd.inline.api.InlineData;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InlinePatternData implements InlineData<InlinePatternData>{

    public static final ResourceLocation rendererId = HexAPI.modLoc("pattern");

    @NotNull
    public final HexPattern pattern;

    public InlinePatternData(@NotNull HexPattern pattern){
        this.pattern = pattern;
    }

    @Override
    public InlinePatternDataType getType(){
        return InlinePatternDataType.INSTANCE;
    }

    @Override
    public ResourceLocation getRendererId(){
        return rendererId;
    }

    public Style getExtraStyle() {
        ItemStack scrollStack = new ItemStack(HexItems.SCROLL_MEDIUM);
        HexItems.SCROLL_MEDIUM.writeDatum(scrollStack, new PatternIota(pattern));
        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(scrollStack));
        // TODO: add copy click event
        return Style.EMPTY.withHoverEvent(he);
    }

    public static class InlinePatternDataType implements InlineDataType<InlinePatternData> {
        private static final ResourceLocation ID = new ResourceLocation(HexAPI.MOD_ID, "pattern");
        public static final InlinePatternDataType INSTANCE = new InlinePatternDataType();

        @Override
        public ResourceLocation getId(){
            return ID;
        }

        @Override
        public Codec<InlinePatternData> getCodec(){
            return HexPattern.CODEC.xmap(
                InlinePatternData::new,
                data -> data.pattern
            );
        }
    }
}
