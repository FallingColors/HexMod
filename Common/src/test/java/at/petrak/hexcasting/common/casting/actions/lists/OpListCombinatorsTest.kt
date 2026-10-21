package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.xplat.DummyXplatAbstractions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class OpListCombinatorsTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun ensureBlackMagicExecuted() {
            DummyXplatAbstractions.forbiddenMagics.get()
        }
    }

    @Test
    fun testListZip() {
        // Empty lists
        val resEmpty = OpListZip.execute(listOf(ListIota(emptyList()), ListIota(emptyList())), null!!)
        Assertions.assertTrue((resEmpty[0] as ListIota).list.isEmpty())

        // Different lengths -> truncates to shorter
        val list1 = ListIota(listOf(DoubleIota(1.0), DoubleIota(2.0), DoubleIota(3.0)))
        val list2 = ListIota(listOf(DoubleIota(10.0), DoubleIota(20.0)))
        val res = OpListZip.execute(listOf(list1, list2), null!!)
        val zippedList = (res[0] as ListIota).list
        Assertions.assertEquals(2, zippedList.size)
        Assertions.assertEquals(2.0, ((zippedList[0] as ListIota).list[0] as DoubleIota).double)
        Assertions.assertEquals(10.0, ((zippedList[0] as ListIota).list[1] as DoubleIota).double)
        Assertions.assertEquals(2.0, ((zippedList[1] as ListIota).list[0] as DoubleIota).double)
        Assertions.assertEquals(20.0, ((zippedList[1] as ListIota).list[1] as DoubleIota).double)
    }

    @Test
    fun testListFlatten() {
        // Empty list
        val resEmpty = OpListFlatten.execute(listOf(ListIota(emptyList())), null!!)
        Assertions.assertTrue((resEmpty[0] as ListIota).list.isEmpty())

        // Nested lists unpacked by one level, non-list elements remain as-is
        val innerList = ListIota(listOf(DoubleIota(2.0), DoubleIota(3.0)))
        val outerList = ListIota(listOf(DoubleIota(1.0), innerList, DoubleIota(4.0)))
        val res = OpListFlatten.execute(listOf(outerList), null!!)
        val flattened = (res[0] as ListIota).list
        Assertions.assertEquals(4, flattened.size)
        Assertions.assertEquals(1.0, (flattened[0] as DoubleIota).double)
        Assertions.assertEquals(2.0, (flattened[1] as DoubleIota).double)
        Assertions.assertEquals(3.0, (flattened[2] as DoubleIota).double)
        Assertions.assertEquals(4.0, (flattened[3] as DoubleIota).double)
    }

    @Test
    fun testListDedup() {
        // Empty list
        val resEmpty = OpListDedup.execute(listOf(ListIota(emptyList())), null!!)
        Assertions.assertTrue((resEmpty[0] as ListIota).list.isEmpty())

        // Consecutive duplicates removed, relative ordering preserved
        val list = ListIota(listOf(
            DoubleIota(1.0),
            DoubleIota(1.0),
            DoubleIota(2.0),
            DoubleIota(2.0),
            DoubleIota(2.0),
            DoubleIota(3.0),
            DoubleIota(1.0),
            DoubleIota(1.0)
        ))
        val res = OpListDedup.execute(listOf(list), null!!)
        val deduped = (res[0] as ListIota).list
        Assertions.assertEquals(4, deduped.size)
        Assertions.assertEquals(1.0, (deduped[0] as DoubleIota).double)
        Assertions.assertEquals(2.0, (deduped[1] as DoubleIota).double)
        Assertions.assertEquals(3.0, (deduped[2] as DoubleIota).double)
        Assertions.assertEquals(1.0, (deduped[3] as DoubleIota).double)
    }

    @Test
    fun testListPartition() {
        // Partition list by boolean mask
        val items = ListIota(listOf(DoubleIota(1.0), DoubleIota(2.0), DoubleIota(3.0), DoubleIota(4.0)))
        val mask = ListIota(listOf(BooleanIota(true), BooleanIota(false), BooleanIota(true), BooleanIota(false)))
        val res = OpListPartition.execute(listOf(items, mask), null!!)
        val trueList = (res[0] as ListIota).list
        val falseList = (res[1] as ListIota).list
        Assertions.assertEquals(2, trueList.size)
        Assertions.assertEquals(2, falseList.size)
        Assertions.assertEquals(1.0, (trueList[0] as DoubleIota).double)
        Assertions.assertEquals(3.0, (trueList[1] as DoubleIota).double)
        Assertions.assertEquals(2.0, (falseList[0] as DoubleIota).double)
        Assertions.assertEquals(4.0, (falseList[1] as DoubleIota).double)
    }

    @Test
    fun testListWindows() {
        // Sliding windows of length 2 over [1, 2, 3] -> [[1, 2], [2, 3]]
        val list = ListIota(listOf(DoubleIota(1.0), DoubleIota(2.0), DoubleIota(3.0)))
        val windowSize = DoubleIota(2.0)
        val res = OpListWindows.execute(listOf(list, windowSize), null!!)
        val windows = (res[0] as ListIota).list
        Assertions.assertEquals(2, windows.size)
        val w0 = (windows[0] as ListIota).list
        val w1 = (windows[1] as ListIota).list
        Assertions.assertEquals(2, w0.size)
        Assertions.assertEquals(1.0, (w0[0] as DoubleIota).double)
        Assertions.assertEquals(2.0, (w0[1] as DoubleIota).double)
        Assertions.assertEquals(2.0, (w1[0] as DoubleIota).double)
        Assertions.assertEquals(3.0, (w1[1] as DoubleIota).double)
    }
}
