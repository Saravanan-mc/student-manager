package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.Student
import com.example.data.model.LocalPerformancePoint
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAllStudentsFlow(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE studentId = :studentId")
    suspend fun getStudentById(studentId: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Query("DELETE FROM students")
    suspend fun clearAllStudents()
}

@Dao
interface PerformancePointDao {
    @Query("SELECT * FROM student_points WHERE studentId = :studentId ORDER BY date ASC")
    fun getPointsForStudent(studentId: String): Flow<List<LocalPerformancePoint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoints(points: List<LocalPerformancePoint>)

    @Query("DELETE FROM student_points WHERE studentId = :studentId")
    suspend fun clearPointsForStudent(studentId: String)
}

@Database(entities = [Student::class, LocalPerformancePoint::class], version = 1, exportSchema = false)
abstract class StudentDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun performancePointDao(): PerformancePointDao

    companion object {
        @Volatile
        private var INSTANCE: StudentDatabase? = null

        fun getDatabase(context: Context): StudentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentDatabase::class.java,
                    "student_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
