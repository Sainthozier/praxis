/*
 * License Header: Change me
 */

package com.example.examplemod

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ModInfo {
    const val MOD_ID = "examplemod"
    const val MOD_NAME = "ExampleMod"
    @JvmStatic // needed so Mixins can access
    val LOG: Logger = LoggerFactory.getLogger(MOD_NAME)
}