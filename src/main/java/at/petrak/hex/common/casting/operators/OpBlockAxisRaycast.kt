package at.petrak.hex.common.casting.operators

import at.petrak.hex.api.SimpleOperator
import at.petrak.hex.api.SpellOperator
import at.petrak.hex.api.SpellOperator.Companion.getChecked
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.casting.SpellWidget
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object OpBlockAxisRaycast : SimpleOperator {
    override val argc = 2
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val origin: Vec3 = args.getChecked(0)
        val look: Vec3 = args.getChecked(1)

        val blockHitResult = ctx.world.clip(
            ClipContext(
                origin,
                SpellOperator.raycastEnd(origin, look),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                ctx.caster
            )
        )

        return SpellOperator.spellListOf(
            if (blockHitResult.type == HitResult.Type.BLOCK) {
                Vec3(blockHitResult.direction.step())
            } else {
                SpellWidget.NULL
            }
        )
    }
}