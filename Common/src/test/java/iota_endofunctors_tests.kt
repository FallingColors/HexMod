import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.xplat.DummyXplatAbstractions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError
import java.util.function.UnaryOperator

internal class IotaEndofunctorsTests {
    fun exampleWalker(iota: Iota) =
        when (iota) {
            is DoubleIota -> DoubleIota(iota.double + 1)
            is ListIota -> if (iota.list.any { it is DoubleIota && it.double == 7.0 }) { NullIota() } else { iota }
            else -> iota
        }

    internal class IotaComparator(val inner: Iota) {
        override fun equals(other: Any?): Boolean =
            when (other) {
                is IotaComparator -> Iota.tolerates(inner, other.inner)
                is Iota -> Iota.tolerates(inner, other)
                else -> false
            }
        override fun hashCode(): Int = inner.hashCode()
        override fun toString(): String = inner.display().string
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun ensureBlackMagicExecuted() {
            DummyXplatAbstractions.forbiddenMagics.get()
        }
    }

    internal fun assertTolerates(left: Iota, right: Iota, message: String? = "iotas were not equal") {
        Assertions.assertEquals(IotaComparator(left), IotaComparator(right), message)
    }

    internal fun assertNotTolerates(left: Iota, right: Iota, message: String? = "iotas were equal") {
        Assertions.assertNotEquals(IotaComparator(left), IotaComparator(right), message)
    }

    @Test
    fun `my tests are working properly`() {
        Assertions.assertAll(
            { assertTolerates(DoubleIota(42.0), DoubleIota(42.0)) },
            { assertNotTolerates(DoubleIota(42.0), DoubleIota(47.0)) },
        )
    }

    @Test
    fun `a sole double iota should be incremented`() {
        assertTolerates(DoubleIota(43.0), DoubleIota(42.0).visit(::exampleWalker))
    }

    @Test
    fun `a list of untouched iotas should be conserved`() {
        val iota = ListIota(listOf(
            PatternIota(HexPattern.fromAnglesUnchecked("aqaaw", HexDir.SOUTH_EAST)),
            PatternIota(HexPattern.fromAnglesUnchecked("aqaawa", HexDir.SOUTH_EAST)),
            PatternIota(HexPattern.fromAnglesUnchecked("aqaawaw", HexDir.SOUTH_EAST)),
        ))
        Assertions.assertSame(iota, iota.visit(::exampleWalker))
    }

    @Test
    fun `iotas should be visited recursively`() {
        val orig = ListIota(listOf(
            DoubleIota(1.0),
            ListIota(listOf(
                DoubleIota(2.0),
                ListIota(listOf(
                    DoubleIota(3.0),
                ))
            ))
        ))
        val expected = ListIota(listOf(
            DoubleIota(2.0),
            ListIota(listOf(
                DoubleIota(3.0),
                ListIota(listOf(
                    DoubleIota(4.0),
                ))
            ))
        ))
        assertTolerates(expected, orig.visit(::exampleWalker))
    }

    @Test
    fun `lists should be visited before their elements`() {
        val orig = ListIota(listOf(
            ListIota(listOf(DoubleIota(5.0))),
            ListIota(listOf(DoubleIota(6.0))),
            ListIota(listOf(DoubleIota(7.0))),
            ListIota(listOf(DoubleIota(9.0))),
        ))
        val expected = ListIota(listOf(
            ListIota(listOf(DoubleIota(6.0))),
            ListIota(listOf(DoubleIota(7.0))),
            NullIota(),
            ListIota(listOf(DoubleIota(10.0))),
        ))
        assertTolerates(expected, orig.visit(::exampleWalker))
    }
}
