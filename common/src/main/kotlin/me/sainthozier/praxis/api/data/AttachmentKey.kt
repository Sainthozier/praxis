/*
 * This file is licensed under the All Rights Reserved license, part of Praxis.
 *
 * Copyright (c) 2025 Sainthozier
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

package me.sainthozier.praxis.api.data

import com.mojang.serialization.Codec
import me.sainthozier.praxis.api.PraxisApi
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import java.util.*

/**
 * Represents a unique, type-safe key for a piece of data.
 * This is the "ID card" for your custom data.
 *
 * Instances of this class should be created via the [Builder].
 */
@ConsistentCopyVisibility
data class AttachmentKey<T : Any> private constructor(
    val id: ResourceLocation,
    val targets: Set<AttachmentTarget>,
    val dataClass: Class<T>,
    val persistenceCodec: Codec<T>?,
    val streamCodec: StreamCodec<in RegistryFriendlyByteBuf, T>?,
    val defaultValueFactory: () -> T
) {
    /**
     * Checks if this attachment is designed to be persistent.
     * @return true if a persistence codec is provided, false otherwise.
     */
    val isPersistent: Boolean get() = persistenceCodec != null

    /**
     * Checks if this attachment is designed to be network synchronized.
     * @return true if a stream codec is provided, false otherwise.
     * */
    val isNetworkSynchronized: Boolean get() = streamCodec != null

    companion object {
        /**
         * Creates a new builder for constructing an [AttachmentKey].
         * The data type is inferred from the reified generic parameter.
         */
        inline fun <reified T : Any> builder(): Builder<T> {
            return Builder(T::class.java)
        }

        /**
         * Creates a new builder for constructing an [AttachmentKey].
         * @param dataClass The class of the data type this key will represent.
         */
        @JvmStatic
        fun <T : Any> builder(dataClass: Class<T>): Builder<T> {
            return Builder(dataClass)
        }
    }

    /**
     * A fluent builder for creating [AttachmentKey] instances.
     */
    class Builder<T : Any>(private val dataClass: Class<T>) {
        private var id: ResourceLocation? = null
        private val targets: MutableSet<AttachmentTarget> = EnumSet.noneOf(AttachmentTarget::class.java)
        private var persistenceCodec: Codec<T>? = null
        private var streamCodec: StreamCodec<in RegistryFriendlyByteBuf, T>? = null
        private var defaultValueFactory: (() -> T)? = null

        /** Sets the unique ID for this attachment key. */
        fun id(id: ResourceLocation) = apply { this.id = id }

        /** Sets the object types this data can be attached to. */
        fun targets(vararg targets: AttachmentTarget) = apply { this.targets.addAll(targets) }

        /**
         * Marks the attachment as persistent and provides the codec for serialization.
         * If this is not called, the attachment will be transient (not saved).
         */
        fun persistent(codec: Codec<T>) = apply { this.persistenceCodec = codec }

        /**
         * Marks the attachment as network synchronized and provides the codec for serialization.
         */
        fun networkSynchronized(codec: StreamCodec<in RegistryFriendlyByteBuf, T>) = apply { this.streamCodec = codec }

        /** Sets the factory that provides a default instance of the data. */
        fun defaultValue(factory: () -> T) = apply { this.defaultValueFactory = factory }

        /**
         * Builds the final, immutable [AttachmentKey] and registers it with the active provider.
         * @return A new [AttachmentKey] instance.
         */
        fun build(): AttachmentKey<T> {
            val finalId = requireNotNull(id) { "ID must be set for an AttachmentKey" }
            val finalDefaultFactory =
                requireNotNull(defaultValueFactory) { "A default value factory must be provided for AttachmentKey with id $finalId" }
            require(targets.isNotEmpty()) { "At least one target must be specified for AttachmentKey with id $finalId" }

            val key =
                AttachmentKey(finalId, targets.toSet(), dataClass, persistenceCodec, streamCodec, finalDefaultFactory)

            PraxisApi.attachmentManager.registerKey(key)

            return key
        }
    }
}