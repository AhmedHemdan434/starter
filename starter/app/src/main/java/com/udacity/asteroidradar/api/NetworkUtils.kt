package com.udacity.asteroidradar.api

import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.network.NetworkAsteroid
import com.udacity.asteroidradar.network.NetworkAsteroidContainer
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//I Changed this class for adapting the parsing with the new version of response
fun parseAsteroidsJsonResult(jsonResult: JSONObject): NetworkAsteroidContainer {
    val nearEarthObjectsJson = jsonResult.getJSONArray("near_earth_objects")

    val asteroidList = ArrayList<NetworkAsteroid>()
    for (i in 0 until nearEarthObjectsJson.length()) {
        val asteroidJson = nearEarthObjectsJson.getJSONObject(i)
        val id = asteroidJson.getLong("id")
        val codename = asteroidJson.getString("name")
        val absoluteMagnitude = asteroidJson.getDouble("absolute_magnitude_h")
        val estimatedDiameter = asteroidJson.getJSONObject("estimated_diameter")
            .getJSONObject("kilometers").getDouble("estimated_diameter_max")

        val closeApproachData = asteroidJson
            .getJSONArray("close_approach_data").getJSONObject(0)
        val relativeVelocity = closeApproachData.getJSONObject("relative_velocity")
            .getDouble("kilometers_per_second")
        val distanceFromEarth = closeApproachData.getJSONObject("miss_distance")
            .getDouble("astronomical")
        val isPotentiallyHazardous = asteroidJson
            .getBoolean("is_potentially_hazardous_asteroid")
        val formattedDate = closeApproachData.getString("close_approach_date")

        val asteroid = NetworkAsteroid(id, codename, formattedDate, absoluteMagnitude,
            estimatedDiameter, relativeVelocity, distanceFromEarth, isPotentiallyHazardous)
        asteroidList.add(asteroid)
    }

    return NetworkAsteroidContainer(asteroidList)
}
