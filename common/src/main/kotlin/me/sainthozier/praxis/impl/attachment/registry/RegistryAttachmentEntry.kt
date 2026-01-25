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

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.function.Function

data class RegistryAttachmentEntry<T>(
    val value: T,
    val replace: Boolean = false
) {
    companion object {
        fun <T> codec(valueCodec: Codec<T>): Codec<RegistryAttachmentEntry<T>> {
            val objectCodec = RecordCodecBuilder.create<RegistryAttachmentEntry<T>> { builder ->
                builder.group(
                    valueCodec.fieldOf("value").forGetter { it.value },
                    Codec.BOOL.optionalFieldOf("replace", false).forGetter { it.replace }
                ).apply(builder, ::RegistryAttachmentEntry)
            }

            return Codec.either(objectCodec, valueCodec).xmap(
                { either -> either.map(Function.identity()) { rawValue -> RegistryAttachmentEntry(rawValue) } },
                { entry -> if (entry.replace) Either.left(entry) else Either.right(entry.value) }
            )
        }
    }
}
