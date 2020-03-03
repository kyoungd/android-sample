package com.dev.sensordataapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SensorDataService extends Service implements SensorEventListener {

    private String LOG_TAG = "SensorDataService";
    private IBinder mBinder = new MyBinder();
    private Handler handler ;
    private List<Sensor> sensors;
    private SensorManager sensorManager;
    private SensorData sensorData;
    private JSONArray results = new JSONArray();
    private Context context ;
    private Runnable myRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        sensorData = new SensorData();
        sensorData.setPhoneModel(Build.MODEL);
        sensorData.setAndroidVersion(Build.VERSION.RELEASE);
        sensorManager =(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Log.e(LOG_TAG,"oncreate");
        myRunnable = new Runnable() {
            @Override
            public void run() {
                sensorData.setData(results.toString());
                Log.e(LOG_TAG,"AAAAAAAAAAAAAAAA");
                Intent intent = new Intent("startservice");
                Bundle b = new Bundle();
                b.putParcelable("SensorData", sensorData);
                intent.putExtra("SensorData",b);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                handler.postDelayed(this,2000);
            }
        };
        handler = new Handler();

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");

        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        List<String> sensorNames =new ArrayList<>();
        for (Sensor sensor : sensors) {
            sensorNames.add(sensor.getName());
            sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
        SharedPreferences sharedPreferences = getSharedPreferences("SENSORS",MODE_PRIVATE);
        if(!sharedPreferences.contains("sensors")){
            sharedPreferences.edit().putStringSet("sensors",new HashSet<>(sensorNames)).commit();
        }else {
            Set<String> mysensors= sharedPreferences.getStringSet("sensors",null);

            if(mysensors.size()==0){
                sharedPreferences.edit().putStringSet("sensors",new HashSet<>(sensorNames)).commit();
            }
        }
        handler.post(myRunnable);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent("stopservice");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                handler.removeCallbacks(myRunnable);
                handler.postDelayed(myRunnable,180000);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        results = new JSONArray();
                        Intent intent = new Intent("resumeservice");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                },180000);
                results = new JSONArray();
                handler.postDelayed(this,240000);
            }
        },60000);
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "in onDestroy");

    }

    private String getSensorCode (int sensorType) {
        switch (sensorType) {
            case Sensor.TYPE_LIGHT :{
                return ("TYPE_LIGHT");
            }
            case Sensor.TYPE_PRESSURE : {
                return ("TYPE_PRESSURE");
            }
            case Sensor.TYPE_PROXIMITY : {
                return ("TYPE_PROXIMITY");
            }
            case Sensor.TYPE_GRAVITY : {
                return ("TYPE_GRAVITY");
            }
            case  Sensor.TYPE_LINEAR_ACCELERATION : {
                return ("TYPE_LINEAR_ACCELERATION");
            }
            case  Sensor.TYPE_ORIENTATION : {
                return ("TYPE_ORIENTATION");
            }
            case Sensor.TYPE_ROTATION_VECTOR : {
                return ("TYPE_ROTATION_VECTOR");
            }
            case Sensor.TYPE_GAME_ROTATION_VECTOR : {
                return ("TYPE_GAME_ROTATION_VECTOR");
            }
            case Sensor.TYPE_RELATIVE_HUMIDITY : {
                return ("TYPE_RELATIVE_HUMIDITY");
            }
            case Sensor.TYPE_AMBIENT_TEMPERATURE: {
                return ("TYPE_AMBIENT_TEMPERATURE");
            }
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED : {
                return ("TYPE_MAGNETIC_FIELD_UNCALIBRATED");
            }
            case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED: {
                return ("TYPE_ACCELEROMETER_UNCALIBRATED");
            }
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED :  {
                return ("TYPE_GYROSCOPE_UNCALIBRATED");
            }
            case Sensor.TYPE_POSE_6DOF : {
                return ("TYPE_POSE_6DOF");
            }
            case Sensor.TYPE_STATIONARY_DETECT: {
                return ("TYPE_STATIONARY_DETECT");
            }
            case Sensor.TYPE_MOTION_DETECT: {
                return ("TYPE_MOTION_DETECT");
            }
            case Sensor.TYPE_HEART_BEAT: {
                return ("TYPE_HEART_BEAT");
            }
            case Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT: {
                return ("TYPE_LOW_LATENCY_OFFBODY_DETECT");
            }
            case  Sensor.TYPE_ACCELEROMETER: {
                return ("TYPE_ACCELEROMETER");
            }
            case Sensor.TYPE_MAGNETIC_FIELD: {
                return ("TYPE_MAGNETIC_FIELD");
            }
            case Sensor.TYPE_GYROSCOPE: {
                return ("TYPE_GYROSCOPE");
            }
        }
        return "UNKNOWN";
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent event) {
        JSONObject jsonObject = new JSONObject();
        Log.e(LOG_TAG,"name "+ event.sensor.getStringType()+" values "+event.values.length);
        try {
            jsonObject.put("SensorName",event.sensor.getName());
            jsonObject.put("SensorType",event.sensor.getType());
            jsonObject.put("SensorCode", getSensorCode(event.sensor.getType()));
            switch (event.sensor.getType()) {
                case Sensor.TYPE_LIGHT :{
                    jsonObject.put("SI lux",event.values[0]);
                    break;
                }
                case Sensor.TYPE_PRESSURE : {
                    jsonObject.put("atmosphere",event.values[0]);
                    break;
                }
                case Sensor.TYPE_PROXIMITY : {
                    jsonObject.put("distance",event.values[0]);
                    break;
                }
                case Sensor.TYPE_GRAVITY : {
                    jsonObject.put("gravity_X",event.values[0]);
                    jsonObject.put("gravity_Y",event.values[1]);
                    jsonObject.put("gravity_Z",event.values[2]);
                    break;
                }
                case  Sensor.TYPE_LINEAR_ACCELERATION : {
                    jsonObject.put("linear_X",event.values[0]);
                    jsonObject.put("gravity_Y",event.values[1]);
                    jsonObject.put("gravity_Z",event.values[2]);
                    break;
                }
                case  Sensor.TYPE_ORIENTATION : {
                    jsonObject.put("Azimuth",event.values[0]);
                    jsonObject.put("Pitch",event.values[1]);
                    jsonObject.put("Roll",event.values[2]);
                    break;
                }
                case Sensor.TYPE_ROTATION_VECTOR :
                case Sensor.TYPE_GAME_ROTATION_VECTOR : {
                    jsonObject.put("X*sin(θ/2)",event.values[0]);
                    jsonObject.put("Y*sin(θ/2)",event.values[1]);
                    jsonObject.put("Z*sin(θ/2)",event.values[2]);
                    jsonObject.put("cos(θ/2)",event.values[3]);
                    if (event.values.length > 4)
                        jsonObject.put("headingAccuracy",event.values[4]);
                    break;

                }
                case Sensor.TYPE_RELATIVE_HUMIDITY : {
                    jsonObject.put("humidity",event.values[0]);
                    break;
                }
                case Sensor.TYPE_AMBIENT_TEMPERATURE: {
                    jsonObject.put("temperature",event.values[0]);
                    break;
                }
                case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED :
                case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED: {
                    jsonObject.put("X_uncalib",event.values[0]);
                    jsonObject.put("Y_uncalib",event.values[1]);
                    jsonObject.put("Z_uncalib)",event.values[2]);
                    jsonObject.put("X_bias",event.values[3]);
                    jsonObject.put("Y_bias",event.values[4]);
                    jsonObject.put("Z_bias",event.values[5]);
                    break;
                }
                case Sensor.TYPE_GYROSCOPE_UNCALIBRATED :  {
                    jsonObject.put("angular_speed_X",event.values[0]);
                    jsonObject.put("angular_speed_Y",event.values[1]);
                    jsonObject.put("angular_speed_Z)",event.values[2]);
                    jsonObject.put("estimated_drift_X",event.values[3]);
                    jsonObject.put("estimated_drift_Y",event.values[4]);
                    jsonObject.put("estimated_drift_Z",event.values[5]);
                    break;
                }
                case Sensor.TYPE_POSE_6DOF : {
                    jsonObject.put("X*sin(θ/2)",event.values[0]);
                    jsonObject.put("Y*sin(θ/2)",event.values[1]);
                    jsonObject.put("Z*sin(θ/2)",event.values[2]);
                    jsonObject.put("cos(θ/2)",event.values[3]);
                    jsonObject.put("translation_along_x",event.values[4]);
                    jsonObject.put("translation_along_y",event.values[5]);
                    jsonObject.put("translation_along_z",event.values[6]);
                    jsonObject.put("Delta_quaternion_rotation_x*sin(θ/2))",event.values[7]);
                    jsonObject.put("Delta_quaternion_rotation_y*sin(θ/2))",event.values[8]);
                    jsonObject.put("Delta_quaternion_rotation_z*sin(θ/2))",event.values[9]);
                    jsonObject.put("Delta_quaternion_rotation_cos(θ/2))",event.values[10]);
                    jsonObject.put("Delta_translation_along_x",event.values[11]);
                    jsonObject.put("Delta_translation_along_y",event.values[12]);
                    jsonObject.put("Delta_translation_along_z",event.values[13]);
                    jsonObject.put("sequence_number",event.values[14]);
                    break;
                }
                case Sensor.TYPE_STATIONARY_DETECT: {
                    jsonObject.put("stationary",event.values[0]);
                    break;
                }
                case Sensor.TYPE_MOTION_DETECT: {
                    jsonObject.put("in_motion",event.values[0]);
                    break;
                }
                case Sensor.TYPE_HEART_BEAT: {
                    jsonObject.put("confidence",event.values[0]);
                    break;
                }
                case Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT: {
                    jsonObject.put("offbody_state",event.values[0]);
                    break;
                }
                case  Sensor.TYPE_ACCELEROMETER: {
                    jsonObject.put("X",event.values[0]);
                    jsonObject.put("Y",event.values[1]);
                    jsonObject.put("Z",event.values[2]);
                    break;
                }
                case Sensor.TYPE_MAGNETIC_FIELD: {
                    jsonObject.put("X_uT",event.values[0]);
                    jsonObject.put("Y_uT",event.values[1]);
                    jsonObject.put("Z_uT",event.values[2]);
                    break;
                }
                case Sensor.TYPE_GYROSCOPE: {
                    jsonObject.put("angular_speed_X",event.values[0]);
                    jsonObject.put("angular_speed_Y",event.values[1]);
                    jsonObject.put("angular_speed_Z",event.values[2]);
                    break;
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LocalDateTime current = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String formatted = current.format(formatter);
        try {
            jsonObject.put("DateTime",formatted);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        results.put(jsonObject);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.e("accuracy changed",sensor.getName()+" : "+accuracy);
    }

    class MyBinder extends Binder {
        SensorDataService getService() {
            return SensorDataService.this;
        }
    }
}
