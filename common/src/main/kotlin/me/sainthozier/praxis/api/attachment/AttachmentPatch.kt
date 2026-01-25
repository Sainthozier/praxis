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

package me.sainthozier.praxis.api.attachment

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import me.sainthozier.praxis.api.PraxisApi
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import java.util.*
import kotlin.collections.iterator

/**
 * A collection of attachment modifications to be applied to a holder in a single operation.
 * This is analogous to vanilla's DataComponentPatch.
 *
 * Use the [Builder] to construct instances of this class.
 */
class AttachmentPatch private constructor(
    private val patches: Map<AttachmentKey<*>, Optional<*>>
) : Iterable<Map.Entry<AttachmentKey<*>, Optional<*>>> {
    /**
     * Checks if this patch is empty.
     * @return true if the patch contains no modifications.
     */
    fun isEmpty(): Boolean = patches.isEmpty()

    /**
     * Gets the modification for a given key from this patch.     *
     * @param key The attachment key to look up.
     * @return An [Optional] containing the new value, or an empty [Optional] if the attachment is to be removed.
     * Returns null if the key is not present in this patch.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: AttachmentKey<T>): Optional<T>? {
        return patches[key] as? Optional<T>
    }

    /**
     * Combines this patch with another patch, producing a new patch that contains all modifications from both patches.
     * If a key is present in both patches, the value from the other patch will be used.
     * @param other The other patch to combine with.
     * @return A new [AttachmentPatch] instance containing all modifications from both patches.
     */
    fun combine(other: AttachmentPatch): AttachmentPatch {
        if (other.isEmpty()) return this
        if (this.isEmpty()) return other

        val combined = patches.toMutableMap()
        combined.putAll(other.patches)
        return AttachmentPatch(combined)
    }

    /**
     *  Filters this patch to only include modifications for the given keys.
     *  @param predicate The predicate to filter by.
     *  @return A new [AttachmentPatch] instance containing only the modifications for the given keys.
     */
    fun filter(predicate: (AttachmentKey<*>) -> Boolean): AttachmentPatch {
        if (isEmpty()) return EMPTY

        val filtered = patches.filterKeys(predicate)
        return if (filtered.isEmpty()) EMPTY else AttachmentPatch(filtered)
    }

    /**
     * Splits this patch into two parts: one containing only the modifications to be added, and one containing only the
     * modifications to be removed.
     * @return A [SplitResult] containing the added and removed modifications.
     */
    fun split(): SplitResult {
        if (isEmpty()) return SplitResult(emptyMap(), emptySet())

        val added = mutableMapOf<AttachmentKey<*>, Any>()
        val removed = mutableSetOf<AttachmentKey<*>>()

        for ((key, optionalValue) in patches) {
            if (optionalValue.isPresent) {
                added[key] = optionalValue.get()
            } else {
                removed.add(key)
            }
        }
        return SplitResult(added, removed)
    }

    /**
     * Executes the given action for each modification in this patch.     *
     * @param action The action to perform for each key-value pair.
     */
    fun forEach(action: (key: AttachmentKey<*>, value: Optional<*>) -> Unit) {
        patches.forEach(action)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AttachmentPatch
        return patches == other.patches
    }

    override fun hashCode(): Int {
        return patches.hashCode()
    }

    override fun toString(): String {
        return patches.entries.joinToString(prefix = "{", postfix = "}", separator = ", ") { (key, optionalValue) ->
            if (optionalValue.isPresent) {
                "${key.id}=>${optionalValue.get()}"
            } else {
                "!${key.id}"
            }
        }
    }

    override fun iterator(): Iterator<Map.Entry<AttachmentKey<*>, Optional<*>>> {
        return patches.iterator()
    }

    data class SplitResult(
        val added: Map<AttachmentKey<*>, Any>,
        val removed: Set<AttachmentKey<*>>
    )

    private data class PatchMapKey(val key: AttachmentKey<*>, val removed: Boolean) {
        fun valueCodec(): Codec<out Any?> {
            return if (removed) {
                Codec.unit(Unit)
            } else {
                key.persistenceCodec
                    ?: throw IllegalStateException("Cannot serialize transient attachment key: ${key.id}")
            }
        }

        companion object {
            private const val REMOVED_PREFIX = "!"

            val CODEC: Codec<PatchMapKey> = Codec.STRING.flatXmap({ stringId ->
                val isRemoved = stringId.startsWith(REMOVED_PREFIX)
                val actualIdString = if (isRemoved) stringId.substring(1) else stringId
                val actualId = ResourceLocation.tryParse(actualIdString)
                    ?: return@flatXmap DataResult.error { "Invalid resource location for patch key: $actualIdString" }

                val attachmentKey = PraxisApi.attachmentManager.getKey(actualId)
                    ?: return@flatXmap DataResult.error { "Unknown attachment key: $actualId" }

                if (!attachmentKey.isPersistent) {
                    return@flatXmap DataResult.error { "Cannot serialize patch for transient attachment key: $actualId" }
                }

                DataResult.success(PatchMapKey(attachmentKey, isRemoved))
            }, { patchKey ->
                val id = patchKey.key.id
                val stringId = if (patchKey.removed) "$REMOVED_PREFIX$id" else id.toString()
                DataResult.success(stringId)
            })
        }
    }

    companion object {
        /** An empty patch that makes no changes. */
        @JvmField
        val EMPTY = AttachmentPatch(emptyMap())

        /** Creates a new builder for constructing an [AttachmentPatch]. */
        @JvmStatic
        fun builder(): Builder = Builder()

        /** Creates a patch that sets a single value. */
        @JvmStatic
        fun <T : Any> of(key: AttachmentKey<T>, value: T): AttachmentPatch {
            return AttachmentPatch(mapOf(key to Optional.of(value)))
        }

        /** Creates a patch that removes a single value. */
        @JvmStatic
        fun removal(key: AttachmentKey<*>): AttachmentPatch {
            return AttachmentPatch(mapOf(key to Optional.empty<Any>()))
        }

        /**
         * A codec for serializing and deserializing an AttachmentPatch.
         * This mimics the behavior of the vanilla DataComponentPatch codec.
         */
        @JvmField
        val CODEC: Codec<AttachmentPatch> =
            Codec.dispatchedMap(PatchMapKey.CODEC) { it.valueCodec() }
                .xmap({ map ->
                    if (map.isEmpty()) return@xmap EMPTY

                    val patches = mutableMapOf<AttachmentKey<*>, Optional<*>>()
                    map.forEach { (patchKey, value) ->
                        patches[patchKey.key] = if (patchKey.removed) Optional.empty<Any>() else Optional.of(value!!)
                    }
                    AttachmentPatch(patches)
                }, { patch ->
                    val map = mutableMapOf<PatchMapKey, Any?>()
                    patch.forEach { key, optionalValue ->
                        if (key.isPersistent) {
                            if (optionalValue.isPresent) {
                                map[PatchMapKey(key, false)] = optionalValue.get()
                            } else {
                                map[PatchMapKey(key, true)] = Unit
                            }
                        }
                    }
                    map
                })

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AttachmentPatch> =
            object : StreamCodec<RegistryFriendlyByteBuf, AttachmentPatch> {
                override fun decode(buf: RegistryFriendlyByteBuf): AttachmentPatch {
                    val setEntries = buf.readVarInt()
                    val removedEntries = buf.readVarInt()
                    if (setEntries == 0 && removedEntries == 0) return EMPTY

                    val patches = mutableMapOf<AttachmentKey<*>, Optional<*>>()
                    repeat(setEntries) {
                        val key = PraxisApi.attachmentManager.getKey(ResourceLocation.STREAM_CODEC.decode(buf))
                        if (key != null && key.isNetworkSynchronized) {
                            val value = key.streamCodec!!.decode(buf)
                            patches[key] = Optional.of(value)
                        }
                    }
                    repeat(removedEntries) {
                        val key = PraxisApi.attachmentManager.getKey(ResourceLocation.STREAM_CODEC.decode(buf))
                        if (key != null) {
                            patches[key] = Optional.empty<Any>()
                        }
                    }
                    return AttachmentPatch(patches)
                }

                override fun encode(buf: RegistryFriendlyByteBuf, patch: AttachmentPatch) {
                    if (patch.isEmpty()) {
                        buf.writeVarInt(0)
                        buf.writeVarInt(0)
                        return
                    }

                    val toSet = patch.patches.filter { it.key.isNetworkSynchronized && it.value.isPresent }
                    val toRemove = patch.patches.filter { it.key.isNetworkSynchronized && it.value.isEmpty }

                    buf.writeVarInt(toSet.size)
                    buf.writeVarInt(toRemove.size)

                    toSet.forEach { (key, optionalValue) ->
                        ResourceLocation.STREAM_CODEC.encode(buf, key.id)
                        @Suppress("UNCHECKED_CAST")
                        (key.streamCodec as StreamCodec<RegistryFriendlyByteBuf, Any>).encode(buf, optionalValue.get())
                    }
                    toRemove.forEach { (key, _) ->
                        ResourceLocation.STREAM_CODEC.encode(buf, key.id)
                    }
                }
            }
    }

    /**
     * A builder for creating [AttachmentPatch].
     */
    class Builder {
        private val patches: MutableMap<AttachmentKey<*>, Optional<*>> = mutableMapOf()

        /**
         * Sets or replaces an attachment's value in the patch.
         *
         * @param key The key of the attachment to set.
         * @param value The new value for the attachment.
         * @return This builder, for chaining.
         */
        fun <T : Any> set(key: AttachmentKey<T>, value: T): Builder {
            patches[key] = Optional.of(value)
            return this
        }

        /**
         * Marks an attachment for removal in the patch.
         *
         * @param key The key of the attachment to remove.
         * @return This builder, for chaining.
         */
        fun <T : Any> remove(key: AttachmentKey<T>): Builder {
            patches[key] = Optional.empty<T>()
            return this
        }

        /**
         * Builds the immutable [AttachmentPatch] from the current state of the builder.
         *
         * @return A new [AttachmentPatch] instance.
         */
        fun build(): AttachmentPatch {
            if (patches.isEmpty()) {
                return EMPTY
            }
            return AttachmentPatch(patches.toMap())
        }
    }
}