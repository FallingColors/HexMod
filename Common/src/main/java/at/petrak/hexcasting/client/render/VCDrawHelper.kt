package at.petrak.hexcasting.client.render

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.world.phys.Vec2
import org.joml.Matrix4f


interface VCDrawHelper {
    fun vcSetupAndSupply(vertMode: VertexFormat.Mode): VertexConsumer
    fun vertex(vc: VertexConsumer, color: Int, pos: Vec2, matrix: Matrix4f)
    fun vcEndDrawer(vc: VertexConsumer)

    class Basic(val z: Float) : VCDrawHelper {

        override fun vcSetupAndSupply(vertMode: VertexFormat.Mode): VertexConsumer{
            val tess = Tesselator.getInstance()
            val buf = tess.builder
            buf.begin(vertMode, DefaultVertexFormat.POSITION_COLOR)
            return buf
        }
        override fun vertex(vc: VertexConsumer, color: Int, pos: Vec2, matrix: Matrix4f){
            vc.vertex(matrix, pos.x, pos.y, z).color(color).endVertex()
        }
        override fun vcEndDrawer(vc: VertexConsumer){
            Tesselator.getInstance().end()
        }
    }
}