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

package me.sainthozier.praxis.api.attachment

import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface AttachmentManager {

    /**
     * Registers an [AttachmentKey] with both the platform and the central registry.
     * This is called automatically by the [AttachmentKey.Builder].
     */
    fun <T : Any> registerKey(key: AttachmentKey<T>)

    /**
     * Retrieves a registered [AttachmentKey] by its ID.
     */
    fun getKey(id: ResourceLocation): AttachmentKey<*>?

    fun <T : Any> get(holder: Any, key: AttachmentKey<T>): T?

    fun <T : Any> set(holder: Any, key: AttachmentKey<T>, value: T)

    fun <T : Any> has(holder: Any, key: AttachmentKey<T>): Boolean

    fun <T : Any> remove(holder: Any, key: AttachmentKey<T>)

    fun <T : Any> modify(holder: Any, key: AttachmentKey<T>, modifier: (T) -> T)

    fun apply(holder: Any, patch: AttachmentPatch)
}