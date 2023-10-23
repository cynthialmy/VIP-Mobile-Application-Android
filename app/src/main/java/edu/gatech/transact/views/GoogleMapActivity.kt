package edu.gatech.transact.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import edu.gatech.transact.data.Location
import edu.gatech.transact.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.ktx.firestore

import com.google.firebase.ktx.Firebase
import io.grpc.InternalChannelz.id
import java.util.*
import kotlin.collections.HashMap

/*
* Note: Location has issue for API 31/32 as the current location does not show up
* The precise issue comes from FusedLocationClient,
* and its lastLocation is always null
*
 */
class GoogleMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private var permissionDenied = true
    private lateinit var map: GoogleMap
    private val INTERVAL = (1000 * 0.1).toLong()
    private val FASTEST_INTERVAL = (1000 * 0.1).toLong()
    private var mLocationRequest: LocationRequest? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocClient: FusedLocationProviderClient
    private var marker = false
    private val firestore = Firebase.firestore
    private var startTime: Long = -1
    private var endTime: Long = -1
    private var locationHashmap = HashMap<String, Location>()
    private lateinit var batchWrite: WriteBatch
    private lateinit var lastLocation: android.location.Location
    private lateinit var currLocation: android.location.Location
    // use it to request location updates and get the latest location
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        Places.initialize(getApplicationContext(),"@string/googleMapAPI");

        createLocationRequest()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setupLocClient()
//        locationBuilder.addLocationRequest(mLocationRequest!!)
        locationCallback = object : LocationCallback() {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(locationResult: LocationResult) {
                Log.i("map", "inside onLocationResult")
                for (location in locationResult.locations){
                    if (location != null) {
                        getCurrentLocation()
                        Log.i("map", "locationResult.locations is not null")
                    }

                }
            }
        }


        val actionBar: ActionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        mapFragment.getMapAsync(this)
//        moveCamera()
    }
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.interval = INTERVAL
        mLocationRequest!!.fastestInterval = FASTEST_INTERVAL
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    override fun onResume() {
        super.onResume()
        Log.i("RESUME", "BEING RESUMED")
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            requestLocPermissions()

        }

    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap //initialise map
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        Log.i("MAP", "SETTING UP MAP")
    }
    override fun onMyLocationClick(p0: android.location.Location) {
        Toast.makeText(this, "Current location:\n$p0", Toast.LENGTH_LONG)
            .show()
    }


    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }
    private fun setupLocClient() {
        fusedLocClient =
            LocationServices.getFusedLocationProviderClient(this)
    }

    // prompt the user to grant/deny access
    private fun requestLocPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), //permission in the manifest
            REQUEST_LOCATION)
        Log.i("Permission", "Location permission is being requested..")
    }

    companion object {
        private const val REQUEST_LOCATION = 1 //request code to identify specific permission request
        private const val TAG = "MapsActivity" // for debugging
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentLocation() {
        Log.e("Permission", "about to check permission")
        // Check if the ACCESS_FINE_LOCATION permission was granted before requesting a location
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {

            // call requestLocPermissions() if permission isn't granted
            requestLocPermissions()
            Log.e("Permission", "Location permission has NOT been granted")
        } else {
            permissionDenied = false
            Log.i("Permission", "Location permission has been granted")
            fusedLocClient.requestLocationUpdates(mLocationRequest!!, locationCallback,
                Looper.getMainLooper())

            fusedLocClient.lastLocation.addOnSuccessListener {
                // lastLocation is a task running in the background
                val location = it //obtain location
                //reference to the database
                val database: FirebaseDatabase = FirebaseDatabase.getInstance()

                /*--------------------make it specific to the user-------------------------------*/
                val user = FirebaseAuth.getInstance().currentUser
                val email = user?.email!!
                val end = user.email?.indexOf('@')!!
                val userName = email.substring(0, end)
                val ref: DatabaseReference = database.getReference(userName)
                /*---------------------------------------------------*/
                if (location != null) {
                    //Save the location data to the database
                    val speed = location.speed
                    val speedAccuracy = location.speedAccuracyMetersPerSecond
                    val longitude = location.longitude
                    val latitude = location.latitude
                    val altitude = location.altitude
                    val date = Date(location.time)
                    val formatter = SimpleDateFormat("MM-dd-yyyy HH:mm:ss")
                    val time = formatter.format(date)
                    val accuracy = location.accuracy
                    val truncatedLocation = Location(longitude, latitude, altitude,
                        speed, time, accuracy, speedAccuracy)
                    locationHashmap[time] = truncatedLocation

                    currLocation = location
                    if (startTime < 0) {
                        startTime = location.time
                        lastLocation = location
                    }
                    endTime = location.time
                    // create an object that will specify how the camera will be updated
                    // create a marker at the exact location
                    // if the previous location differs from the current one
                    if (currLocation.longitude != lastLocation.longitude ||
                            currLocation.latitude != lastLocation.latitude ||
                            !marker) {
                        marker = true
                        val latLng = LatLng(location.latitude, location.longitude)
                        val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                        map.clear()
                        map.addMarker(MarkerOptions().position(latLng)
                            .title("You are currently here!"))
                        map.moveCamera(update)
                        Log.i("location", "different locations")
                    }
                    lastLocation = currLocation


                    // 1 min = 60 * 1000ms, time is counted in ms
                    // if true, push to firebase, reset array and time
                    if (endTime - startTime >= 60000) {
                        startTime = endTime
                        // one push specify the ending time
                        firestore.collection("users")
                            .document(userName)
                            .collection("location")
                            .document("time = $time") // the location data is not added in order
                            .set(locationHashmap)
                            .addOnSuccessListener {
                                Log.v("Firestore Push", "Location uploaded")
                            }
                        // locationHashmap = HashMap()
                    } else {
                        val diff = endTime - startTime
                        Log.v("time difference", "time difference = $diff")
                    }

                    // this line is for firebase realtime database
                    //ref.child("location").push().setValue(truncatedLocation)
                    Log.i("map", "requested location is not null")
                } else {
                    // if location is null , log an error message
                    Log.e(TAG, "No location found")
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //check if the request code matches the REQUEST_LOCATION
        if (requestCode == REQUEST_LOCATION) {
            Log.i("grantResults", grantResults.size.toString())
            //check if grantResults contains PERMISSION_GRANTED.If it does, call getCurrentLocation()
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
                permissionDenied = false
                getCurrentLocation()
                Log.i("Permission", "permission has been requested and granted")
            } else {
                //if it doesn't log an error message

                permissionDenied = true
                Log.e(TAG, "Location permission has been denied")
            }
        }
    }


    override fun onMarkerClick(p0: Marker): Boolean = false

    private fun moveCamera() {
        Log.i("Permission", "permission will be checked now")
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "permission has not been granted")
            // call requestLocPermissions() if permission isn't granted
            requestLocPermissions()
        } else {
            permissionDenied = false
            Log.i("Permission", "permission has been granted")
            fusedLocClient.requestLocationUpdates(mLocationRequest!!, locationCallback,
                Looper.getMainLooper())
            fusedLocClient.lastLocation.addOnCompleteListener {
                // lastLocation is a task running in the background
                val location = it.result //obtain location
                if (location != null) {
                    marker = true
                    val latLng = LatLng(location.latitude, location.longitude)
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    map.addMarker(MarkerOptions().position(latLng)
                        .title("You are currently here!"))
                    map.moveCamera(update)


                } else {
                    // if location is null , log an error message
                    Log.e(TAG, "No location found")
                }
            }
        }
    }



}

