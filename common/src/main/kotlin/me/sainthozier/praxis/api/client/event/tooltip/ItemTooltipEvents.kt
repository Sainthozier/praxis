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

package me.sainthozier.praxis.api.client.event.tooltip

import me.sainthozier.praxis.api.event.Event
import me.sainthozier.praxis.api.event.EventFactory
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

object ItemTooltipEvents {

    /**
     * Fired at various sections during tooltip creation, allowing for custom tooltip lines to be added.
     * Register a listener to this event and check the `section` parameter to
     * inject your tooltip lines at the desired location.
     *
     * Example:
     * ```kotlin
     * ItemTooltipEvents.MODIFY.register { section, stack, context, flag, player, lines ->
     *   if (section == TooltipSection.AFTER_NAME) {
     *      lines.add(Component.literal("Injected after name!"))
     *   }
     * }
     * ```
     */
    @JvmField
    val MODIFY: Event<ItemTooltipCallback> = EventFactory.broadcast()

    /**
     * A callback for modifying an item's tooltip.
     */
    fun interface ItemTooltipCallback {
        /**
         * Called when the tooltip for an item is being built.
         *
         * @param section The specific [TooltipSection] where the event is being fired.
         * @param stack The item stack being inspected.
         * @param context The tooltip context provided by the game.
         * @param flag The tooltip flag provided by the game.
         * @param player The player who is inspecting the item, may be null.
         * @param lines A mutable list of components representing the tooltip.
         */
        fun onTooltip(
            section: TooltipSection,
            stack: ItemStack,
            context: Item.TooltipContext,
            flag: TooltipFlag,
            player: Player?,
            lines: MutableList<Component>
        )
    }
}