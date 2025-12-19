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

import me.sainthozier.praxis.api.registry.RegisteredObject
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

internal class FabricRegisteredObject<T : Any, R : T>(
    modId: String,
    name: String,
    private val registryKey: ResourceKey<out Registry<T>>
) : RegisteredObject<T, R> {

    override val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath(modId, name)

    override val key: ResourceKey<T> by lazy { ResourceKey.create(this.registryKey, id) }

    private var value: R? = null

    override val holder: Holder<T> by lazy {
        checkRegistered()
        @Suppress("UNCHECKED_CAST")
        val registry = BuiltInRegistries.REGISTRY.get(registryKey.location()) as Registry<T>
        registry.getHolderOrThrow(key)
    }

    fun initialize(obj: R) {
        this.value = obj
    }

    override fun get(): R {
        checkRegistered()
        return value!!
    }

    private fun checkRegistered() {
        if (value == null) {
            throw IllegalStateException("Object '$id' has not been registered yet. It should only be accessed after bootstrap.")
        }
    }
}