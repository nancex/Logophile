package me.nancex.logophile.ui.theme

enum class TimeRange(val key: String, val days: Int?) {
    ONE_DAY("1d", 1),
    THREE_DAYS("3d", 3),
    ONE_WEEK("1w", 7),
    ONE_MONTH("1m", 30),
    THREE_MONTHS("3m", 90),
    ALL("all", null)
}