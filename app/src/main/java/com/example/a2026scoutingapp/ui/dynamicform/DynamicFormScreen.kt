package com.example.a2026scoutingapp.ui.dynamicform

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.a2026scoutingapp.ui.start.Alliance
import com.example.a2026scoutingapp.ui.theme.SegUnselectedBg
import com.example.a2026scoutingapp.ui.theme.SegUnselectedBorder
import com.example.a2026scoutingapp.ui.theme.SegUnselectedText
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

@Composable
fun DynamicFormScreen(
    layoutJson: JSONObject,
    headerLine: String,
    scoutName: String,
    alliance: Alliance,
    position: Int,
    matchType: String,
    matchNumber: Int,
    teamNumber: Int,
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    val values: SnapshotStateMap<String, Any> =
        remember { ValueStore.createWithDefaults(layoutJson) }

    val categories = layoutJson.getJSONArray("categories")

    Column(Modifier.fillMaxSize()) {
        MatchHeaderBar(
            scoutName = scoutName,
            alliance = alliance,
            position = position,
            matchType = matchType,
            matchNumber = matchNumber,
            teamNumber = teamNumber,
            onBack = onBack
        )


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            for (i in 0 until categories.length()) {
                val category = categories.getJSONObject(i)
                item {
                    CategoryCard(category)
                    {
                        val rows = category.getJSONArray("rows")
                        for (r in 0 until rows.length()) {
                            RowLayoutRow(rows.getJSONObject(r), values)
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        val responses = JSONObject(ValueStore.exportAsJson(values))
                        val full = JSONObject()
                            .put("header", headerLine)
                            .put("responses", responses)

                        onSave(full.toString(2))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SegUnselectedBg,
                        contentColor = SegUnselectedText,
                        disabledContainerColor = SegUnselectedBg,
                        disabledContentColor = SegUnselectedText.copy(alpha = 0.4f)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(SegUnselectedBorder)
                    )
                ) {
                    Text("Save Match")
                }
            }
        }
    }
}

@Composable
private fun MatchHeaderBar(
    scoutName: String,
    alliance: Alliance,
    position: Int,
    matchType: String,
    matchNumber: Int,
    teamNumber: Int,
    onBack: () -> Unit
) {
    val bg = if (alliance == Alliance.RED)
        Color(0xFFDC2626) else Color(0xFF2563EB)

    Surface(color = bg) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(8.dp),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(Modifier.width(8.dp))

            Column(Modifier.weight(1f)) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        scoutName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                    Text(
                        "${matchType.replaceFirstChar { it.uppercase() }} $matchNumber",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }

                Text(
                    "${alliance.name.lowercase().replaceFirstChar { it.uppercase() }} $position",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )

                Text(
                    "Team $teamNumber",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}


@Composable
private fun CategoryCard(
    category: JSONObject,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                category.optString("title", "Section"),
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

@Composable
private fun RowLayoutRow(
    row: JSONObject,
    values: SnapshotStateMap<String, Any>
) {
    val widgets = row.getJSONArray("widgets")

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (i in 0 until widgets.length()) {
            Box(Modifier.weight(1f)) {
                WidgetRenderer(widgets.getJSONObject(i), values)
            }
        }
    }
}


@Composable
private fun WidgetRenderer(
    widget: JSONObject,
    values: SnapshotStateMap<String, Any>
) {
    when (widget.getString("type")) {
        "checkbox" -> CheckboxWidget(widget, values)
        "counter" -> CounterWidget(widget, values)
        "text" -> TextWidget(widget, values)
        "dropdown" -> DropdownWidget(widget, values)
        "slider" -> SliderWidget(widget, values)
        else -> Text("Unknown widget")
    }
}

@Composable
private fun CheckboxWidget(w: JSONObject, values: SnapshotStateMap<String, Any>) {
    val id = w.getString("id")
    val label = w.getString("label")
    val hint = w.optString("hint").takeIf { it.isNotBlank() }

    Surface(
        shape = MaterialTheme.shapes.large,
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = if (hint == null)
                Alignment.CenterVertically else Alignment.Top
        ) {
            Checkbox(
                checked = values.bool(id),
                onCheckedChange = { values.setBool(id, it) }
            )

            Spacer(Modifier.width(8.dp))

            if (hint == null) {
                Text(
                    label,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Column {
                    Text(label)
                    Text(
                        hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CounterWidget(w: JSONObject, values: SnapshotStateMap<String, Any>) {
    val id = w.getString("id")
    val label = w.getString("label")
    val hint = w.optString("hint").takeIf { it.isNotBlank() }
    val min = w.optInt("min", 0)
    val max = w.optInt("max", 999)
    val v = values.int(id)

    Surface(
        shape = MaterialTheme.shapes.large,
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                if (hint == null) {
                    Text(label, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                } else {
                    Text(label)
                    Text(
                        hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = {
                    values.setInt(id, (v - 1).coerceAtLeast(min))
                }) { Text("−") }

                Text(
                    v.toString(),
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )

                OutlinedButton(onClick = {
                    values.setInt(id, (v + 1).coerceAtMost(max))
                }) { Text("+") }
            }
        }
    }
}

@Composable
private fun TextWidget(w: JSONObject, values: SnapshotStateMap<String, Any>) {
    val id = w.getString("id")
    val label = w.getString("label")
    val multiline = w.optBoolean("multiline", false)
    val minLines = if (multiline) w.optInt("minLines", 3) else 1

    OutlinedTextField(
        value = values.string(id),
        onValueChange = { values.setString(id, it) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = !multiline,
        minLines = minLines
    )
}

@Composable
private fun DropdownWidget(w: JSONObject, values: SnapshotStateMap<String, Any>) {
    val id = w.getString("id")
    val label = w.getString("label")
    val options = w.getJSONArray("options")
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label)
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(values.string(id).ifBlank { "Select…" })
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (i in 0 until options.length()) {
                val opt = options.getString(i)
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        values.setString(id, opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SliderWidget(w: JSONObject, values: SnapshotStateMap<String, Any>) {
    val id = w.getString("id")
    val label = w.getString("label")
    val min = w.optDouble("sliderMin", 0.0).toFloat()
    val max = w.optDouble("sliderMax", 10.0).toFloat()
    val v = values.float(id).coerceIn(min, max)

    Column {
        Text("$label: ${v.roundToInt()}")
        Slider(
            value = v,
            onValueChange = { values.setFloat(id, it) },
            valueRange = min..max
        )
    }
}
