package com.dev.sensordataapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.midi.MidiSender;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.dev.sensordataapp.R.id;


public final class MainActivity extends AppCompatActivity {

    SensorDataService mSensorDataService;
    Boolean mServiceBound = false;
    BroadcastReceiver mMessageReceiver,mStopReciever,mResumeReceiver;
    TextView phoneModel_tv,androidVersion_tv,data_tv;
    ProgressBar progressBar;
    Button bind;
    SensorData MysensorData;
    AzureAPI myAzure;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MysensorData = new SensorData();
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               if(mServiceBound){
                   Bundle b = intent.getBundleExtra("SensorData");
                   SensorData sensorData = (SensorData) b.getParcelable("SensorData");
                   MysensorData.setData(MysensorData.getData()+sensorData.getData());
                   myAzure = new AzureAPI(MysensorData, "iot_device", "11");
                   myAzure.run();
                   phoneModel_tv.setText("Phone Model : "+sensorData.getPhoneModel());
                   androidVersion_tv.setText("Android Version : "+sensorData.getAndroidVersion());
                   Log.e("Main Activity","data is : "+sensorData.getData());
                   progressBar.setVisibility(View.GONE);
                   bind.setEnabled(false);
               }


            }
        };
        mStopReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                data_tv.setText(MysensorData.getData());
                MysensorData.setData("");
                mServiceBound = false;
                Log.e("MAin activity","stopping service");
            }
        };
        mResumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mServiceBound = true;
            }
        };
        phoneModel_tv =  findViewById(id.phonemodel_tv);
        androidVersion_tv = findViewById(id.androidversion_tv);
        data_tv = findViewById(id.data_tv);
        bind = findViewById(id.bind);
        progressBar = findViewById(id.progressbar);
        bind.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SensorDataService.class);
                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                progressBar.setVisibility(View.VISIBLE);
                mServiceBound=true;
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }



    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorDataService.MyBinder myBinder = (SensorDataService.MyBinder) service;
            mSensorDataService = myBinder.getService();
            mServiceBound = true;
            LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(
                    mMessageReceiver, new IntentFilter("startservice"));
            LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(
                    mStopReciever, new IntentFilter("stopservice"));
            LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(
                    mResumeReceiver, new IntentFilter("resumeservice"));
        }
    };
}
