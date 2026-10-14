package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.data.model.StudentPointsForm
import com.example.ui.viewmodel.StudentViewModel
import com.example.ui.viewmodel.SubmitState
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val students by viewModel.studentsList.collectAsState()
    val pointsSubmitState by viewModel.pointsSubmitState.collectAsState()

    // Form states
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var pointsDate by remember { mutableStateOf("") }
    var readingScore by remember { mutableStateOf("") }
    var writingScore by remember { mutableStateOf("") }
    var speakingScore by remember { mutableStateOf("") }

    // Set initial date as today
    LaunchedEffect(Unit) {
        val today = Calendar.getInstance()
        pointsDate = "${today.get(Calendar.YEAR)}-${String.format("%02d", today.get(Calendar.MONTH) + 1)}-${String.format("%02d", today.get(Calendar.DAY_OF_MONTH))}"
    }

    // Date picker dialog helper
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            pointsDate = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Dropdown helpers
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Heading Banner
        Text(
            text = "Daily Scorecard",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Assess day-to-day reading, writing, and speaking competencies.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Main form card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header inside form card
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Grade,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Competency Evaluation Form",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Student Selection Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedStudent?.fullName ?: "Select Student...",
                        onValueChange = {},
                        label = { Text("Select Student *") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownExpanded = true }
                            .testTag("student_select_dropdown"),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (students.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("(No Registered Students)") },
                                onClick = { isDropdownExpanded = false }
                            )
                        } else {
                            students.forEach { s ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(s.fullName, fontWeight = FontWeight.Bold)
                                            Text(s.studentId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedStudent = s
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Date Picker field
                OutlinedTextField(
                    value = pointsDate,
                    onValueChange = {},
                    label = { Text("Evaluation Date *") },
                    placeholder = { Text("Select date") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .testTag("evaluation_date_input"),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                // Reading Input
                OutlinedTextField(
                    value = readingScore,
                    onValueChange = { readingScore = it },
                    label = { Text("Reading score (0 - 100)") },
                    placeholder = { Text("Enter scores") },
                    leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF1E3C72)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("reading_input")
                )

                // Writing Input
                OutlinedTextField(
                    value = writingScore,
                    onValueChange = { writingScore = it },
                    label = { Text("Writing score (0 - 100)") },
                    placeholder = { Text("Enter scores") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF10B981)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("writing_input")
                )

                // Speaking Input
                OutlinedTextField(
                    value = speakingScore,
                    onValueChange = { speakingScore = it },
                    label = { Text("Speaking score (0 - 100)") },
                    placeholder = { Text("Enter scores") },
                    leadingIcon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color(0xFFF59E0B)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("speaking_input")
                )
            }
        }

        // Action button to trigger Sheets submission
        Button(
            onClick = {
                val student = selectedStudent
                if (student == null) {
                    return@Button
                }
                val rVal = readingScore.toIntOrNull()?.coerceIn(0, 100) ?: 0
                val wVal = writingScore.toIntOrNull()?.coerceIn(0, 100) ?: 0
                val sVal = speakingScore.toIntOrNull()?.coerceIn(0, 100) ?: 0

                val form = StudentPointsForm(
                    studentId = student.studentId,
                    date = pointsDate,
                    reading = rVal.toString(),
                    writing = wVal.toString(),
                    speaking = sVal.toString()
                )
                viewModel.submitPoints(form) {
                    // Reset fields on success
                    readingScore = ""
                    writingScore = ""
                    speakingScore = ""
                    selectedStudent = null
                }
            },
            enabled = (selectedStudent != null && pointsSubmitState !is SubmitState.Submitting),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("submit_scorecard_button")
        ) {
            if (pointsSubmitState is SubmitState.Submitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Submitting scorecard...")
            } else {
                Icon(Icons.Default.Publish, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Publish Scorecard & Sync Sheets", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (pointsSubmitState is SubmitState.Error) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Saving failing: ${(pointsSubmitState as SubmitState.Error).message}",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
