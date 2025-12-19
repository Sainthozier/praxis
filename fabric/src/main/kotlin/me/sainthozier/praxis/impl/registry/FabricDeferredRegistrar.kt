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

import me.sainthozier.praxis.api.registry.DeferredRegistrar
import me.sainthozier.praxis.api.registry.RegisteredObject
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import java.util.function.Supplier

internal class FabricDeferredRegistrar<T : Any>(
    private val modId: String,
    private val registryKey: ResourceKey<out Registry<T>>
) : DeferredRegistrar<T> {

    private val registrationQueue = mutableListOf<() -> Unit>()

    @Suppress("UNCHECKED_CAST")
    private val registry: Registry<T> by lazy {
        BuiltInRegistries.REGISTRY.get(registryKey.location()) as? Registry<T>
            ?: throw IllegalStateException("Failed to find registry: ${registryKey.location()}")
    }

    override fun <R : T> register(name: String, supplier: Supplier<out R>): RegisteredObject<T, R> {
        val registeredObject = FabricRegisteredObject<T, R>(modId, name, registryKey)

        registrationQueue.add {
            val obj = supplier.get()
            Registry.register(registry, ResourceLocation.fromNamespaceAndPath(modId, name), obj)
            registeredObject.initialize(obj)
        }

        return registeredObject
    }

    fun registerAll() {
        registrationQueue.forEach { it.invoke() }
        registrationQueue.clear()
    }
}