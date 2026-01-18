package com.example.a2026scoutingapp.ui.start

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.a2026scoutingapp.ui.theme.*

enum class Alliance { RED, BLUE }

val Positions = listOf(1, 2, 3)
val MatchTypes = listOf("qualification", "semifinals", "finals", "practice", "other")

data class StartPayload(
    val alliance: Alliance,
    val position: Int,
    val scoutName: String,
    val matchType: String,
    val matchNumber: Int,
    val teamNumber: Int
)

data class StartFormState(
    val alliance: Alliance = Alliance.RED,
    val position: Int = 1,
    val scoutName: String = "",
    val matchType: String = "qualification",
    val matchNumberText: String = "",
    val teamNumberText: String = ""
)

data class StartFormErrors(
    val scoutName: String? = null,
    val matchType: String? = null,
    val matchNumber: String? = null,
    val teamNumber: String? = null
) {
    val hasAny: Boolean
        get() = listOf(scoutName, matchType, matchNumber, teamNumber).any { it != null }
}

fun validate(state: StartFormState): StartFormErrors {
    fun validatePositiveInt(text: String, label: String): String? {
        val s = text.trim()
        return when {
            s.isEmpty() -> "Enter a $label."
            s.toIntOrNull() == null -> "$label must be a whole number."
            s.toInt() < 1 -> "$label must be at least 1."
            else -> null
        }
    }

    return StartFormErrors(
        scoutName = if (state.scoutName.trim().isEmpty()) "Enter your name." else null,
        matchType = if (state.matchType !in MatchTypes) "Invalid match type." else null,
        matchNumber = validatePositiveInt(state.matchNumberText, "match number"),
        teamNumber = validatePositiveInt(state.teamNumberText, "team number")
    )
}

fun toPayloadOrNull(state: StartFormState, errors: StartFormErrors): StartPayload? {
    if (errors.hasAny) return null
    val mn = state.matchNumberText.trim().toIntOrNull() ?: return null
    val tn = state.teamNumberText.trim().toIntOrNull() ?: return null
    return StartPayload(
        alliance = state.alliance,
        position = state.position,
        scoutName = state.scoutName.trim(),
        matchType = state.matchType,
        matchNumber = mn,
        teamNumber = tn
    )
}

@Composable
fun StartScreen(
    modifier: Modifier = Modifier,
    onStart: (StartPayload) -> Unit,
    onViewSaved: () -> Unit
) {
    var state by remember { mutableStateOf(StartFormState()) }
    val errors = remember(state) { validate(state) }
    val canStart = !errors.hasAny

    Column(modifier.fillMaxSize()) {
        StartHeaderBar()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 0.dp,
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Alliance & Position", style = MaterialTheme.typography.titleMedium)

                        AlliancePicker(
                            value = state.alliance,
                            onChange = { state = state.copy(alliance = it) }
                        )

                        PositionPicker(
                            value = state.position,
                            onChange = { state = state.copy(position = it) }
                        )
                    }
                }
            }

            item {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 0.dp,
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Match Setup", style = MaterialTheme.typography.titleMedium)

                        LabeledTextField(
                            label = "Scout Name",
                            value = state.scoutName,
                            onValueChange = { state = state.copy(scoutName = it) },
                            placeholder = "Enter your name",
                            errorText = errors.scoutName
                        )

                        MatchTypeDropdownNonExperimental(
                            label = "Match Type",
                            options = MatchTypes,
                            value = state.matchType,
                            onChange = { state = state.copy(matchType = it) },
                            errorText = errors.matchType
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NumberField(
                                modifier = Modifier.weight(1f),
                                label = "Match Number",
                                value = state.matchNumberText,
                                onValueChange = { state = state.copy(matchNumberText = it.onlyDigits()) },
                                placeholder = "e.g. 12",
                                errorText = errors.matchNumber
                            )

                            NumberField(
                                modifier = Modifier.weight(1f),
                                label = "Team Number",
                                value = state.teamNumberText,
                                onValueChange = { state = state.copy(teamNumberText = it.onlyDigits()) },
                                placeholder = "e.g. 254",
                                errorText = errors.teamNumber
                            )
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    enabled = canStart,
                    onClick = {
                        val payload = toPayloadOrNull(state, errors) ?: return@OutlinedButton
                        onStart(payload)
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
                    Text("Start")
                }
            }

            item {
                OutlinedButton(
                    onClick = onViewSaved,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SegUnselectedBg,
                        contentColor = SegUnselectedText
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(SegUnselectedBorder)
                    )
                ) {
                    Text("View Saved Matches")
                }
            }

            if (!canStart) {
                item {
                    Text(
                        "Fill out all fields to start.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StartHeaderBar() {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Match Start",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Enter details before you begin scouting.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AlliancePicker(
    value: Alliance,
    onChange: (Alliance) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text("Alliance", style = MaterialTheme.typography.labelLarge)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SegmentedChoiceButton(
                text = "Red",
                selected = value == Alliance.RED,
                onClick = { onChange(Alliance.RED) },
                selectedKind = Alliance.RED,
                modifier = Modifier.weight(1f)
            )

            SegmentedChoiceButton(
                text = "Blue",
                selected = value == Alliance.BLUE,
                onClick = { onChange(Alliance.BLUE) },
                selectedKind = Alliance.BLUE,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PositionPicker(
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text("Position (robot in alliance)", style = MaterialTheme.typography.labelLarge)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Positions.forEach { p ->
                SegmentedChoiceButton(
                    text = p.toString(),
                    selected = value == p,
                    onClick = { onChange(p) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SegmentedChoiceButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedKind: Alliance? = null
) {
    val contentPadding = PaddingValues(vertical = 12.dp)

    val (bg, content, border) = if (!selected) {
        Triple(SegUnselectedBg, SegUnselectedText, SegUnselectedBorder)
    } else {
        when (selectedKind) {
            Alliance.RED -> Triple(SegRedBg, SegAllianceText, SegRedBorder)
            Alliance.BLUE -> Triple(SegBlueBg, SegAllianceText, SegBlueBorder)
            null -> Triple(SegSelectedNeutralBg, SegSelectedNeutralText, SegSelectedNeutralBg)
        }
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = contentPadding,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = bg,
            contentColor = content
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = SolidColor(border)
        )
    ) {
        Text(text)
    }
}

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorText: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            isError = errorText != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (errorText != null) {
            Text(
                errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorText: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = errorText != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (errorText != null) {
            Text(
                errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun MatchTypeDropdownNonExperimental(
    label: String,
    options: List<String>,
    value: String,
    onChange: (String) -> Unit,
    errorText: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge)

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(value.replaceFirstChar { it.uppercase() })
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt.replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            onChange(opt)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (errorText != null) {
            Text(
                errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun String.onlyDigits(): String = filter { it.isDigit() }
