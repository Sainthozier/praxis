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

package me.sainthozier.praxis.api.attachment.registry

import me.sainthozier.praxis.impl.attachment.registry.RegistryAttachmentHolder
import me.sainthozier.praxis.impl.attachment.registry.RegistryAttachmentTypeRegistry
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import kotlin.jvm.optionals.getOrNull

object RegistryAttachmentManager {

    @JvmStatic
    fun <R, T> getAttachments(registryKey: ResourceKey<Registry<R>>): Map<ResourceLocation, RegistryAttachmentType<R, T>> {
        @Suppress("UNCHECKED_CAST")
        return RegistryAttachmentTypeRegistry.getAttachmentsForRegistry(registryKey) as Map<ResourceLocation, RegistryAttachmentType<R, T>>
    }

    @JvmStatic
    fun <R> hasAttachments(registryKey: ResourceKey<Registry<R>>): Boolean {
        return RegistryAttachmentTypeRegistry.hasAttachments(registryKey)
    }

    @JvmStatic
    fun <R, T> getAttachment(
        registry: Registry<R>, type: RegistryAttachmentType<R, T>, key: ResourceKey<R>
    ): T? {
        if (registry is RegistryAttachmentHolder<*>) {
            @Suppress("UNCHECKED_CAST")
            return (registry as RegistryAttachmentHolder<R>).`praxis$getAttachment`(
                type,
                key
            )
        }
        return null
    }

    @JvmStatic
    fun <R, T> hasAttachment(
        registry: Registry<R>, type: RegistryAttachmentType<R, T>, key: ResourceKey<R>
    ): Boolean {
        return getAttachment(registry, type, key) != null
    }

    @JvmStatic
    fun <R, T> getAttachmentOrElse(
        registry: Registry<R>, type: RegistryAttachmentType<R, T>, key: ResourceKey<R>, defaultValue: T
    ): T {
        return getAttachment(registry, type, key) ?: defaultValue
    }

    @JvmStatic
    fun <R, T> getAttachmentOrThrow(
        registry: Registry<R>, type: RegistryAttachmentType<R, T>, key: ResourceKey<R>
    ): T {
        return getAttachment(registry, type, key)
            ?: throw IllegalStateException("Missing required attachment '${type.id}' for key '${key.location()}'")
    }

    @JvmStatic
    fun <R, T> getAttachment(
        registryAccess: RegistryAccess, type: RegistryAttachmentType<R, T>, key: ResourceKey<R>
    ): T? {
        val registry = registryAccess.registryOrThrow(type.registryKey)
        return getAttachment(registry, type, key)
    }

    @JvmStatic
    fun <R, T> getAttachment(
        type: RegistryAttachmentType<R, T>, holder: Holder<R>
    ): T? {
        val key = holder.unwrapKey().getOrNull() ?: return null

        val registryKey = type.registryKey
        val registry = BuiltInRegistries.REGISTRY.get(registryKey.location())

        if (registry != null) {
            @Suppress("UNCHECKED_CAST")
            return getAttachment(registry as Registry<R>, type, key)
        }

        return null
    }
}