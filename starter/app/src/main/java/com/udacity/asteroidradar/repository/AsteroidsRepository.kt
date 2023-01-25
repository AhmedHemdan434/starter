package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.map
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.getNextSevenDaysFormattedDates
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.network.Network
import com.udacity.asteroidradar.network.NetworkPictureOfDay
import com.udacity.asteroidradar.network.asDatabaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

enum class FILTER_PARAM {ALL_DATA, WEEK, TODAY}

class AsteroidsRepository(private val database: AsteroidsDatabase) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val pictureAdapter: JsonAdapter<NetworkPictureOfDay> = moshi.adapter(NetworkPictureOfDay::class.java)

    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAsteroids()){ databaseAsteroids ->
            databaseAsteroids.asDomainModel()
        }

    val pictureOfDay: LiveData<List<PictureOfDay>> = Transformations.map(database.pictureOfDayDao.getPictureOfDay()){
        it.asDomainModel()
    }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            val asteroidsString =  Network.asteroids.getAsteroids().await()
            val asteroids = parseAsteroidsJsonResult(JSONObject(asteroidsString))
            database.asteroidDao.insertAll(*asteroids.asDatabaseModel())
        }
    }

    suspend fun refreshPictureOfDay() {
        withContext(Dispatchers.IO) {
            val pictureOfDayString = Network.asteroids.getPhotoOfDay().await()
            val pictureOfDay = pictureAdapter.fromJson(pictureOfDayString)
            database.pictureOfDayDao.insert(pictureOfDay!!.asDatabaseModel())
        }
    }

    suspend fun deleteAllData(){
        withContext(Dispatchers.IO){
            database.asteroidDao.deleteAll()
            database.pictureOfDayDao.getPictureOfDay()
        }
    }
}