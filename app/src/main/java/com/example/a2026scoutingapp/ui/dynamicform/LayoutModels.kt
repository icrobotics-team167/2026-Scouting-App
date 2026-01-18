package com.example.a2026scoutingapp.ui.dynamicform

data class ScreenLayout(
    val screenId: String,
    val title: String,
    val categories: List<CategoryLayout>
)

data class CategoryLayout(
    val id: String,
    val title: String,
    val rows: List<RowLayout>
)

data class RowLayout(
    val widgets: List<WidgetLayout>
)

data class WidgetLayout(
    val type: WidgetType,
    val id: String,
    val label: String,

    val min: Int? = null,
    val max: Int? = null,
    val options: List<String>? = null,
    val multiline: Boolean? = null,
    val minLines: Int? = null,
    val default: Any? = null
)

enum class WidgetType {
    CHECKBOX,
    COUNTER,
    TEXT,
    DROPDOWN
}
