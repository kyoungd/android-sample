package com.dev.sensordataapp;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorData implements Parcelable, ITelemetryDataPoint {

    private String phoneModel="",androidVersion="",data="";

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String serialize() { return data; }

    SensorData() {}

    protected SensorData(Parcel in) {
        phoneModel = in.readString();
        androidVersion= in.readString();
        data = in.readString();
    }

    public static final Creator<SensorData> CREATOR = new Creator<SensorData>() {
        @Override
        public SensorData createFromParcel(Parcel in) {
            return new SensorData(in);
        }

        @Override
        public SensorData[] newArray(int size) {
            return new SensorData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
