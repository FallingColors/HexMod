package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.SpellOperator
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.RenderedSpell
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.phys.Vec3

object OpLightning : SpellOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int> {
        val target = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(target)
        return Pair(
            Spell(target),
            1_500_000
        )
    }

    private data class Spell(val target: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val lightning = LightningBolt(EntityType.LIGHTNING_BOLT, ctx.world)
            lightning.setPosRaw(target.x, target.y, target.z)
            ctx.world.addWithUUID(lightning) // why the hell is it called this it doesnt even involve a uuid
        }
    }
}