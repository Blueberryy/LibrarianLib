package com.teamwizardry.librarianlib.common.base.block

import com.google.common.base.Optional
import net.minecraft.block.properties.PropertyHelper
import java.util.*

/**
 * @author WireSegal
 * Created at 4:31 PM on 1/8/17.
 */
open class PropertyString(name: String, open val values: SortedSet<String>) : PropertyHelper<String>(name, String::class.java) {
    constructor(name: String, vararg values: String) : this(name, values.toSortedSet())

    private val indexToValues by lazy { mapOf(*values.mapIndexed { i, s -> i to s }.toTypedArray()) }
    private val valuesToIndex by lazy { mapOf(*values.mapIndexed { i, s -> s to i }.toTypedArray()) }

    override fun parseValue(value: String): Optional<String> = if (value in values) Optional.of(value) else Optional.absent()
    override fun getName(value: String) = value
    override fun getAllowedValues() = values

    fun getMetaFromName(name: String) = valuesToIndex[name] ?: 0
    fun getNameFromMeta(meta: Int) = indexToValues[meta] ?: values.first()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PropertyString) return false
        if (!super.equals(other)) return false
        if (values != other.values) return false
        return true
    }

    override fun hashCode() = 31 * super.hashCode() + values.hashCode()
}


