package at.petrak.hex.casting.operators

import at.petrak.hex.casting.CastingContext
import at.petrak.hex.casting.SpellDatum
import at.petrak.hex.casting.operators.SpellOperator.Companion.getChecked
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object OpBlockAxisRaycast : SpellOperator {
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
                Unit
            }
        )
    }
}