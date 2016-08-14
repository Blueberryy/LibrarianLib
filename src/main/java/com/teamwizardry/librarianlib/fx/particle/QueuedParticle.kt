package com.teamwizardry.librarianlib.fx.particle

import net.minecraft.client.particle.Particle
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

abstract class QueuedParticle protected constructor(worldIn: World, posXIn: Double, posYIn: Double, posZIn: Double) : Particle(worldIn, posXIn, posYIn, posZIn) {

    var partialTicks: Float = 0.toFloat()
    var rotationX: Float = 0.toFloat()
    var rotationZ: Float = 0.toFloat()
    var rotationYZ: Float = 0.toFloat()
    var rotationXY: Float = 0.toFloat()
    var rotationXZ: Float = 0.toFloat()
    var distFromPlayer: Double = 0.toDouble()

    protected abstract fun queue(): ParticleRenderQueue<*>

    val pos: Vec3d
        get() {
            val posX = (this.prevPosX + (this.posX - this.prevPosX) * partialTicks.toDouble() - Particle.interpPosX).toFloat()
            val posY = (this.prevPosY + (this.posY - this.prevPosY) * partialTicks.toDouble() - Particle.interpPosY).toFloat()
            val posZ = (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks.toDouble() - Particle.interpPosZ).toFloat()
            return Vec3d(posX.toDouble(), posY.toDouble(), posZ.toDouble())
        }

    override fun renderParticle(worldRendererIn: VertexBuffer, entityIn: Entity?, partialTicks: Float, rotationX: Float,
                                rotationZ: Float, rotationYZ: Float, rotationXY: Float, rotationXZ: Float) {
        this.partialTicks = partialTicks
        this.rotationX = rotationX
        this.rotationZ = rotationZ
        this.rotationYZ = rotationYZ
        this.rotationXY = rotationXY
        this.rotationXZ = rotationXZ

        queue().add(this)
    }

}
