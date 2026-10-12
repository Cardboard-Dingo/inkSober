package app.unpolished.inksober.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

data class PresetSubstance(
    val id: String,
    val displayName: String,
    val category: String,
    val iconName: String,
    val defaultNote: String = ""
) {
    companion object {
        val ALL_PRESETS: List<PresetSubstance> = listOf(
            PresetSubstance(
                id = "alcohol",
                displayName = "Alcohol",
                category = "Substances",
                iconName = "local_bar",
                defaultNote = "Decided to live sober and clear-minded."
            ),
            PresetSubstance(
                id = "tobacco",
                displayName = "Tobacco / Smoking",
                category = "Substances",
                iconName = "smoking_rooms",
                defaultNote = "Quitting cigarettes/tobacco for better lung health."
            ),
            PresetSubstance(
                id = "vaping",
                displayName = "Vaping / E-Cigarettes",
                category = "Substances",
                iconName = "air",
                defaultNote = "Freeing myself from e-cigarettes and nicotine vapor."
            ),
            PresetSubstance(
                id = "cannabis",
                displayName = "Cannabis / Marijuana",
                category = "Substances",
                iconName = "eco",
                defaultNote = "Choosing mental clarity without cannabis."
            ),
            PresetSubstance(
                id = "narcotics",
                displayName = "Narcotics / Opioids",
                category = "Substances",
                iconName = "medication",
                defaultNote = "Reclaiming my body and life from opioids/narcotics."
            ),
            PresetSubstance(
                id = "gambling",
                displayName = "Gambling",
                category = "Behavioral",
                iconName = "casino",
                defaultNote = "Stopping betting to rebuild financial security and peace."
            ),
            PresetSubstance(
                id = "caffeine",
                displayName = "Caffeine / Energy Drinks",
                category = "Substances",
                iconName = "local_cafe",
                defaultNote = "Reducing caffeine intake for better sleep and calm."
            ),
            PresetSubstance(
                id = "sugar",
                displayName = "Sugar / Junk Food",
                category = "Habits",
                iconName = "fastfood",
                defaultNote = "Cutting out processed sugar and compulsive junk food."
            ),
            PresetSubstance(
                id = "social_media",
                displayName = "Social Media / Screen Time",
                category = "Digital",
                iconName = "smartphone",
                defaultNote = "Unplugging to be present in real life."
            ),
            PresetSubstance(
                id = "pornography",
                displayName = "Pornography / Adult Content",
                category = "Behavioral",
                iconName = "visibility_off",
                defaultNote = "Building healthy relationships and focus."
            ),
            PresetSubstance(
                id = "shopping",
                displayName = "Shopping / Compulsive Buying",
                category = "Behavioral",
                iconName = "shopping_cart",
                defaultNote = "Practicing mindful spending and financial freedom."
            ),
            PresetSubstance(
                id = "prescription_meds",
                displayName = "Prescription Medications",
                category = "Substances",
                iconName = "medical_services",
                defaultNote = "Managing prescription use with medical oversight."
            ),
            PresetSubstance(
                id = "video_games",
                displayName = "Video Games / Gaming",
                category = "Digital",
                iconName = "sports_esports",
                defaultNote = "Limiting excessive gaming to focus on personal goals."
            ),
            PresetSubstance(
                id = "custom",
                displayName = "Custom Substance / Habit",
                category = "Custom",
                iconName = "tune",
                defaultNote = "Personal tracking goal."
            )
        )

        fun getIconForSubstance(iconName: String, category: String): ImageVector {
            return when (iconName) {
                "local_bar" -> Icons.Rounded.LocalBar
                "smoking_rooms" -> Icons.Rounded.SmokingRooms
                "air" -> Icons.Rounded.Air
                "eco" -> Icons.Rounded.Eco
                "medication" -> Icons.Rounded.Medication
                "casino" -> Icons.Rounded.Casino
                "local_cafe" -> Icons.Rounded.LocalCafe
                "fastfood" -> Icons.Rounded.Fastfood
                "smartphone" -> Icons.Rounded.Smartphone
                "visibility_off" -> Icons.Rounded.VisibilityOff
                "shopping_cart" -> Icons.Rounded.ShoppingCart
                "medical_services" -> Icons.Rounded.MedicalServices
                "sports_esports" -> Icons.Rounded.SportsEsports
                "tune" -> Icons.Rounded.Tune
                else -> when (category.lowercase()) {
                    "substances" -> Icons.Rounded.Medication
                    "behavioral" -> Icons.Rounded.Casino
                    "digital" -> Icons.Rounded.Smartphone
                    "habits" -> Icons.Rounded.Fastfood
                    else -> Icons.Rounded.Tune
                }
            }
        }
    }
}
