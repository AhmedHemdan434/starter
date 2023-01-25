package com.udacity.asteroidradar.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.udacity.asteroidradar.PictureOfDay

@Entity
data class DatabasePictureOfDay constructor(
    val mediaType: String,
    val title: String,
    @PrimaryKey
    val url: String
)
fun List<DatabasePictureOfDay>.asDomainModel(): List<PictureOfDay> {
    return map { databasePictureOfDay ->
        val modifiedUrl = if (databasePictureOfDay.mediaType != "image") {
            ""
        } else databasePictureOfDay.url
        PictureOfDay(
            mediaType = databasePictureOfDay.mediaType,
            title = databasePictureOfDay.title,
            url = modifiedUrl
        )
    }
}