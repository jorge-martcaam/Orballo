package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: WeatherCacheEntity)

    @Query("SELECT * FROM weather_cache WHERE locationKey = :locationKey LIMIT 1")
    suspend fun getByLocationKey(locationKey: String): WeatherCacheEntity?

    @Query("DELETE FROM weather_cache WHERE cachedAtEpochMs < :thresholdEpochMs")
    suspend fun deleteOlderThan(thresholdEpochMs: Long)

    @Query("DELETE FROM weather_cache")
    suspend fun clearAll()
}
