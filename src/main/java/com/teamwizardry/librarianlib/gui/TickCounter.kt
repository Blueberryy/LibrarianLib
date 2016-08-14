package com.teamwizardry.librarianlib.gui

import java.lang.reflect.Field

import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase
import net.minecraftforge.fml.relauncher.ReflectionHelper

import net.minecraft.client.Minecraft
import net.minecraft.util.Timer

class TickCounter {

    private fun calcDelta() {
        val oldTotal = total
        total = ticksInGame + partialTicks
        delta = total - oldTotal
    }

    @SubscribeEvent
    fun renderTick(event: TickEvent.RenderTickEvent) {
        if (event.phase == Phase.START)
            partialTicks = event.renderTickTime
    }

    @SubscribeEvent
    fun clientTickEnd(event: ClientTickEvent) {
        if (event.phase == Phase.END) {
            val mc = Minecraft.getMinecraft()
            val gui = mc.currentScreen
            if (gui == null || !gui.doesGuiPauseGame()) {
                ticksInGame++
                partialTicks = 0f
            }

            ticks++

            calcDelta()
        }
    }

    companion object {

        var ticks = 0
        var ticksInGame = 0
        var partialTicks = 0f
        var delta = 0f
        var total = 0f
    }

}
