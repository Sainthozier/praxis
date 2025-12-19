/*
 * License Header: Change me
 */

package com.example.examplemod.platform

import com.example.examplemod.platform.services.PlatformHelper
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

class FabricPlatformHelper : PlatformHelper {
    override fun getPlatformName(): String {
        return "Fabric"
    }

    override fun isModLoaded(modId: String?): Boolean {
        return FabricLoader.getInstance().isModLoaded(modId)
    }

    override fun isDevelopmentEnvironment(): Boolean {
        return FabricLoader.getInstance().isDevelopmentEnvironment
    }

    override fun getConfigDir(): Path {
        return FabricLoader.getInstance().configDir
    }
}