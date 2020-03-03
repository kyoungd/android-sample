package com.dev.sensordatamodule

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast

class SensorDataService : Service() {
    lateinit var context: Context
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(context,"Sensor data service",Toast.LENGTH_LONG).show()
        return super.onStartCommand(intent, flags, startId)
    }
}