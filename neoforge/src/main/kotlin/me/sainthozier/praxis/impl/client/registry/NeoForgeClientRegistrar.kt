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

package me.sainthozier.praxis.impl.client.registry

import me.sainthozier.praxis.api.client.registry.ClientRegistrar
import me.sainthozier.praxis.api.registry.RegisteredObject
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent

internal class NeoForgeClientRegistrar(private val eventBus: IEventBus) : ClientRegistrar {

    private data class ScreenRegistration<M : AbstractContainerMenu>(
        val menuType: MenuType<M>,
        val factory: MenuScreens.ScreenConstructor<M, out AbstractContainerScreen<M>>
    )

    private val keyMappingQueue = mutableListOf<KeyMapping>()
    private val screenQueue = mutableListOf<ScreenRegistration<*>>()
    private var isBootstrapped = false

    override fun registerKeyMapping(keyMapping: KeyMapping): KeyMapping {
        if (isBootstrapped) {
            throw IllegalStateException("Cannot register key mappings after bootstrap has been called.")
        }
        keyMappingQueue.add(keyMapping)
        return keyMapping
    }

    override fun <T : MenuType<*>, R : T, M : AbstractContainerMenu, S : AbstractContainerScreen<M>> registerScreen(
        menuType: RegisteredObject<T, R>,
        screenFactory: MenuScreens.ScreenConstructor<M, S>
    ) {
        if (isBootstrapped) {
            throw IllegalStateException("Cannot register screens after bootstrap has been called.")
        }
        @Suppress("UNCHECKED_CAST")
        screenQueue.add(ScreenRegistration(menuType.get() as MenuType<M>, screenFactory))
    }

    override fun bootstrap() {
        if (isBootstrapped) {
            return
        }

        eventBus.addListener(::onRegisterKeyMappings)
        eventBus.addListener(::onRegisterMenuScreens)

        isBootstrapped = true
    }

    private fun onRegisterKeyMappings(event: RegisterKeyMappingsEvent) {
        keyMappingQueue.forEach { event.register(it) }
        keyMappingQueue.clear()
    }

    private fun onRegisterMenuScreens(event: RegisterMenuScreensEvent) {
        screenQueue.forEach { registration ->
            @Suppress("UNCHECKED_CAST")
            event.register(
                registration.menuType as MenuType<AbstractContainerMenu>,
                registration.factory as MenuScreens.ScreenConstructor<AbstractContainerMenu, AbstractContainerScreen<AbstractContainerMenu>>
            )
        }
        screenQueue.clear()
    }
}