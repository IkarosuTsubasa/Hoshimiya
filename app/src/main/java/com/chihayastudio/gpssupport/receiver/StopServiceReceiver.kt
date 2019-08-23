package com.chihayastudio.gpssupport.receiver

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import com.chihayastudio.gpssupport.service.GpsService


class StopServiceReceiver : BroadcastReceiver() {

   override fun onReceive(context: Context, intent: Intent) {
        val service = Intent(context, GpsService::class.java)
        context.stopService(service)
    }
}
