package com.chihayastudio.gpssupport.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import com.chihayastudio.gpssupport.MainActivity
import com.chihayastudio.gpssupport.R
import com.chihayastudio.gpssupport.receiver.StopServiceReceiver


class GpsService : Service() {
    private var locationManager: LocationManager? = null
    private var notificationBuild: Notification.Builder? = null
    private var notificationManager: NotificationManager? = null

    private val locationListener: LocationListener = object : LocationListener {
        override fun onProviderDisabled(p0: String?) {
            notificationBuild?.setContentText(applicationContext.getString(R.string.gps_off))
            notificationManager?.notify(notificationId, notificationBuild?.build())
        }

        override fun onProviderEnabled(p0: String?) {
            notificationBuild?.setContentText(applicationContext.getString(R.string.working))
            notificationManager?.notify(notificationId, notificationBuild?.build())
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        }

        override fun onLocationChanged(p0: Location?) {
            notificationBuild?.setContentText(
                "${getString(R.string.latitude)}: ${p0?.latitude}, ${getString(
                    R.string.longitude
                )}: ${p0?.longitude}"
            )
            notificationManager?.notify(notificationId, notificationBuild?.build())
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
                android.R.drawable.ic_menu_close_clear_cancel, "STOP", hide
            )
                .build()

        notificationBuild?.let {
            val notification = it.setContentTitle(getString(R.string.location_information))
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentText(context.getString(R.string.working))
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .addAction(stopServiceAction)
                .build()

            startForeground(notificationId, notification)
        }

        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_HIGH
        criteria.isAltitudeRequired = false
        criteria.isSpeedRequired = false
        criteria.isCostAllowed = true
        criteria.isBearingRequired = false

        locationManager?.requestLocationUpdates(
            MinTime,
            MinDistance,
            criteria,
            locationListener,
            null
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
        private const val MinDistance = 1f
        private const val requestCode = 2757
        private const val notificationId = 1134
        private const val channelId = "GpsService"
        private const val title = "GpsSupport"
    }
}