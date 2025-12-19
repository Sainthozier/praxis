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

package me.sainthozier.praxis.api.event.entity.player

import me.sainthozier.praxis.api.event.Event
import me.sainthozier.praxis.api.event.EventFactory
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

object ServerPlayerEvents {

    @JvmField
    val COPY: Event<Copy> = EventFactory.broadcast()

    @JvmField
    val RESPAWN: Event<Respawn> = EventFactory.broadcast()

    @JvmField
    val JOIN: Event<Join> = EventFactory.broadcast()

    @JvmField
    val LEAVE: Event<Leave> = EventFactory.broadcast()

    @JvmField
    val CHANGE_DIMENSION: Event<ChangeDimension> = EventFactory.broadcast()

    fun interface Copy {
        fun onCopy(oldPlayer: ServerPlayer, newPlayer: ServerPlayer, wonGame: Boolean)
    }

    fun interface Respawn {
        fun onRespawn(player: ServerPlayer, wonGame: Boolean, removalReason: Entity.RemovalReason?)
    }

    fun interface Join {
        fun onJoin(player: ServerPlayer)
    }

    fun interface Leave {
        fun onLeave(player: ServerPlayer)
    }

    fun interface ChangeDimension {
        fun onChangeDimension(player: ServerPlayer, from: ResourceKey<Level>, to: ResourceKey<Level>)
    }
}