package me.nancex.logophile.ui.theme

enum class AppTheme(val labelKey: String) {
    LIGHT("theme_light"),
    DARK("theme_dark"),
    OCEAN("theme_ocean"),
    ROSE("theme_rose"),
    FOREST("theme_forest")
}

enum class AppFont(val labelKey: String, val displayName: String) {
    DEFAULT("font_default", "Default"),
    SERIF("font_serif", "Serif"),
    MONOSPACE("font_monospace", "Monospace"),
    EIGHT_BIT("font_8bit", "8bitoperator JVE")
}

enum class AppLanguage(val labelKey: String, val code: String) {
    CHINESE("lang_chinese", "zh"),
    ENGLISH("lang_english", "en")
}
