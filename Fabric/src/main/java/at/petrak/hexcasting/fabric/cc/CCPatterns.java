package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.spell.casting.ResolvedPattern;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CCPatterns implements Component {
    public static final String TAG_PATTERNS = "patterns";

    private final Player owner;

    private List<ResolvedPattern> patterns = Collections.emptyList();

    public CCPatterns(ServerPlayer owner) {
        this.owner = owner;
    }


    public List<ResolvedPattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<ResolvedPattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        ListTag patternsTag = tag.getList(TAG_PATTERNS, Tag.TAG_COMPOUND);

        List<ResolvedPattern> patterns = new ArrayList<>(patternsTag.size());

        for (int i = 0; i < patternsTag.size(); i++) {
            patterns.add(ResolvedPattern.fromNBT(patternsTag.getCompound(i)));
        }
        this.patterns = patterns;
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        var listTag = new ListTag();
        for (ResolvedPattern pattern : patterns) {
            listTag.add(pattern.serializeToNBT());
        }
        tag.put(TAG_PATTERNS, listTag);
    }
}
