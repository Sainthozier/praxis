/*
 * License Header: Change me
 */

package com.example.examplemod.platform

import com.example.examplemod.platform.services.PlatformHelper
import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.fml.loading.FMLPaths
import java.nio.file.Path

class NeoForgePlatformHelper : PlatformHelper {
    override fun getPlatformName(): String {
        return "NeoForge"
    }

    override fun isModLoaded(modId: String?): Boolean {
        return ModList.get().isLoaded(modId)
    }

    override fun isDevelopmentEnvironment(): Boolean {
        return !FMLLoader.isProduction()
    }

    override fun getConfigDir(): Path {
        return FMLPaths.CONFIGDIR.get()
    }
}