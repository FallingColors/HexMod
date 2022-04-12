package at.petrak.hexcasting.common.casting.operators.akashic

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicRecord
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.api.spell.math.HexPattern
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3

object OpAkashicWrite : SpellOperator {
    override val argc = 3

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val pos = args.getChecked<Vec3>(0)
        val key = args.getChecked<HexPattern>(1)
        val datum = args[2]

        ctx.assertVecInRange(pos)

        val bpos = BlockPos(pos)
        val tile = ctx.world.getBlockEntity(bpos)
        if (tile !is BlockEntityAkashicRecord) {
            throw MishapNoAkashicRecord(bpos)
        }

        val trueName = MishapOthersName.getTrueNameFromDatum(datum, ctx.caster)
        if (trueName != null)
            throw MishapOthersName(trueName)

        return Triple(
            Spell(tile, key, datum),
            10_000,
            listOf()
        )
    }

    private data class Spell(val record: BlockEntityAkashicRecord, val key: HexPattern, val datum: SpellDatum<*>) :
        RenderedSpell {
        override fun cast(ctx: CastingContext) {
            record.addNewDatum(key, datum)

            ctx.world.playSound(
                null, record.blockPos, HexSounds.SCROLL_SCRIBBLE.get(), SoundSource.BLOCKS,
                1f, 0.8f
            )

            // val colorizer = HexPlayerDataHelper.getColorizer(ctx.caster)
            // val normal = record.blockState.getValue(BlockAkashicBookshelf.FACING).normal
            // ParticleSpray(
            //     Vec3.atCenterOf(record.blockPos), Vec3.atBottomCenterOf(normal),
            //     0.5, Math.PI / 4, 10
            // ).sprayParticles(ctx.world, colorizer)
        }
    }
}
