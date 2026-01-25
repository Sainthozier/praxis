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

package me.sainthozier.praxis.mixin;

import me.sainthozier.praxis.api.attachment.registry.RegistryAttachmentType;
import me.sainthozier.praxis.impl.attachment.registry.RegistryAttachmentHolder;
import me.sainthozier.praxis.impl.util.ReferenceMapAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(MappedRegistry.class)
public class MixinMappedRegistry<R> implements RegistryAttachmentHolder<R>, ReferenceMapAccessor<R> {

    @Shadow
    @Final
    private Map<R, Holder.Reference<R>> byValue;

    @Unique
    private final Map<RegistryAttachmentType<R, ?>, Map<ResourceKey<R>, ?>> praxis$registryAttachments = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> Map<@NotNull ResourceKey<R>, T> praxis$getAttachments(@NotNull RegistryAttachmentType<R, T> type) {
        return (Map<ResourceKey<R>, T>) praxis$registryAttachments.getOrDefault(type, Collections.emptyMap());
    }

    @Override
    public @Nullable <T> T praxis$getAttachment(@NotNull RegistryAttachmentType<R, T> type, @NotNull ResourceKey<R> key) {
        return praxis$getAttachments(type).get(key);
    }

    @Override
    public <T> void praxis$setAttachmentsForType(@NotNull RegistryAttachmentType<R, T> type, @NotNull Map<@NotNull ResourceKey<R>, ? extends T> values) {
        praxis$registryAttachments.put(type, values);
    }

    @Override
    public void praxis$clearAttachments() {
        praxis$registryAttachments.clear();
    }

    @Override
    public @Nullable Holder<R> praxis$wrapAsHolderOrNull(R value) {
        return byValue.get(value) != null ? byValue.get(value) : null;
    }

    @Override
    public @NotNull Holder<R> praxis$wrapAsHolderOrThrow(R value) {
        var maybeHolder = byValue.get(value);
        if (maybeHolder == null) {
            throw new IllegalStateException("Holder not found for value: " + value.toString());
        }
        return maybeHolder;
    }
}
