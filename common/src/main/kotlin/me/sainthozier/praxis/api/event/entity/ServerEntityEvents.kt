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

package me.sainthozier.praxis.api.event.entity

import me.sainthozier.praxis.api.event.Event
import me.sainthozier.praxis.api.event.EventFactory
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

object ServerEntityEvents {

    @JvmField
    val LOAD: Event<Load> = EventFactory.broadcast()

    @JvmField
    val UNLOAD: Event<Unload> = EventFactory.broadcast()

    fun interface Load {
        fun onEntityLoad(entity: Entity, world: ServerLevel, loadedFromDisk: Boolean)
    }

    fun interface Unload {
        fun onEntityUnload(entity: Entity, world: ServerLevel)
    }
}