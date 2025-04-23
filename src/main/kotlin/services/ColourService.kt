package com.example.services

import kotlin.random.Random

class ColourService {
    fun generateRandomPalette(count: Int = 5): List<String> {
        val colors = mutableListOf<String>()
        
        for (i in 0 until count) {
            val red = Random.nextInt(256)
            val green = Random.nextInt(256)
            val blue = Random.nextInt(256)
            colors.add(String.format("#%02X%02X%02X", red, green, blue))
        }
        
        return colors
    }
    
    fun generateAnalogousPalette(baseColor: String, count: Int = 5): List<String> {
        // Simple implementation for analogous colors
        val colors = mutableListOf<String>()
        colors.add(baseColor)
        
        try {
            // Parse the base color
            val r = Integer.parseInt(baseColor.substring(1, 3), 16)
            val g = Integer.parseInt(baseColor.substring(3, 5), 16)
            val b = Integer.parseInt(baseColor.substring(5, 7), 16)
            
            // Convert to HSL (
            val hsl = rgbToHsl(r, g, b)
            
            // Generate colors with slightly different hues
            for (i in 1 until count) {
                val newHue = (hsl[0] + (i * 30) % 360).toFloat()
                val rgb = hslToRgb(newHue, hsl[1], hsl[2])
                colors.add(String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2]))
            }
        } catch (e: Exception) {
            // Fallback to random if color parsing fails
            return generateRandomPalette(count)
        }
        
        return colors
    }
    
    // Simplified RGB to HSL conversion
    private fun rgbToHsl(r: Int, g: Int, b: Int): FloatArray {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f
        
        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val delta = max - min
        
        var h = 0f
        val s: Float
        val l = (max + min) / 2f
        
        if (delta == 0f) {
            h = 0f
            s = 0f
        } else {
            s = if (l < 0.5f) delta / (max + min) else delta / (2 - max - min)
            
            h = when {
                rf == max -> (gf - bf) / delta + (if (gf < bf) 6 else 0)
                gf == max -> (bf - rf) / delta + 2
                else -> (rf - gf) / delta + 4
            }
            
            h *= 60
        }
        
        return floatArrayOf(h, s, l)
    }
    
    // Simplified HSL to RGB conversion
    private fun hslToRgb(h: Float, s: Float, l: Float): IntArray {
        if (s == 0f) {
            val gray = (l * 255).toInt()
            return intArrayOf(gray, gray, gray)
        }
        
        val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
        val p = 2 * l - q
        
        val hk = h / 360f
        val tc = floatArrayOf(hk + 1/3f, hk, hk - 1/3f)
        
        val colors = FloatArray(3)
        
        for (i in 0..2) {
            var t = tc[i]
            if (t < 0) t += 1f
            if (t > 1) t -= 1f
            
            colors[i] = when {
                t < 1/6f -> p + (q - p) * 6 * t
                t < 1/2f -> q
                t < 2/3f -> p + (q - p) * (2/3f - t) * 6
                else -> p
            }
        }
        
        return intArrayOf(
            (colors[0] * 255).toInt(),
            (colors[1] * 255).toInt(),
            (colors[2] * 255).toInt()
        )
    }
}