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
import me.sainthozier.praxis.api.event.server.CommandRegistrationCallback
import me.sainthozier.praxis.api.event.server.ServerLifecycleEvents
import me.sainthozier.praxis.api.event.server.ServerTickEvents
import me.sainthozier.praxis.api.event.world.ServerWorldEvents
import me.sainthozier.praxis.api.event.world.ServerWorldTickEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.event.player.*
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback as FabricCommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents as FabricServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents as FabricServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents as FabricServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents as FabricServerTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents as FabricServerWorldEvents

object FabricEventHandler {

    fun bootstrap() {
        FabricServerWorldEvents.LOAD.register { server, world ->
            ServerWorldEvents.LOAD.invoker.onWorldLoad(server, world)
        }
        FabricServerWorldEvents.UNLOAD.register { server, world ->
            ServerWorldEvents.UNLOAD.invoker.onWorldUnload(server, world)
        }
        FabricServerTickEvents.START_WORLD_TICK.register { level ->
            ServerWorldTickEvents.START.invoker.onStartTick(level)
        }
        FabricServerTickEvents.END_WORLD_TICK.register { level ->
            ServerWorldTickEvents.END.invoker.onEndTick(level)
        }
        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            PlayerInteractEvents.USE_BLOCK.invoker.onUseBlock(
                player,
                world,
                hand,
                hitResult
            )
        }
        AttackBlockCallback.EVENT.register { player, world, hand, pos, direction ->
            PlayerInteractEvents.ATTACK_BLOCK.invoker.onAttackBlock(
                player,
                world,
                hand,
                null
            )
        }
        UseItemCallback.EVENT.register { player, world, hand ->
            PlayerInteractEvents.USE_ITEM.invoker.onUseItem(
                player,
                world,
                hand
            )
        }
        UseEntityCallback.EVENT.register { player, world, hand, entity, hitResult ->
            PlayerInteractEvents.USE_ENTITY.invoker.onUseEntity(
                player,
                world,
                hand,
                entity,
                hitResult
            )
        }
        AttackEntityCallback.EVENT.register { player, world, hand, entity, hitResult ->
            PlayerInteractEvents.ATTACK_ENTITY.invoker.onAttackEntity(
                player,
                world,
                hand,
                entity,
                hitResult
            )
        }
        FabricServerPlayerEvents.COPY_FROM.register { oldPlayer, newPlayer, alive ->
            ServerPlayerEvents.COPY.invoker.onCopy(oldPlayer, newPlayer, alive)
        }
        FabricServerPlayerEvents.AFTER_RESPAWN.register { oldPlayer, newPlayer, alive ->
            ServerPlayerEvents.RESPAWN.invoker.onRespawn(newPlayer, alive, oldPlayer.removalReason)
        }
        FabricServerPlayerEvents.JOIN.register { serverPlayer ->
            ServerPlayerEvents.JOIN.invoker.onJoin(serverPlayer)
        }
        FabricServerPlayerEvents.LEAVE.register { serverPlayer ->
            ServerPlayerEvents.LEAVE.invoker.onLeave(serverPlayer)
        }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, origin, destination ->
            ServerPlayerEvents.CHANGE_DIMENSION.invoker.onChangeDimension(
                player,
                origin.dimension(),
                destination.dimension()
            )
        }
        FabricServerEntityEvents.ENTITY_LOAD.register { entity, world ->
            ServerEntityEvents.LOAD.invoker.onEntityLoad(entity, world, false)
        }
        FabricServerEntityEvents.ENTITY_UNLOAD.register { entity, world ->
            ServerEntityEvents.UNLOAD.invoker.onEntityUnload(entity, world)
        }
        FabricServerTickEvents.START_SERVER_TICK.register { server ->
            ServerTickEvents.START.invoker.onStartTick(server)
        }
        FabricServerTickEvents.END_SERVER_TICK.register { server ->
            ServerTickEvents.END.invoker.onEndTick(server)
        }
        FabricServerLifecycleEvents.SERVER_STARTING.register { server ->
            ServerLifecycleEvents.STARTING.invoker.onStarting(server)
        }
        FabricServerLifecycleEvents.SERVER_STARTED.register { server ->
            ServerLifecycleEvents.STARTED.invoker.onStarted(server)
        }
        FabricServerLifecycleEvents.SERVER_STOPPING.register { server ->
            ServerLifecycleEvents.STOPPING.invoker.onStopping(server)
        }
        FabricServerLifecycleEvents.SERVER_STOPPED.register { server ->
            ServerLifecycleEvents.STOPPED.invoker.onStopped(server)
        }
        FabricCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
            CommandRegistrationCallback.EVENT.invoker.onCommandRegistration(
                dispatcher,
                registryAccess,
                environment
            )
        }
    }
}