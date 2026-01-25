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

package me.sainthozier.praxis.api.serialization

import com.mojang.datafixers.util.Pair
import com.mojang.datafixers.util.Unit
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.Lifecycle
import java.util.stream.Stream

/**
 * A Codec that serializes a [Set] of elements.
 * Mirrors Mojang's `ListCodec` but ensures uniqueness and returns a Set.
 */
data class SetCodec<E>(
    private val elementCodec: Codec<E>, private val minSize: Int = 0, private val maxSize: Int = Int.MAX_VALUE
) : Codec<Set<E>> {

    init {
        require(maxSize >= minSize) { "MaxSize ($maxSize) must be >= MinSize ($minSize)" }
    }

    private fun <R> createTooShortError(size: Int): DataResult<R> =
        DataResult.error { "Set is too short: $size, expected range [$minSize-$maxSize]" }

    private fun <R> createTooLongError(size: Int): DataResult<R> =
        DataResult.error { "Set is too long: $size, expected range [$minSize-$maxSize]" }

    override fun <T> encode(input: Set<E>, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        val size = input.size
        if (size < minSize) return createTooShortError(size)
        if (size > maxSize) return createTooLongError(size)

        val builder = ops.listBuilder()
        for (element in input) {
            builder.add(elementCodec.encodeStart(ops, element))
        }
        return builder.build(prefix)
    }

    override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<Set<E>, T>> {
        return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap { stream ->
            val decoder = DecoderState(ops)
            stream.accept { decoder.accept(it) }
            decoder.build()
        }
    }

    override fun toString(): String = "SetCodec[$elementCodec]"

    private inner class DecoderState<T>(private val ops: DynamicOps<T>) {
        private val elements = mutableSetOf<E>()
        private val failed = Stream.builder<T>()
        private var result: DataResult<Unit> = DataResult.success(Unit.INSTANCE, Lifecycle.stable())
        private var totalCount = 0

        fun accept(value: T) {
            totalCount++
            if (elements.size >= maxSize) {
                failed.add(value)
                return
            }

            val elementResult = elementCodec.decode(ops, value)
            elementResult.error().ifPresent { failed.add(value) }
            elementResult.resultOrPartial().ifPresent { pair ->
                elements.add(pair.first)
            }
            result = result.apply2stable({ result, _ -> result }, elementResult)
        }

        fun build(): DataResult<Pair<Set<E>, T>> {
            if (elements.size < minSize) {
                return createTooShortError(elements.size)
            }

            val errors = ops.createList(failed.build())
            val pair = Pair.of(elements.toSet(), errors)

            if (totalCount > maxSize) {
                result = createTooLongError(totalCount)
            }

            return result.map { pair }.setPartial(pair)
        }
    }
}
