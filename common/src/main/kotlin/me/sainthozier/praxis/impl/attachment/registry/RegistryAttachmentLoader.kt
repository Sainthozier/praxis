/*
 * This file is licensed under the All Rights Reserved license, part of Praxis.
 *
 * Copyright (c) 2025 - 2026 Sainthozier
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Educational Use: Additionally, permission is granted to use and partially incorporate this code for educational
 * purposes and within personal projects, provided that original authorship is acknowledged. This permission is intended
 * to facilitate learning and knowledge sharing within the modding community.
 */

package me.sainthozier.praxis.impl.attachment.registry

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.datafixers.util.Either
import com.mojang.serialization.JsonOps
import me.sainthozier.praxis.api.attachment.registry.RegistryAttachmentType
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.tags.TagKey
import net.minecraft.util.GsonHelper
import net.minecraft.util.profiling.ProfilerFiller
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class RegistryAttachmentLoader(
    private val registryAccess: RegistryAccess
) : SimplePreparableReloadListener<Map<ResourceKey<out Registry<*>>, Map<RegistryAttachmentType<*, *>, List<RegistryAttachmentDataFile<*, *>>>>>() {

    private var pendingData: Map<ResourceKey<out Registry<*>>, Map<RegistryAttachmentType<*, *>, List<RegistryAttachmentDataFile<*, *>>>>? =
        null

    companion object {
        private val GSON = Gson()
        private val LOG = LoggerFactory.getLogger("RegistryAttachmentLoader")
        const val DIRECTORY = "registry_attachments"
    }

    @Suppress("UNCHECKED_CAST")
    override fun prepare(
        resourceManager: ResourceManager, profiler: ProfilerFiller
    ): Map<ResourceKey<out Registry<*>>, Map<RegistryAttachmentType<*, *>, List<RegistryAttachmentDataFile<*, *>>>> {
        LOG.info("Starting registry attachment scan...")
        val finalResults =
            ConcurrentHashMap<ResourceKey<out Registry<*>>, Map<RegistryAttachmentType<*, *>, List<RegistryAttachmentDataFile<*, *>>>>()
        var totalRegistriesProcessed = 0

        for (registryEntry in registryAccess.registries()) {
            val registryKey = registryEntry.key()
            val attachmentTypes = RegistryAttachmentTypeRegistry.getTypesForRegistry(registryKey)

            if (attachmentTypes.isEmpty()) continue
            totalRegistriesProcessed++

            val registryFiles =
                ConcurrentHashMap<RegistryAttachmentType<*, *>, List<RegistryAttachmentDataFile<*, *>>>()

            for (type in attachmentTypes) {
                val typedType = type as RegistryAttachmentType<Any, Any>
                val files = loadAttachmentFiles(typedType, resourceManager)
                if (files.isNotEmpty()) {
                    registryFiles[type] = files
                }
            }

            if (registryFiles.isNotEmpty()) {
                finalResults[registryKey] = registryFiles
            }
        }

        LOG.info("Scan complete. Found pending attachments for $totalRegistriesProcessed registries.")
        return finalResults
    }

    private fun <R, T> loadAttachmentFiles(
        type: RegistryAttachmentType<R, T>, manager: ResourceManager
    ): List<RegistryAttachmentDataFile<R, T>> {
        val validFiles = LinkedList<RegistryAttachmentDataFile<R, T>>()
        val attachmentNs = type.id.namespace
        val attachmentPath = type.id.path
        val regLoc = type.registryKey.location()
        val middlePath = if (regLoc.namespace == "minecraft") regLoc.path else "${regLoc.namespace}/${regLoc.path}"
        val resourceLocation =
            ResourceLocation.fromNamespaceAndPath(attachmentNs, "$DIRECTORY/$middlePath/$attachmentPath.json")

        val fileCodec = RegistryAttachmentDataFile.codec(type.registryKey, type)

        for (resource in manager.getResourceStack(resourceLocation)) {
            try {
                resource.openAsReader().use { reader ->
                    val json = GsonHelper.fromJson(GSON, reader, JsonElement::class.java)
                    fileCodec.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial { LOG.error("Syntax error in '$resourceLocation': $it") }
                        .ifPresent { dataFile ->
                            if (dataFile.conditions.all { it.test() }) {
                                validFiles.add(dataFile)
                            } else {
                                LOG.debug("Skipping file '{}' due to conditions.", resourceLocation)
                            }
                        }
                }
            } catch (e: Exception) {
                LOG.error("Failed to load attachment '$resourceLocation'", e)
            }
        }
        return validFiles
    }

    @Suppress("UNCHECKED_CAST")
    override fun apply(
        prepared: Map<ResourceKey<out Registry<*>>, Map<RegistryAttachmentType<*, *>, List<RegistryAttachmentDataFile<*, *>>>>,
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        this.pendingData = prepared
        LOG.info("Attachments prepared, waiting for tags to bind...")
    }

    fun finishAndApply() {
        val data = this.pendingData ?: return
        LOG.info("Applying registry attachments...")

        data.forEach { (registryKey, attachments) ->
            applyToRegistry(registryKey, attachments)
        }

        this.pendingData = null
    }

    @Suppress("UNCHECKED_CAST")
    private fun applyToRegistry(
        registryKey: ResourceKey<out Registry<*>>,
        attachments: Map<RegistryAttachmentType<*, *>, List<RegistryAttachmentDataFile<*, *>>>
    ) {
        val registry = registryAccess.registryOrThrow(registryKey as ResourceKey<out Registry<Any>>)

        if (registry is RegistryAttachmentHolder<*>) {
            val holder = registry as RegistryAttachmentHolder<Any>
            holder.`praxis$clearAttachments`()

            attachments.forEach { (type, files) ->
                val typedType = type as RegistryAttachmentType<Any, Any>
                val typedFiles = files as List<RegistryAttachmentDataFile<Any, Any>>

                val resolvedMap = resolveAndMerge(registry, typedFiles)
                holder.`praxis$setAttachmentsForType`(typedType, resolvedMap)

                LOG.debug(
                    "Registry '{}' -> Attached '{}' ({} values)",
                    registryKey.location(),
                    typedType.id,
                    resolvedMap.size
                )
            }
        } else {
            LOG.error("CRITICAL: Registry '${registryKey.location()}' does not implement RegistryAttachmentHolder.")
        }
    }

    // TODO: actually add merging logic
    private fun <R, T> resolveAndMerge(
        registry: Registry<R>,
        files: List<RegistryAttachmentDataFile<R, T>>
    ): Map<ResourceKey<R>, T> {
        val merged = IdentityHashMap<ResourceKey<R>, T>()

        for (file in files) {
            if (file.replace) merged.clear()

            file.values.forEach { (source, entry) ->
                resolveKeyOrTag(registry, source, true) { holder ->
                    val key = holder.unwrapKey().orElseThrow()
                    if (merged[key] == null || entry.replace) {
                        merged[key] = entry.value
                    }
                }
            }

            file.removals.forEach { removal ->
                resolveKeyOrTag(registry, removal.key, false) { holder ->
                    val key = holder.unwrapKey().orElseThrow()
                    merged.remove(key)
                }
            }
        }
        return merged
    }

    private fun <R> resolveKeyOrTag(
        registry: Registry<R>,
        keyOrTag: Either<TagKey<R>, ResourceKey<R>>,
        required: Boolean,
        action: (Holder<R>) -> Unit
    ) {
        keyOrTag.ifLeft { tag ->
            registry.getTagOrEmpty(tag).forEach(action)
        }.ifRight { id ->
            val holder = registry.getHolder(id)
            if (holder.isPresent) {
                action(holder.get())
            } else if (required) {
                LOG.warn(
                    "Target object '${id.location()}' not found in registry '${
                        registry.key().location()
                    }'. Ignoring."
                )
            }
        }
    }
}