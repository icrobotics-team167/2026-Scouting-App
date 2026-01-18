package com.example.a2026scoutingapp.ui.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.a2026scoutingapp.data.MatchStorage
import java.io.File

private enum class SavedPage { LIST, DETAIL }

@Composable
fun SavedMatchesScreen(
    files: List<File>,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onExport: () -> Unit
) {
    var page by remember { mutableStateOf(SavedPage.LIST) }
    var selected by remember { mutableStateOf<File?>(null) }
    var jsonText by remember { mutableStateOf("") }

    var confirmDeleteAll by remember { mutableStateOf(false) }
    var confirmDeleteOne by remember { mutableStateOf<File?>(null) }

    Column(Modifier.fillMaxSize()) {
        SavedHeaderBar(
            page = page,
            hasFiles = files.isNotEmpty(),
            onBack = onBack,
            onExport = onExport,
            onRefresh = onRefresh,
            onDeleteAll = { confirmDeleteAll = true }
        )

        if (confirmDeleteAll) {
            AlertDialog(
                onDismissRequest = { confirmDeleteAll = false },
                title = { Text("Delete all saved matches?") },
                text = { Text("This will permanently delete every saved JSON file on this device.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            files.forEach { MatchStorage.delete(it) }
                            confirmDeleteAll = false
                            onRefresh()
                        }
                    ) { Text("Delete All") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDeleteAll = false }) { Text("Cancel") }
                }
            )
        }

        val pendingDelete = confirmDeleteOne
        if (pendingDelete != null) {
            AlertDialog(
                onDismissRequest = { confirmDeleteOne = null },
                title = { Text("Delete this match?") },
                text = { Text(pendingDelete.name) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            MatchStorage.delete(pendingDelete)
                            confirmDeleteOne = null
                            if (selected == pendingDelete) {
                                selected = null
                                page = SavedPage.LIST
                            }
                            onRefresh()
                        }
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDeleteOne = null }) { Text("Cancel") }
                }
            )
        }

        when (page) {
            SavedPage.LIST -> {
                if (files.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No saved matches yet.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(files.size) { i ->
                            val f = files[i]
                            SavedFileCard(
                                file = f,
                                onOpen = {
                                    selected = f
                                    jsonText = MatchStorage.read(f)
                                    page = SavedPage.DETAIL
                                },
                                onDelete = { confirmDeleteOne = f }
                            )
                        }
                    }
                }
            }

            SavedPage.DETAIL -> {
                val f = selected
                if (f == null) {
                    page = SavedPage.LIST
                } else {
                    DetailView(
                        file = f,
                        json = jsonText,
                        onBackToList = {
                            selected = null
                            page = SavedPage.LIST
                        },
                        onDelete = { confirmDeleteOne = f }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedHeaderBar(
    page: SavedPage,
    hasFiles: Boolean,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onRefresh: () -> Unit,
    onDeleteAll: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 5.dp)
                .padding(horizontal = 12.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Back",
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = "Saved Matches",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = onExport) { Text("Export All") }
            TextButton(onClick = onRefresh) { Text("Refresh") }

            if (page == SavedPage.LIST && hasFiles) {
                TextButton(onClick = onDeleteAll) { Text("Delete All") }
            }
        }
    }
}

@Composable
private fun SavedFileCard(
    file: File,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpen() }
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Modified: ${file.lastModified()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

@Composable
private fun DetailView(
    file: File,
    json: String,
    onBackToList: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackToList) { Text("← List") }
            TextButton(onClick = onDelete) { Text("Delete") }
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 0.dp,
            border = ButtonDefaults.outlinedButtonBorder,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = json,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
