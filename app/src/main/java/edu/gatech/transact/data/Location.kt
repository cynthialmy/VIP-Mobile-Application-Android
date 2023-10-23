package edu.gatech.transact.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Location(
    val longitude: Double? = null,
    val latitude: Double? = null,
    val altitude: Double? = null,
    val speed: Float? = null,
    var time: String? = null,
    val accuracy: Float? = null,
    val speedAccuracyMetersPerSecond: Float? = null) {

}