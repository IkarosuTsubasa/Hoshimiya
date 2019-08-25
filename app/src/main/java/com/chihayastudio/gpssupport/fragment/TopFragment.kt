package com.chihayastudio.gpssupport.fragment

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.ActivityManager
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import com.chihayastudio.gpssupport.R
import com.chihayastudio.gpssupport.service.GpsService
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.fragment_top.view.*


class TopFragment : Fragment() {
    private var locationManager: LocationManager? = null
    private var startButton: Button? = null
    private var stopButton: Button? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_top, container, false)
        locationManager = context?.getSystemService(LOCATION_SERVICE) as LocationManager
        val adRequest = AdRequest.Builder().build()

        startButton = view.startButton
        stopButton = view.stopButton

        view.apply {
            isServiceWorking()
            startButton.setOnClickListener {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermission()
                } else {
                    startGPSLocationService()
                }
            }

//            startButton.visibility =
            stopButton.setOnClickListener {
                context.stopService(Intent(context, GpsService::class.java))
                changeButtonView()
            }
            adView.loadAd(adRequest)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        changeButtonView(isServiceWorking())
    }

    private fun checkPermission() {
        if (isAccessFineLocationGranted()) {
            startGPSLocationService()
        } else {
            requestPermissions(
                arrayOf(ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
        }
    }

    private fun startGPSLocationService() {
        locationManager?.let {
            val gpsEnabled = it.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!gpsEnabled) {
                val settingsIntent = Intent(ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(settingsIntent)
            } else {
                context?.let { context ->
                    startForegroundService(context, Intent(context, GpsService::class.java))
                    changeButtonView(true)
                }
            }
        }
    }

    // GPS位置情報permission check
    private fun isAccessFineLocationGranted(): Boolean {
        context?.let {
            return checkSelfPermission(
                it,
                ACCESS_FINE_LOCATION
            ) == PermissionChecker.PERMISSION_GRANTED
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE && grantResults.any { it == PermissionChecker.PERMISSION_GRANTED }) {
            startGPSLocationService()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun isServiceWorking(): Boolean {
        val manager = activity?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { GpsService::class.java.name == it.service.className }
    }

    private fun changeButtonView(isGpsServiceRunning: Boolean = false) {
        if (isGpsServiceRunning) {
            startButton?.let {
                it.apply {
                    text = getString(R.string.working)
                    isEnabled = false
                }
                stopButton?.let {
                    it.apply {
                        text = getString(R.string.stop_button)
                        isEnabled = true
                    }
                }
            }
        } else {
            startButton?.let {
                it.apply {
                    text = getString(R.string.start_button)
                    isEnabled = true
                }
                stopButton?.let {
                    it.apply {
                        text = getString(R.string.stop_button)
                        isEnabled = false
                    }
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 1000
    }
}