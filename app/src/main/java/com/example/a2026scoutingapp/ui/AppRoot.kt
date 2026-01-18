package com.example.a2026scoutingapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.example.a2026scoutingapp.data.ExportUtils
import com.example.a2026scoutingapp.data.MatchStorage
import com.example.a2026scoutingapp.ui.dynamicform.DynamicFormScreen
import com.example.a2026scoutingapp.ui.dynamicform.LayoutLoader
import com.example.a2026scoutingapp.ui.saved.SavedMatchesScreen
import com.example.a2026scoutingapp.ui.start.Alliance
import com.example.a2026scoutingapp.ui.start.StartPayload
import com.example.a2026scoutingapp.ui.start.StartScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Page { START, MATCH, SAVED }

@Composable
fun AppRoot() {
    // Persist these across rotation
    var lastAllianceName by rememberSaveable { mutableStateOf(Alliance.RED.name) }
    var lastPosition by rememberSaveable { mutableStateOf(1) }

    // Save current page as String name (safe)
    var pageName by rememberSaveable { mutableStateOf(Page.START.name) }
    val page: Page = remember(pageName) { Page.valueOf(pageName) }
    fun setPage(p: Page) { pageName = p.name }

    // Save StartPayload as primitives (safe)
    var hasStart by rememberSaveable { mutableStateOf(false) }
    var savedScoutName by rememberSaveable { mutableStateOf("") }
    var savedAllianceName by rememberSaveable { mutableStateOf(Alliance.RED.name) }
    var savedPosition by rememberSaveable { mutableStateOf(1) }
    var savedMatchType by rememberSaveable { mutableStateOf("qualification") }
    var savedMatchNumber by rememberSaveable { mutableStateOf(1) }
    var savedTeamNumber by rememberSaveable { mutableStateOf(1) }

    val startPayload: StartPayload? = if (!hasStart) null else StartPayload(
        alliance = Alliance.valueOf(savedAllianceName),
        position = savedPosition,
        scoutName = savedScoutName,
        matchType = savedMatchType,
        matchNumber = savedMatchNumber,
        teamNumber = savedTeamNumber
    )

    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            ExportUtils.exportToUsb(context, uri)
        }
    }

    var savedFiles by remember { mutableStateOf(MatchStorage.list(context)) }

    fun refreshSaved() {
        savedFiles = MatchStorage.list(context)
    }

    when (page) {
        Page.START -> StartScreen(
            initialAlliance = Alliance.valueOf(lastAllianceName),
            initialPosition = lastPosition,
            onAllianceChange = { a -> lastAllianceName = a.name },
            onPositionChange = { p -> lastPosition = p },
            onStart = { payload ->
                hasStart = true
                savedScoutName = payload.scoutName
                savedAllianceName = payload.alliance.name
                savedPosition = payload.position
                savedMatchType = payload.matchType
                savedMatchNumber = payload.matchNumber
                savedTeamNumber = payload.teamNumber

                setPage(Page.MATCH)
            },
            onViewSaved = {
                refreshSaved()
                setPage(Page.SAVED)
            }
        )

        Page.MATCH -> {
            val start = requireNotNull(startPayload)

            val layoutJson = remember {
                LayoutLoader.loadJsonObjectFromAssets(context, "match_layout.json")
            }

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
                onBack = { setPage(Page.START) },
                onSave = { json ->
                    MatchStorage.save(context, header, json)
                    refreshSaved()
                    setPage(Page.SAVED)
                }
            )
        }

        Page.SAVED -> SavedMatchesScreen(
            files = savedFiles,
            onRefresh = { refreshSaved() },
            onBack = { setPage(Page.START) },
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
