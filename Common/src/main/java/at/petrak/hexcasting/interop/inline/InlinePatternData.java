package at.petrak.hexcasting.interop.inline;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.mojang.serialization.Codec;
import com.samsthenerd.inline.api.InlineData;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class InlinePatternData implements InlineData<InlinePatternData>{

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
        return new ResourceLocation(HexAPI.MOD_ID, "pattern");
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
