package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {
    @Query("select * from databaseasteroid order by closeApproachDate asc")
    fun getAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroid)

    @Query("delete from databaseasteroid")
    fun deleteAll()
}

@Dao
interface PictureOfDayDao {
    @Query("select * from databasepictureofday")
    fun getPictureOfDay(): LiveData<List<DatabasePictureOfDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(pictureOfDay: DatabasePictureOfDay)

    @Query("delete from databasepictureofday")
    fun deleteAll()
}

@Database(entities = [DatabaseAsteroid::class, DatabasePictureOfDay::class], version = 1)
abstract class AsteroidsDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
    abstract val pictureOfDayDao: PictureOfDayDao
}

private lateinit var INSTANCE: AsteroidsDatabase

fun getAsteroidsDatabase(context: Context): AsteroidsDatabase {
    synchronized(AsteroidsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                AsteroidsDatabase::class.java,
                "asteroids").build()
        }
    }
    return INSTANCE
}