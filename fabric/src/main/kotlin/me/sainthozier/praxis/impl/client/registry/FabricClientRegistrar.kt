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
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType

internal class FabricClientRegistrar(private val modId: String) : ClientRegistrar {

    private val keyMappingQueue = mutableListOf<KeyMapping>()
    private val screenQueue = mutableListOf<() -> Unit>()

    override fun registerKeyMapping(keyMapping: KeyMapping): KeyMapping {
        keyMappingQueue.add(keyMapping)
        return keyMapping
    }

    override fun <T : MenuType<*>, R : T, M : AbstractContainerMenu, S : AbstractContainerScreen<M>> registerScreen(
        menuType: RegisteredObject<T, R>,
        screenFactory: MenuScreens.ScreenConstructor<M, S>
    ) {
        screenQueue.add {
            val actualMenuType = menuType.get()
            @Suppress("UNCHECKED_CAST")
            MenuScreens.register(actualMenuType as MenuType<M>, screenFactory)
        }
    }

    override fun bootstrap() {
        keyMappingQueue.forEach {
            KeyBindingHelper.registerKeyBinding(it)
        }

        screenQueue.forEach { it.invoke() }

        keyMappingQueue.clear()
        screenQueue.clear()
    }
}