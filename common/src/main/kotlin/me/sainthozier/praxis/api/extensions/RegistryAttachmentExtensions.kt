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

import me.sainthozier.praxis.api.attachment.registry.RegistryAttachmentManager
import me.sainthozier.praxis.api.attachment.registry.RegistryAttachmentType
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

fun <R, T> Holder<R>.getAttachment(type: RegistryAttachmentType<R, T>): T? =
    RegistryAttachmentManager.getAttachment(type, this)

fun <R, T> Holder<R>.getAttachmentOrElse(type: RegistryAttachmentType<R, T>, default: T): T =
    getAttachment(type) ?: default

fun <R, T> Holder<R>.hasAttachment(type: RegistryAttachmentType<R, T>): Boolean =
    getAttachment(type) != null

fun <R, T> Registry<R>.getAttachment(type: RegistryAttachmentType<R, T>, key: ResourceKey<R>): T? =
    RegistryAttachmentManager.getAttachment(this, type, key)

fun <R, T> Registry<R>.getAttachmentOrElse(type: RegistryAttachmentType<R, T>, key: ResourceKey<R>, default: T): T =
    RegistryAttachmentManager.getAttachmentOrElse(this, type, key, default)

fun <R, T> Registry<R>.hasAttachment(type: RegistryAttachmentType<R, T>, key: ResourceKey<R>): Boolean =
    RegistryAttachmentManager.hasAttachment(this, type, key)