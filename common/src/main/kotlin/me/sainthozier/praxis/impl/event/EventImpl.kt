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

package me.sainthozier.praxis.impl.event

import me.sainthozier.praxis.api.event.Event
import me.sainthozier.praxis.api.event.EventPhase
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

internal class EventImpl<L : Any>(
    private val listenerClass: KClass<L>,
    private val invokerLoop: (listeners: List<L>, args: Array<out Any>?, invoker: (L) -> Any?) -> Any?
) : Event<L>, InvocationHandler {

    private val phasedListeners = ConcurrentSkipListMap<EventPhase, CopyOnWriteArrayList<L>>()

    @Volatile
    private var allListeners: List<L> = emptyList()

    override val invoker: L by lazy {
        require(listenerClass.java.isInterface && listenerClass.java.declaredMethods.size == 1) {
            "Event listener class '${listenerClass.simpleName}' must be a functional interface with exactly one method."
        }
        @Suppress("UNCHECKED_CAST")
        Proxy.newProxyInstance(
            listenerClass.java.classLoader,
            arrayOf(listenerClass.java),
            this
        ) as L
    }

    override fun register(listener: L) {
        register(EventPhase.DEFAULT, listener)
    }

    override fun register(phase: EventPhase, listener: L) {
        phasedListeners.computeIfAbsent(phase) { CopyOnWriteArrayList() }.add(listener)
        updateListenerCache()
    }

    private fun updateListenerCache() {
        allListeners = phasedListeners.values.flatten()
    }

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        val singleInvoker: (L) -> Any? = { listener ->
            method.invoke(listener, *(args ?: emptyArray()))
        }
        return invokerLoop(allListeners, args, singleInvoker)
    }
}