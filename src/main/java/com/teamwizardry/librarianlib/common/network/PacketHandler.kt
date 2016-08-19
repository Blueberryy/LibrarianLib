package com.teamwizardry.librarianlib.common.network

import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.relauncher.Side

object PacketHandler {

    var network: SimpleNetworkWrapper
    private var id = 0

    init {
        network = NetworkRegistry.INSTANCE.newSimpleChannel("TeamWizardry")
    }

    fun <T : PacketBase> register(clazz: Class<T>, targetSide: Side) {
        network.registerMessage<T, PacketBase>(PacketBase.Handler<T>(), clazz, id++, targetSide)
    }
}
