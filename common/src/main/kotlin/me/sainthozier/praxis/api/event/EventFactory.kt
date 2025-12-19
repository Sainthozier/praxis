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

package me.sainthozier.praxis.api.event

import me.sainthozier.praxis.impl.event.EventImpl
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import kotlin.reflect.KClass

object EventFactory {

    /**
     * The core event creation function for advanced use.
     * Allows for the creation of events with custom invocation logic.
     *
     * @param L The listener interface type.
     * @param listenerClass The KClass of the listener interface.
     * @param invokerLoop The lambda defining how to loop through listeners and process results.
     * @return A new Event instance.
     */
    fun <L : Any> create(
        listenerClass: KClass<L>,
        invokerLoop: (listeners: List<L>, args: Array<out Any>?, invoker: (L) -> Any?) -> Any?
    ): Event<L> {
        return EventImpl(listenerClass, invokerLoop)
    }

    inline fun <reified L : Any> broadcast(): Event<L> {
        return create(L::class) { listeners, _, invoker ->
            for (listener in listeners) {
                invoker(listener)
            }
        }
    }

    inline fun <reified L : Any> untilSuccess(): Event<L> {
        return create(L::class) { listeners, _, invoker ->
            for (listener in listeners) {
                val result = invoker(listener) as InteractionResult
                if (result.isDefinitive) {
                    return@create result
                }
            }
            InteractionResult.PASS
        }
    }

    inline fun <reified L : Any, T> untilSuccessWithHolder(
        crossinline defaultResultProvider: (args: Array<out Any>?) -> InteractionResultHolder<T>
    ): Event<L> {
        return create(L::class) { listeners, args, invoker ->
            for (listener in listeners) {
                val result = invoker(listener) as InteractionResultHolder<*>
                if (result.result.isDefinitive) {
                    return@create result
                }
            }
            defaultResultProvider(args)
        }
    }

    @JvmStatic
    fun <L : Any> createBroadcast(listenerClass: Class<L>): Event<L> {
        return create(listenerClass.kotlin) { listeners, _, invoker ->
            for (listener in listeners) {
                invoker(listener)
            }
        }
    }

    @JvmStatic
    fun <L : Any> createUntilSuccess(listenerClass: Class<L>): Event<L> {
        return create(listenerClass.kotlin) { listeners, _, invoker ->
            for (listener in listeners) {
                val result = invoker(listener) as InteractionResult
                if (result.isDefinitive) {
                    return@create result
                }
            }
            InteractionResult.PASS
        }
    }

    @JvmStatic
    fun <L : Any, T> createUntilSuccessWithHolder(
        listenerClass: Class<L>,
        defaultResultProvider: DefaultResultProvider<InteractionResultHolder<T>>
    ): Event<L> {
        return create(listenerClass.kotlin) { listeners, args, invoker ->
            for (listener in listeners) {
                val result = invoker(listener) as InteractionResultHolder<*>
                if (result.result.isDefinitive) {
                    return@create result
                }
            }
            defaultResultProvider.get(args)
        }
    }
}