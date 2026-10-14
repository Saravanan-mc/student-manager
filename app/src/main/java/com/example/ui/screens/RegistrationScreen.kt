package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.StudentRegisterForm
import com.example.ui.viewmodel.StudentViewModel
import com.example.ui.viewmodel.UploadState
import com.example.ui.viewmodel.SubmitState
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form inputs state
    var studentId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var dob by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var joiningDate by remember { mutableStateOf("") }

    var fatherName by remember { mutableStateOf("") }
    var fatherOccupation by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var motherOccupation by remember { mutableStateOf("") }

    var phone by remember { mutableStateOf("") }
    var totalFee by remember { mutableStateOf("") }
    var dueFee by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var notes by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf("") }

    val uploadState by viewModel.uploadState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    // Date Picker helpers
    val calendar = Calendar.getInstance()
    val dobPickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            dob = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val joinPickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            joiningDate = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Image Picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                uploadedImageUrl = "" // clear previous uploads
                viewModel.resetUploadState()
            }
        }
    )

    // Sync upload results
    LaunchedEffect(uploadState) {
        if (uploadState is UploadState.Success) {
            uploadedImageUrl = (uploadState as UploadState.Success).url
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Heading Header Banner
        Text(
            text = "Student Registration",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Create a secure account and profile for Sheets database.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Section 1: Credentials & Primary Details
        FormCard(title = "👤 Primary Credentials", icon = Icons.Default.Person) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Student ID *") },
                    placeholder = { Text("e.g. STU-2026-001") },
                    leadingIcon = { Icon(Icons.Default.AssignmentInd, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("student_id_input"),
                    singleLine = true
                )

                var passwordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password *") },
                    placeholder = { Text("Secure access credential") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(image, contentDescription = null)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().testTag("password_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    placeholder = { Text("Enter student name") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("fullname_input"),
                    singleLine = true
                )

                // Gender selector dropdown/row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Gender Selection",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Male", "Female", "Other").forEach { choice ->
                            val isSelected = gender == choice
                            FilterChip(
                                selected = isSelected,
                                onClick = { gender = choice },
                                label = { Text(choice) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f).testTag("gender_chip_$choice")
                            )
                        }
                    }
                }

                // DOB Picker
                OutlinedTextField(
                    value = dob,
                    onValueChange = {},
                    label = { Text("Date of Birth") },
                    placeholder = { Text("Select Date") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dobPickerDialog.show() }
                        .testTag("dob_input"),
                    enabled = false, // delegate focus to clickable box
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Education Level / Department") },
                    placeholder = { Text("e.g. Computer Science") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("department_input"),
                    singleLine = true
                )

                // Joining Date Picker
                OutlinedTextField(
                    value = joiningDate,
                    onValueChange = {},
                    label = { Text("Joining Date") },
                    placeholder = { Text("Select Date") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { joinPickerDialog.show() },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // Section 2: Parent Information
        FormCard(title = "👨‍👩‍👧 Parent Details", icon = Icons.Default.People) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("Father's Name") },
                    placeholder = { Text("Enter father's name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = fatherOccupation,
                    onValueChange = { fatherOccupation = it },
                    label = { Text("Father's Occupation") },
                    placeholder = { Text("Enter father's job") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                OutlinedTextField(
                    value = motherName,
                    onValueChange = { motherName = it },
                    label = { Text("Mother's Name") },
                    placeholder = { Text("Enter mother's name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = motherOccupation,
                    onValueChange = { motherOccupation = it },
                    label = { Text("Mother's Occupation") },
                    placeholder = { Text("Enter mother's job") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Section 3: Contact & Expenses
        FormCard(title = "📞 Contact & Fee Details", icon = Icons.Default.Payment) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+1 (555) 000-0000") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = totalFee,
                    onValueChange = { totalFee = it },
                    label = { Text("Total Fee ($)") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dueFee,
                    onValueChange = { dueFee = it },
                    label = { Text("Due Fee ($)") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Icon(Icons.Default.MoneyOff, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Residential Address") },
                    placeholder = { Text("Enter physical home address") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        }

        // Section 4: Photo Selection
        FormCard(title = "📷 Profile Photo & Bio Notes", icon = Icons.Default.PhotoLibrary) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Selection Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        .testTag("photo_picker_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = selectedImageUri),
                            contentDescription = "Selected Local Image Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Selection Overlay icon
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text(
                                text = "Click to change photo",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else if (uploadedImageUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = uploadedImageUrl),
                            contentDescription = "Uploaded Online Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✅ Uploaded to Cloud!",
                                color = Color.Green,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Pick Profile Photograph",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Supports jpg, png, webp assets",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Cloudinary action button
                if (selectedImageUri != null && uploadedImageUrl.isEmpty()) {
                    Button(
                        onClick = {
                            viewModel.uploadPhoto(context, selectedImageUri!!, studentId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth().testTag("upload_cloud_button"),
                        enabled = uploadState !is UploadState.Loading
                    ) {
                        if (uploadState is UploadState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uploading photo...")
                        } else {
                            Icon(Icons.Default.CloudQueue, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload chosen image to Cloud")
                        }
                    }
                }

                // State messages
                when (uploadState) {
                    is UploadState.Error -> {
                        Text(
                            text = "❌ Error: ${(uploadState as UploadState.Error).message}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    is UploadState.Success -> {
                        Text(
                            text = "✅ Image uploaded successfully",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    else -> {}
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Bio / Notes") },
                    placeholder = { Text("Any medical states, special allergy guidelines, or other details...") },
                    modifier = Modifier.fillMaxWidth().testTag("notes_input"),
                    minLines = 3,
                    maxLines = 5
                )
            }
        }

        // Global registration action Button
        Button(
            onClick = {
                if (studentId.trim().isEmpty() || password.trim().isEmpty() || fullName.trim().isEmpty()) {
                    ScaffoldDefaults
                    return@Button
                }
                val form = StudentRegisterForm(
                    studentId = studentId,
                    password = password,
                    fullName = fullName,
                    gender = gender,
                    dob = dob,
                    department = department,
                    joiningDate = joiningDate,
                    fatherName = fatherName,
                    fatherOccupation = fatherOccupation,
                    motherName = motherName,
                    motherOccupation = motherOccupation,
                    phone = phone,
                    address = address,
                    totalFee = totalFee,
                    dueFee = dueFee,
                    notes = notes,
                    photoUrl = uploadedImageUrl
                )
                viewModel.registerStudent(form) {
                    // Reset inputs
                    studentId = ""
                    password = ""
                    fullName = ""
                    gender = "Male"
                    dob = ""
                    department = ""
                    joiningDate = ""
                    fatherName = ""
                    fatherOccupation = ""
                    motherName = ""
                    motherOccupation = ""
                    phone = ""
                    totalFee = ""
                    dueFee = ""
                    address = ""
                    notes = ""
                    selectedImageUri = null
                    uploadedImageUrl = ""
                    viewModel.resetUploadState()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("submit_registration_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = (submitState !is SubmitState.Submitting)
        ) {
            if (submitState is SubmitState.Submitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Saving student record...")
            } else {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Register Record & Save to Sheets", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (submitState is SubmitState.Error) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Saving failure: ${(submitState as SubmitState.Error).message}",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun FormCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            content()
        }
    }
}
