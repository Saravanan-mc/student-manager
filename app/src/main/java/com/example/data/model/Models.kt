package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ROOM ENTITIES

@Entity(tableName = "students")
@JsonClass(generateAdapter = true)
data class Student(
    @PrimaryKey
    @Json(name = "Student ID") val studentId: String,
    @Json(name = "Password") val password: String = "",
    @Json(name = "Full Name") val fullName: String = "",
    @Json(name = "Gender") val gender: String = "Male",
    @Json(name = "Date of Birth") val dob: String = "",
    @Json(name = "Department") val department: String = "",
    @Json(name = "Joining Date") val joiningDate: String = "",
    @Json(name = "Father's Name") val fatherName: String = "",
    @Json(name = "Father's Occupation") val fatherOccupation: String = "",
    @Json(name = "Mother's Name") val motherName: String = "",
    @Json(name = "Mother's Occupation") val motherOccupation: String = "",
    @Json(name = "Photo URL") val photoUrl: String = "",
    @Json(name = "Phone Number") val phone: String = "",
    @Json(name = "Address") val address: String = "",
    @Json(name = "Total Fee") val totalFee: String = "0",
    @Json(name = "Due Fee") val dueFee: String = "0",
    @Json(name = "Notes") val notes: String = "",
    @Json(name = "Timestamp") val timestamp: String = ""
)

@Entity(tableName = "student_points")
@JsonClass(generateAdapter = true)
data class LocalPerformancePoint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val date: String,
    val reading: Int,
    val writing: Int,
    val speaking: Int,
    val isSynced: Boolean = true
)

// NETWORK RESPONSE MODELS

@JsonClass(generateAdapter = true)
data class PointResponse(
    @Json(name = "date") val date: String,
    @Json(name = "reading") val reading: Double = 0.0, // using double to prevent parse errors if dynamic type is numeric
    @Json(name = "writing") val writing: Double = 0.0,
    @Json(name = "speaking") val speaking: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class CloudinaryResponse(
    @Json(name = "secure_url") val secureUrl: String? = null,
    @Json(name = "error") val error: CloudinaryError? = null
)

@JsonClass(generateAdapter = true)
data class CloudinaryError(
    @Json(name = "message") val message: String
)

// NETWORK REQUEST PAYLOADS

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "action") val action: String = "register",
    @Json(name = "formData") val formData: StudentRegisterForm
)

@JsonClass(generateAdapter = true)
data class StudentRegisterForm(
    val studentId: String,
    val password: String,
    val fullName: String,
    val gender: String,
    val dob: String,
    val department: String,
    val joiningDate: String,
    val fatherName: String,
    val fatherOccupation: String,
    val motherName: String,
    val motherOccupation: String,
    val phone: String,
    val address: String,
    val totalFee: String,
    val dueFee: String,
    val notes: String,
    val photoUrl: String
)

@JsonClass(generateAdapter = true)
data class AddPointsRequest(
    @Json(name = "action") val action: String = "addPoints",
    @Json(name = "formData") val formData: StudentPointsForm
)

@JsonClass(generateAdapter = true)
data class StudentPointsForm(
    val studentId: String,
    val date: String,
    val reading: String,
    val writing: String,
    val speaking: String
)
