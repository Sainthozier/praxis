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

package me.sainthozier.praxis

import me.sainthozier.praxis.impl.CommonModEntrypoint
import me.sainthozier.praxis.impl.client.event.NeoForgeClientEventHandler
import me.sainthozier.praxis.impl.attachment.NeoForgeAttachmentManager
import me.sainthozier.praxis.impl.event.NeoForgeEventHandler
import me.sainthozier.praxis.impl.network.NeoForgePacketRegistrar
import me.sainthozier.praxis.impl.services.Services
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.neoforge.common.NeoForge

@Mod(PraxisModInfo.MOD_ID)
class NeoForgeModEntrypoint(eventBus: IEventBus, modContainer: ModContainer) {
    init {
        (Services.PACKET_REGISTRAR as? NeoForgePacketRegistrar)?.let { handler ->
            eventBus.addListener(handler::onRegisterPayloadHandlers)
        }

        if (FMLLoader.getDist().isClient) {
            NeoForge.EVENT_BUS.register(NeoForgeClientEventHandler)
        }

        NeoForge.EVENT_BUS.register(NeoForgeEventHandler)
        NeoForgeAttachmentManager.ATTACHMENT_TYPES.register(eventBus)
        NeoForgeAttachmentManager.DATA_COMPONENT_TYPES.register(eventBus)
        CommonModEntrypoint.init()
    }
}