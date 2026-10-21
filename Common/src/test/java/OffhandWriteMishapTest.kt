import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class OffhandWriteMishapTest {
    @Test
    fun `test empty offhand mishap message`() {
        val mishap = MishapBadOffhandItem.of(null, "iota.offhand_empty")
        Assertions.assertNotNull(mishap)
    }

    @Test
    fun `test sealed offhand mishap message`() {
        val mishap = MishapBadOffhandItem.of(null, "iota.offhand_sealed")
        Assertions.assertNotNull(mishap)
    }
}
