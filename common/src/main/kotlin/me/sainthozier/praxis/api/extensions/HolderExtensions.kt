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

@file:Suppress("UNCHECKED_CAST")

package me.sainthozier.praxis.api.extensions

import me.sainthozier.praxis.impl.util.ReferenceMapAccessor
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.item.Item
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.level.block.Block

fun Item.asHolder(): Holder<Item> =
    (BuiltInRegistries.ITEM as ReferenceMapAccessor<Item>).`praxis$wrapAsHolderOrThrow`(this)

fun Block.asHolder(): Holder<Block> =
    (BuiltInRegistries.BLOCK as ReferenceMapAccessor<Block>).`praxis$wrapAsHolderOrThrow`(this)

fun EntityType<*>.asHolder(): Holder<EntityType<*>> =
    (BuiltInRegistries.ENTITY_TYPE as ReferenceMapAccessor<EntityType<*>>).`praxis$wrapAsHolderOrThrow`(this)

fun Attribute.asHolder(): Holder<Attribute> =
    (BuiltInRegistries.ATTRIBUTE as ReferenceMapAccessor<Attribute>).`praxis$wrapAsHolderOrThrow`(this)

fun Potion.asHolder(): Holder<Potion> =
    (BuiltInRegistries.POTION as ReferenceMapAccessor<Potion>).`praxis$wrapAsHolderOrThrow`(this)

fun MobEffect.asHolder(): Holder<MobEffect> =
    (BuiltInRegistries.MOB_EFFECT as ReferenceMapAccessor<MobEffect>).`praxis$wrapAsHolderOrThrow`(this)