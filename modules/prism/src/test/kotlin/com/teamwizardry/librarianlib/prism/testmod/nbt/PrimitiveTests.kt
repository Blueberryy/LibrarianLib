package com.teamwizardry.librarianlib.prism.testmod.nbt

import com.teamwizardry.librarianlib.core.util.kotlin.NBTBuilder
import com.teamwizardry.librarianlib.prism.nbt.PrimitiveBooleanSerializer
import com.teamwizardry.librarianlib.prism.nbt.PrimitiveByteSerializer
import com.teamwizardry.librarianlib.prism.nbt.PrimitiveCharSerializer
import com.teamwizardry.librarianlib.prism.nbt.PrimitiveDoubleSerializer
import com.teamwizardry.librarianlib.prism.nbt.PrimitiveFloatSerializer
import com.teamwizardry.librarianlib.prism.nbt.PrimitiveIntSerializer
import com.teamwizardry.librarianlib.prism.nbt.PrimitiveLongSerializer
import com.teamwizardry.librarianlib.prism.nbt.PrimitiveShortSerializer
import dev.thecodewarrior.mirror.Mirror
import org.junit.jupiter.api.Test

internal class PrimitiveTests: NBTPrismTest() {
    @Test
    fun `read+write for a double should be symmetrical`()
        = simple<PrimitiveDoubleSerializer>(Mirror.types.double, 1.0, NBTBuilder.double(1))
    @Test
    fun `read+write for a float should be symmetrical`()
        = simple<PrimitiveFloatSerializer>(Mirror.types.float, 1f, NBTBuilder.float(1))
    @Test
    fun `read+write for a long should be symmetrical`()
        = simple<PrimitiveLongSerializer>(Mirror.types.long, 1L, NBTBuilder.long(1))
    @Test
    fun `read+write for an int should be symmetrical`()
        = simple<PrimitiveIntSerializer>(Mirror.types.int, 1, NBTBuilder.int(1))
    @Test
    fun `read+write for a short should be symmetrical`()
        = simple<PrimitiveShortSerializer>(Mirror.types.short, 1.toShort(), NBTBuilder.short(1))
    @Test
    fun `read+write for a byte should be symmetrical`()
        = simple<PrimitiveByteSerializer>(Mirror.types.byte, 1.toByte(), NBTBuilder.byte(1))
    @Test
    fun `read+write for a char should be symmetrical`()
        = simple<PrimitiveCharSerializer>(Mirror.types.char, 1.toChar(), NBTBuilder.int(1))
    @Test
    fun `read+write for true should be symmetrical`()
        = simple<PrimitiveBooleanSerializer>(Mirror.types.boolean, true, NBTBuilder.byte(1))
    @Test
    fun `read+write for false should be symmetrical`()
        = simple<PrimitiveBooleanSerializer>(Mirror.types.boolean, false, NBTBuilder.byte(0))

    @Test
    fun `read for double with IntNBT should cast`()
        = simpleRead<PrimitiveDoubleSerializer>(Mirror.types.double, 1.0, NBTBuilder.int(1))
    @Test
    fun `read for float with IntNBT should cast`()
        = simpleRead<PrimitiveFloatSerializer>(Mirror.types.float, 1f, NBTBuilder.int(1))
    @Test
    fun `read for long with DoubleNBT should cast and clamp`()
        = simpleRead<PrimitiveLongSerializer>(Mirror.types.long, Long.MAX_VALUE, NBTBuilder.double(1e20))
    @Test
    fun `read for int with DoubleNBT should cast and clamp`()
        = simpleRead<PrimitiveIntSerializer>(Mirror.types.int, Int.MAX_VALUE, NBTBuilder.double(1e10))
    @Test
    fun `read for short with DoubleNBT should cast and truncate`()
        = simpleRead<PrimitiveShortSerializer>(Mirror.types.short, 100000.toShort(), NBTBuilder.double(1e5))
    @Test
    fun `read for byte with DoubleNBT should cast and truncate`()
        = simpleRead<PrimitiveByteSerializer>(Mirror.types.byte, 1000.toByte(), NBTBuilder.double(1e3))
    @Test
    fun `read for char with DoubleNBT should cast and truncate`()
        = simpleRead<PrimitiveCharSerializer>(Mirror.types.char, 100000.toChar(), NBTBuilder.double(1e5))
}