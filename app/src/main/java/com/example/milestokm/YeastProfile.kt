package com.example.milestokm

data class YeastProfile(
    val kBase: Double,
    val t0: Double,
    val maxTolerance: Double
)

object YeastProfiles {
    val pikahiiva = YeastProfile(0.7, 0.08, 20.0)
    val breadYeast = YeastProfile(0.3, 0.5, 10.0)
    val wineYeast = YeastProfile(0.4, 1.0, 15.0)
}
