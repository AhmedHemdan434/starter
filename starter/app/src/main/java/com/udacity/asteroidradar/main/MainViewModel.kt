package com.udacity.asteroidradar.main

import android.app.Application
import android.graphics.Picture
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.getNextSevenDaysFormattedDates
import com.udacity.asteroidradar.database.getAsteroidsDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import com.udacity.asteroidradar.repository.FILTER_PARAM
import kotlinx.coroutines.launch

enum class NasaApiStatus { LOADING, ERROR, DONE }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // The internal MutableLiveData that stores the status of the most recent request
    private val _listStatus = MutableLiveData<NasaApiStatus>()

    // The external immutable LiveData for the request status
    val listStatus: LiveData<NasaApiStatus>
        get() = _listStatus

    // The internal MutableLiveData that stores the status of the most recent request
    private val _picStatus = MutableLiveData<NasaApiStatus>()

    // The external immutable LiveData for the request status
    val picStatus: LiveData<NasaApiStatus>
        get() = _picStatus

    private val database = getAsteroidsDatabase(application)
    private val asteroidsRepository = AsteroidsRepository(database)

    private val _asteroids = asteroidsRepository.asteroids
    var asteroids: MutableLiveData<List<Asteroid>> = MutableLiveData()


    private val _pictureOfDayList = asteroidsRepository.pictureOfDay
    val pictureOfDayList: LiveData<List<PictureOfDay>>
        get() = _pictureOfDayList

    init {
        _asteroids.observeForever {
            filterData()
        }
        if(_asteroids.value == null || _asteroids.value!!.isEmpty()){
            refreshAsteroids()
        }
        if(pictureOfDayList.value == null || pictureOfDayList.value!!.isEmpty()){
            refreshPictureOfDay()
        }
    }

    private fun refreshPictureOfDay() {
        viewModelScope.launch {
            _picStatus.value = NasaApiStatus.LOADING
            try {
                asteroidsRepository.refreshPictureOfDay()
                _picStatus.value = NasaApiStatus.DONE
            } catch (e: Exception){
                _picStatus.value = NasaApiStatus.ERROR
            }
        }
    }

    private fun refreshAsteroids() {
        viewModelScope.launch {
            _listStatus.value = NasaApiStatus.LOADING
            try {
                asteroidsRepository.refreshAsteroids()
                _listStatus.value = NasaApiStatus.DONE
            } catch (e: Exception){
                _listStatus.value = NasaApiStatus.ERROR
            }
        }
    }

    fun onAsteroidClicked(asteroidId: Long): Asteroid? {
        for(asteroid in asteroids.value!!){
            if(asteroid.id == asteroidId){
                return asteroid
            }
        }
        return null
    }

    fun filterData(filterParam: FILTER_PARAM = FILTER_PARAM.ALL_DATA){
        asteroids.postValue(_asteroids.value?.filter { asteroid ->
            when(filterParam){
                FILTER_PARAM.ALL_DATA -> true
                FILTER_PARAM.TODAY -> isDateWithin(asteroid.closeApproachDate, true)
                FILTER_PARAM.WEEK -> isDateWithin(asteroid.closeApproachDate, false)
            }
        })
    }


    private fun isDateWithin(date: String, isToday: Boolean): Boolean {
        val formattedDayList = getNextSevenDaysFormattedDates()
        return if(isToday){
            formattedDayList[0] == date
        } else {
            formattedDayList.subList(1, formattedDayList.size).contains(date)
        }
    }
}