package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.StudentDatabase
import com.example.data.model.LocalPerformancePoint
import com.example.data.model.Student
import com.example.data.model.StudentPointsForm
import com.example.data.model.StudentRegisterForm
import com.example.data.remote.CloudinaryService
import com.example.data.remote.SheetsApiService
import com.example.data.repository.StudentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UploadState {
    object Idle : UploadState
    object Loading : UploadState
    data class Success(val url: String) : UploadState
    data class Error(val message: String) : UploadState
}

sealed interface SubmitState {
    object Idle : SubmitState
    object Submitting : SubmitState
    object Success : SubmitState
    data class Error(val message: String) : SubmitState
}

class StudentViewModel(private val repository: StudentRepository) : ViewModel() {

    // Students state
    val studentsList: StateFlow<List<Student>> = repository.allStudents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoadingStudents = MutableStateFlow(false)
    val isLoadingStudents: StateFlow<Boolean> = _isLoadingStudents.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    // Charting state
    private val _selectedStudentIdForChart = MutableStateFlow<String?>(null)
    val selectedStudentIdForChart: StateFlow<String?> = _selectedStudentIdForChart.asStateFlow()

    val chartStudentPoints: StateFlow<List<LocalPerformancePoint>> = _selectedStudentIdForChart
        .flatMapLatest { studentId ->
            if (studentId != null) {
                repository.getLocalPointsForStudent(studentId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoadingPoints = MutableStateFlow(false)
    val isLoadingPoints: StateFlow<Boolean> = _isLoadingPoints.asStateFlow()

    // Photo Uploading state
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    // Save states
    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    private val _pointsSubmitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val pointsSubmitState: StateFlow<SubmitState> = _pointsSubmitState.asStateFlow()

    init {
        // Initial sync of students list
        refreshStudents()
    }

    fun clearStatusMessage() {
        _statusMessage.value = ""
    }

    fun refreshStudents() {
        viewModelScope.launch {
            _isLoadingStudents.value = true
            val result = repository.refreshStudents()
            _isLoadingStudents.value = false
            result.onSuccess {
                _statusMessage.value = "Students list synchronized successfully!"
            }.onFailure {
                _statusMessage.value = "Using offline data: ${it.localizedMessage}"
            }
        }
    }

    fun selectStudentForChart(studentId: String) {
        _selectedStudentIdForChart.value = studentId
        loadPointsForStudent(studentId)
    }

    fun loadPointsForStudent(studentId: String) {
        viewModelScope.launch {
            _isLoadingPoints.value = true
            val result = repository.fetchPointsForStudent(studentId)
            _isLoadingPoints.value = false
            result.onFailure {
                _statusMessage.value = "Failed to sync latest graphics: showing local values"
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }

    fun uploadPhoto(context: Context, uri: Uri, studentId: String) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            val trimmedId = studentId.trim().ifEmpty { "student_${System.currentTimeMillis()}" }
            val result = repository.uploadToCloudinary(context, uri, trimmedId)
            result.onSuccess { secureUrl ->
                _uploadState.value = UploadState.Success(secureUrl)
            }.onFailure {
                _uploadState.value = UploadState.Error(it.localizedMessage ?: "Unknown upload issue")
            }
        }
    }

    fun registerStudent(form: StudentRegisterForm, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Submitting
            val result = repository.registerStudent(form)
            result.onSuccess {
                _submitState.value = SubmitState.Success
                _statusMessage.value = "Student registered successfully!"
                refreshStudents() // Fetch updated lists
                onSuccess()
            }.onFailure {
                _submitState.value = SubmitState.Error(it.localizedMessage ?: "Failed to submit")
                _statusMessage.value = "Registration error: ${it.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun submitPoints(form: StudentPointsForm, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _pointsSubmitState.value = SubmitState.Submitting
            val result = repository.addPoints(form)
            result.onSuccess {
                _pointsSubmitState.value = SubmitState.Success
                _statusMessage.value = "Performance scorecard saved!"
                _selectedStudentIdForChart.value?.let { currentId ->
                    if (currentId == form.studentId) {
                        loadPointsForStudent(currentId)
                    }
                }
                onSuccess()
            }.onFailure {
                _pointsSubmitState.value = SubmitState.Error(it.localizedMessage ?: "Failed to submit scorecard")
                _statusMessage.value = "Submission failure: ${it.localizedMessage}"
            }
        }
    }

    fun clearSubmitStates() {
        _submitState.value = SubmitState.Idle
        _pointsSubmitState.value = SubmitState.Idle
    }

    // Factory Class to provide StudentRepository dependency
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
                val db = StudentDatabase.getDatabase(context)
                val sheetsApi = SheetsApiService.create()
                val cloudinaryService = CloudinaryService.create()
                val repository = StudentRepository(
                    studentDao = db.studentDao(),
                    pointDao = db.performancePointDao(),
                    sheetsApi = sheetsApi,
                    cloudinaryService = cloudinaryService
                )
                @Suppress("UNCHECKED_CAST")
                return StudentViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
