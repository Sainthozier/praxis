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

package me.sainthozier.praxis.api.event.entity.player

import me.sainthozier.praxis.api.event.Event
import me.sainthozier.praxis.api.event.EventFactory
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

object PlayerInteractEvents {

    @JvmField
    val USE_BLOCK: Event<UseBlock> = EventFactory.untilSuccess()

    @JvmField
    val ATTACK_BLOCK: Event<AttackBlock> = EventFactory.untilSuccess()

    @JvmField
    val USE_ENTITY: Event<UseEntity> = EventFactory.untilSuccess()

    @JvmField
    val ATTACK_ENTITY: Event<AttackEntity> = EventFactory.untilSuccess()

    @JvmField
    val USE_ITEM: Event<UseItem> = EventFactory.untilSuccessWithHolder { args ->
        val player = args!![0] as Player
        val hand = args[2] as InteractionHand
        InteractionResultHolder.pass(player.getItemInHand(hand))
    }

    fun interface UseBlock {
        fun onUseBlock(
            player: Player,
            level: Level,
            hand: InteractionHand,
            hitResult: BlockHitResult
        ): InteractionResult
    }

    fun interface AttackBlock {
        fun onAttackBlock(
            player: Player,
            level: Level,
            hand: InteractionHand,
            hitResult: BlockHitResult?
        ): InteractionResult
    }

    fun interface UseItem {
        fun onUseItem(player: Player, level: Level, hand: InteractionHand): InteractionResultHolder<ItemStack>
    }

    fun interface UseEntity {
        fun onUseEntity(
            player: Player,
            level: Level,
            hand: InteractionHand,
            entity: Entity,
            hitResult: EntityHitResult?
        ): InteractionResult
    }

    fun interface AttackEntity {
        fun onAttackEntity(
            player: Player,
            level: Level,
            hand: InteractionHand,
            entity: Entity,
            hitResult: EntityHitResult?
        ): InteractionResult
    }
}