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
import me.sainthozier.praxis.api.network.PacketDistributor
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer

@AutoService(PacketDistributor::class)
class FabricPacketDistributor : PacketDistributor {
    override fun sendToServer(packet: CustomPacketPayload) {
        if (packet.type().id !in FabricNetworkState.registeredServerboundPackets) {
            throw IllegalStateException("Packet payload with ID ${packet.type().id} is not registered for serverbound (C2S) sending.")
        }
        ClientPlayNetworking.send(packet)
    }

    override fun sendToPlayer(
        player: ServerPlayer,
        packet: CustomPacketPayload
    ) {
        if (packet.type().id !in FabricNetworkState.registeredClientboundPackets) {
            throw IllegalStateException("Packet payload with ID ${packet.type().id} is not registered for clientbound (S2C) sending.")
        }
        ServerPlayNetworking.send(player, packet)
    }
}