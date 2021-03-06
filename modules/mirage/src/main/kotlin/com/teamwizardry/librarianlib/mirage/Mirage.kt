package com.teamwizardry.librarianlib.mirage

import com.teamwizardry.librarianlib.core.util.kotlin.synchronized
import net.minecraft.resources.FallbackResourceManager
import net.minecraft.resources.IResourceManager
import net.minecraft.resources.IResourcePack
import net.minecraft.resources.ResourcePack
import net.minecraft.resources.ResourcePackType
import net.minecraft.resources.SimpleReloadableResourceManager
import net.minecraft.resources.data.IMetadataSectionSerializer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.LanguageMap
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.function.Predicate
import kotlin.concurrent.read
import kotlin.concurrent.write

class Mirage internal constructor(val type: ResourcePackType) {
    internal val fallback = FallbackResourceManager(type, "librarianlib")
    internal val files = mutableMapOf<ResourceLocation, ByteArray>().synchronized()
    internal val generators = mutableMapOf<ResourceLocation, () -> ByteArray>().synchronized()
    internal val packs = mutableListOf<VirtualResourcePack>().synchronized()
    internal val languageKeys = mutableMapOf<String, String>().synchronized()
    internal val lock = ReentrantReadWriteLock()

    fun remove(location: ResourceLocation): Boolean {
        lock.write {
            var removed = false
            removed = removeFile(location) || removed // can't do removeFile || removeGenerator because of short-circuiting
            removed = removeGenerator(location) || removed
            return removed
        }
    }

    fun removeFile(location: ResourceLocation): Boolean {
        lock.write {
            if(files.remove(location) != null) {
                logger.debug("Removed file $location")
                return true
            }
            return false
        }
    }

    fun removeGenerator(location: ResourceLocation): Boolean {
        lock.write {
            if(generators.remove(location) != null) {
                logger.debug("Removed generator $location")
                return true
            }
            return false
        }
    }

    fun add(location: ResourceLocation, text: String) {
        addRaw(location, text.toByteArray())
    }

    fun addRaw(location: ResourceLocation, data: ByteArray) {
        lock.write {
            files[location] = data
            logger.debug("Added resource $location")
        }
    }

    /**
     * Note: the passed generator may be run on another thread
     */
    fun add(location: ResourceLocation, generator: () -> String) {
        addRaw(location) {
            generator().toByteArray()
        }
    }

    /**
     * Note: the passed generator may be run on another thread
     */
    fun addRaw(location: ResourceLocation, generator: () -> ByteArray) {
        lock.write {
            generators[location] = generator
            logger.debug("Added resource generator $location")
        }
    }

    /**
     * Add a language key.
     */
    fun addLanguageKey(name: String, value: String) {
        lock.write {
            languageKeys[name] = value
            logger.debug("Added language key $name")
        }
    }

    internal inline fun <T> read(callback: (Mirage) -> T): T = lock.read { callback(this) }
    internal inline fun <T> write(callback: (Mirage) -> T): T = lock.write { callback(this) }

