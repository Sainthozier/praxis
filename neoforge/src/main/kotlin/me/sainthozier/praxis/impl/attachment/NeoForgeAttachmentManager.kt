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

package me.sainthozier.praxis.impl.attachment

import com.google.auto.service.AutoService
import me.sainthozier.praxis.PraxisModInfo
import me.sainthozier.praxis.api.attachment.AttachmentKey
import me.sainthozier.praxis.api.attachment.AttachmentManager
import me.sainthozier.praxis.api.attachment.AttachmentPatch
import me.sainthozier.praxis.api.attachment.AttachmentTarget
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.attachment.IAttachmentHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier

@AutoService(AttachmentManager::class)
internal class NeoForgeAttachmentManager : AttachmentManager {

    private val keyRegistry = mutableMapOf<ResourceLocation, AttachmentKey<*>>()
    private val genericAttachmentTypes = mutableMapOf<AttachmentKey<*>, Supplier<out AttachmentType<*>>>()
    private val itemDataComponentTypes = mutableMapOf<AttachmentKey<*>, Supplier<out DataComponentType<*>>>()

    override fun <T : Any> registerKey(key: AttachmentKey<T>) {
        check(key.id !in keyRegistry) { "Key with id ${key.id} already registered" }

        keyRegistry[key.id] = key

        if (key.targets.contains(AttachmentTarget.ENTITY) || key.targets.contains(AttachmentTarget.BLOCK_ENTITY)) {
            val supplier = Supplier {
                val builder = AttachmentType.builder(key.defaultValueFactory)
                if (key.isPersistent) {
                    builder.serialize(key.persistenceCodec!!)
                }
                builder.build()
            }
            genericAttachmentTypes[key] = ATTACHMENT_TYPES.register(key.id.path, supplier)
        }

        if (key.targets.contains(AttachmentTarget.ITEM_STACK)) {
            val supplier = Supplier {
                val builder = DataComponentType.builder<T>()
                if (key.isPersistent) {
                    builder.persistent(key.persistenceCodec!!)
                }
                builder.build()
            }
            itemDataComponentTypes[key] = DATA_COMPONENT_TYPES.register(key.id.path, supplier)
        }
    }

    override fun getKey(id: ResourceLocation): AttachmentKey<*>? = keyRegistry[id]

    override fun <T : Any> get(holder: Any, key: AttachmentKey<T>): T? {
        return when (holder) {
            is ItemStack -> holder.get(getItemComponentType(key))
            is IAttachmentHolder -> holder.getExistingData(getNeoForgeAttachmentType(key)).orElse(null)
            else -> null
        }
    }

    override fun <T : Any> set(holder: Any, key: AttachmentKey<T>, value: T) {
        when (holder) {
            is ItemStack -> holder.set(getItemComponentType(key), value)
            is IAttachmentHolder -> holder.setData(getNeoForgeAttachmentType(key), value)
        }
    }

    override fun <T : Any> has(holder: Any, key: AttachmentKey<T>): Boolean {
        return when (holder) {
            is ItemStack -> holder.has(getItemComponentType(key))
            is IAttachmentHolder -> holder.hasData(getNeoForgeAttachmentType(key))
            else -> false
        }
    }

    override fun <T : Any> remove(holder: Any, key: AttachmentKey<T>) {
        when (holder) {
            is ItemStack -> holder.remove(getItemComponentType(key))
            is IAttachmentHolder -> holder.removeData(getNeoForgeAttachmentType(key))
        }
    }

    override fun <T : Any> modify(holder: Any, key: AttachmentKey<T>, modifier: (T) -> T) {
        get(holder, key)?.let {
            set(holder, key, modifier(it))
        }
    }

    override fun apply(holder: Any, patch: AttachmentPatch) {
        if (patch.isEmpty()) return

        // For ItemStacks, it's more efficient to build a single DataComponentPatch
        if (holder is ItemStack) {
            val vanillaPatchBuilder = DataComponentPatch.builder()
            patch.forEach { key, optionalValue ->
                if (key.targets.contains(AttachmentTarget.ITEM_STACK)) {
                    @Suppress("UNCHECKED_CAST")
                    val componentType = getItemComponentType(key as AttachmentKey<Any>)

                    if (optionalValue.isPresent) {
                        vanillaPatchBuilder.set(componentType, optionalValue.get())
                    } else {
                        vanillaPatchBuilder.remove(componentType)
                    }
                }
            }
            holder.applyComponentsAndValidate(vanillaPatchBuilder.build())
            return
        }

        // For other holders, apply sequentially
        patch.forEach { key, optionalValue ->
            if (optionalValue.isPresent) {
                @Suppress("UNCHECKED_CAST")
                val typedKey = key as AttachmentKey<Any>
                val valueToSet = optionalValue.get()
                set(holder, typedKey, valueToSet)
            } else {
                remove(holder, key)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getNeoForgeAttachmentType(key: AttachmentKey<T>): AttachmentType<T> =
        (genericAttachmentTypes[key] as? Supplier<AttachmentType<T>>)?.get()
            ?: throw IllegalStateException("No NeoForge AttachmentType registered for key: ${key.id}")

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getItemComponentType(key: AttachmentKey<T>): DataComponentType<T> =
        (itemDataComponentTypes[key] as? Supplier<DataComponentType<T>>)?.get()
            ?: throw IllegalStateException("No ItemStack DataComponentType registered for key: ${key.id}")

    companion object {
        val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PraxisModInfo.MOD_ID)

        val DATA_COMPONENT_TYPES: DeferredRegister<DataComponentType<*>> =
            DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, PraxisModInfo.MOD_ID)
    }
}