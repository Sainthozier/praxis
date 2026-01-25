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

import me.sainthozier.praxis.api.attachment.registry.RegistryAttachmentType
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.ConcurrentHashMap

object RegistryAttachmentTypeRegistry {
    private val attachments =
        ConcurrentHashMap<ResourceKey<Registry<*>>, MutableMap<ResourceLocation, RegistryAttachmentType<*, *>>>()

    fun <R, T> register(registry: ResourceKey<Registry<R>>, attachmentType: RegistryAttachmentType<R, T>) {
        @Suppress("UNCHECKED_CAST")
        val registryKey = registry as ResourceKey<Registry<*>>

        val registryMap = attachments.computeIfAbsent(registryKey) { ConcurrentHashMap() }

        if (registryMap.containsKey(attachmentType.id)) {
            throw IllegalArgumentException("Attachment type '${attachmentType.id}' already registered.")
        }

        registryMap[attachmentType.id] = attachmentType
    }

    fun hasAttachments(registryKey: ResourceKey<out Registry<*>>): Boolean = attachments.containsKey(registryKey)

    fun getAttachmentType(registry: ResourceKey<out Registry<*>>, id: ResourceLocation): RegistryAttachmentType<*, *>? =
        attachments[registry]?.get(id)

    fun hasAttachmentType(registry: ResourceKey<out Registry<*>>, id: ResourceLocation): Boolean =
        attachments[registry]?.containsKey(id) ?: false

    fun getAttachmentsForRegistry(registryKey: ResourceKey<out Registry<*>>): Map<ResourceLocation, RegistryAttachmentType<*, *>> =
        attachments[registryKey] ?: emptyMap()

    fun getTypesForRegistry(registryKey: ResourceKey<out Registry<*>>): Collection<RegistryAttachmentType<*, *>> =
        attachments[registryKey]?.values ?: emptyList()

    fun getAllRegisteredTypes(): Collection<RegistryAttachmentType<*, *>> = attachments.values.flatMap { it.values }
}