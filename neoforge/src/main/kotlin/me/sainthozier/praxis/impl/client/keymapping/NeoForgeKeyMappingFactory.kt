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

package me.sainthozier.praxis.impl.client.keymapping

import com.mojang.blaze3d.platform.InputConstants
import me.sainthozier.praxis.api.client.keymapping.KeyConflictContext
import me.sainthozier.praxis.api.client.keymapping.KeyMappingFactory
import me.sainthozier.praxis.api.client.keymapping.KeyModifier
import net.minecraft.client.KeyMapping
import net.neoforged.neoforge.client.settings.IKeyConflictContext
import net.neoforged.neoforge.client.settings.KeyConflictContext as NeoForgeKeyConflictContext
import net.neoforged.neoforge.client.settings.KeyModifier as NeoForgeKeyModifier

internal class NeoForgeKeyMappingFactory : KeyMappingFactory {

    private fun mapContext(context: KeyConflictContext): IKeyConflictContext = when (context) {
        KeyConflictContext.UNIVERSAL -> NeoForgeKeyConflictContext.UNIVERSAL
        KeyConflictContext.GUI -> NeoForgeKeyConflictContext.GUI
        KeyConflictContext.IN_GAME -> NeoForgeKeyConflictContext.IN_GAME
    }

    private fun mapModifier(modifier: KeyModifier): NeoForgeKeyModifier = when (modifier) {
        KeyModifier.NONE -> NeoForgeKeyModifier.NONE
        KeyModifier.SHIFT -> NeoForgeKeyModifier.SHIFT
        KeyModifier.CONTROL -> NeoForgeKeyModifier.CONTROL
        KeyModifier.ALT -> NeoForgeKeyModifier.ALT
    }

    override fun create(
        description: String,
        type: InputConstants.Type,
        keyCode: Int,
        category: String,
        conflictContext: KeyConflictContext,
        keyModifier: KeyModifier
    ): KeyMapping {
        return KeyMapping(description, mapContext(conflictContext), mapModifier(keyModifier), type, keyCode, category)
    }

    override fun create(
        description: String,
        key: InputConstants.Key,
        category: String,
        conflictContext: KeyConflictContext,
        keyModifier: KeyModifier
    ): KeyMapping {
        return KeyMapping(description, mapContext(conflictContext), mapModifier(keyModifier), key, category)
    }

    override fun create(
        description: String,
        type: InputConstants.Type,
        keyCode: Int,
        category: String,
        conflictContext: KeyConflictContext
    ): KeyMapping {
        return KeyMapping(description, mapContext(conflictContext), type, keyCode, category)
    }

    override fun create(
        description: String,
        key: InputConstants.Key,
        category: String,
        conflictContext: KeyConflictContext
    ): KeyMapping {
        return KeyMapping(description, mapContext(conflictContext), key, category)
    }
}