package at.petrak.hexcasting.api.client;

import at.petrak.hexcasting.api.casting.math.HexPattern;

import java.util.ArrayList;
import java.util.List;

public class ClientCastingStack {
    public static List<HexPatternRenderHolder> patterns = new ArrayList<>();
    public static List<HexPatternRenderHolder> toRemove = new ArrayList<>();

    public static List<HexPatternRenderHolder> toAdd = new ArrayList<>();

    public static void addPattern(HexPattern pattern) {
        addPattern(pattern, 20);
    }

    public static void addPattern(HexPattern pattern, int lifetime){
        if(pattern == null) return;
        if(patterns.stream().anyMatch(patternRenderHolder -> patternRenderHolder.pattern().hashCode() == pattern.hashCode())){
            return;
        }
        if(patterns.size() > 100){
            patterns.remove(0);
        }
        patterns.add(new HexPatternRenderHolder(pattern, lifetime));
    }

    public static void clear() {
        patterns.clear();
    }

    public static List<HexPatternRenderHolder> getPatterns() {
        return patterns;
    }

    public static int getPatternLifetime(HexPattern pattern) {
        for (HexPatternRenderHolder patternRenderHolder : patterns) {
            if (patternRenderHolder.pattern().equals(pattern)) {
                return patternRenderHolder.lifetime();
            }
        }
        return 0;
    }

    public static HexPattern getPattern(int index) {
        if(index < 0 || index >= patterns.size()) return null;
        return patterns.get(index).pattern();
    }

    public static int size() {
        return patterns.size();
    }

    public static void tick(){
        // tick without getting a cme
        toAdd.forEach(pattern -> {
            if(patterns.size() > 100){
                patterns.remove(0);
            }
            patterns.add(pattern);
        });
        toAdd.clear();
        patterns.forEach(HexPatternRenderHolder::tick);
        patterns.forEach(pattern -> {
            if(pattern.lifetime() <= 0){
                toRemove.add(pattern);
            }
        });
        patterns.removeAll(toRemove);
        toRemove.clear();
    }
}
