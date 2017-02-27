package com.teamwizardry.librarianlib.test.testcore

import com.teamwizardry.librarianlib.LibrarianLib
import com.teamwizardry.librarianlib.LibrarianLog
import com.teamwizardry.librarianlib.common.util.getUnsafe
import com.teamwizardry.librarianlib.common.util.hookIntoUnsafe
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLInterModComms
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

/**
 * Created by Elad on 1/19/2017.
 */
object UnsafeTest : TestEntryPoint {
    init {
        hookIntoUnsafe()
    }

    override fun preInit(event: FMLPreInitializationEvent) {

    }

    override fun init(event: FMLInitializationEvent) {

    }

    override fun postInit(event: FMLPostInitializationEvent) {
        LibrarianLog.info("${TestMod.MODID} | ${getUnsafe()}")
    }
}
