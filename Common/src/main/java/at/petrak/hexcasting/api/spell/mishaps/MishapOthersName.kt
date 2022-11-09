package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.ListIota
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor

class MishapOthersName(val other: Player) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLACK)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        ctx.caster.addEffect(MobEffectInstance(MobEffects.BLINDNESS, 20 * 60))
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("others_name", other.name)

    companion object {
        @JvmStatic
        fun getTrueNameFromDatum(datum: Iota, caster: Player): Player? {
            val poolToSearch = ArrayDeque<Iota>()
            poolToSearch.addLast(datum)

            while (poolToSearch.isNotEmpty()) {
                val datumToCheck = poolToSearch.removeFirst()
                if (datumToCheck is EntityIota && datumToCheck.entity is Player && datumToCheck.entity != caster)
                    return datumToCheck.entity as Player
                if (datumToCheck is ListIota)
                    poolToSearch.addAll(datumToCheck.list)
            }

            return null
        }

        @JvmStatic
        fun getTrueNameFromArgs(datums: List<Iota>, caster: Player): Player? {
            return datums.firstNotNullOfOrNull { getTrueNameFromDatum(it, caster) }
        }
    }
}
