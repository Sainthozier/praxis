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

package me.sainthozier.praxis.api.event

/**
 * Represents a collection of listeners (callbacks) that can be invoked.
 * @param L The type of the listener. Must be a functional interface.
 */
interface Event<L : Any> {
    /**
     * The invoker instance. Calling a method on this object will fire the event
     * and call all registered listeners.
     */
    val invoker: L

    /**
     * Registers a listener for this event with DEFAULT phase.
     * @param listener The listener instance to register.
     */
    fun register(listener: L)

    /**
     * Registers a listener for this event at a specific phase.
     * @param phase The phase at which to register the listener.
     * @param listener The listener instance to register.
     */
    fun register(phase: EventPhase, listener: L)
}