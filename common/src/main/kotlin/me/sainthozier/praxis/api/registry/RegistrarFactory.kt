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

package me.sainthozier.praxis.api.registry

import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import java.util.function.Supplier

/**
 * A factory that creates and manages all registrars for a single mod.
 *
 * An instance of this factory should be created once and stored in your main mod class.
 * It ensures all registrations for your mod are managed in an isolated context.
 */
interface RegistrarFactory {
    /**
     * The unique ID of the mod this factory is associated with.
     */
    val modId: String

    /**
     * Gets or creates a [DeferredRegistrar] for a specific registry.
     *
     * If a registrar for the given [registryKey] has already been created, it will be
     * returned. Otherwise, a new one will be created and stored.
     *
     * @param registryKey The key for the target registry (e.g., `Registries.ITEM`).
     * @return A [DeferredRegistrar] for the specified registry type.
     */
    fun <T : Any> getOrCreate(registryKey: ResourceKey<out Registry<T>>): DeferredRegistrar<T>

    /**
     * Creates and registers a new custom registry.
     *
     * This allows you to create your own registries for custom content types.
     *
     * @param registryKey The key for the new custom registry you want to create.
     * @return A [Supplier] that will provide the [Registry] instance once it has been created.
     */
    fun <T : Any> createRegistry(registryKey: ResourceKey<out Registry<T>>): Supplier<Registry<T>>

    /**
     * Declares a new data-driven custom registry.
     *
     * This method is a "fire-and-forget" declaration. It tells the game to create a new registry
     * that will be loaded from data packs, but it does **not** return the registry instance,
     * as it will not exist until much later in the game's loading process.
     *
     * ### How to Access the Registry
     * To access the contents of this registry, you must do so from a context where data packs
     * have been loaded (e.g., inside a loaded world or on a running server).
     *
     * ```kotlin
     * // Example from a CommandSourceStack (like in a command)
     * val registryAccess = commandContext.source.server.registryAccess()
     * val myRegistry = registryAccess.registryOrThrow(MyRegistries.MY_KEY)
     * ```
     *
     * ### How to Add Entries
     * Entries are added via JSON files in a data pack. The file path follows a specific structure:
     * `data/<namespace>/<registry_path>/<entry_name>.json`
     *
     * - `<namespace>`: Your mod ID (e.g., `mymod`).
     * - `<registry_path>`: The path from your registry's ResourceLocation (e.g., if your key is `praxis:rarity`, the path is `rarity`).
     * - `<entry_name>`: The name of your new entry (e.g., `mythic`).
     *
     * **Example Path:** For a registry key `praxis:rarity` and an entry `mythic` from mod `mymod`,
     * the path would be: `data/mymod/praxis/rarity/mythic.json`
     *
     * @param T The type of object the registry will hold.
     * @param registryKey The key for the new custom registry you want to create.
     * @param elementCodec The [Codec] used to serialize and deserialize objects of type `T` from JSON.
     */
    fun <T : Any> createDataDrivenRegistry(
        registryKey: ResourceKey<out Registry<T>>,
        elementCodec: Codec<T>
    )

    /**
     * Executes the registration for all queued objects on the current platform.
     *
     * This method should be called only once from your mod's main entry point
     * after all registration declarations have been made.
     */
    fun bootstrap()
}