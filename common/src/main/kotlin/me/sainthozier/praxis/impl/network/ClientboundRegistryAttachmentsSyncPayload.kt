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

import me.sainthozier.praxis.PraxisModInfo
import me.sainthozier.praxis.api.attachment.registry.RegistryAttachmentType
import me.sainthozier.praxis.api.extensions.location
import me.sainthozier.praxis.impl.attachment.registry.RegistryAttachmentTypeRegistry
import net.minecraft.core.Registry
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceKey

data class ClientboundRegistryAttachmentsSyncPayload(
    val registryKey: ResourceKey<out Registry<*>>,
    val attachments: Map<RegistryAttachmentType<*, *>, Map<ResourceKey<*>, *>>
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<ClientboundRegistryAttachmentsSyncPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<ClientboundRegistryAttachmentsSyncPayload>(
            "registry_attachments_sync".location(PraxisModInfo.MOD_ID)
        )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ClientboundRegistryAttachmentsSyncPayload> =
            StreamCodec.of(::write, ::read)

        @Suppress("UNCHECKED_CAST")
        private fun write(buf: RegistryFriendlyByteBuf, payload: ClientboundRegistryAttachmentsSyncPayload) {
            buf.writeResourceKey(payload.registryKey)

            buf.writeVarInt(payload.attachments.size)

            payload.attachments.forEach { (type, dataMap) ->
                buf.writeResourceLocation(type.id)

                buf.writeVarInt(dataMap.size)

                val codec = type.networkCodec as? StreamCodec<RegistryFriendlyByteBuf, Any>
                    ?: throw IllegalStateException("Attachment ${type.id} has no network codec but was queued for sync!")

                dataMap.forEach { (entryKey, entryValue) ->
                    buf.writeResourceKey(entryKey)
                    codec.encode(buf, entryValue!!)
                }
            }
        }

        private fun read(buf: RegistryFriendlyByteBuf): ClientboundRegistryAttachmentsSyncPayload {
            val registryLocation = buf.readResourceLocation()
            val registryKey = ResourceKey.createRegistryKey<Registry<*>>(registryLocation)

            val attachments = HashMap<RegistryAttachmentType<*, *>, Map<ResourceKey<*>, *>>()
            val attachmentCount = buf.readVarInt()

            for (i in 0 until attachmentCount) {
                val typeId = buf.readResourceLocation()
                val type = RegistryAttachmentTypeRegistry.getAttachmentType(registryKey, typeId)
                    ?: throw IllegalStateException("Received sync for unknown attachment '$typeId' on registry '$registryKey'")

                val mapSize = buf.readVarInt()
                val dataMap = HashMap<ResourceKey<*>, Any>()

                @Suppress("UNCHECKED_CAST")
                val codec = type.networkCodec as? StreamCodec<RegistryFriendlyByteBuf, Any>
                    ?: throw IllegalStateException("Attachment ${type.id} received from server but has no network codec on client!")

                for (j in 0 until mapSize) {
                    @Suppress("UNCHECKED_CAST")
                    val typedRegistryKey = registryKey as ResourceKey<Registry<Any>>

                    val entryKey = buf.readResourceKey(typedRegistryKey)

                    val entryValue = codec.decode(buf)
                    dataMap[entryKey] = entryValue
                }

                attachments[type] = dataMap
            }

            return ClientboundRegistryAttachmentsSyncPayload(registryKey, attachments)
        }
    }
}
