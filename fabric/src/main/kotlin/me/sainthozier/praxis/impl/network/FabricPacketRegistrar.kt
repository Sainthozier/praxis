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
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.entity.player.Player

@AutoService(PacketRegistrar::class)
class FabricPacketRegistrar : PacketRegistrar {
    override fun <T : CustomPacketPayload> registerPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
        destination: PacketDestination,
        handler: PacketHandler<T>
    ) {
        PayloadTypeRegistry.playC2S().register(type, codec)
        PayloadTypeRegistry.playS2C().register(type, codec)

        // Register for client-to-server communication (payloads sent from the client)
        if (destination.isServerbound || destination.isBidirectional) {
            FabricNetworkState.registeredServerboundPackets.add(type.id)
            ServerPlayNetworking.registerGlobalReceiver(type) { payload, context ->
                val packetContext = FabricPacketContext(context.player())
                context.player().server?.execute { handler.onReceive(payload, packetContext) }
            }
        }

        // Register for server-to-client communication (payloads sent from the server)
        if (destination.isClientbound || destination.isBidirectional) {
            FabricNetworkState.registeredClientboundPackets.add(type.id)
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
                ClientPlayNetworking.registerGlobalReceiver(type) { payload, context ->
                    val packetContext = FabricPacketContext(context.player())
                    context.client().execute { handler.onReceive(payload, packetContext) }
                }
            }
        }
    }

    private class FabricPacketContext(override val player: Player?) : PacketContext {
        override fun enqueueWork(runnable: () -> Unit) {
            runnable()
        }
    }
}