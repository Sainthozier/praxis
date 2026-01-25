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

package me.sainthozier.praxis.api.extensions

import me.sainthozier.praxis.api.PraxisApi
import me.sainthozier.praxis.api.attachment.AttachmentKey
import me.sainthozier.praxis.api.attachment.AttachmentPatch
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity

// --- Entity Extensions ---

fun <T : Any> Entity.getAttached(key: AttachmentKey<T>): T? = PraxisApi.attachmentManager.get(this, key)
fun <T : Any> Entity.getAttachedOrDefault(key: AttachmentKey<T>): T = getAttached(key) ?: key.defaultValueFactory()
fun <T : Any> Entity.getAttachedOrElse(key: AttachmentKey<T>, `else`: T): T = getAttached(key) ?: `else`
fun <T : Any> Entity.setAttached(key: AttachmentKey<T>, value: T) = PraxisApi.attachmentManager.set(this, key, value)
fun <T : Any> Entity.hasAttached(key: AttachmentKey<T>): Boolean = PraxisApi.attachmentManager.has(this, key)
fun <T : Any> Entity.removeAttached(key: AttachmentKey<T>) = PraxisApi.attachmentManager.remove(this, key)
fun <T : Any> Entity.modifyAttached(key: AttachmentKey<T>, modifier: (T) -> T) =
    PraxisApi.attachmentManager.modify(this, key, modifier)

fun Entity.applyAttachments(patch: AttachmentPatch) = PraxisApi.attachmentManager.apply(this, patch)

// --- BlockEntity Extensions ---

fun <T : Any> BlockEntity.getAttached(key: AttachmentKey<T>): T? = PraxisApi.attachmentManager.get(this, key)
fun <T : Any> BlockEntity.getAttachedOrDefault(key: AttachmentKey<T>): T = getAttached(key) ?: key.defaultValueFactory()
fun <T : Any> BlockEntity.getAttachedOrElse(key: AttachmentKey<T>, `else`: T): T = getAttached(key) ?: `else`
fun <T : Any> BlockEntity.setAttached(key: AttachmentKey<T>, value: T) =
    PraxisApi.attachmentManager.set(this, key, value)

fun <T : Any> BlockEntity.hasAttached(key: AttachmentKey<T>): Boolean = PraxisApi.attachmentManager.has(this, key)
fun <T : Any> BlockEntity.removeAttached(key: AttachmentKey<T>) = PraxisApi.attachmentManager.remove(this, key)
fun <T : Any> BlockEntity.modifyAttached(key: AttachmentKey<T>, modifier: (T) -> T) =
    PraxisApi.attachmentManager.modify(this, key, modifier)

fun BlockEntity.applyAttachments(patch: AttachmentPatch) = PraxisApi.attachmentManager.apply(this, patch)

// --- ItemStack Extensions ---

fun <T : Any> ItemStack.getAttached(key: AttachmentKey<T>): T? = PraxisApi.attachmentManager.get(this, key)
fun <T : Any> ItemStack.getAttachedOrDefault(key: AttachmentKey<T>): T = getAttached(key) ?: key.defaultValueFactory()
fun <T : Any> ItemStack.getAttachedOrElse(key: AttachmentKey<T>, `else`: T): T = getAttached(key) ?: `else`
fun <T : Any> ItemStack.setAttached(key: AttachmentKey<T>, value: T) =
    PraxisApi.attachmentManager.set(this, key, value)

fun <T : Any> ItemStack.hasAttached(key: AttachmentKey<T>): Boolean = PraxisApi.attachmentManager.has(this, key)
fun <T : Any> ItemStack.removeAttached(key: AttachmentKey<T>) = PraxisApi.attachmentManager.remove(this, key)
fun <T : Any> ItemStack.modifyAttached(key: AttachmentKey<T>, modifier: (T) -> T) =
    PraxisApi.attachmentManager.modify(this, key, modifier)

fun ItemStack.applyAttachments(patch: AttachmentPatch) = PraxisApi.attachmentManager.apply(this, patch)