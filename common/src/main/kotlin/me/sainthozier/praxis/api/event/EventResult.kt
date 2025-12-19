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

package me.sainthozier.praxis.api.event

import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder


val InteractionResult.isPass: Boolean
    get() = this == InteractionResult.PASS

val InteractionResult.isFail: Boolean
    get() = this == InteractionResult.FAIL

val InteractionResult.isSuccess: Boolean
    get() = this.consumesAction()

val InteractionResult.isDefinitive: Boolean
    get() = this != InteractionResult.PASS

val <T> InteractionResultHolder<T>.isPass: Boolean
    get() = this.result == InteractionResult.PASS

val <T> InteractionResultHolder<T>.isFail: Boolean
    get() = this.result == InteractionResult.FAIL

val <T> InteractionResultHolder<T>.isSuccess: Boolean
    get() = this.result.consumesAction()

val <T> InteractionResultHolder<T>.isDefinitive: Boolean
    get() = this.result != InteractionResult.PASS