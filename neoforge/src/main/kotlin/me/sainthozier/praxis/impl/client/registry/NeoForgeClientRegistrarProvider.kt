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

package me.sainthozier.praxis.impl.client.registry

import com.google.auto.service.AutoService
import me.sainthozier.praxis.api.client.registry.ClientRegistrar
import me.sainthozier.praxis.api.client.registry.ClientRegistrarProvider
import net.neoforged.fml.ModList
import java.util.*

@AutoService(ClientRegistrarProvider::class)
internal class NeoForgeClientRegistrarProvider : ClientRegistrarProvider {
    override fun create(modId: String): ClientRegistrar {
        val eventBus = ModList.get().getModContainerById(modId)
            .flatMap { container -> Optional.ofNullable(container.eventBus) }
            .orElseThrow { IllegalStateException("Could not find mod container for '$modId' or it has no event bus.") }

        return NeoForgeClientRegistrar(eventBus)
    }
}