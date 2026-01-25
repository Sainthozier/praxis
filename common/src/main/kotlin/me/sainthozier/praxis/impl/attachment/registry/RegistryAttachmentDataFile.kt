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
import me.sainthozier.praxis.api.data.condition.DataLoadCondition
import me.sainthozier.praxis.api.attachment.registry.RegistryAttachmentType
import me.sainthozier.praxis.api.registries.PraxisDataLoadConditionCodecRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.util.ExtraCodecs

data class RegistryAttachmentDataFile<R, T>(
    val replace: Boolean,
    val values: Map<Either<TagKey<R>, ResourceKey<R>>, RegistryAttachmentEntry<T>>,
    val removals: List<RegistryAttachmentRemovalEntry<R>>,
    val conditions: List<DataLoadCondition>
) {
    companion object {
        fun <R, T> codec(
            registryKey: ResourceKey<Registry<R>>, attachmentType: RegistryAttachmentType<R, T>
        ): Codec<RegistryAttachmentDataFile<R, T>> {
            val keyCodec = ExtraCodecs.TAG_OR_ELEMENT_ID.xmap<Either<TagKey<R>, ResourceKey<R>>>({ tagOrId ->
                if (tagOrId.tag) Either.left(TagKey.create(registryKey, tagOrId.id))
                else Either.right(ResourceKey.create(registryKey, tagOrId.id))
            }, { either ->
                either.map(
                    { tag -> ExtraCodecs.TagOrElementLocation(tag.location(), true) },
                    { key -> ExtraCodecs.TagOrElementLocation(key.location(), false) })
            })

            return RecordCodecBuilder.create { builder ->
                builder.group(
                    Codec.BOOL.optionalFieldOf("replace", false).forGetter { it.replace },
                    ExtraCodecs.strictUnboundedMap(keyCodec, RegistryAttachmentEntry.codec(attachmentType.codec))
                        .optionalFieldOf("values", mapOf()).forGetter { it.values },
                    RegistryAttachmentRemovalEntry.codec(registryKey).listOf().optionalFieldOf("remove", emptyList())
                        .forGetter { it.removals },
                    PraxisDataLoadConditionCodecRegistry.codec.listOf().optionalFieldOf("conditions", emptyList())
                        .forGetter { it.conditions }
                ).apply(builder, ::RegistryAttachmentDataFile)
            }
        }
    }
}
