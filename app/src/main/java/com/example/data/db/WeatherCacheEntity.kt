package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val locationKey: String,
    val locationName: String,
    val jsonPayload: String,
    val cachedAtEpochMs: Long
)
