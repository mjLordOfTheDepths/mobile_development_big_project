package com.example.milestokm

import kotlin.math.exp

object FermentationCalculator {
    /*
     * Calculates the potential ABV based on sugar mass and volume.
     * Formula: ABV_raw = G / (V * 17)
     */
    fun calculatePotentialAbv(volume: Double, sugar: Double): Double {
        if (volume <= 0.0) return 0.0
        return sugar / (volume * 17.0)
    }

    /*
     * Adjusts the growth rate (k) based on temperature.
     * Formula: k_adj = k_base * (1 + 0.07 * (T - 20))
     */
    fun adjustGrowthRate(kBase: Double, temperature: Double): Double {
        return kBase * (1.0 + 0.07 * (temperature - 20.0))
    }

    /*
     * Calculates the alcohol percentage at time t using the Logistic Growth Formula.
     * Formula: A(t) = ABV_max / (1 + e^(-k * (t - t0)))
     */
    fun calculateAt(abvMax: Double, k: Double, t: Double, t0: Double): Double {
        return abvMax / (1.0 + exp(-k * (t - t0)))
    }
}
