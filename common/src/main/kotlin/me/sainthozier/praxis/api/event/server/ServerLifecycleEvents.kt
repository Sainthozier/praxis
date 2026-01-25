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

package me.sainthozier.praxis.api.event.server

import me.sainthozier.praxis.api.event.Event
import me.sainthozier.praxis.api.event.EventFactory
import net.minecraft.server.MinecraftServer

object ServerLifecycleEvents {

    @JvmField
    val STARTING: Event<Starting> = EventFactory.broadcast()

    @JvmField
    val STARTED: Event<Started> = EventFactory.broadcast()

    @JvmField
    val STOPPING: Event<Stopping> = EventFactory.broadcast()

    @JvmField
    val STOPPED: Event<Stopped> = EventFactory.broadcast()

    fun interface Starting {
        fun onStarting(server: MinecraftServer)
    }

    fun interface Started {
        fun onStarted(server: MinecraftServer)
    }

    fun interface Stopping {
        fun onStopping(server: MinecraftServer)
    }

    fun interface Stopped {
        fun onStopped(server: MinecraftServer)
    }
}