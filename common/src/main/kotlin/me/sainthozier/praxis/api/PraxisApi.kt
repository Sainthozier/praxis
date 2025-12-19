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

package me.sainthozier.praxis.api

import me.sainthozier.praxis.ModInfo
import me.sainthozier.praxis.api.data.AttachmentManager
import me.sainthozier.praxis.api.network.PacketDistributor
import me.sainthozier.praxis.api.network.PacketRegistrar
import me.sainthozier.praxis.api.registry.RegistrarFactoryProvider
import me.sainthozier.praxis.api.util.CreativeModeTabBuilderProvider
import me.sainthozier.praxis.api.util.PlatformHelper
import me.sainthozier.praxis.impl.services.Services
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// TODO: single api entrypoint is questionable good, but you can do better than this
object PraxisApi {
    @JvmStatic
    internal val LOG: Logger = LoggerFactory.getLogger(ModInfo.MOD_NAME)

    @JvmStatic
    val platformHelper: PlatformHelper = Services.PLATFORM

    @JvmStatic
    val packetRegistrar: PacketRegistrar = Services.PACKET_REGISTRAR

    @JvmStatic
    val packetDistributor: PacketDistributor = Services.PACKET_DISTRIBUTOR

    @JvmStatic
    val registration: RegistrarFactoryProvider = Services.REGISTRATION

    @JvmStatic
    val attachmentManager: AttachmentManager = Services.ATTACHMENT_MANAGER

    @JvmStatic
    val creativeModeTabBuilderProvider: CreativeModeTabBuilderProvider = Services.CREATIVE_MODE_TAB_BUILDER_PROVIDER
}