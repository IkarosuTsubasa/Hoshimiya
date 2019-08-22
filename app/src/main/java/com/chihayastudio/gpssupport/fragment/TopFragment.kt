package com.chihayastudio.gpssupport.fragment

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context.LOCATION_SERVICE
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import com.chihayastudio.gpssupport.R
import kotlinx.android.synthetic.main.fragment_top.view.*
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.content.Intent
import androidx.core.content.ContextCompat.startForegroundService
import com.chihayastudio.gpssupport.service.GpsService


class TopFragment : Fragment() {
    private var locationManager: LocationManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_top, container, false)
        locationManager = context?.getSystemService(LOCATION_SERVICE) as LocationManager

        view.apply {
            startButton.setOnClickListener {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermission()
                } else {
                    startGPSLocationService()
                }
            }
        }
        return view
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
                    val intent = Intent(context, GpsService::class.java)
                    startForegroundService(context, intent)
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

    companion object {
        const val REQUEST_CODE = 1000
    }
}