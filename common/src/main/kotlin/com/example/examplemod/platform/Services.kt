/*
 * License Header: Change me
 */

package com.example.examplemod.platform

import com.example.examplemod.ModInfo
import com.example.examplemod.platform.services.PlatformHelper
import java.util.ServiceLoader

object Services {
    val PLATFORM = load(PlatformHelper::class.java)

    fun <T> load(clazz: Class<T>): T {
        val loadedService = ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow {
                IllegalStateException("Failed to load service for ${clazz.name}")
            }
        ModInfo.LOG.debug("Loaded {} for service {}", loadedService, clazz)
        return loadedService
    }
}