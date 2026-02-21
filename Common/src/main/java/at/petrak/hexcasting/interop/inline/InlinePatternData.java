package at.petrak.hexcasting.interop.inline;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.PatternShapeMatch;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.serialization.Codec;
import com.samsthenerd.inline.api.InlineData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
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

    @Override
    public Style getExtraStyle() {
        ItemStack scrollStack = new ItemStack(HexItems.SCROLL_MEDIUM);
        HexItems.SCROLL_MEDIUM.writeDatum(scrollStack, new PatternIota(pattern));
        scrollStack.set(DataComponents.ITEM_NAME, getPatternName(pattern).copy().withStyle(ChatFormatting.WHITE));
        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(scrollStack));
        ClickEvent ce = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, pattern.toString());
        return Style.EMPTY.withHoverEvent(he).withClickEvent(ce);
    }

    public static Component getPatternName(HexPattern pattern){
        try {
            PatternShapeMatch shapeMatch = PatternRegistryManifest.matchPattern(pattern, null, false);
            if(shapeMatch instanceof PatternShapeMatch.Normal normMatch){
                return HexAPI.instance().getActionI18n(normMatch.key, false);
            }
            // TODO: this doesn't actually ever hit because it errors out with server castinv env stuff first :(
            if(shapeMatch instanceof PatternShapeMatch.Special specialMatch){
                return HexAPI.instance().getSpecialHandlerI18n(specialMatch.key);
            }
        } catch (Exception e){
            // nop
        }
        return PatternIota.displayNonInline(pattern);
    }

    @Override
    public Component asText(boolean withExtra) {
        return Component.literal(pattern.toString()).withStyle(asStyle(withExtra));
    }

    public static class InlinePatternDataType implements InlineDataType<InlinePatternData> {
        private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HexAPI.MOD_ID, "pattern");
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
