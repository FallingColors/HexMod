@file:JvmName("ClientAccessorWrappers")
package at.petrak.hexcasting.client.ktxt

import at.petrak.hexcasting.mixin.accessor.client.AccessorMouseHandler
import net.minecraft.client.MouseHandler

var MouseHandler.accumulatedScroll: Double
    get() = (this as AccessorMouseHandler).`hex$getAccumulatedScrollY`()
    set(value) = (this as AccessorMouseHandler).`hex$setAccumulatedScrollY`(value)
