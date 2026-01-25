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

package me.sainthozier.praxis.impl.network

import me.sainthozier.praxis.api.PraxisApi
import me.sainthozier.praxis.api.attachment.registry.RegistryAttachmentType
import me.sainthozier.praxis.impl.attachment.registry.RegistryAttachmentHolder
import me.sainthozier.praxis.impl.attachment.registry.RegistryAttachmentTypeRegistry
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.PlayerList

object RegistryAttachmentsSyncHelper {
    fun syncToPlayer(player: ServerPlayer) {
        val registryAccess = player.server.registryAccess()
        syncInternal(registryAccess, listOf(player))
    }

    fun syncToAll(playerList: PlayerList) {
        val registryAccess = playerList.server.registryAccess()
        syncInternal(registryAccess, playerList.players)
    }

    private fun syncInternal(registryAccess: RegistryAccess, players: List<ServerPlayer>) {
        if (players.isEmpty()) return

        registryAccess.registries().forEach { registryEntry ->
            val registryKey = registryEntry.key()
            val registry = registryEntry.value()

            if (registry is RegistryAttachmentHolder<*>) {
                @Suppress("UNCHECKED_CAST")
                val attachmentsToSync = collectSyncableAttachments(
                    registry as RegistryAttachmentHolder<Any>,
                    registryKey
                )

                if (attachmentsToSync.isNotEmpty()) {
                    val packet = ClientboundRegistryAttachmentsSyncPayload(registryKey, attachmentsToSync)
                    players.forEach { PraxisApi.packetDistributor.sendToPlayer(it, packet) }
                }
            }
        }
    }

    private fun <R> collectSyncableAttachments(
        holder: RegistryAttachmentHolder<R>,
        registryKey: ResourceKey<out Registry<*>>
    ): Map<RegistryAttachmentType<*, *>, Map<ResourceKey<*>, *>> {

        val result = HashMap<RegistryAttachmentType<*, *>, Map<ResourceKey<*>, *>>()

        RegistryAttachmentTypeRegistry.getTypesForRegistry(registryKey).forEach { type ->
            if (type.networkCodec != null) {
                @Suppress("UNCHECKED_CAST")
                val typedType = type as RegistryAttachmentType<R, Any>

                val map = holder.`praxis$getAttachments`(typedType)
                if (map.isNotEmpty()) {
                    @Suppress("UNCHECKED_CAST")
                    result[type] = map as Map<ResourceKey<*>, *>
                }
            }
        }
        return result
    }
}