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

/**
 * Manages the creation of a polymorphic codec for a base type `B`.
 * This class encapsulates the logic for registering subtypes and creating
 * a master codec that can serialize/deserialize any of the registered subtypes.
 *
 * @param B The base type (e.g., an interface) for which the codec is being created.
 * @param typeGetter A function that retrieves the unique ResourceLocation from an instance of type B.
 */
class PolymorphicCodecManager<B : Any>(private val typeGetter: (B) -> ResourceLocation) {

    private val codecs = mutableMapOf<ResourceLocation, MapCodec<out B>>()

    /**
     * The master polymorphic codec.
     * It's created lazily upon first access.
     */
    val codec: Codec<B> by lazy {
        ResourceLocation.CODEC.dispatch(
            typeGetter
        ) { id ->
            codecs[id] ?: throw IllegalStateException("Unknown type for polymorphic codec: $id")
        }
    }

    /**
     * Registers a new subtype and its corresponding MapCodec.
     * @param T The concrete subtype that extends B.
     * @param id The unique ResourceLocation for this subtype.
     * @param codec The MapCodec for this subtype.
     * @return The same codec, for convenience.
     */
    fun <T : B> register(id: ResourceLocation, codec: MapCodec<T>): MapCodec<T> {
        if (codecs.containsKey(id)) {
            throw IllegalArgumentException("Type with id '$id' is already registered.")
        }
        codecs[id] = codec
        return codec
    }
}