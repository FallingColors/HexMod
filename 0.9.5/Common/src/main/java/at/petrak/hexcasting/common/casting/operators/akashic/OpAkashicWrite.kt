package at.petrak.hexcasting.common.casting.operators.akashic

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicRecord
import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3

object OpAkashicWrite : SpellOperator {
    override val argc = 3

    override val isGreat = true
    override val alwaysProcessGreatSpell = false
    override val causesBlindDiversion = false

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val pos = args.getChecked<Vec3>(0, argc)
        val key = args.getChecked<HexPattern>(1, argc)
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
            ManaConstants.DUST_UNIT,
            listOf()
        )
    }

    private data class Spell(val record: BlockEntityAkashicRecord, val key: HexPattern, val datum: SpellDatum<*>) :
        RenderedSpell {
        override fun cast(ctx: CastingContext) {
            record.addNewDatum(key, datum)

            ctx.world.playSound(
                null, record.blockPos, HexSounds.SCROLL_SCRIBBLE, SoundSource.BLOCKS,
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
