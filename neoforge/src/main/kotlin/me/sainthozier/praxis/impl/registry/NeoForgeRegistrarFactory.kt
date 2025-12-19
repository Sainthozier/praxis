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
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList
import net.neoforged.neoforge.registries.DataPackRegistryEvent
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.*
import java.util.function.Supplier

internal class NeoForgeRegistrarFactory(override val modId: String) : RegistrarFactory {
    private val registrars = mutableListOf<DeferredRegister<*>>()
    private val dynamicRegistryQueue = mutableListOf<Pair<ResourceKey<out Registry<*>>, Codec<*>>>()
    private var listenerAttached = false

    private val eventBus: IEventBus by lazy {
        ModList.get().getModContainerById(modId).flatMap { container -> Optional.ofNullable(container.eventBus) }
            .orElseThrow { IllegalStateException("Could not find mod container for '$modId' or it has no event bus") }
    }

    override fun <T : Any> getOrCreate(registryKey: ResourceKey<out Registry<T>>): DeferredRegistrar<T> {
        val deferredRegister = DeferredRegister.create(registryKey, modId)
        registrars.add(deferredRegister)
        return NeoForgeDeferredRegistrar(deferredRegister)
    }

    override fun <T : Any> createRegistry(registryKey: ResourceKey<out Registry<T>>): Supplier<Registry<T>> {
        val deferredRegister = DeferredRegister.create(registryKey, modId)
        deferredRegister.makeRegistry {}
        registrars.add(deferredRegister)
        return deferredRegister.registry
    }

    override fun <T : Any> createDataDrivenRegistry(
        registryKey: ResourceKey<out Registry<T>>,
        elementCodec: Codec<T>
    ) {
        if (!listenerAttached) {
            eventBus.addListener(::onNewDataPackRegistry)
            listenerAttached = true
        }

        dynamicRegistryQueue.add(registryKey to elementCodec)
    }

    private fun onNewDataPackRegistry(event: DataPackRegistryEvent.NewRegistry) {
        dynamicRegistryQueue.forEach { (key, codec) ->
            @Suppress("UNCHECKED_CAST")
            event.dataPackRegistry(key as ResourceKey<Registry<Any>>, codec as Codec<Any>, codec)
        }
    }

    override fun bootstrap() {
        registrars.forEach { it.register(eventBus) }
    }
}