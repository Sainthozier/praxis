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

import me.sainthozier.praxis.api.event.entity.ServerEntityEvents
import me.sainthozier.praxis.api.event.entity.player.PlayerInteractEvents
import me.sainthozier.praxis.api.event.entity.player.ServerPlayerEvents
import me.sainthozier.praxis.api.event.isDefinitive
import me.sainthozier.praxis.api.event.isFail
import me.sainthozier.praxis.api.event.isSuccess
import me.sainthozier.praxis.api.event.server.CommandRegistrationCallback
import me.sainthozier.praxis.api.event.server.ServerLifecycleEvents
import me.sainthozier.praxis.api.event.server.ServerTickEvents
import me.sainthozier.praxis.api.event.world.ServerWorldEvents
import me.sainthozier.praxis.api.event.world.ServerWorldTickEvents
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.util.TriState
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.level.LevelEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent

object NeoForgeEventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onWorldLoad(event: LevelEvent.Load) {
        (event.level as? ServerLevel)?.let {
            ServerWorldEvents.LOAD.invoker.onWorldLoad(it.server, it)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onWorldUnload(event: LevelEvent.Unload) {
        (event.level as? ServerLevel)?.let {
            ServerWorldEvents.UNLOAD.invoker.onWorldUnload(it.server, it)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onWorldTickPre(event: LevelTickEvent.Pre) {
        (event.level as? ServerLevel)?.let { ServerWorldTickEvents.START.invoker.onStartTick(it) }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onWorldTickPost(event: LevelTickEvent.Post) {
        (event.level as? ServerLevel)?.let { ServerWorldTickEvents.END.invoker.onEndTick(it) }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onUseBlock(event: PlayerInteractEvent.RightClickBlock) {
        val result =
            PlayerInteractEvents.USE_BLOCK.invoker.onUseBlock(event.entity, event.level, event.hand, event.hitVec)
        if (result.isDefinitive) {
            event.isCanceled = true
            event.cancellationResult = result
            event.useBlock = TriState.FALSE
            event.useItem = TriState.FALSE
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onAttackBlock(event: PlayerInteractEvent.LeftClickBlock) {
        if (event.action != PlayerInteractEvent.LeftClickBlock.Action.START) return

        val result =
            PlayerInteractEvents.ATTACK_BLOCK.invoker.onAttackBlock(event.entity, event.level, event.hand, null)
        if (result.isDefinitive) {
            event.isCanceled = true
            event.useBlock = if (result.isSuccess) TriState.TRUE else TriState.FALSE
            event.useItem = if (result.isSuccess) TriState.TRUE else TriState.FALSE
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onUseItem(event: PlayerInteractEvent.RightClickItem) {
        val result = PlayerInteractEvents.USE_ITEM.invoker.onUseItem(event.entity, event.level, event.hand)
        if (result.result.isDefinitive) {
            event.isCanceled = true
            event.cancellationResult = result.result
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onUseEntity(event: PlayerInteractEvent.EntityInteract) {
        val result = PlayerInteractEvents.USE_ENTITY.invoker.onUseEntity(
            event.entity,
            event.level,
            event.hand,
            event.target,
            null
        )
        if (result.isDefinitive) {
            event.isCanceled = true
            event.cancellationResult = result
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onAttackEntity(event: AttackEntityEvent) {
        val result = PlayerInteractEvents.ATTACK_ENTITY.invoker.onAttackEntity(
            event.entity,
            event.entity.level(),
            event.entity.usedItemHand,
            event.target,
            null
        )
        if (result.isFail) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPlayerClone(event: PlayerEvent.Clone) {
        if (event.original is ServerPlayer && event.entity is ServerPlayer) {
            ServerPlayerEvents.COPY.invoker.onCopy(
                event.original as ServerPlayer,
                event.entity as ServerPlayer,
                !event.isWasDeath
            )
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) {
        ServerPlayerEvents.RESPAWN.invoker.onRespawn(
            event.entity as ServerPlayer,
            event.isEndConquered,
            event.entity.removalReason
        )
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        ServerPlayerEvents.JOIN.invoker.onJoin(event.entity as ServerPlayer)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPlayerLeave(event: PlayerEvent.PlayerLoggedOutEvent) {
        ServerPlayerEvents.LEAVE.invoker.onLeave(event.entity as ServerPlayer)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPlayerChangeDimension(event: PlayerEvent.PlayerChangedDimensionEvent) {
        (event.entity as? ServerPlayer)?.let {
            ServerPlayerEvents.CHANGE_DIMENSION.invoker.onChangeDimension(it, event.from, event.to)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onEntityJoinWorld(event: EntityJoinLevelEvent) {
        (event.level as? ServerLevel)?.let {
            ServerEntityEvents.LOAD.invoker.onEntityLoad(event.entity, it, event.loadedFromDisk())
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onEntityLeaveWorld(event: EntityLeaveLevelEvent) {
        (event.level as? ServerLevel)?.let {
            ServerEntityEvents.UNLOAD.invoker.onEntityUnload(event.entity, it)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onServerStartTick(event: ServerTickEvent.Pre) {
        ServerTickEvents.START.invoker.onStartTick(event.server)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onServerEndTick(event: ServerTickEvent.Post) {
        ServerTickEvents.END.invoker.onEndTick(event.server)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onServerStarting(event: ServerStartingEvent) {
        ServerLifecycleEvents.STARTING.invoker.onStarting(event.server)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onServerStarted(event: ServerStartedEvent) {
        ServerLifecycleEvents.STARTED.invoker.onStarted(event.server)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onServerStopping(event: ServerStoppingEvent) {
        ServerLifecycleEvents.STOPPING.invoker.onStopping(event.server)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onServerStopped(event: ServerStoppedEvent) {
        ServerLifecycleEvents.STOPPED.invoker.onStopped(event.server)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        CommandRegistrationCallback.EVENT.invoker.onCommandRegistration(
            event.dispatcher,
            event.buildContext,
            event.commandSelection
        )
    }
}