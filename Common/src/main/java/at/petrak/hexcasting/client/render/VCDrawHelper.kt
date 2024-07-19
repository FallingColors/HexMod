package at.petrak.hexcasting.client.render

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.client.render.PatternRenderer.WorldlyBits
import com.ibm.icu.impl.CurrencyData.provider
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f


interface VCDrawHelper {
    fun vcSetupAndSupply(vertMode: VertexFormat.Mode): VertexConsumer
    fun vertex(vc: VertexConsumer, color: Int, pos: Vec2, matrix: Matrix4f){
        vertex(vc, color, pos, Vec2(0f,0f), matrix)
    }
    fun vertex(vc: VertexConsumer, color: Int, pos: Vec2, uv: Vec2, matrix: Matrix4f)

    fun vcEndDrawer(vc: VertexConsumer)

    companion object {

        @JvmStatic
        val WHITE: ResourceLocation = HexAPI.modLoc("textures/entity/white.png")

        @JvmStatic
        fun getHelper(worldlyBits: WorldlyBits?, ps: PoseStack, z: Float, texture: ResourceLocation) : VCDrawHelper{
            if(worldlyBits != null){
                return Worldly(worldlyBits, ps, z, texture)
            }
            return Basic(z, texture)
        }

        @JvmStatic
        fun getHelper(worldlyBits: WorldlyBits?, ps: PoseStack, z: Float) : VCDrawHelper{
            return getHelper(worldlyBits, ps, z, WHITE)
        }
    }

    class Basic(val z: Float, val texture: ResourceLocation = WHITE) : VCDrawHelper {

        override fun vcSetupAndSupply(vertMode: VertexFormat.Mode): VertexConsumer{
            val tess = Tesselator.getInstance()
            val buf = tess.builder
            buf.begin(vertMode, DefaultVertexFormat.POSITION_COLOR_TEX)
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            RenderSystem.disableCull()
            RenderSystem.enableDepthTest()
            RenderSystem.enableBlend()
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
            RenderSystem.setShaderTexture(0, texture)
            return buf
        }
        override fun vertex(vc: VertexConsumer, color: Int, pos: Vec2, uv: Vec2, matrix: Matrix4f){
            vc.vertex(matrix, pos.x, pos.y, z).color(color).uv(uv.x, uv.y).endVertex()
        }
        override fun vcEndDrawer(vc: VertexConsumer){
            Tesselator.getInstance().end()
        }
    }

    class Worldly(val worldlyBits: WorldlyBits, val ps: PoseStack, val z: Float, val texture: ResourceLocation) : VCDrawHelper {

        var lastVertMode: VertexFormat.Mode ?= null // i guess this assumes that the vcHelper is only used once at a time? maybe reconsider that

        override fun vcSetupAndSupply(vertMode: VertexFormat.Mode): VertexConsumer{
            val provider = worldlyBits.provider
            if (provider is MultiBufferSource.BufferSource) {
                // tells it to draw whatever was here before so that we don't get depth buffer weirdness
                provider.endBatch()
            }
            lastVertMode = vertMode
            val buf = Tesselator.getInstance().builder
            if(vertMode == VertexFormat.Mode.QUADS){
                val layer = RenderType.entityTranslucentCull(texture)
                layer.setupRenderState()
                if (provider == null) {
                    buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY)
                    RenderSystem.setShader { GameRenderer.getRendertypeEntityTranslucentCullShader() }
                    return buf
                } else {
                    return provider.getBuffer(layer)
                }
            }
            buf.begin( vertMode, DefaultVertexFormat.NEW_ENTITY )
            // Generally this would be handled by a RenderLayer, but that doesn't seem to actually work here,,
            RenderSystem.setShaderTexture(0, texture)
            RenderSystem.enableDepthTest()
            RenderSystem.disableCull()
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer()
            RenderSystem.enableBlend()
            RenderSystem.blendFuncSeparate( GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA )
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            if (Minecraft.useShaderTransparency()) {
                Minecraft.getInstance().levelRenderer.translucentTarget!!.bindWrite(false)
            }
            RenderSystem.setShader( GameRenderer::getRendertypeEntityTranslucentCullShader )
            return buf
        }

        override fun vertex(vc: VertexConsumer, color: Int, pos: Vec2, uv: Vec2, matrix: Matrix4f){
            val nv = worldlyBits.normal?: Vec3(1.0, 1.0, 1.0)
            vc.vertex(matrix, pos.x, pos.y, z)
                .color(color)
                .uv(uv.x, uv.y)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(worldlyBits.light?: LightTexture.FULL_BRIGHT )
                .normal(ps.last().normal(), nv.x.toFloat(), nv.y.toFloat(), nv.z.toFloat())

            vc.endVertex()
        }
        override fun vcEndDrawer(vc: VertexConsumer){
            if(lastVertMode == VertexFormat.Mode.QUADS){
                if (provider == null) {
                    val layer = RenderType.entityTranslucentCull(texture)
                    layer.end(Tesselator.getInstance().builder, VertexSorting.ORTHOGRAPHIC_Z)
                }
            } else {
                Tesselator.getInstance().end()
                Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer()
                RenderSystem.disableBlend()
                RenderSystem.defaultBlendFunc()
                if (Minecraft.useShaderTransparency()) {
                    Minecraft.getInstance().mainRenderTarget.bindWrite(false)
                }
                RenderSystem.enableCull()
            }
            lastVertMode = null
        }
    }


}