    companion object {
        @JvmField
        val client = Mirage(ResourcePackType.CLIENT_RESOURCES)
        @JvmField
        val data = Mirage(ResourcePackType.SERVER_DATA)

        @JvmStatic
        fun resources(type: ResourcePackType): Mirage = when(type) {
            ResourcePackType.CLIENT_RESOURCES -> client
            ResourcePackType.SERVER_DATA -> data
        }

        /**
         * This injects our virtual resource pack as a last resort for every FallbackResourceManager
         * in the game.
         *
         * Injects in:
         * - [FallbackResourceManager.&lt;init&gt;][FallbackResourceManager]
         */
        @JvmStatic
        fun `fallbackresourcemanager-init-asm`(pack: FallbackResourceManager) {
            pack.resourcePacks.add(Pack)
        }

        /**
         * This allows virtual resources to be in any namespace. Without this patch, if the
         * SimpleReloadableResourceManager isn't aware of a namespace it'll bail out early, never
         * even checking if any packs contain that asset. This provides a default manager in case
         * the namespace isn't recognized, making sure that it'll still query our pack.
         *
         * Injects in:
         * - [SimpleReloadableResourceManager.getResource]
         * - [SimpleReloadableResourceManager.hasResource]
         * - [SimpleReloadableResourceManager.getAllResources]
         * - [SimpleReloadableResourceManager.getAllResourceLocations]
         */
        @JvmStatic
        fun `simplereloadableresourcemanager-namespace_fallback-asm`(manager: IResourceManager?, type: ResourcePackType): IResourceManager? {
            return manager ?: resources(type).fallback
        }

        /**
         * Injects in:
         * - [LanguageMap.tryTranslateKey]
         */
        @JvmStatic
        fun `languagemap-trytranslatekey-asm`(result: String?, key: String?): String? {
            return result
                ?: resources(ResourcePackType.SERVER_DATA).read { it.languageKeys[key] }
                ?: resources(ResourcePackType.CLIENT_RESOURCES).read { it.languageKeys[key] }
        }

        /**
         * Injects in:
         * - [LanguageMap.exists]
         */
        @JvmStatic
        fun `languagemap-exists-asm`(result: Boolean, key: String?): Boolean {
            return result ||
                resources(ResourcePackType.SERVER_DATA).read { key in it.languageKeys } ||
                resources(ResourcePackType.CLIENT_RESOURCES).read { key in it.languageKeys }
        }

        /**
         *
         */
        @JvmStatic
        fun `locale-translatekeyprivate-asm`(result: String?, key: String?): String? {
            return result
                ?: resources(ResourcePackType.SERVER_DATA).read { it.languageKeys[key] }
                ?: resources(ResourcePackType.CLIENT_RESOURCES).read { it.languageKeys[key] }
        }

        /**
         *
         */
        @JvmStatic
        fun `locale-haskey-asm`(result: Boolean, key: String?): Boolean {
            return result ||
                resources(ResourcePackType.SERVER_DATA).read { key in it.languageKeys } ||
                resources(ResourcePackType.CLIENT_RESOURCES).read { key in it.languageKeys }
        }
    }

    private object Pack: IResourcePack {

        override fun getResourceStream(type: ResourcePackType, location: ResourceLocation): InputStream {
            resources(type).read { resources ->
                resources.files[location]?.also {
                    return ByteArrayInputStream(it)
                }
                resources.generators[location]?.also {
                    return ByteArrayInputStream(it())
                }
                resources.packs.forEach { pack ->
                    pack.getStream(location)?.also {
                        return it
                    }
                }
                throw FileNotFoundException("Virtual resource $location not found")
            }
        }

        override fun getAllResourceLocations(type: ResourcePackType, namespaceIn: String, pathIn: String, maxDepth: Int, filter: Predicate<String>): Collection<ResourceLocation> {
            resources(type).read { resources ->
                val locations = mutableSetOf<ResourceLocation>()

                locations.addAll(resources.files.keys)
                locations.addAll(resources.generators.keys)
                resources.packs.forEach {
                    locations.addAll(it.listResources(pathIn, maxDepth))
                }

                return locations.filter {
                    when {
                        !it.path.startsWith(pathIn) -> false
                        it.path.removePrefix(pathIn).count { it == '/' } > maxDepth -> false
                        it.path.endsWith(".mcmeta") -> false
                        else -> filter.test(it.path.split('/').last())
                    }
                }.sortedBy { it.path }
            }
        }

        override fun resourceExists(type: ResourcePackType, location: ResourceLocation): Boolean {
            resources(type).read { resources ->
                return location in resources.files || location in resources.generators || resources.packs.any { location in it }
            }
        }

        override fun getName(): String = "LibrarianLib Mirage Resources"

        // The LibrarianLib virtual resource pack is added using ASM, so this is never normally called
        override fun getResourceNamespaces(type: ResourcePackType): Set<String> = setOf()

        override fun getRootResourceStream(fileName: String): InputStream {
            return javaClass.getResourceAsStream("/assets/librarianlib/mirage/root_resources/$fileName")
        }

        override fun <T: Any?> getMetadata(deserializer: IMetadataSectionSerializer<T>): T? {
            return ResourcePack.getResourceMetadata(deserializer, getRootResourceStream("pack.mcmeta"))
        }

        override fun close() {
            // nop
        }
    }
}