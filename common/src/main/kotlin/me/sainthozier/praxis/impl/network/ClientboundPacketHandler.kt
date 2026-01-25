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

import me.sainthozier.praxis.api.attachment.registry.RegistryAttachmentType
import me.sainthozier.praxis.api.network.PacketContext
import me.sainthozier.praxis.impl.attachment.registry.RegistryAttachmentHolder
import net.minecraft.client.Minecraft
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import org.slf4j.LoggerFactory
import java.util.*

object ClientboundPacketHandler {

    private val LOG = LoggerFactory.getLogger("PraxisClientboundPacketHandler")

    fun handleRegistryAttachmentsSync(payload: ClientboundRegistryAttachmentsSyncPayload, context: PacketContext) {
        context.enqueueWork {
            try {
                val registryAccess = Minecraft.getInstance().connection?.registryAccess()
                    ?: Minecraft.getInstance().level?.registryAccess()

                if (registryAccess == null) {
                    LOG.error("Failed to sync registry attachments: Client registry access is null.")
                    return@enqueueWork
                }

                @Suppress("UNCHECKED_CAST")
                val registryKey = payload.registryKey as ResourceKey<out Registry<Any>>
                val registry = registryAccess.registryOrThrow(registryKey)

                if (registry is RegistryAttachmentHolder<*>) {
                    @Suppress("UNCHECKED_CAST")
                    val holder = registry as RegistryAttachmentHolder<Any>

                    holder.`praxis$clearAttachments`()

                    payload.attachments.forEach { (type, rawMap) ->
                        @Suppress("UNCHECKED_CAST")
                        val typedType = type as RegistryAttachmentType<Any, Any>

                        @Suppress("UNCHECKED_CAST")
                        val typedMap = rawMap as Map<ResourceKey<Any>, Any>

                        holder.`praxis$setAttachmentsForType`(typedType, Collections.unmodifiableMap(typedMap))

                        LOG.debug(
                            "Synced attachment '{}' for registry '{}' ({} entries)",
                            type.id,
                            registryKey.location(),
                            typedMap.size
                        )
                    }
                } else {
                    LOG.error("Registry '${registryKey.location()}' does not implement RegistryAttachmentHolder. Sync failed.")
                }

            } catch (t: Throwable) {
                LOG.error(
                    "Failed to handle registry attachment sync for '${payload.registryKey.location()}'",
                    t
                )
            }
        }
    }
}