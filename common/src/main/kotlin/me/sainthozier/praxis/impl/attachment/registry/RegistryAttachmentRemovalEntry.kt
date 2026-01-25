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
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.util.ExtraCodecs

@JvmInline
value class RegistryAttachmentRemovalEntry<R>(
    val key: Either<TagKey<R>, ResourceKey<R>>
) {
    companion object {
        fun <R> codec(registryKey: ResourceKey<Registry<R>>): Codec<RegistryAttachmentRemovalEntry<R>> {
            return ExtraCodecs.TAG_OR_ELEMENT_ID.xmap<RegistryAttachmentRemovalEntry<R>>(
                { tagOrId ->
                    val key: Either<TagKey<R>, ResourceKey<R>> = if (tagOrId.tag) {
                        Either.left(TagKey.create(registryKey, tagOrId.id))
                    } else {
                        Either.right(ResourceKey.create(registryKey, tagOrId.id))
                    }
                    RegistryAttachmentRemovalEntry(key)
                },
                { removal ->
                    removal.key.map(
                        { t -> ExtraCodecs.TagOrElementLocation(t.location(), true) },
                        { r -> ExtraCodecs.TagOrElementLocation(r.location(), false) }
                    )
                }
            )
        }
    }
}
