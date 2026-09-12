package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Query("SELECT * FROM medicines WHERE isActive = 1 ORDER BY createdAt ASC")
    fun getAllActiveMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines ORDER BY createdAt ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id LIMIT 1")
    suspend fun getMedicineById(id: Long): Medicine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medicines: List<Medicine>)

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    @Query("UPDATE medicines SET remainingCount = :newCount WHERE id = :id")
    suspend fun updateStock(id: Long, newCount: Int)

    @Query("UPDATE medicines SET remainingCount = MAX(0, remainingCount - :amount) WHERE id = :id")
    suspend fun decrementStock(id: Long, amount: Int)

    @Query("UPDATE medicines SET remainingCount = remainingCount + :amount WHERE id = :id")
    suspend fun addStock(id: Long, amount: Int)

    @Query("SELECT COUNT(*) FROM medicines WHERE isActive = 1 AND remainingCount <= alertThreshold")
    fun getLowStockCount(): Flow<Int>
}
