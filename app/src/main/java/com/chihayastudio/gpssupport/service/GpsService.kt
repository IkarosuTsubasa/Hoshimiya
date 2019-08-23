package com.chihayastudio.gpssupport.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.chihayastudio.gpssupport.MainActivity
import com.chihayastudio.gpssupport.receiver.StopServiceReceiver

class GpsService : Service() {
    private var locationManager: LocationManager? = null
    private var locationLatitude: Double? = null
    private var locationLongitude: Double? = null
    private var notificationBuild: Notification.Builder? = null
    private var notificationManager: NotificationManager? = null

    private val locationListener: LocationListener = object : LocationListener {
        override fun onProviderDisabled(p0: String?) {
            Log.d("星宮いちご1", p0)
        }

        override fun onProviderEnabled(p0: String?) {
            Log.d("星宮いちご2", p0)
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
            Log.d("星宮いちご3", p0)
        }

        override fun onLocationChanged(p0: Location?) {
            locationLongitude = p0?.longitude
            locationLatitude = p0?.latitude
            notificationBuild?.setContentText("$locationLatitude, $locationLongitude")
            notificationManager?.notify(notificationId, notificationBuild?.build())
            Log.d("星宮いちご4", p0.toString())
        }

    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val context = applicationContext

        val pendingIntent = PendingIntent.getActivity(
            context, requestCode,
            Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= 26) {
            notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel =
                NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
            channel.apply {
                setSound(null, null)
                description = "GPS Notification"
                enableLights(false)
                enableVibration(false)
            }
            notificationManager?.createNotificationChannel(channel)

            notificationBuild = Notification.Builder(context, channelId)
        } else {
            notificationBuild = Notification.Builder(context)
        }
        val intentHide = Intent(this, StopServiceReceiver::class.java)

        val hide = PendingIntent.getBroadcast(
            this,
            System.currentTimeMillis().toInt(),
            intentHide,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val stopServiceAction: Notification.Action =
            Notification.Action.Builder(
                android.R.drawable.btn_star, "STOP", hide
            )
                .build()

        notificationBuild?.let {
            val notification = it.setContentTitle(title)
                .setSmallIcon(android.R.drawable.btn_star)
                .setContentText("searching")
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .addAction(stopServiceAction)
                .build()

            startForeground(notificationId, notification)
        }
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            MinTime, MinDistance, locationListener
        )

        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopGPS() {
        locationManager?.removeUpdates(locationListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGPS()
        Log.d("星宮いちご", "close")
    }

    companion object {
        private const val MinTime = 1000L
        private const val MinDistance = 50f
        private const val requestCode = 2757
        private const val notificationId = 1134
        private const val channelId = "GpsService"
        private const val title = "GpsSupport"
    }
}