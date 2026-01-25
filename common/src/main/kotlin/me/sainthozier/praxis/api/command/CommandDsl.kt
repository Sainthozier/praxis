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

package me.sainthozier.praxis.api.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

@DslMarker
annotation class CommandDsl

fun <S : SharedSuggestionProvider> CommandDispatcher<S>.registerCommand(
    name: String, block: LiteralArgumentBuilder<S>.() -> Unit
) {
    this.register(
        LiteralArgumentBuilder.literal<S>(name).apply(block)
    )
}

@CommandDsl
fun <S : SharedSuggestionProvider> ArgumentBuilder<S, *>.literal(
    name: String, block: LiteralArgumentBuilder<S>.() -> Unit
) {
    this.then(
        LiteralArgumentBuilder.literal<S>(name).apply(block)
    )
}

@CommandDsl
fun <S : SharedSuggestionProvider, T> ArgumentBuilder<S, *>.argument(
    name: String, type: ArgumentType<T>, block: RequiredArgumentBuilder<S, T>.() -> Unit
) {
    this.then(
        RequiredArgumentBuilder.argument<S, T>(name, type).apply(block)
    )
}

@CommandDsl
fun <S : SharedSuggestionProvider> ArgumentBuilder<S, *>.executes(command: (CommandContext<S>) -> Int) {
    this.executes(command)
}

@CommandDsl
fun <S : SharedSuggestionProvider> ArgumentBuilder<S, *>.requires(predicate: (S) -> Boolean) {
    this.requires(predicate)
}

inline fun <reified T> CommandContext<*>.getArgument(name: String): T {
    return this.getArgument(name, T::class.java)
}

fun CommandContext<*>.success(message: String) {
    (this.source as? CommandSourceStack)?.sendSuccess({ Component.literal(message) }, false)
}

fun CommandContext<*>.fail(message: String) {
    (this.source as? CommandSourceStack)?.sendFailure(Component.literal(message))
}