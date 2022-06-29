package com.moop.gamerguides.helper

object GameTitleUtil {
    fun change(str: String?): String {
        return when (str) {
            "Valorant" -> {
                "valorant"
            }
            "Free Fire" -> {
                "free_fire"
            }
            "Mobile Legends" -> {
                "mobile_legends"
            }
            "PUBG Mobile" -> {
                "pubgm"
            }
            "Fortnite" -> {
                "fortnite"
            }
            else -> {"unadded"}
        }
    }
}