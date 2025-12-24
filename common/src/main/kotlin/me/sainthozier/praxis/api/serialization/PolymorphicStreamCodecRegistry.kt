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

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.ConcurrentHashMap

open class PolymorphicStreamCodecRegistry<T : Any>(
    private val typeGetter: (T) -> ResourceLocation
) {
    private val codecs = ConcurrentHashMap<ResourceLocation, StreamCodec<in RegistryFriendlyByteBuf, out T>>()

    val codec: StreamCodec<RegistryFriendlyByteBuf, T> = StreamCodec.of({ buf, value ->
        val id = typeGetter(value)
        val codec = codecs[id] ?: throw IllegalArgumentException("Unknown polymorphic type: $id")

        ResourceLocation.STREAM_CODEC.encode(buf, id)
        @Suppress("UNCHECKED_CAST")
        (codec as StreamCodec<RegistryFriendlyByteBuf, T>).encode(buf, value)
    }, { buf ->
        val id = ResourceLocation.STREAM_CODEC.decode(buf)
        val codec = codecs[id] ?: throw IllegalArgumentException("Unknown polymorphic type: $id")

        codec.decode(buf)
    })

    fun <S : T> register(id: ResourceLocation, codec: StreamCodec<in RegistryFriendlyByteBuf, S>) {
        if (codecs.putIfAbsent(id, codec) != null) {
            throw IllegalArgumentException("Polymorphic type '$id' is already registered")
        }
    }

    fun <S : T> register(namespace: String, path: String, codec: StreamCodec<in RegistryFriendlyByteBuf, S>) {
        register(ResourceLocation.fromNamespaceAndPath(namespace, path), codec)
    }
}