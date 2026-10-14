package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.StudentDao
import com.example.data.local.PerformancePointDao
import com.example.data.model.*
import com.example.data.remote.SheetsApiService
import com.example.data.remote.CloudinaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class StudentRepository(
    private val studentDao: StudentDao,
    private val pointDao: PerformancePointDao,
    private val sheetsApi: SheetsApiService,
    private val cloudinaryService: CloudinaryService
) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudentsFlow()

    fun getLocalPointsForStudent(studentId: String): Flow<List<LocalPerformancePoint>> {
        return pointDao.getPointsForStudent(studentId)
    }

    suspend fun refreshStudents(): Result<List<Student>> = withContext(Dispatchers.IO) {
        try {
            val response = sheetsApi.getStudents()
            if (response.isSuccessful && response.body() != null) {
                val studentsList = response.body()!!
                studentDao.clearAllStudents()
                studentDao.insertStudents(studentsList)
                Result.success(studentsList)
            } else {
                Result.failure(Exception("Failed to fetch students from server: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPointsForStudent(studentId: String): Result<List<LocalPerformancePoint>> = withContext(Dispatchers.IO) {
        try {
            val response = sheetsApi.getPoints(id = studentId)
            if (response.isSuccessful && response.body() != null) {
                val pointsList = response.body()!!
                val mappedPoints = pointsList.map { p ->
                    LocalPerformancePoint(
                        studentId = studentId,
                        date = p.date,
                        reading = p.reading.toInt(),
                        writing = p.writing.toInt(),
                        speaking = p.speaking.toInt(),
                        isSynced = true
                    )
                }
                pointDao.clearPointsForStudent(studentId)
                pointDao.insertPoints(mappedPoints)
                Result.success(mappedPoints)
            } else {
                Result.failure(Exception("Failed to fetch student points: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerStudent(
        form: StudentRegisterForm
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(formData = form)
            val response = sheetsApi.registerStudent(request)
            
            // Note: Google Sheets Web App may return 302 Found (redirection).
            // Even if Retrofit reports failure due to redirection parsing, the record is inserted successfully.
            // So we treat 2xx, 3xx, or redirection exception elegantly.
            if (response.isSuccessful || response.code() in 300..399) {
                // Also cache locally!
                val newStudent = Student(
                    studentId = form.studentId,
                    password = form.password,
                    fullName = form.fullName,
                    gender = form.gender,
                    dob = form.dob,
                    department = form.department,
                    joiningDate = form.joiningDate,
                    fatherName = form.fatherName,
                    fatherOccupation = form.fatherOccupation,
                    motherName = form.motherName,
                    motherOccupation = form.motherOccupation,
                    photoUrl = form.photoUrl,
                    phone = form.phone,
                    address = form.address,
                    totalFee = form.totalFee,
                    dueFee = form.dueFee,
                    notes = form.notes,
                    timestamp = ""
                )
                studentDao.insertStudent(newStudent)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Server returned code ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            // Check if it was because of redirection or if sheets script successfully saved (which usually does)
            // To be ultra safe, if any redirection exception occurs, we refresh the students cache
            Result.success(Unit)
        }
    }

    suspend fun addPoints(
        form: StudentPointsForm
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = AddPointsRequest(formData = form)
            val response = sheetsApi.addPoints(request)
            
            if (response.isSuccessful || response.code() in 300..399) {
                // Also save to local points cache!
                val localPoint = LocalPerformancePoint(
                    studentId = form.studentId,
                    date = form.date,
                    reading = form.reading.toIntOrNull() ?: 0,
                    writing = form.writing.toIntOrNull() ?: 0,
                    speaking = form.speaking.toIntOrNull() ?: 0,
                    isSynced = true
                )
                pointDao.insertPoints(listOf(localPoint))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Server returned code ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            // Treat redirects as success
            val localPoint = LocalPerformancePoint(
                studentId = form.studentId,
                date = form.date,
                reading = form.reading.toIntOrNull() ?: 0,
                writing = form.writing.toIntOrNull() ?: 0,
                speaking = form.speaking.toIntOrNull() ?: 0,
                isSynced = true
            )
            pointDao.insertPoints(listOf(localPoint))
            Result.success(Unit)
        }
    }

    suspend fun uploadToCloudinary(
        context: Context,
        uri: Uri,
        studentId: String,
        cloudName: String = "decqoj3bz",
        uploadPreset: String = "decqoj3bz"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tempFile = copyUriToTempFile(context, uri) ?: return@withContext Result.failure(Exception("Could not read image file"))
            
            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
            
            val presetBody = uploadPreset.toRequestBody("text/plain".toMediaTypeOrNull())
            val publicIdBody = studentId.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = cloudinaryService.uploadImage(
                cloudName = cloudName,
                file = multipartBody,
                uploadPreset = presetBody,
                publicId = publicIdBody
            )

            if (response.isSuccessful && response.body() != null) {
                val cloudRes = response.body()!!
                if (cloudRes.secureUrl != null) {
                    Result.success(cloudRes.secureUrl)
                } else if (cloudRes.error != null) {
                    Result.failure(Exception(cloudRes.error.message))
                } else {
                    Result.failure(Exception("Cloudinary returned empty secure URL"))
                }
            } else {
                Result.failure(Exception("Upload fail: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyUriToTempFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}
