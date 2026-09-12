package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseRecordDao {

    @Query("SELECT * FROM dose_records WHERE date = :date ORDER BY scheduledTime ASC, id ASC")
    fun getRecordsForDate(date: String): Flow<List<DoseRecord>>

    @Query("SELECT * FROM dose_records WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC, scheduledTime ASC")
    fun getRecordsForMonth(monthPrefix: String): Flow<List<DoseRecord>>

    @Query("SELECT * FROM dose_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, scheduledTime ASC")
    fun getRecordsInRange(startDate: String, endDate: String): Flow<List<DoseRecord>>

    @Query("SELECT * FROM dose_records ORDER BY date DESC, scheduledTime ASC")
    fun getAllRecords(): Flow<List<DoseRecord>>

    @Query("SELECT * FROM dose_records WHERE date = :date AND medicineId = :medicineId AND scheduledTime = :scheduledTime LIMIT 1")
    suspend fun getRecord(date: String, medicineId: Long, scheduledTime: String): DoseRecord?

    @Query("SELECT * FROM dose_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): DoseRecord?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecord(record: DoseRecord): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(records: List<DoseRecord>)

    @Update
    suspend fun updateRecord(record: DoseRecord)

    @Query("UPDATE dose_records SET isTaken = 1, takenTimeMillis = :takenTime WHERE id = :id")
    suspend fun markTaken(id: Long, takenTime: Long)

    @Query("UPDATE dose_records SET isTaken = 0, takenTimeMillis = NULL WHERE id = :id")
    suspend fun markUntaken(id: Long)

    @Delete
    suspend fun deleteRecord(record: DoseRecord)

    @Query("DELETE FROM dose_records WHERE medicineId = :medicineId")
    suspend fun deleteRecordsForMedicine(medicineId: Long)

    @Query("DELETE FROM dose_records WHERE medicineId = :medicineId AND date < :startDate AND isTaken = 0")
    suspend fun deleteUntakenRecordsBeforeDate(medicineId: Long, startDate: String)

    @Query("SELECT * FROM dose_records WHERE medicineId = :medicineId AND date >= :fromDate AND isTaken = 0 ORDER BY date ASC, scheduledTime ASC")
    suspend fun getUntakenRecordsFromDate(medicineId: Long, fromDate: String): List<DoseRecord>

    @Query("DELETE FROM dose_records WHERE medicineId = :medicineId AND date >= :fromDate AND isTaken = 0")
    suspend fun deleteUntakenRecordsFromDate(medicineId: Long, fromDate: String)

    @Query("SELECT * FROM dose_records WHERE isTaken = 0 AND date >= :fromDate AND date <= :toDate ORDER BY date ASC, scheduledTime ASC")
    suspend fun getUpcomingUntakenRecords(fromDate: String, toDate: String): List<DoseRecord>
}
