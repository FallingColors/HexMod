package at.petrak.hex.server

import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*

object TickScheduler {
    val tasks: MutableList<Task> = LinkedList()

    fun schedule(task: Task) {
        this.tasks.add(task)
    }

    fun schedule(ticks: Int, task: Runnable) {
        this.tasks.add(Task(ticks, task))
    }

    @SubscribeEvent
    fun onTick(evt: TickEvent.ServerTickEvent) {
        this.tasks.removeIf {
            it.ticks--
            if (it.ticks <= 0) {
                it.task.run()
                true
            } else {
                false
            }
        }
    }

    data class Task(var ticks: Int, val task: Runnable)
}