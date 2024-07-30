package at.petrak.hexcasting.client.render;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A simple wrapper around the parts of HexPattern that are actually used for rendering.
 *
 * This lets the pattern renderer work on arbitrary lists of vecs - this is never used in base hex but is included
 * to future-proof and for if addons or something wants to use it.
 */
public interface HexPatternLike {
    List<Vec2> getNonZappyPoints();

    String getName();

    Set<Integer> getDups();

    static HexPatternLike of(HexPattern pat){
        return new HexPatternLikeBecauseItsActuallyAHexPattern(pat);
    }

    static HexPatternLike of(List<Vec2> lines, String name){
        return new PureLines(lines, name);
    }

    record HexPatternLikeBecauseItsActuallyAHexPattern(HexPattern pat) implements HexPatternLike{
        public List<Vec2> getNonZappyPoints(){
            return pat.toLines(1, Vec2.ZERO);
        }

        public String getName(){
            return pat.getStartDir() + "-" + pat.anglesSignature();
        }

        public Set<Integer> getDups(){
            return RenderLib.findDupIndices(pat.positions());
        }
    }

    record PureLines(List<Vec2> lines, String name) implements HexPatternLike{

        public List<Vec2> getNonZappyPoints(){
            return lines;
        }

        public String getName(){
            return name;
        }

        public Set<Integer> getDups(){
            return RenderLib.findDupIndices(
                lines().stream().map(p ->
                    // I hate mojang
                    new Vec2(p.x, p.y){
                        @Override public boolean equals(Object other){
                            if(other instanceof Vec2 otherVec) return p.equals(otherVec);
                            return false;
                        }

                        @Override public int hashCode(){ return Objects.hash(p.x, p.y); }
                    }).toList()
            );
        }
    }
}
