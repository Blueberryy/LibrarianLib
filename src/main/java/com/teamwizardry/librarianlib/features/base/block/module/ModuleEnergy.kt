package com.teamwizardry.librarianlib.features.base.block.module

import com.teamwizardry.librarianlib.features.kotlin.JSON.obj
import com.teamwizardry.librarianlib.features.kotlin.nbt
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.energy.EnergyStorage
import net.minecraftforge.fluids.capability.CapabilityFluidHandler

/**
 * @author WireSegal
 * Created at 10:41 AM on 6/13/17.
 */
class ModuleEnergy(handler: SerializableEnergyStorage) : ModuleCapability<ModuleEnergy.SerializableEnergyStorage>(CapabilityEnergy.ENERGY, handler) {
    constructor(capacity: Int) : this(SerializableEnergyStorage(capacity))
    constructor(capacity: Int, maxTransfer: Int) : this(SerializableEnergyStorage(capacity, maxTransfer))
    constructor(capacity: Int, maxReceive: Int, maxExtract: Int) : this(SerializableEnergyStorage(capacity, maxReceive, maxExtract))
    constructor(capacity: Int, maxReceive: Int, maxExtract: Int, energy: Int) : this(SerializableEnergyStorage(capacity, maxReceive, maxExtract, energy))

    open class SerializableEnergyStorage : EnergyStorage, INBTSerializable<NBTTagCompound> {

        constructor(capacity: Int) : super(capacity)
        constructor(capacity: Int, maxTransfer: Int) : super(capacity, maxTransfer)
        constructor(capacity: Int, maxReceive: Int, maxExtract: Int) : super(capacity, maxReceive, maxExtract)
        constructor(capacity: Int, maxReceive: Int, maxExtract: Int, energy: Int) : super(capacity, maxReceive, maxExtract, energy)

        override fun deserializeNBT(nbt: NBTTagCompound) {
            energy = nbt.getInteger("Energy")
            maxExtract = nbt.getInteger("Extract")
            maxReceive = nbt.getInteger("Receive")
            capacity = nbt.getInteger("Capacity")
        }

        override fun serializeNBT() = NBTTagCompound().apply {
            setInteger("Energy", energy)
            setInteger("Extract", maxExtract)
            setInteger("Receive", maxReceive)
            setInteger("Capacity", capacity)
        }
    }
}
