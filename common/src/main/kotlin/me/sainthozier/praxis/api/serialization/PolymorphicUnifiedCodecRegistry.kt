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

package me.sainthozier.praxis.api.serialization

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.ConcurrentHashMap

open class PolymorphicUnifiedCodecRegistry<B : Any>(
    private val typeGetter: (B) -> ResourceLocation
) {
    private val mapCodecs = ConcurrentHashMap<ResourceLocation, MapCodec<out B>>()
    private val streamCodecs = ConcurrentHashMap<ResourceLocation, StreamCodec<in RegistryFriendlyByteBuf, out B>>()

    val codec: Codec<B> = ResourceLocation.CODEC.dispatch(
        typeGetter
    ) { id -> mapCodecs[id] ?: throw IllegalArgumentException("Unknown polymorphic type: $id") }

    val streamCodec: StreamCodec<RegistryFriendlyByteBuf, B> = StreamCodec.of({ buf, value ->
        val id = typeGetter(value)
        val encoder = streamCodecs[id] ?: throw IllegalArgumentException("Unknown polymorphic type: $id")

        ResourceLocation.STREAM_CODEC.encode(buf, id)
        @Suppress("UNCHECKED_CAST")
        (encoder as StreamCodec<RegistryFriendlyByteBuf, B>).encode(buf, value)
    }, { buf ->
        val id = ResourceLocation.STREAM_CODEC.decode(buf)
        val decoder = streamCodecs[id] ?: throw IllegalArgumentException("Unknown polymorphic type: $id")

        decoder.decode(buf)
    })

    fun <T : B> register(
        id: ResourceLocation, codec: MapCodec<T>, streamCodec: StreamCodec<in RegistryFriendlyByteBuf, T>
    ) {
        if (mapCodecs.putIfAbsent(id, codec) != null) {
            throw IllegalArgumentException("Type '$id' is already registered (Codec)")
        }
        if (streamCodecs.putIfAbsent(id, streamCodec) != null) {
            // Rollback
            mapCodecs.remove(id)
            throw IllegalArgumentException("Type '$id' is already registered (Stream Codec)")
        }
    }

    fun <T : B> register(
        namespace: String, path: String, codec: MapCodec<T>, streamCodec: StreamCodec<in RegistryFriendlyByteBuf, T>
    ) {
        register(ResourceLocation.fromNamespaceAndPath(namespace, path), codec, streamCodec)
    }
}