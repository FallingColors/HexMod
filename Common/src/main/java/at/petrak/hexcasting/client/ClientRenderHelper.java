package at.petrak.hexcasting.client;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.client.ClientCastingStack;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.client.render.RenderLib;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ClientRenderHelper {

    public static void renderCastingStack(PoseStack ps, Player player, MultiBufferSource bufferSource, float pticks) {
        for(int k = 0; k < ClientCastingStack.getPatterns().size(); k++){
            if(ClientCastingStack.patterns.get(k) == null) continue;
            ps.pushPose();
            int lifetime = ClientCastingStack.patterns.get(k).lifetime();
            float lifetimeOffset = lifetime <= 5f ? ((5f - lifetime) / 5f) : 0f;
            ps.mulPose(Vector3f.YP.rotationDegrees((float)(((player.level.getGameTime()+pticks)*(Math.sin(k*12.543565f) * 3.4f)*(k/12.43d)%360)+(1+ k)*45f)));
            ps.translate(0,1f + (Math.sin(k)*0.75f),0.75f+((Math.cos(k/8f))*0.25f) + Math.cos((player.level.getGameTime()+pticks)/(7f + (k/4f)))*0.065f);
            ps.scale(1/24f* (1-lifetimeOffset), 1/24f* (1-lifetimeOffset), 1/24f * (1-lifetimeOffset));
            ps.translate(0,Math.floor(k/8f),0);
            ps.translate(0,Math.sin((player.level.getGameTime()+pticks)/(7f + (k/8f))),0f);
            HexPattern pattern = ClientCastingStack.getPattern(k);
            if(pattern == null) {
                ps.popPose();
                continue;
            }
            ShaderInstance oldShader = RenderSystem.getShader();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            Vec2 com1 = pattern.getCenter(1);
            List<Vec2> lines1 = pattern.toLines(1, Vec2.ZERO);
            float maxDx = -1f;
            float maxDy = -1f;
            for (Vec2 line : lines1) {
                float dx = Math.abs(line.x - com1.x);
                if (dx > maxDx) {
                    maxDx = dx;
                }
                float dy = Math.abs(line.y - com1.y);
                if (dy > maxDy) {
                    maxDy = dy;
                }
            }
            float scale = Math.min(3.8f, Math.min(16/2.5f/maxDx, 16/2.5f/maxDy));
            Vec2 com2 = pattern.getCenter(scale);
            List<Vec2> lines2 = pattern.toLines(scale, com2.negated());
            for(int i = 0; i < lines2.size(); i++){
                Vec2 line = lines2.get(i);
                lines2.set(i, new Vec2(line.x, -line.y));
            }
            float variance = 0.65f;
            float speed = 0.1f;
            float stupidHash = player.hashCode();
            List<Vec2> zappy = RenderLib.makeZappy(lines2, RenderLib.findDupIndices(pattern.positions()),
                    5, variance, speed, 0.2f, 0f,
                    1f, stupidHash);
            int outer = IXplatAbstractions.INSTANCE.getPigment(player).getColorProvider().getColor(ClientTickCounter.getTotal() / 2f, Vec3.ZERO);
            int rgbOnly = outer & 0x00FFFFFF;
            int newAlpha = outer >>> 24;
            if(lifetime <= 60){
                newAlpha = (int) Math.floor((lifetime/60f) * 255);
            }
            int newARGB = (newAlpha << 24) | rgbOnly;
            int inner = RenderLib.screen(newARGB);
            RenderLib.drawLineSeq(ps.last().pose(), zappy, 0.35f, 0f, newARGB, newARGB);
            RenderLib.drawLineSeq(ps.last().pose(), zappy, 0.14f, 0.01f, inner, inner);
            ps.popPose();
            RenderSystem.setShader(() -> oldShader);
            RenderSystem.enableCull();
        }
    }
}