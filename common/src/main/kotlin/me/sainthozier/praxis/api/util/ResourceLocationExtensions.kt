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

package me.sainthozier.praxis.api.util

import me.sainthozier.praxis.ModInfo
import net.minecraft.resources.ResourceLocation

/**
 * @return [ResourceLocation] from the String using the mod id specified in [me.sainthozier.praxis.ModInfo]
 */
fun String.location() = ResourceLocation.fromNamespaceAndPath(ModInfo.MOD_ID, this)

/**
 * @return [ResourceLocation] from the string using the passed namespace
 */
fun String.location(namespace: String) = ResourceLocation.fromNamespaceAndPath(namespace, this)

/**
 * @return [ResourceLocation] from the string using the vanilla namespace
 */
fun String.vanillaLocation() = ResourceLocation.withDefaultNamespace(this)