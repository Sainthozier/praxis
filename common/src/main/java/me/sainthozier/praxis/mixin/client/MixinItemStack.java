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

package me.sainthozier.praxis.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import me.sainthozier.praxis.api.client.event.tooltip.ItemTooltipEvents;
import me.sainthozier.praxis.api.client.event.tooltip.TooltipSection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/TooltipFlag;isAdvanced()Z", ordinal = 0))
    private void injectTooltipAfterName(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> tooltip) {
        ItemTooltipEvents.MODIFY.getInvoker().onTooltip(TooltipSection.AFTER_NAME, (ItemStack) (Object) this, tooltipContext, tooltipFlag, player, tooltip);
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V", shift = At.Shift.AFTER))
    private void injectTooltipAfterAdditionalInfo(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> tooltip) {
        ItemTooltipEvents.MODIFY.getInvoker().onTooltip(TooltipSection.AFTER_ADDITIONAL_INFO, (ItemStack) (Object) this, tooltipContext, tooltipFlag, player, tooltip);
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void injectTooltipAfterTrim(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> tooltip) {
        ItemTooltipEvents.MODIFY.getInvoker().onTooltip(TooltipSection.AFTER_TRIM, (ItemStack) (Object) this, tooltipContext, tooltipFlag, player, tooltip);
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V", ordinal = 3, shift = At.Shift.AFTER))
    private void injectTooltipAfterEnchantments(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> tooltip) {
        ItemTooltipEvents.MODIFY.getInvoker().onTooltip(TooltipSection.AFTER_ENCHANTMENTS, (ItemStack) (Object) this, tooltipContext, tooltipFlag, player, tooltip);
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V", ordinal = 5, shift = At.Shift.AFTER))
    private void injectTooltipAfterLore(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> tooltip) {
        ItemTooltipEvents.MODIFY.getInvoker().onTooltip(TooltipSection.AFTER_LORE, (ItemStack) (Object) this, tooltipContext, tooltipFlag, player, tooltip);
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V", ordinal = 6, shift = At.Shift.AFTER))
    private void injectTooltipAfterAttributes(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> tooltip) {
        ItemTooltipEvents.MODIFY.getInvoker().onTooltip(TooltipSection.AFTER_ATTRIBUTES, (ItemStack) (Object) this, tooltipContext, tooltipFlag, player, tooltip);
    }

    @Inject(method = "getTooltipLines", at = @At(value = "TAIL"))
    private void injectTooltipEnd(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> tooltip) {
        ItemTooltipEvents.MODIFY.getInvoker().onTooltip(TooltipSection.END, (ItemStack) (Object) this, tooltipContext, tooltipFlag, player, tooltip);
    }
}
