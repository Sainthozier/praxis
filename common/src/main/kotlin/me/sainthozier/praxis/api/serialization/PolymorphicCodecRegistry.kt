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
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.ConcurrentHashMap

open class PolymorphicCodecRegistry<B : Any>(
    private val typeGetter: (B) -> ResourceLocation
) {
    private val codecs = ConcurrentHashMap<ResourceLocation, MapCodec<out B>>()

    val codec: Codec<B> = ResourceLocation.CODEC.dispatch(
        typeGetter, this::getCodecFor
    )

    fun <T : B> register(id: ResourceLocation, codec: MapCodec<T>) {
        if (codecs.putIfAbsent(id, codec) != null) {
            throw IllegalArgumentException("Polymorphic type '$id' is already registered")
        }
    }

    fun <T : B> register(namespace: String, path: String, codec: MapCodec<T>) {
        register(ResourceLocation.fromNamespaceAndPath(namespace, path), codec)
    }

    private fun getCodecFor(id: ResourceLocation): MapCodec<out B> {
        return codecs[id] ?: throw IllegalArgumentException("Unknown polymorphic type: $id")
    }
}