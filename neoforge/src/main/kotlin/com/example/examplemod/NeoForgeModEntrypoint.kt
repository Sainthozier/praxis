/*
 * License Header: Change me
 */

package com.example.examplemod

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(ModInfo.MOD_ID)
class NeoForgeModEntrypoint(eventBus: IEventBus, modContainer: ModContainer) {
    init {
        ModInfo.LOG.info("Hello NeoForge world from Kotlin!")
        CommonModEntrypoint.init()
    }
}