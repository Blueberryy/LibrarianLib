package com.teamwizardry.librarianlib.prism.nbt

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.DeserializationException
import net.minecraft.nbt.INBT
import net.minecraft.nbt.StringNBT
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry

open class IForgeRegistryEntrySerializerFactory(prism: NBTPrism): NBTSerializerFactory(prism, Mirror.reflect<IForgeRegistryEntry<*>>()) {
    override fun create(mirror: TypeMirror): NBTSerializer<*> {
        return IForgeRegistryEntrySerializer(prism, mirror as ClassMirror)
    }

    class IForgeRegistryEntrySerializer(prism: NBTPrism, type: ClassMirror): NBTSerializer<IForgeRegistryEntry<*>>(type) {
        private val registryType = type.getSuperclass(IForgeRegistryEntry::class.java).typeParameters[0].erasure
        private val registry: IForgeRegistry<*> by lazy {
            @Suppress("UNCHECKED_CAST")
            GameRegistry.findRegistry(registryType as Class<DummyRegistryEntry>)
        }

        /**
         * The type checker doesn't like accessing `IForgeRegistry<*>.registryName`, so we have to cast it down to an
         * `IForgeRegistry<DummyRegistryEntry>` in order to access the name.
         */
        private val registryName: ResourceLocation by lazy {
            @Suppress("UNCHECKED_CAST")
            (registry as IForgeRegistry<DummyRegistryEntry>).registryName
        }

        override fun deserialize(tag: INBT, existing: IForgeRegistryEntry<*>?): IForgeRegistryEntry<*> {
            val entryName = ResourceLocation(tag.expectType<StringNBT>("tag").string)
            return registry.getValue(entryName)
                ?: throw DeserializationException("Could not find entry $entryName in $registryName")
        }

        override fun serialize(value: IForgeRegistryEntry<*>): INBT {
            return StringNBT.valueOf(value.registryName.toString())
        }

        /**
         * Since I can't cast a `Class<*>` to satisfy the `K extends IForgeRegistryEntry<K>` requirement of
         * [GameRegistry.findRegistry], I have to take advantage of the fact that generic parameters in casts aren't
         * type checked at runtime by casting to a dummy registry entry.
         */
        private abstract class DummyRegistryEntry: IForgeRegistryEntry<DummyRegistryEntry>
    }
}
