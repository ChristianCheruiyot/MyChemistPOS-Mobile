package com.habit.mychemistpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habit.mychemistpos.data.model.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAll(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Supplier?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: Supplier)

    @Update
    suspend fun update(supplier: Supplier)

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteById(id: String)
}
