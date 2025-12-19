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

import java.util.*
import java.util.function.Supplier
import java.util.stream.Collectors

object PraxisServiceLoader {

    inline fun <reified T> load(noinline customError: (() -> String)? = null): T {
        return load(T::class.java, customError)
    }

    @JvmStatic
    @JvmOverloads
    fun <T> load(service: Class<T>, customError: Supplier<String>? = null): T {
        return ServiceLoader.load(service).findFirst().orElseThrow {
            val defaultMessage = """
                Failed to load service for '${service.name}'.
                This is a critical error and is likely caused by a missing implementation or a dependency issue.
                Double check that the service is properly implemented and specified in the META-INF/services directory.
            """.trimIndent()
            IllegalStateException(customError?.get() ?: defaultMessage)
        }
    }

    inline fun <reified T> loadAll(): List<T> {
        return loadAll(T::class.java)
    }

    @JvmStatic
    fun <T> loadAll(service: Class<T>): List<T> {
        return ServiceLoader.load(service).stream()
            .map { obj: ServiceLoader.Provider<T> -> obj.get() }
            .collect(Collectors.toList())
    }
}