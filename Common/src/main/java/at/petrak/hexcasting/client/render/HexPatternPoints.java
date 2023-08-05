package at.petrak.hexcasting.client.render;

import net.minecraft.world.phys.Vec2;

import java.util.List;

public class HexPatternPoints {
    public List<Vec2> zappyPoints = null;
    public String pointsKey = null; //TODO: if a string key isnt performant enough override hashcode for points

    public HexPatternPoints(List<Vec2> zappyPoints) {
        this.zappyPoints = zappyPoints;
        pointsKey = PatternTextureManager.getPointsKey(zappyPoints);
    }
}