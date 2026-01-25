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

package me.sainthozier.praxis.api.data.condition

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.sainthozier.praxis.PraxisModInfo
import me.sainthozier.praxis.api.registries.PraxisDataLoadConditionCodecRegistry
import me.sainthozier.praxis.api.extensions.location
import net.minecraft.resources.ResourceLocation

data class NotDataLoadCondition(val condition: DataLoadCondition) : DataLoadCondition {
    override val type: ResourceLocation = "not".location(PraxisModInfo.MOD_ID)

    override fun test(): Boolean = !condition.test()

    companion object {
        @JvmField
        val CODEC: MapCodec<NotDataLoadCondition> = RecordCodecBuilder.mapCodec { builder ->
            builder.group(
                PraxisDataLoadConditionCodecRegistry.codec.fieldOf("condition").forGetter { it.condition }
            ).apply(builder, ::NotDataLoadCondition)
        }
    }
}
