package com.example.a2026scoutingapp.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.a2026scoutingapp.data.MatchStorage
import com.example.a2026scoutingapp.ui.dynamicform.DynamicFormScreen
import com.example.a2026scoutingapp.ui.dynamicform.LayoutLoader
import com.example.a2026scoutingapp.ui.saved.SavedMatchesScreen
import com.example.a2026scoutingapp.ui.start.Alliance
import com.example.a2026scoutingapp.ui.start.StartPayload
import com.example.a2026scoutingapp.ui.start.StartScreen

private enum class Page { START, MATCH, SAVED }

@Composable
fun AppRoot() {
    var lastAlliance by remember { mutableStateOf(com.example.a2026scoutingapp.ui.start.Alliance.RED) }
    var lastPosition by remember { mutableStateOf(1) }
    var page by remember { mutableStateOf(Page.START) }
    var startPayload by remember { mutableStateOf<StartPayload?>(null) }

    val context = LocalContext.current
    var savedFiles by remember { mutableStateOf(MatchStorage.list(context)) }

    fun refreshSaved() {
        savedFiles = MatchStorage.list(context)
    }

    when (page) {
        Page.START -> StartScreen(
            initialAlliance = lastAlliance,
            initialPosition = lastPosition,
            onAllianceChange = { lastAlliance = it },
            onPositionChange = { lastPosition = it },
            onStart = {
                startPayload = it
                page = Page.MATCH
            },
            onViewSaved = {
                refreshSaved()
                page = Page.SAVED
            }
        )

        Page.MATCH -> {
            val start = requireNotNull(startPayload)
            val layoutJson =
                LayoutLoader.loadJsonObjectFromAssets(context, "match_layout.json")

            val header = headerLine(start)

            DynamicFormScreen(
                layoutJson = layoutJson,
                headerLine = header,
                scoutName = start.scoutName,
                alliance = start.alliance,
                position = start.position,
                matchType = start.matchType,
                matchNumber = start.matchNumber,
                teamNumber = start.teamNumber,
                onBack = { page = Page.START },
                onSave = { json ->
                    MatchStorage.save(context, header, json)
                    refreshSaved()
                    page = Page.SAVED
                }
            )
        }

        Page.SAVED -> SavedMatchesScreen(
            files = savedFiles,
            onRefresh = { refreshSaved() },
            onBack = { page = Page.START }
        )
    }
}

private fun headerLine(s: StartPayload): String {
    val alliance = when (s.alliance) {
        Alliance.RED -> "Red"
        Alliance.BLUE -> "Blue"
    }
    val matchType = s.matchType.replaceFirstChar { it.uppercase() }
    return "${s.scoutName} - $alliance ${s.position} ${s.teamNumber} $matchType ${s.matchNumber}"
}
