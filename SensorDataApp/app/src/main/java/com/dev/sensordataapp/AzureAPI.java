
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

// This application uses the Azure IoT Hub device SDK for Java
// For samples see: https://github.com/Azure/azure-iot-sdk-java/tree/master/device/iot-device-samples

package com.dev.sensordataapp;

import android.util.Log;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import com.google.gson.Gson;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import static com.android.volley.VolleyLog.TAG;

public class AzureAPI {

    // The device connection string to authenticate the device with your IoT hub.
    // Using the Azure CLI:
    // az iot hub device-identity show-connection-string --hub-name {YourIoTHubName} --device-id MyJavaDevice --output table
    // private static String connString = "{Your device connection string here}";
    private static String connString = "HostName=SYLO-LOVE-IOTHUB.azure-devices.net;DeviceId=my-sample-raspberry-pi-device;SharedAccessKey=83MhzzoYp9p3snEBvt295MCOxW3LdnG4so34FZqPRQg=";

    // Using the MQTT protocol to connect to IoT Hub
    private static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
    private static DeviceClient client;
    private static ITelemetryDataPoint telemetryDataPoint;
    private static String apiType;
    private static String apiVersion;

    public AzureAPI(ITelemetryDataPoint telemetryDataPoint, String apiType, String apiVersion) {
        this.telemetryDataPoint = telemetryDataPoint;
        this.apiType = apiType;
        this.apiVersion = apiVersion;
    }

    public static void run() {
        // Connect to the IoT hub.
        try {
            client = new DeviceClient(connString, protocol);
            client.open();

            // Create new thread and start sending messages
            AzureAPI.MessageSender sender = new AzureAPI.MessageSender();
            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.execute(sender);

            // Stop the application.
            executor.shutdownNow();
            client.closeNow();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    // Print the acknowledgement received from IoT Hub for the telemetry message sent.
    private static class EventCallback implements IotHubEventCallback {
        public void execute(IotHubStatusCode status, Object context) {
            System.out.println("IoT Hub responded to message with status: " + status.name());

            if (context != null) {
                synchronized (context) {
                    context.notify();
                }
            }
        }
    }

    private static class MessageSender implements Runnable {
        public void run() {
            try {
                // Initialize the simulated telemetry.
                double minTemperature = 20;
                double minHumidity = 60;
                Random rand = new Random();

                while (true) {
                    // Add the telemetry to the message body as JSON.
                    String msgStr = telemetryDataPoint.serialize();
                    Message msg = new Message(msgStr);

                    // Add a custom application property to the message.
                    // An IoT hub can filter on these properties without access to the message body.
                    msg.setProperty("apitype", apiType);
                    msg.setProperty("apiversion", apiVersion);
                    System.out.println("Sending message: " + msgStr);

                    Object lockobj = new Object();

                    // Send the message.
                    EventCallback callback = new EventCallback();
                    client.sendEventAsync(msg, callback, lockobj);

                    synchronized (lockobj) {
                        lockobj.wait();
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("Finished.");
            }
        }
    }

}
