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


class GpsService : Service() {
    private var locationManager: LocationManager? = null

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
        val notificationBuild: Notification.Builder?

        val pendingIntent = PendingIntent.getActivity(
            context, requestCode,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel =
                NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
            channel.apply {
                setSound(null, null)
                description = "GPS Notification"
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)

            notificationBuild = Notification.Builder(context, channelId)
        } else {
            notificationBuild = Notification.Builder(context)
        }

        val notification = notificationBuild.setContentTitle(title)
            .setSmallIcon(android.R.drawable.btn_star)
            .setContentText("GPS")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .build()

        startForeground(275, notification)

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
    }

    companion object {
        private const val MinTime = 1000L
        private const val MinDistance = 50f
        private const val requestCode = 2757
        private const val channelId = "GpsService"
        private const val title = "GpsSupport"
    }
}