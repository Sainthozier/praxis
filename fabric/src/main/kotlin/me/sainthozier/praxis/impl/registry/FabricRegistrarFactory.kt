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

package me.sainthozier.praxis.impl.registry

import com.mojang.serialization.Codec
import me.sainthozier.praxis.api.registry.DeferredRegistrar
import me.sainthozier.praxis.api.registry.RegistrarFactory
import net.fabricmc.fabric.api.event.registry.DynamicRegistries
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import java.util.function.Supplier

internal class FabricRegistrarFactory(override val modId: String) : RegistrarFactory {
    private val registrars = mutableMapOf<ResourceKey<out Registry<*>>, FabricDeferredRegistrar<*>>()

    override fun <T : Any> getOrCreate(registryKey: ResourceKey<out Registry<T>>): DeferredRegistrar<T> {
        @Suppress("UNCHECKED_CAST")
        return registrars.computeIfAbsent(registryKey) {
            FabricDeferredRegistrar<T>(modId, it as ResourceKey<out Registry<T>>)
        } as DeferredRegistrar<T>
    }

    override fun <T : Any> createRegistry(registryKey: ResourceKey<out Registry<T>>): Supplier<Registry<T>> {
        val newRegistry = FabricRegistryBuilder.createSimple(registryKey as ResourceKey<Registry<T>>).buildAndRegister()
        return Supplier { newRegistry }
    }

    override fun <T : Any> createDataDrivenRegistry(
        registryKey: ResourceKey<out Registry<T>>,
        elementCodec: Codec<T>
    ) {
        @Suppress("UNCHECKED_CAST")
        DynamicRegistries.registerSynced(registryKey as ResourceKey<Registry<T>>, elementCodec)
    }

    override fun bootstrap() {
        registrars.values.forEach { it.registerAll() }
    }
}