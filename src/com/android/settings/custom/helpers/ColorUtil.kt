/*
 * Copyright (C) 2020 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.custom.helpers

import android.graphics.Color

import androidx.annotation.ColorInt
import androidx.annotation.Size

import kotlin.math.max
import kotlin.math.min

object ColorUtil {
    @Size(3)
    fun colorToHSL(@ColorInt color: Int, @Size(3) hsl: FloatArray = FloatArray(3)): FloatArray {
        val r = Color.red(color) / 255F
        val g = Color.green(color) / 255F
        val b = Color.blue(color) / 255F

        val max = max(r, max(g, b))
        val min = min(r, min(g, b))

        hsl[2] = (max + min) / 2

        if (max == min) {
            hsl[1] = 0F
            hsl[0] = hsl[1]
        } else {
            val d = max - min

            hsl[1] = if (hsl[2] > 0.5F) d / (2F - max - min) else d / (max + min)
            when (max) {
                r -> hsl[0] = (g - b) / d + (if (g < b) 6 else 0)
                g -> hsl[0] = (b - r) / d + 2
                b -> hsl[0] = (r - g) / d + 4
            }
            hsl[0] /= 6F
        }

        return hsl
    }

    @ColorInt
    fun hslToColor(@Size(3) hsl: FloatArray): Int {
        val r: Float
        val g: Float
        val b: Float

        val h = hsl[0]
        val s = hsl[1]
        val l = hsl[2]

        if (s == 0F) {
            b = l
            g = b
            r = g
        } else {
            val q = if (l < 0.5F) l * (1 + s) else l + s - l * s
            val p = 2 * l - q
            r = hue2rgb(p, q, h + 1F / 3)
            g = hue2rgb(p, q, h)
            b = hue2rgb(p, q, h - 1F / 3)
        }

        return Color.rgb((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
    }

    fun hue2rgb(p: Float, q: Float, t: Float): Float {
        var valueT = t
        if (valueT < 0) valueT += 1F
        if (valueT > 1) valueT -= 1F
        if (valueT < 1F / 6) return p + (q - p) * 6F * valueT
        if (valueT < 1F / 2) return q
        return if (valueT < 2F / 3) p + (q - p) * (2F / 3 - valueT) * 6F else p
    }
}