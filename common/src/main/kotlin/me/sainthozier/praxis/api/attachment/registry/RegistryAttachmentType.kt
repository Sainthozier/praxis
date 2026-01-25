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

import com.mojang.serialization.Codec
import me.sainthozier.praxis.impl.attachment.registry.RegistryAttachmentTypeRegistry
import net.minecraft.core.Registry
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

class RegistryAttachmentType<R, T> private constructor(
    val id: ResourceLocation,
    val registryKey: ResourceKey<Registry<R>>,
    val codec: Codec<T>,
    val networkCodec: StreamCodec<in RegistryFriendlyByteBuf, T>?
) {
    companion object {
        fun <R, T> builder(
            id: ResourceLocation,
            registryKey: ResourceKey<Registry<R>>,
            codec: Codec<T>
        ): Builder<R, T> = Builder(id, registryKey, codec)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegistryAttachmentType<*, *>

        if (id != other.id) return false
        if (registryKey.registry() != other.registryKey.registry() || registryKey.location() != other.registryKey.location()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + registryKey.registry().hashCode()
        result = 31 * result + registryKey.location().hashCode()
        return result
    }

    class Builder<R, T>(
        private val id: ResourceLocation,
        private val registryKey: ResourceKey<Registry<R>>,
        private val codec: Codec<T>
    ) {
        private var networkCodec: StreamCodec<in RegistryFriendlyByteBuf, T>? = null

        fun networkCodec(networkCodec: StreamCodec<in RegistryFriendlyByteBuf, T>) = apply {
            this.networkCodec = networkCodec
        }

        fun buildAndRegister(): RegistryAttachmentType<R, T> {
            val type = RegistryAttachmentType(id, registryKey, codec, networkCodec)
            RegistryAttachmentTypeRegistry.register(registryKey, type)
            return type
        }
    }
}