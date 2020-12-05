package com.teamwizardry.librarianlib.foundation.block

import net.minecraft.block.FenceBlock
import net.minecraft.block.FenceGateBlock
import net.minecraftforge.client.model.generators.BlockStateProvider

/**
 * A base class for Foundation fence gates. The passed [textureName] is used for the model texture. e.g. for oak fence
 * gates, the `textureName` would be `oak_planks`.
 *
 * Required textures:
 * - `<modid>:block/<textureName>.png`
 */
public open class BaseFenceGateBlock(properties: Properties, private val textureName: String):
    FenceGateBlock(properties), IFoundationBlock {
    override fun generateBlockState(gen: BlockStateProvider) {
        gen.fenceGateBlock(this, gen.modLoc("block/$textureName"))
    }
}