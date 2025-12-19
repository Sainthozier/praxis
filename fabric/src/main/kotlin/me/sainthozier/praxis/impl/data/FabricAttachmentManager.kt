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

package me.sainthozier.praxis.impl.data

import com.google.auto.service.AutoService
import me.sainthozier.praxis.api.data.AttachmentKey
import me.sainthozier.praxis.api.data.AttachmentManager
import me.sainthozier.praxis.api.data.AttachmentPatch
import me.sainthozier.praxis.api.data.AttachmentTarget
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity

@Suppress("UnstableApiUsage")
@AutoService(AttachmentManager::class)
internal class FabricAttachmentManager : AttachmentManager {

    private val keyRegistry = mutableMapOf<ResourceLocation, AttachmentKey<*>>()
    private val genericAttachmentTypes = mutableMapOf<AttachmentKey<*>, AttachmentType<*>>()
    private val itemDataComponentTypes = mutableMapOf<AttachmentKey<*>, DataComponentType<*>>()

    override fun <T : Any> registerKey(key: AttachmentKey<T>) {
        check(!keyRegistry.containsKey(key.id)) { "Key with id ${key.id} already registered" }

        keyRegistry[key.id] = key

        if (key.targets.contains(AttachmentTarget.ENTITY) || key.targets.contains(AttachmentTarget.BLOCK_ENTITY)) {
            val attachmentType: AttachmentType<T> = if (key.isPersistent) {
                AttachmentRegistry.createPersistent(key.id, key.persistenceCodec!!)
            } else {
                AttachmentRegistry.create(key.id)
            }
            genericAttachmentTypes[key] = attachmentType
        }
        if (key.targets.contains(AttachmentTarget.ITEM_STACK)) {
            val builder = DataComponentType.builder<T>()
            if (key.isPersistent) {
                builder.persistent(key.persistenceCodec!!)
            }
            val componentType = builder.build()
            Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, key.id, componentType)
            itemDataComponentTypes[key] = componentType
        }
    }

    override fun getKey(id: ResourceLocation): AttachmentKey<*>? = keyRegistry[id]

    override fun <T : Any> get(holder: Any, key: AttachmentKey<T>): T? {
        return when (holder) {
            is ItemStack -> holder.get(getItemComponentType(key))
            is Entity, is BlockEntity -> holder.getAttached(getFabricAttachmentType(key))
            else -> null
        }
    }

    override fun <T : Any> set(holder: Any, key: AttachmentKey<T>, value: T) {
        when (holder) {
            is ItemStack -> holder.set(getItemComponentType(key), value)
            is Entity, is BlockEntity -> holder.setAttached(getFabricAttachmentType(key), value)
        }
    }

    override fun <T : Any> has(holder: Any, key: AttachmentKey<T>): Boolean {
        return when (holder) {
            is ItemStack -> holder.has(getItemComponentType(key))
            is Entity, is BlockEntity -> holder.hasAttached(getFabricAttachmentType(key))
            else -> false
        }
    }

    override fun <T : Any> remove(holder: Any, key: AttachmentKey<T>) {
        when (holder) {
            is ItemStack -> holder.remove(getItemComponentType(key))
            is Entity, is BlockEntity -> holder.removeAttached(getFabricAttachmentType(key))
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
    private fun <T : Any> getFabricAttachmentType(key: AttachmentKey<T>): AttachmentType<T> =
        genericAttachmentTypes[key] as? AttachmentType<T>
            ?: throw IllegalStateException("No Entity/BlockEntity AttachmentType registered for key: ${key.id}")

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getItemComponentType(key: AttachmentKey<T>): DataComponentType<T> =
        itemDataComponentTypes[key] as? DataComponentType<T>
            ?: throw IllegalStateException("No ItemStack DataComponentType registered for key: ${key.id}")
}