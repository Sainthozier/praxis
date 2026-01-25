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

package me.sainthozier.praxis.impl

import me.sainthozier.praxis.api.PraxisApi
import me.sainthozier.praxis.api.network.PacketDestination
import me.sainthozier.praxis.api.registries.PraxisDataLoadConditionCodecRegistry
import me.sainthozier.praxis.impl.network.ClientboundPacketHandler
import me.sainthozier.praxis.impl.network.ClientboundRegistryAttachmentsSyncPayload

object CommonModEntrypoint {
    fun init() {
        PraxisDataLoadConditionCodecRegistry.bootstrap()
        PraxisApi.packetRegistrar.registerPacket(
            ClientboundRegistryAttachmentsSyncPayload.TYPE,
            ClientboundRegistryAttachmentsSyncPayload.STREAM_CODEC, PacketDestination.SERVER_TO_CLIENT,
            ClientboundPacketHandler::handleRegistryAttachmentsSync
        )
    }
}