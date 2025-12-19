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

package me.sainthozier.praxis.impl.network

import com.google.auto.service.AutoService
import me.sainthozier.praxis.api.network.PacketContext
import me.sainthozier.praxis.api.network.PacketDestination
import me.sainthozier.praxis.api.network.PacketHandler
import me.sainthozier.praxis.api.network.PacketRegistrar
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadContext
import net.neoforged.neoforge.network.registration.PayloadRegistrar
import org.jetbrains.annotations.ApiStatus

@AutoService(PacketRegistrar::class)
class NeoForgePacketRegistrar : PacketRegistrar {

    private val pendingRegistrations = mutableListOf<PendingPacketRegistration<*>>()

    private data class PendingPacketRegistration<T : CustomPacketPayload>(
        val type: CustomPacketPayload.Type<T>,
        val codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
        val destination: PacketDestination,
        val handler: PacketHandler<T>
    )

    override fun <T : CustomPacketPayload> registerPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
        destination: PacketDestination,
        handler: PacketHandler<T>
    ) {
        // Register for client-to-server communication (payloads sent from the client)
        if (destination.isServerbound || destination.isBidirectional) {
            NeoForgeNetworkState.registeredServerboundPackets.add(type.id)
        }
        // Register for server-to-client communication (payloads sent from the server)
        if (destination.isClientbound || destination.isBidirectional) {
            NeoForgeNetworkState.registeredClientboundPackets.add(type.id)
        }

        pendingRegistrations.add(PendingPacketRegistration(type, codec, destination, handler))
    }

    @ApiStatus.Internal
    fun onRegisterPayloadHandlers(event: RegisterPayloadHandlersEvent) {
        val registrationsByNamespace = pendingRegistrations.groupBy { it.type.id.namespace }

        registrationsByNamespace.forEach { (namespace, packets) ->
            val registrar = event.registrar(namespace).optional()
            packets.forEach { pending ->
                registerPayload(registrar, pending)
            }
        }
    }

    private fun <T : CustomPacketPayload> registerPayload(
        registrar: PayloadRegistrar,
        registration: PendingPacketRegistration<T>
    ) {
        when (registration.destination) {
            PacketDestination.SERVER_TO_CLIENT -> registrar.optional()
                .playToClient(registration.type, registration.codec) { payload, context ->
                    val packetContext = NeoForgePacketContext(context)
                    packetContext.enqueueWork {
                        registration.handler.onReceive(payload, packetContext)
                    }
                }

            PacketDestination.CLIENT_TO_SERVER -> registrar.optional()
                .playToServer(registration.type, registration.codec) { payload, context ->
                    val packetContext = NeoForgePacketContext(context)
                    packetContext.enqueueWork {
                        registration.handler.onReceive(payload, packetContext)
                    }
                }

            PacketDestination.BIDIRECTIONAL -> registrar.optional()
                .playBidirectional(registration.type, registration.codec) { payload, context ->
                    val packetContext = NeoForgePacketContext(context)
                    packetContext.enqueueWork {
                        registration.handler.onReceive(payload, packetContext)
                    }
                }
        }
    }

    private class NeoForgePacketContext(private val context: IPayloadContext) : PacketContext {
        override val player: Player?
            get() = context.player()

        override fun enqueueWork(runnable: () -> Unit) {
            context.enqueueWork(runnable)
        }
    }
}